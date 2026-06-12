package org.example.hseconnect.controller;

import org.example.hseconnect.services.BlockService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/{currentUserId}/{targetUserId}")
    public void blockUser(
            @PathVariable Long currentUserId,
            @PathVariable Long targetUserId
    ) {
        blockService.blockUser(currentUserId, targetUserId);
    }

    @DeleteMapping("/{currentUserId}/{targetUserId}")
    public void unblockUser(
            @PathVariable Long currentUserId,
            @PathVariable Long targetUserId
    ) {
        blockService.unblockUser(currentUserId, targetUserId);
    }

    @GetMapping("/{currentUserId}/{targetUserId}")
    public boolean isBlocked(
            @PathVariable Long currentUserId,
            @PathVariable Long targetUserId
    ) {
        return blockService.hasBlockBetween(currentUserId, targetUserId);
    }
}