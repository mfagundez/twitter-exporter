# twitter-exporter
Timeline tweets exporter tool

# usage
Fill the following values:
* bearer-token: Your twitter api bearer token.
* filepath: The filepath where files will be generated (ending with / or \\).
* usernames: The username(s) you want to retrieve the timeline, separated by comma.
* startDatetime: (opt) Start date to extract tweets (Must be defined if end date is defined). yyyy-mm-ddThh:MM:ss
* endDatetime: (opt) End date to extract tweets (Must be defined if start date is defined). yyyy-mm-ddThh:MM:ss

You can choose how to provide argument values:

a) Command-line Arguments (argument order is important)
```bash
java -jar twitter-exporter-x.x.x.jar bearerToken filePath usernames [startDatetime] [endDatetime]
```

b) application.properties file:

```bash
java -jar twitter-exporter-x.x.x.jar
```

# output
A csv file for each given user with tweets that match with datetime filter criteria (if any).

Each file row represents one tweet with following information:

* tweetId.
* creationDate. yyyy-mm-ddTdd:MM:ssZ format.
* conversationId.
* editsRemaining.
* language.
* possiblySensitive.
* likeCount.
* quoteCount.
* replyCount.
* retweetCount.
* replySettings.
* text. (Note: just characters provided by Twitter API are included. In RTs may be cropped).
