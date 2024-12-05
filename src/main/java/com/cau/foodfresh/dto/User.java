package com.cau.foodfresh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class User {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Signup {
        private String id;
        private String name;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {
        private String id;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String id;
    }
}