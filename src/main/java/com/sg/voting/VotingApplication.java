package com.sg.voting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VotingApplication {
    public static void main(String[] args) {
        SpringApplication.run(VotingApplication.class, args);
        System.out.println("  Voting Widget Service - Saint-Gobain");
        System.out.println("  http://localhost:8080");

    }
}