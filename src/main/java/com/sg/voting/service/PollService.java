package com.sg.voting.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sg.voting.model.Poll;
import com.sg.voting.model.PollOption;
import com.sg.voting.model.Vote;
import com.sg.voting.repository.OptionRepository;
import com.sg.voting.repository.PollRepository;
import com.sg.voting.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class PollService {

    private final PollRepository pollRepository;
    private final OptionRepository optionRepository;
    private final VoteRepository voteRepository;

    public PollService(PollRepository pollRepository,
                       OptionRepository optionRepository,
                       VoteRepository voteRepository) {
        this.pollRepository = pollRepository;
        this.optionRepository = optionRepository;
        this.voteRepository = voteRepository;
    }

    public List<Poll> findAllPolls() {
        return pollRepository.findAll();
    }

    public Optional<Poll> findPoll(String pollId) {
        return pollRepository.findByPollId(pollId);
    }

    public boolean hasVoted(String pollId, String voterKey) {
        return voteRepository.existsByPollIdAndVoterKey(pollId, voterKey);
    }

    @Transactional
    public Poll setTitle(String pollId, String title) {
        Poll poll = pollRepository.findByPollId(pollId).orElse(new Poll(pollId));
        poll.setTitle(title);
        return pollRepository.save(poll);
    }

    @Transactional
    public Poll addOption(String pollId, String optionText) {
        Poll poll = pollRepository.findByPollId(pollId).orElse(new Poll(pollId));
        poll = pollRepository.save(poll);
        optionRepository.save(new PollOption(optionText, poll));
        return pollRepository.findByPollId(pollId).orElseThrow();
    }

    @Transactional
    public boolean castVote(String pollId, Long optionId, String voterKey) {
        if (voteRepository.existsByPollIdAndVoterKey(pollId, voterKey)) return false;
        Optional<PollOption> optOpt = optionRepository.findById(optionId);
        if (optOpt.isEmpty()) return false;
        PollOption option = optOpt.get();
        if (!option.getPoll().getPollId().equals(pollId)) return false;
        option.incrementVotes();
        optionRepository.save(option);
        voteRepository.save(new Vote(pollId, voterKey));
        return true;
    }

    @Transactional
    public boolean removeOption(String pollId, Long optionId) {
        Optional<PollOption> optOpt = optionRepository.findById(optionId);
        if (optOpt.isEmpty()) return false;
        PollOption option = optOpt.get();
        if (!option.getPoll().getPollId().equals(pollId)) return false;
        optionRepository.delete(option);
        return true;
    }

    public String generateQrCodeBase64(String url) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            return "";
        }
    }
}