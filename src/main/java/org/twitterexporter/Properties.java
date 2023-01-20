package org.twitterexporter;

import java.util.Arrays;
import java.util.List;

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
    
    @Setter
    @Value("${usernames}")
    private String usernames;

    public List<String> getUsernames() {return Arrays.asList(usernames.split(","));}

}
