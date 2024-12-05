package com.cau.foodfresh.dto;

import com.cau.foodfresh.enums.FoodDeletionReason;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Food {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private String ImageURL;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewFood {
        private String imageURL;
        private String name;
        private String expirationDate;
        private int quantity;
        private String category;
        private String barcode;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodInfo {
        private String fid;
        private String imageURL;
        private String name;
        private Timestamp expirationDate;
        private Timestamp registeredDate;
        private int quantity;
        private String category;
        private String barcode;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddFoodResponse {
        private String fid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodInfoResponse {
        private String fid;
        private String imageURL;
        private String name;
        private String expirationDate; // yyyy-MM-dd
        private String registeredDate; // yyyy-MM-dd
        private int quantity;
        private String category;
        private String barcode;
        private String description;

        public FoodInfoResponse(Food.FoodInfo foodInfo) {
            this.fid = foodInfo.getFid();
            this.imageURL = foodInfo.getImageURL();
            this.name = foodInfo.getName();
            this.expirationDate = convertTimestampToString(foodInfo.getExpirationDate());
            this.registeredDate = convertTimestampToString(foodInfo.getRegisteredDate());
            this.quantity = foodInfo.getQuantity();
            this.category = foodInfo.getCategory();
            this.barcode = foodInfo.getBarcode();
            this.description = foodInfo.getDescription();
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeletedFoodInfo {
        private String fid;
        private String name;
        private String imageURL;
        private Timestamp expirationDate;
        private Timestamp deletedDate;
        private String deletedBy;
        private String reason;

        public DeletedFoodInfo(Food.FoodInfo foodInfo, String userId, FoodDeletionReason reason) {
            this.fid = foodInfo.getFid();
            this.name = foodInfo.getName();
            this.imageURL = foodInfo.getImageURL();
            this.expirationDate = foodInfo.getExpirationDate();
            this.deletedDate = Timestamp.now();
            this.deletedBy = userId;
            this.reason = reason.name();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeletedFoodInfoResponse {
        private String fid;
        private String name;
        private String imageURL;
        private String expirationDate; // yyyy-MM-dd
        private String deletedDate;    // yyyy-MM-dd
        private String reason;

        public DeletedFoodInfoResponse(Food.DeletedFoodInfo deletedFood) {
            this.fid = deletedFood.getFid();
            this.name = deletedFood.getName();
            this.imageURL = deletedFood.getImageURL();
            this.expirationDate = convertTimestampToString(deletedFood.getExpirationDate());
            this.deletedDate = convertTimestampToString(deletedFood.getDeletedDate());
            this.reason = deletedFood.getReason();
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