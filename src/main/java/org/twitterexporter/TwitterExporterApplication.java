package org.twitterexporter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.Get2UsersIdTweetsResponse;
import com.twitter.clientlib.model.Tweet;
import com.twitter.clientlib.model.User;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class TwitterExporterApplication { 

	private static final Set<String> TWEET_FIELDS = Stream.of("attachments","author_id","context_annotations","conversation_id",
	"created_at","edit_controls","edit_history_tweet_ids","entities","geo","id","in_reply_to_user_id","lang","possibly_sensitive",
	"public_metrics","referenced_tweets","reply_settings","source","text","withheld").collect(Collectors.toSet());
	private static Properties properties;

	@Autowired
	public void setProperties(Properties properties) {
		TwitterExporterApplication.properties = properties;
	}

	public static void main(String[] args) {
		SpringApplication.run(TwitterExporterApplication.class, args);

		// Create api client using bearer token from properties file
		TwitterApi twitterApi = new TwitterApi(new TwitterCredentialsBearer(properties.getBearerToken()));

		try {
			for(String username : properties.getUsernames()) {
				// Get user ID from each username
				User userData = twitterApi.users().findUserByUsername(username).execute().getData();
				if (userData != null) {
					String userId = userData.getId();
					
					Get2UsersIdTweetsResponse tweetsResponse = twitterApi.tweets().usersIdTweets(userId).tweetFields(TWEET_FIELDS).execute();
					// Retrieve all tweets from user, including RT.
					List<Tweet> tweets = tweetsResponse.getData();
					if (tweets != null) {
						// Log each tweet text
						tweets.forEach(tweet -> log.info(tweet.toJson()));
					}
				} else {
					log.error("Username '{0}' not found. Unable to retrieve data from this user.", username);
				}
			}
		} catch (ApiException e) {
			log.error("An error took place: '{0}'.", e.getLocalizedMessage());
		}
	}
}
