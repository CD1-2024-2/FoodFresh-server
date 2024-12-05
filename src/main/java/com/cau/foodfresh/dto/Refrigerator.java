package com.cau.foodfresh.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Refrigerator {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewRefrigerator {
        private String name;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefrigeratorInfo {
        private String rid;
        private String name;
        private String description;
        private Timestamp createdDate;
        private String manager;
        private List<String> sharedUsers;
        private Boolean isShared;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRefrigeratorResponse {
        private String rid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SharedUsers {
        private List<String> sharedUserIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefrigeratorInfoResponse {
        private String rid;
        private String name;
        private String description;
        private String createdDate; // yyyy-MM-dd
        private String manager;
        private List<String> sharedUsers;
        private Boolean isShared;

        public RefrigeratorInfoResponse(Refrigerator.RefrigeratorInfo fridge) {
            this.rid = fridge.getRid();
            this.name = fridge.getName();
            this.description = fridge.getDescription();
            this.createdDate = convertTimestampToString(fridge.getCreatedDate());
            this.manager = fridge.getManager();
            this.sharedUsers = fridge.getSharedUsers();
            this.isShared = fridge.getIsShared();
        }

        // Timestamp -> yyyy-MM-dd 변환
        private String convertTimestampToString(Timestamp timestamp) {
            if (timestamp == null) {
                return null;
            }
            return DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .withZone(ZoneId.of("UTC"))
                    .format(timestamp.toDate().toInstant());
        }
    }

}