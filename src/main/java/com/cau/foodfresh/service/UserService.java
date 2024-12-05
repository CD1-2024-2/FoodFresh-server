package com.cau.foodfresh.service;

import com.cau.foodfresh.dto.ErrorCode;
import com.cau.foodfresh.dto.Food;
import com.cau.foodfresh.dto.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final Firestore firestore = FirestoreClient.getFirestore();

    // 회원가입 (ID 중복 확인 후 저장)
    public String registerUser(User.Signup user) throws Exception {
        // ID 중복 확인
        List<QueryDocumentSnapshot> existingUsers = firestore.collection("User")
                .whereEqualTo("id", user.getId())
                .get()
                .get()
                .getDocuments();

        if (!existingUsers.isEmpty()) {
            throw new Exception("ID already exists");
        }

        // Firestore에 새로운 유저 추가
        ApiFuture<DocumentReference> result = firestore.collection("User").add(user);
        return result.get().getId();
    }

    // 로그인 (ID와 Password 확인)
    public String loginUser(User.Login user) throws Exception {
        // ID와 Password로 사용자 검색
        List<QueryDocumentSnapshot> documents = firestore.collection("User")
                .whereEqualTo("id", user.getId())
                .whereEqualTo("password", user.getPassword())
                .get()
                .get()
                .getDocuments();

        if (documents.isEmpty()) {
            throw new Exception("Invalid credentials");
        }

        // 성공: 이름 반환
        return documents.get(0).getString("id");
    }

    public List<Food.DeletedFoodInfoResponse> getDeletedFoods(String userId) throws Exception {
        CollectionReference deletedFoodsRef = firestore.collection("User").document(userId).collection("DeletedFoods");
        List<QueryDocumentSnapshot> documents = deletedFoodsRef.get().get().getDocuments();

        List<Food.DeletedFoodInfoResponse> deletedFoodList = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Food.DeletedFoodInfo deletedFoodInfo = document.toObject(Food.DeletedFoodInfo.class);
            deletedFoodList.add(new Food.DeletedFoodInfoResponse(deletedFoodInfo));
        }

        return deletedFoodList;
    }
}