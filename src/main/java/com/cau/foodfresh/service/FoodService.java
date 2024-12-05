package com.cau.foodfresh.service;

import com.cau.foodfresh.dto.Food;
import com.cau.foodfresh.dto.Refrigerator;
import com.cau.foodfresh.enums.FoodDeletionReason;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FoodService {

    private final Firestore firestore = FirestoreClient.getFirestore();

    // 식품 추가
    public String addFood(String refrigeratorId, Food.NewFood food) throws Exception {
        DocumentReference fridgeRef = firestore.collection("Refrigerators").document(refrigeratorId);

        // 냉장고 존재 확인
        if (!fridgeRef.get().get().exists()) {
            throw new Exception("Refrigerator not found.");
        }

        // 기본값 처리
        if (Objects.equals(food.getCategory(), "")) food.setCategory("기타");
        if (Objects.equals(food.getQuantity(), 0)) food.setQuantity(1);

        // 유통기한 처리
        String expirationDate = food.getExpirationDate();
        com.google.cloud.Timestamp expirationTimestamp = com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                LocalDate.parse(expirationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        .atStartOfDay(ZoneOffset.UTC)
                        .toEpochSecond(),
                0
        );

        // 등록일 처리 (현재 날짜)
        com.google.cloud.Timestamp registeredTimestamp = com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                LocalDate.now().atStartOfDay(ZoneOffset.UTC).toEpochSecond(),
                0
        );

        // Firestore에 저장
        DocumentReference foodRef = fridgeRef.collection("Foods").document();
        Food.FoodInfo foodInfo = new Food.FoodInfo(
                foodRef.getId(),
                food.getImageURL(),
                food.getName(),
                expirationTimestamp,
                registeredTimestamp,
                food.getQuantity(),
                food.getCategory(),
                food.getBarcode(),
                food.getDescription()
        );

        foodRef.set(foodInfo).get();

        return foodInfo.getFid(); // 생성된 식품 ID 반환
    }

    // 식품 조회
    public List<Food.FoodInfoResponse> getFoodList(String refrigeratorId) throws Exception {
        DocumentReference fridgeRef = firestore.collection("Refrigerators").document(refrigeratorId);

        // 냉장고 존재 여부 확인
        if (!fridgeRef.get().get().exists()) {
            throw new Exception("Refrigerator not found.");
        }

        // Foods 컬렉션 가져오기
        CollectionReference foodsRef = fridgeRef.collection("Foods");
        List<QueryDocumentSnapshot> documents = foodsRef.get().get().getDocuments();

        // Food.FoodInfo 객체 생성
        List<Food.FoodInfoResponse> foodList = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Food.FoodInfo foodInfo = document.toObject(Food.FoodInfo.class);
            foodList.add(new Food.FoodInfoResponse(foodInfo)); // 변환된 DTO로 추가
        }

        return foodList;
    }

    // 식품 삭제
    public void deleteFood(String refrigeratorId, String foodId, String userId) throws Exception {
        DocumentReference fridgeRef = firestore.collection("Refrigerators").document(refrigeratorId);

        // 냉장고 존재 확인
        DocumentSnapshot fridgeSnapshot = fridgeRef.get().get();
        if (!fridgeSnapshot.exists()) {
            throw new Exception("Refrigerator not found.");
        }

        // 권한 확인 (냉장고 관리자 or 공유 사용자 삭제 가능)
        Refrigerator.RefrigeratorInfo refrigerator = fridgeSnapshot.toObject(Refrigerator.RefrigeratorInfo.class);
        if (refrigerator == null) {
            throw new Exception("Failed to parse refrigerator data.");
        }
        if (!refrigerator.getManager().equals(userId) &&
                (refrigerator.getSharedUsers() == null || !refrigerator.getSharedUsers().contains(userId))) {
            throw new Exception("You do not have permission to delete food from this refrigerator.");
        }

        // 식품 존재 확인 및 삭제
        DocumentReference foodRef = fridgeRef.collection("Foods").document(foodId);
        DocumentSnapshot foodSnapshot = foodRef.get().get();
        if (!foodSnapshot.exists()) {
            throw new Exception("Food not found.");
        }

//         식품 삭제
        foodRef.delete().get();
    }

    // 식품 상세 삭제
    public void deleteDetailedFood(String refrigeratorId, String foodId, String userId, FoodDeletionReason reason) throws Exception {
        DocumentReference fridgeRef = firestore.collection("Refrigerators").document(refrigeratorId);

        // 냉장고 존재 확인
        DocumentSnapshot fridgeSnapshot = fridgeRef.get().get();
        if (!fridgeSnapshot.exists()) {
            throw new Exception("Refrigerator not found.");
        }

        // 권한 확인 (냉장고 관리자 or 공유 사용자 삭제 가능)
        Refrigerator.RefrigeratorInfo refrigerator = fridgeSnapshot.toObject(Refrigerator.RefrigeratorInfo.class);
        if (refrigerator == null) {
            throw new Exception("Failed to parse refrigerator data.");
        }
        if (!refrigerator.getManager().equals(userId) &&
                (refrigerator.getSharedUsers() == null || !refrigerator.getSharedUsers().contains(userId))) {
            throw new Exception("You do not have permission to delete food from this refrigerator.");
        }

        // 식품 존재 확인 및 삭제
        DocumentReference foodRef = fridgeRef.collection("Foods").document(foodId);
        DocumentSnapshot foodSnapshot = foodRef.get().get();
        if (!foodSnapshot.exists()) {
            throw new Exception("Food not found.");
        }

        // 삭제 식품 정보 저장
        Food.FoodInfo foodInfo = foodSnapshot.toObject(Food.FoodInfo.class);
        if (foodInfo != null) {
            Food.DeletedFoodInfo deletedFood = new Food.DeletedFoodInfo(foodInfo, userId, reason);
//            fridgeRef.collection("DeletedFoods").document(foodId).set(deletedFood).get();
            firestore.collection("User").document(userId)
                    .collection("DeletedFoods").document(foodId).set(deletedFood).get();
        }

        foodRef.delete().get();
    }
}