package com.sg.voting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"poll_id", "voter_key"})
})
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "poll_id", nullable = false)
    private String pollId;

    @Column(name = "voter_key", nullable = false)
    private String voterKey;

    public Vote() {}

    public Vote(String pollId, String voterKey) {
        this.pollId = pollId;
        this.voterKey = voterKey;
    }

    public Long getId() { return id; }
    public String getPollId() { return pollId; }
    public String getVoterKey() { return voterKey; }
}