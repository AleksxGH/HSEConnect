package org.example.hseconnect.model;

import java.util.ArrayList;
import java.util.List;

public class EventDto {
    private Long id;
    private Long creatorId;
    private Long categoryId;
    private Long accessTypeId;
    private Long addressId;

    private String title;
    private String type;
    private String location;
    private String date;
    private String time;
    private String description;

    private String startsAt;
    private String endsAt;
    private Integer maxParticipants;
    private String status;
    private String visibility;
    private Integer participantsCount = 0;

    private String photoUrl;

    private List<Long> respondedUserIds = new ArrayList<>();

    public EventDto() {}

    private String privacy;
    private List<Long> invitedFriends = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getAccessTypeId() { return accessTypeId; }
    public void setAccessTypeId(Long accessTypeId) { this.accessTypeId = accessTypeId; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStartsAt() { return startsAt; }
    public void setStartsAt(String startsAt) { this.startsAt = startsAt; }
    public String getEndsAt() { return endsAt; }
    public void setEndsAt(String endsAt) { this.endsAt = endsAt; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public List<Long> getRespondedUserIds() { return respondedUserIds; }
    public void setRespondedUserIds(List<Long> respondedUserIds) { this.respondedUserIds = respondedUserIds; }
    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public List<Long> getInvitedFriends() {
        return invitedFriends;
    }

    public void setInvitedFriends(List<Long> invitedFriends) {
        this.invitedFriends = invitedFriends;
    }

    public Integer getParticipantsCount() {
        return participantsCount;
    }

    public void setParticipantsCount(Integer participantsCount) {
        this.participantsCount = participantsCount;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
