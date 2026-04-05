package com.sg.voting.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
public class Poll {

    @Id
    @Column(name = "poll_id", nullable = false, unique = true)
    private String pollId;

    @Column(name = "title")
    private String title;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PollOption> options = new ArrayList<>();

    public Poll() {}

    public Poll(String pollId) {
        this.pollId = pollId;
    }

    public String getPollId() { return pollId; }
    public void setPollId(String pollId) { this.pollId = pollId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<PollOption> getOptions() { return options; }
    public void setOptions(List<PollOption> options) { this.options = options; }

    public boolean isInitialized() {
        return (title != null && !title.isBlank()) || !options.isEmpty();
    }

    public long getTotalVotes() {
        return options.stream().mapToLong(PollOption::getVotes).sum();
    }
}