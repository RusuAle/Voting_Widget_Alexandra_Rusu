package com.sg.voting.repository;

import com.sg.voting.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<Poll, String> {
    Optional<Poll> findByPollId(String pollId);
}