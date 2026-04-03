package com.zorvyn.finance.entities;

import com.zorvyn.finance.enums.Gender;
import com.zorvyn.finance.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_role", columnList = "role")
        }
)
@Getter
@Setter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "mobile_no", unique = true, length = 20)
    private String mobileNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;
}