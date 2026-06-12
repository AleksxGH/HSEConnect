package org.example.hseconnect.model;

public class RelationStatusDto {

    private boolean friend;
    private boolean following;

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    private boolean blocked;
    private boolean blockedByTarget;

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isBlockedByTarget() {
        return blockedByTarget;
    }

    public void setBlockedByTarget(boolean blockedByTarget) {
        this.blockedByTarget = blockedByTarget;
    }
}