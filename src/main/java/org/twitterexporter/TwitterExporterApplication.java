package org.twitterexporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class TwitterExporterApplication { 

	public static void main(String[] args) {
		SpringApplication.run(TwitterExporterApplication.class, args);
		log.debug("Starting Twitter exporter app");

	}

}
