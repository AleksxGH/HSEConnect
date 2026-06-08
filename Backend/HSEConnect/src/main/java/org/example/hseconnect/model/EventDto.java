package org.example.hseconnect.model;

import java.util.ArrayList;
import java.util.List;

public class EventDto {
    private Long id;
    private String title;
    private String type;
    private String location;
    private String date;
    private String time;
    private String description;
    private List<Long> respondedUserIds = new ArrayList<>();

    public EventDto() {}

    public EventDto(Long id, String title, String type, String location, String date, String time, String description,  List<Long> respondedUserIds) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.location = location;
        this.date = date;
        this.time = time;
        this.description = description;
        this.respondedUserIds = respondedUserIds;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setType(String type) { this.type = type; }
    public void setLocation(String location) { this.location = location; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setDescription(String description) { this.description = description; }

    public List<Long> getRespondedUserIds() {
        return respondedUserIds;
    }

    public void setRespondedUserIds(List<Long> respondedUserIds) {
        this.respondedUserIds = respondedUserIds;
    }
}
