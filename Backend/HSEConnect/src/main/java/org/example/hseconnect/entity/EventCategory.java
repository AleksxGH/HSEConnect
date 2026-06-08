package org.example.hseconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "app", name = "event_category")
public class EventCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_category_id")
    private Long id;

    @Column(name = "name")
    private String name;

    public EventCategory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
