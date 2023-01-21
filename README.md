# twitter-exporter
Timeline tweets exporter tool

# usage
Fill values in application.properties file with:
* bearer-token: Your twitter api bearer token.
* usernames: The username(s) you want to retrieve the timeline, separated by comma.
* filepath: The filepath where files will be generated (ending with / or \\).
* startDatetime: (opt) Start date to extract tweets (Must be defined if end date is defined). yyyy-mm-ddThh:MM:ss
* endDatetime: (opt) End date to extract tweets (Must be defined if start date is defined). yyyy-mm-ddThh:MM:ss

java -jar twitter-exporter.jar

# output
All tweets from all provided usernames will be logged.

