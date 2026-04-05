package com.sg.voting.model;

public class PollListItem {
    private String pollId;
    private String title;
    private int optionCount;
    private long totalVotes;
    private boolean voted;
    private long maxVotes;

    public PollListItem(Poll poll, boolean voted) {
        this.pollId = poll.getPollId();
        this.title = poll.getTitle() != null && !poll.getTitle().isBlank() ? poll.getTitle() : "Untitled Poll";
        this.optionCount = poll.getOptions().size();
        this.totalVotes = poll.getTotalVotes();
        this.voted = voted;
        this.maxVotes = poll.getOptions().stream().mapToLong(PollOption::getVotes).max().orElse(0);
    }

    public String getPollId() { return pollId; }
    public String getTitle() { return title; }
    public int getOptionCount() { return optionCount; }
    public long getTotalVotes() { return totalVotes; }
    public boolean isVoted() { return voted; }
    public long getMaxVotes() { return maxVotes; }
    public double getBarWidth() {
        if (maxVotes == 0) return 0;
        return Math.min(100, (double) totalVotes / (maxVotes * optionCount) * 100);
    }
}