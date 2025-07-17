package com.facebook.service;

import com.facebook.enums.FriendStatus;
import com.facebook.repository.CommentRepository;
import com.facebook.repository.FriendRepository;
import com.facebook.repository.LikeRepository;
import com.facebook.repository.PostRepository;
import com.facebook.util.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class StatisticService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final FriendRepository friendRepository;

    HashMap<String, Long> result = new HashMap<>();

    public ResponseEntity<?> getAllTimeStatistic(Long userId) {
        result.put("amount of posts", (long) postRepository.findByUserId(userId).size());
        result.put("amount of given comments", (long) commentRepository.findByUserId(userId).size());
        result.put("amount of given likes", (long) likeRepository.findByUserId(userId).size());
        result.put("amount of received comments", postRepository.findByUserId(userId)
                .stream()
                .mapToLong(post -> post.getComments().size())
                .sum());
        result.put("amount of received likes", postRepository.findByUserId(userId)
                .stream()
                .mapToLong(post -> post.getLikes().size())
                .sum());
        result.put("amount of friends", (long) friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId).size()
                + friendRepository.findByStatusAndFriendId(FriendStatus.ACCEPTED, userId).size());
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Statistic for all time fetched successfully",
                result
        );
    }


    public ResponseEntity<?> getStatisticForLastDays(Long userId, Long days) {
        if (days < 1)
            return ResponseHandler.generateResponse(
                    HttpStatus.BAD_REQUEST,
                    true,
                    "Amount of days must be greater than 0",
                    null
            );

        result.put("amount of posts", postRepository.findByUserId(userId)
                .stream()
                .filter(post -> ChronoUnit.DAYS.between(post.getCreatedDate().toLocalDate(), LocalDate.now()) < days)
                .count()
        );
        result.put("amount of given comments", commentRepository.findByUserId(userId)
                .stream()
                .filter(com -> ChronoUnit.DAYS.between(com.getCreatedDate().toLocalDate(), LocalDate.now()) < days)
                .count()
        );
        result.put("amount of given likes", likeRepository.findByUserId(userId)
                .stream()
                .filter(like -> ChronoUnit.DAYS.between(like.getCreatedDate().toLocalDate(), LocalDate.now()) < days)
                .count()
        );
        result.put("amount of received comments", postRepository.findByUserId(userId)
                .stream()
                .filter(post -> ChronoUnit.DAYS.between(post.getCreatedDate().toLocalDate(), LocalDate.now()) < days)
                .mapToLong(post -> post.getComments().size())
                .sum());
        result.put("amount of received likes", postRepository.findByUserId(userId)
                .stream()
                .filter(post -> ChronoUnit.DAYS.between(post.getCreatedDate().toLocalDate(), LocalDate.now()) < days)
                .mapToLong(post -> post.getLikes().size())
                .sum());
        result.put("amount of friends",
                friendRepository.findByStatusAndUserId(FriendStatus.ACCEPTED, userId)
                        .stream()
                        .filter(friend -> ChronoUnit.DAYS.between(friend.getAcceptedDate().toLocalDate(), LocalDate.now()) < days)
                        .count()
                + friendRepository.findByStatusAndFriendId(FriendStatus.ACCEPTED, userId)
                        .stream()
                        .filter(friend -> ChronoUnit.DAYS.between(friend.getAcceptedDate().toLocalDate(), LocalDate.now()) < days)
                        .count()
        );
        return ResponseHandler.generateResponse(
                HttpStatus.OK,
                false,
                "Statistic for past time fetched successfully",
                result
        );
    }
}
