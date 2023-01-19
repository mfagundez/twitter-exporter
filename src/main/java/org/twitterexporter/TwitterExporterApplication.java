package org.twitterexporter;

import java.util.List;

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
					Get2UsersIdTweetsResponse tweetsResponse = twitterApi.tweets().usersIdTweets(userId).execute();
					// Retrieve all tweets from user, including RT.
					List<Tweet> tweets = tweetsResponse.getData();
					if (tweets != null) {
						// Log each tweet text
						tweets.forEach(tweet -> log.info(tweet.getText()));
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
