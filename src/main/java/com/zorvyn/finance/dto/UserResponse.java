package com.zorvyn.finance.dto;

import com.zorvyn.finance.enums.Gender;
import com.zorvyn.finance.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String mobileNo;
    private Role role;
    private Gender gender;
    private String status;
    private Set<String> permissions;
    private OffsetDateTime dateCreated;
    private OffsetDateTime dateUpdated;
}

