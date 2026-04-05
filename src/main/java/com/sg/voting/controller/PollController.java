package com.sg.voting.controller;

import com.sg.voting.model.Poll;
import com.sg.voting.model.PollListItem;
import com.sg.voting.service.NgrokService;
import com.sg.voting.service.PollService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.MessageDigest;
import java.util.*;

@Controller
public class PollController {

    private final PollService pollService;
    private final NgrokService ngrokService;

    public PollController(PollService pollService, NgrokService ngrokService) {
        this.pollService = pollService;
        this.ngrokService = ngrokService;
    }

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        if (isNgrokRequest(request)) return "redirect:/polls";
        return "index";
    }

    @GetMapping("/polls")
    public String pollsList(HttpServletRequest request, Model model) {
        List<Poll> allPolls = pollService.findAllPolls();
        String voterKey = getVoterKey(request);

        List<PollListItem> pollList = new ArrayList<>();
        long totalVotesCast = 0;
        int votedCount = 0;

        for (Poll p : allPolls) {
            if (!p.isInitialized()) continue;
            boolean voted = pollService.hasVoted(p.getPollId(), voterKey);
            pollList.add(new PollListItem(p, voted));
            totalVotesCast += p.getTotalVotes();
            if (voted) votedCount++;
        }

        model.addAttribute("pollList", pollList);
        model.addAttribute("pollCount", pollList.size());
        model.addAttribute("totalVotesCast", totalVotesCast);
        model.addAttribute("votedCount", votedCount);
        return "polls";
    }

    @GetMapping("/{pollId}")
    public String widget(@PathVariable String pollId,
                         HttpServletRequest request,
                         Model model) {
        Optional<Poll> pollOpt = pollService.findPoll(pollId);
        if (pollOpt.isEmpty() || !pollOpt.get().isInitialized()) {
            model.addAttribute("pollId", pollId);
            return "widget";
        }
        Poll poll = pollOpt.get();
        String base = ngrokService.isActive() ? ngrokService.getPublicUrl() : getLocalBaseUrl(request);
        String voteUrl = base + "/" + pollId + "/vote";
        String qrCode = pollService.generateQrCodeBase64(voteUrl);

        model.addAttribute("poll", poll);
        model.addAttribute("voteUrl", voteUrl);
        model.addAttribute("qrCode", qrCode);
        model.addAttribute("alreadyVoted", pollService.hasVoted(pollId, getVoterKey(request)));
        model.addAttribute("totalVotes", poll.getTotalVotes());
        return "widget";
    }

    @GetMapping("/{pollId}/vote")
    public String votePage(@PathVariable String pollId,
                           HttpServletRequest request,
                           Model model) {
        Optional<Poll> pollOpt = pollService.findPoll(pollId);
        if (pollOpt.isEmpty() || !pollOpt.get().isInitialized()) {
            model.addAttribute("error", "Invalid or uninitialized voting poll.");
            model.addAttribute("pollId", pollId);
            return "vote";
        }
        Poll poll = pollOpt.get();
        model.addAttribute("poll", poll);
        model.addAttribute("pollId", pollId);
        model.addAttribute("alreadyVoted", pollService.hasVoted(pollId, getVoterKey(request)));
        return "vote";
    }

    @PostMapping("/{pollId}/vote")
    public String castVote(@PathVariable String pollId,
                           @RequestParam Long optionId,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        String voterKey = getVoterKey(request);
        boolean success = pollService.castVote(pollId, optionId, voterKey);
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", "Your vote has been recorded!");
        } else {
            if (pollService.hasVoted(pollId, voterKey)) {
                redirectAttributes.addFlashAttribute("alreadyVotedMsg", "You have already voted in this poll.");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Invalid option. Please try again.");
            }
        }
        return "redirect:/" + pollId + "/vote";
    }

    @GetMapping("/{pollId}/admin")
    public String adminPage(@PathVariable String pollId, Model model) {
        Optional<Poll> pollOpt = pollService.findPoll(pollId);
        Poll poll = pollOpt.orElse(null);
        model.addAttribute("poll", poll);
        model.addAttribute("pollId", pollId);
        model.addAttribute("initialized", poll != null && poll.isInitialized());
        return "admin";
    }

    @PostMapping("/{pollId}/admin/title")
    public String setTitle(@PathVariable String pollId, @RequestParam String title,
                           RedirectAttributes ra) {
        pollService.setTitle(pollId, title);
        ra.addFlashAttribute("successMsg", "Title updated successfully!");
        return "redirect:/" + pollId + "/admin";
    }

    @PostMapping("/{pollId}/admin/option")
    public String addOption(@PathVariable String pollId, @RequestParam String optionText,
                            RedirectAttributes ra) {
        if (optionText == null || optionText.isBlank()) {
            ra.addFlashAttribute("errorMsg", "Option text cannot be empty.");
            return "redirect:/" + pollId + "/admin";
        }
        pollService.addOption(pollId, optionText);
        ra.addFlashAttribute("successMsg", "Option added!");
        return "redirect:/" + pollId + "/admin";
    }

    @PostMapping("/{pollId}/admin/option/{optionId}/delete")
    public String removeOption(@PathVariable String pollId, @PathVariable Long optionId,
                               RedirectAttributes ra) {
        pollService.removeOption(pollId, optionId);
        ra.addFlashAttribute("successMsg", "Option removed.");
        return "redirect:/" + pollId + "/admin";
    }

    private String getVoterKey(HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        if (ua == null) ua = "unknown";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest((ip + "|" + ua).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return ip + "|" + ua;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For","X-Real-IP","Proxy-Client-IP","WL-Proxy-Client-IP"};
        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip))
                return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isNgrokRequest(HttpServletRequest request) {
        if (!ngrokService.isActive()) return false;
        String host = request.getHeader("Host");
        if (host == null) return false;
        String ngrokHost = ngrokService.getPublicUrl()
                .replace("https://","").replace("http://","");
        return host.equals(ngrokHost);
    }

    private String getLocalBaseUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort();
    }
}