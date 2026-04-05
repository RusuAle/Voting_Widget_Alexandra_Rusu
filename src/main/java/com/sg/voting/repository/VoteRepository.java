package com.sg.voting.repository;

import com.sg.voting.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByPollIdAndVoterKey(String pollId, String voterKey);
}