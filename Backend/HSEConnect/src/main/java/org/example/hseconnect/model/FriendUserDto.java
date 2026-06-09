package org.example.hseconnect.model;

public class FriendUserDto {
    private Long id;
    private String name;
    private String avatar;
    private String status;
    private boolean friend;
    private int mutualFriends;

    public FriendUserDto(Long id, String name, String avatar, String status, boolean friend, int mutualFriends) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.status = status;
        this.friend = friend;
        this.mutualFriends = mutualFriends;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAvatar() { return avatar; }
    public String getStatus() { return status; }
    public boolean isFriend() { return friend; }
    public int getMutualFriends() { return mutualFriends; }
}