package com.sg.voting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "poll_options")
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    @Column(name = "votes", nullable = false)
    private long votes = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    public PollOption() {}

    public PollOption(String optionText, Poll poll) {
        this.optionText = optionText;
        this.poll = poll;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }

    public long getVotes() { return votes; }
    public void setVotes(long votes) { this.votes = votes; }

    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }

    public void incrementVotes() { this.votes++; }

    public double getPercentage(long total) {
        if (total == 0) return 0;
        return (double) votes / total * 100;
    }
}