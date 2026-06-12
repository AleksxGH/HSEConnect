package org.example.hseconnect.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlockService {

    private final JdbcTemplate jdbcTemplate;

    public BlockService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new RuntimeException("Нельзя заблокировать самого себя");
        }

        jdbcTemplate.update("""
            INSERT INTO app.user_block (blocker_id, blocked_id, created_at)
            VALUES (?, ?, NOW())
            ON CONFLICT DO NOTHING
        """, blockerId, blockedId);

        jdbcTemplate.update("""
            DELETE FROM app.follow
            WHERE (follower_id = ? AND following_id = ?)
               OR (follower_id = ? AND following_id = ?)
        """, blockerId, blockedId, blockedId, blockerId);

        jdbcTemplate.update("""
            DELETE FROM app.friendship
            WHERE (user_id_1 = LEAST(?, ?) AND user_id_2 = GREATEST(?, ?))
        """, blockerId, blockedId, blockerId, blockedId);
    }

    @Transactional
    public void unblockUser(Long blockerId, Long blockedId) {
        jdbcTemplate.update("""
            DELETE FROM app.user_block
            WHERE blocker_id = ? AND blocked_id = ?
        """, blockerId, blockedId);
    }

    public boolean isBlockedBy(Long blockerId, Long blockedId) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM app.user_block
            WHERE blocker_id = ? AND blocked_id = ?
        """, Integer.class, blockerId, blockedId);

        return count != null && count > 0;
    }

    public boolean hasBlockBetween(Long userA, Long userB) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM app.user_block
            WHERE (blocker_id = ? AND blocked_id = ?)
               OR (blocker_id = ? AND blocked_id = ?)
        """, Integer.class, userA, userB, userB, userA);

        return count != null && count > 0;
    }
}