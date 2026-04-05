package com.sg.voting.repository;

import com.sg.voting.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<PollOption, Long> {
    List<PollOption> findByPollPollId(String pollId);
}