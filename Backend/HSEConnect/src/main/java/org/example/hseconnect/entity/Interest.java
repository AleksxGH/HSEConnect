package org.example.hseconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "app", name = "interest")
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "is_approved")
    private Boolean approved;

    public Interest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public Boolean isApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

}
