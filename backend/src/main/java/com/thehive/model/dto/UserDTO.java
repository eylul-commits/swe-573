package com.thehive.model.dto;

import com.thehive.model.enums.UserRole;
import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String email;
    private String name;
    private String bio;
    private String province;
    private String district;
    private String geohash;
    private UserRole role;
    private Integer balanceHours;
}

