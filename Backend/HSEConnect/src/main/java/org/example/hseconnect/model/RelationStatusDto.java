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
}