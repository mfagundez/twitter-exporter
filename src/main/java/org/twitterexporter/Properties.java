package org.twitterexporter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class Properties {
    
    @Getter @Setter
    @Value("${bearer-token}")
    private String bearerToken;

    @Getter @Setter
    @Value("${filepath}")
    private String filepath;

    @Getter @Setter
    @Value("${startDatetime}")
    private String startDatetime;
    
    @Getter @Setter
    @Value("${endDatetime}")
    private String endDatetime;

    @Getter @Setter
    @Value("${usernames}")
    private String usernames;

}
