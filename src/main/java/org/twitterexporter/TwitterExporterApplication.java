package org.twitterexporter;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.opencsv.CSVWriter;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.api.TweetsApi.APIusersIdTweetsRequest;
import com.twitter.clientlib.model.Get2UsersIdMentionsResponseMeta;
import com.twitter.clientlib.model.Get2UsersIdTweetsResponse;
import com.twitter.clientlib.model.Problem;
import com.twitter.clientlib.model.Tweet;
import com.twitter.clientlib.model.User;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class TwitterExporterApplication { 

	private static final Set<String> TWEET_FIELDS = Stream.of("author_id","conversation_id",
	"created_at","edit_controls","id","lang","possibly_sensitive",
	"public_metrics","reply_settings","text").collect(Collectors.toSet());

	private static final Integer CHUNK_SIZE = 100;
	private static final String EXPORT_FILE_EXTENSION = ".csv";
	private static Properties properties;

	@Autowired
	public void setProperties(Properties properties) {
		TwitterExporterApplication.properties = properties;
	}

	public static void main(String[] args) {
		SpringApplication.run(TwitterExporterApplication.class, args);

		// Create api client using bearer token from properties file
		TwitterApi twitterApi = new TwitterApi(new TwitterCredentialsBearer(properties.getBearerToken()));

		OffsetDateTime startDatetime = null;
		OffsetDateTime endDatetime = null;

		// Parsing start and end date if defined (none or both must be defined)
		if ((properties.getStartDatetime() != null) && (!properties.getStartDatetime().isBlank()) && (properties.getEndDatetime() != null) && (!properties.getEndDatetime().isBlank())) {
			startDatetime = LocalDateTime.parse(properties.getStartDatetime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.systemDefault()).toOffsetDateTime();
			endDatetime = LocalDateTime.parse(properties.getEndDatetime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.systemDefault()).toOffsetDateTime();

			// Start date (if defined) must be earlier than end date
			if (!startDatetime.isBefore(endDatetime)){
				log.error("StartDate ({}) is after than endDate ({}).", startDatetime, endDatetime);
				throw new IllegalArgumentException();
			}
		}

		// Each user has its own file
		for(String username : properties.getUsernames()) {
			try {
				// Get user ID from each username
				User userData = twitterApi.users().findUserByUsername(username).execute().getData();
				if (userData != null) {
					String userId = userData.getId();
					// Generating file using username as filename
					try (CSVWriter writer = new CSVWriter(new FileWriter(properties.getFilepath() + username + EXPORT_FILE_EXTENSION))){
						// header row
						writer.writeNext(new String[]{"tweetId", "creationDate", "conversationId", "editsRemaining", "language", "possiblySensitive", 
						"likeCount", "quoteCount", "replyCount", "retweetCount", "replySettings", "text"});
						if ((startDatetime != null) && (endDatetime != null)) {
							log.info("Getting tweets from {} between {} and {}", username, startDatetime.toString(), endDatetime.toString());
						} else {
							log.info("Getting all tweets from {}", username);
						}
						
						String nextToken = "";
						while(nextToken != null) {
							// Retrieve tweets from user, including RT.
							APIusersIdTweetsRequest request = twitterApi.tweets().usersIdTweets(userId).maxResults(CHUNK_SIZE).tweetFields(TWEET_FIELDS);
							// Include date filter if defined
							if ((startDatetime != null) && (endDatetime != null)) {
								request.startTime(startDatetime);
								request.endTime(endDatetime);
							}
							if (!nextToken.isEmpty()) {
								request.paginationToken(nextToken);
							}
							Get2UsersIdTweetsResponse tweetsResponse = request.execute();

							List<Tweet> tweets = tweetsResponse.getData();
							if (tweets != null) {
								tweets.forEach(tweet -> {
									log.debug(tweet.getText());
									String creationDate = (tweet.getCreatedAt() != null) ? tweet.getCreatedAt().toString() : null;
									String editsRemaining = (tweet.getEditControls() != null) ? tweet.getEditControls().getEditsRemaining().toString() : null;
									String possibleSensitive = (tweet.getPossiblySensitive() != null) ? tweet.getPossiblySensitive().toString() : null;
									String likeCount = ((tweet.getPublicMetrics() != null) && (tweet.getPublicMetrics().getLikeCount() != null)) ? tweet.getPublicMetrics().getLikeCount().toString() : null;
									String quoteCount = ((tweet.getPublicMetrics() != null) && (tweet.getPublicMetrics().getQuoteCount() != null)) ? tweet.getPublicMetrics().getQuoteCount().toString() : null;
									String replyCount = ((tweet.getPublicMetrics() != null) && (tweet.getPublicMetrics().getReplyCount() != null)) ? tweet.getPublicMetrics().getReplyCount().toString() : null;
									String retweetCount = ((tweet.getPublicMetrics() != null) && (tweet.getPublicMetrics().getRetweetCount() != null)) ? tweet.getPublicMetrics().getRetweetCount().toString() : null;
									String replySettings = (tweet.getReplySettings() != null) ? tweet.getReplySettings().toString() : null;
									String flatText = (tweet.getText() != null) ? tweet.getText().replace("\n", " ") : null;

									// Write file row with tweet info
									writer.writeNext(new String[] {
										tweet.getId(), 
										creationDate,
										tweet.getConversationId(), 
										editsRemaining, 
										tweet.getLang(), 
										possibleSensitive, 
										likeCount, 
										quoteCount, 
										replyCount, 
										retweetCount,
										replySettings, 
										flatText});
								});
							}

							// Get next token to retrieve next chunk
							Get2UsersIdMentionsResponseMeta meta = tweetsResponse.getMeta();
							if (meta != null) {
								nextToken = meta.getNextToken();
							}
							// Log each detected problem (if any)
							List<Problem> problems = tweetsResponse.getErrors();
							if (problems != null){
								log.error("Detected errors wile retrieving data from Twitter API: {}", problems.toString());
							}
						}
					}
				} else {
					log.error("Username {} not found. Unable to retrieve data from this user.", username);
				}
			} catch (Exception e) {
				log.error("An error took place: '{}'.", e);
			}
		}
	}
}
