package org.example.hseconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(schema = "app", name = "employee_profile")
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_profile_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "work_phone")
    private String workPhone;

    @Column(name = "work_address_id")
    private Long workAddressId;

    public EmployeeProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getWorkPhone() { return workPhone; }
    public void setWorkPhone(String workPhone) { this.workPhone = workPhone; }

    public Long getWorkAddressId() { return workAddressId; }
    public void setWorkAddressId(Long workAddressId) { this.workAddressId = workAddressId; }

}
