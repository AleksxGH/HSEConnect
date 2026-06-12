package org.example.hseconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "app", name = "user_interest")
@IdClass(UserInterestId.class)
public class UserInterest {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "interest_id")
    private Long interestId;

    public UserInterest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getInterestId() { return interestId; }
    public void setInterestId(Long interestId) { this.interestId = interestId; }

}
