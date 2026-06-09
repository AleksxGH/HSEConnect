package org.example.hseconnect.services;

import org.example.hseconnect.model.FriendUserDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendsService {

    private final JdbcTemplate jdbcTemplate;

    public FriendsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FriendUserDto> getFriends(Long userId) {
        String sql = """
        SELECT 
            other_user.user_id,
            CONCAT(p.first_name, ' ', p.last_name) AS name,
            p.avatar_url
        FROM (
            SELECT 
                CASE
                    WHEN f.user_id_1 = ? THEN f.user_id_2
                    ELSE f.user_id_1
                END AS user_id
            FROM app.friendship f
            WHERE f.user_id_1 = ? OR f.user_id_2 = ?
        ) other_user
        JOIN app.profile p ON p.user_id = other_user.user_id
        ORDER BY p.first_name, p.last_name
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long friendId = rs.getLong("user_id");
            String name = rs.getString("name");

            return new FriendUserDto(
                    friendId,
                    name,
                    makeAvatar(name),
                    "offline",
                    true,
                    countMutualFriends(userId, friendId)
            );
        }, userId, userId, userId);
    }

    public List<FriendUserDto> getFollowers(Long userId) {
        String sql = """
            SELECT 
                u.user_id,
                CONCAT(p.first_name, ' ', p.last_name) AS name,
                p.avatar_url
            FROM app.follow f
            JOIN app."users" u ON u.user_id = f.follower_id
            JOIN app.profile p ON p.user_id = u.user_id
            WHERE f.following_id = ?
            ORDER BY p.first_name, p.last_name
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long followerId = rs.getLong("user_id");
            return new FriendUserDto(
                    followerId,
                    rs.getString("name"),
                    makeAvatar(rs.getString("name")),
                    "offline",
                    areFriends(userId, followerId),
                    countMutualFriends(userId, followerId)
            );
        }, userId);
    }

    public List<FriendUserDto> getFollowing(Long userId) {
        String sql = """
            SELECT 
                u.user_id,
                CONCAT(p.first_name, ' ', p.last_name) AS name,
                p.avatar_url
            FROM app.follow f
            JOIN app."users" u ON u.user_id = f.following_id
            JOIN app.profile p ON p.user_id = u.user_id
            WHERE f.follower_id = ?
            ORDER BY p.first_name, p.last_name
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long followingId = rs.getLong("user_id");
            return new FriendUserDto(
                    followingId,
                    rs.getString("name"),
                    makeAvatar(rs.getString("name")),
                    "offline",
                    areFriends(userId, followingId),
                    countMutualFriends(userId, followingId)
            );
        }, userId);
    }

    public void follow(Long userId, Long targetUserId) {
        validateDifferentUsers(userId, targetUserId);

        Integer exists = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM app.follow
            WHERE follower_id = ? AND following_id = ?
        """, Integer.class, userId, targetUserId);

        if (exists != null && exists > 0) {
            throw new RuntimeException("Вы уже подписаны на этого пользователя");
        }

        jdbcTemplate.update("""
            INSERT INTO app.follow (follower_id, following_id, created_at)
            VALUES (?, ?, NOW())
        """, userId, targetUserId);
    }

    public void unfollow(Long userId, Long targetUserId) {
        jdbcTemplate.update("""
            DELETE FROM app.follow
            WHERE follower_id = ? AND following_id = ?
        """, userId, targetUserId);

        removeFriend(userId, targetUserId);
    }

    public void addFriend(Long userId, Long targetUserId) {
        validateDifferentUsers(userId, targetUserId);

        if (areFriends(userId, targetUserId)) {
            throw new RuntimeException("Пользователь уже у вас в друзьях");
        }

        follow(userId, targetUserId);

        Integer targetFollowsMe = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM app.follow
            WHERE follower_id = ? AND following_id = ?
        """, Integer.class, targetUserId, userId);

        if (targetFollowsMe != null && targetFollowsMe > 0) {
            Long a = Math.min(userId, targetUserId);
            Long b = Math.max(userId, targetUserId);

            jdbcTemplate.update("""
                INSERT INTO app.friendship (user_id_1, user_id_2, created_at)
                VALUES (?, ?, NOW())
            """, a, b);
        }
    }

    public void removeFriend(Long userId, Long targetUserId) {
        Long a = Math.min(userId, targetUserId);
        Long b = Math.max(userId, targetUserId);

        jdbcTemplate.update("""
            DELETE FROM app.friendship
            WHERE user_id_1 = ? AND user_id_2 = ?
        """, a, b);
    }

    public void removeFollower(Long userId, Long followerId) {
        jdbcTemplate.update("""
            DELETE FROM app.follow
            WHERE follower_id = ? AND following_id = ?
        """, followerId, userId);

        removeFriend(userId, followerId);
    }

    private boolean areFriends(Long userId, Long targetUserId) {
        Long a = Math.min(userId, targetUserId);
        Long b = Math.max(userId, targetUserId);

        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM app.friendship
            WHERE user_id_1 = ? AND user_id_2 = ?
        """, Integer.class, a, b);

        return count != null && count > 0;
    }

    private int countMutualFriends(Long userId, Long otherUserId) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM app.friendship f1
            JOIN app.friendship f2
                ON (
                    CASE WHEN f1.user_id_1 = ? THEN f1.user_id_2 ELSE f1.user_id_1 END
                ) = (
                    CASE WHEN f2.user_id_1 = ? THEN f2.user_id_2 ELSE f2.user_id_1 END
                )
            WHERE 
                (f1.user_id_1 = ? OR f1.user_id_2 = ?)
                AND (f2.user_id_1 = ? OR f2.user_id_2 = ?)
        """, Integer.class, userId, otherUserId, userId, userId, otherUserId, otherUserId);

        return count == null ? 0 : count;
    }

    private void validateDifferentUsers(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new RuntimeException("Нельзя выполнить действие с самим собой");
        }
    }

    private String makeAvatar(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return name.substring(0, 1).toUpperCase();
    }
}