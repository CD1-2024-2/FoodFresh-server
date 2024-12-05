package com.cau.foodfresh.service;

import com.cau.foodfresh.dto.Refrigerator;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@DependsOn("firebaseConfig")
public class RefrigeratorService {

    private final Firestore firestore = FirestoreClient.getFirestore();

    // 냉장고 생성
    public String createRefrigerator(Refrigerator.NewRefrigerator refrigerator, String userId) throws Exception {

        com.google.cloud.Timestamp createdTimestamp = com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
                LocalDate.now().atStartOfDay(ZoneOffset.UTC).toEpochSecond(),
                0
        );

        // Firestore에 문서 ID 생성
        DocumentReference docRef = firestore.collection("Refrigerators").document();

        Refrigerator.RefrigeratorInfo refrigeratorInfo = new Refrigerator.RefrigeratorInfo(
                docRef.getId(),
                refrigerator.getName(),
                refrigerator.getDescription(),
                createdTimestamp,
                userId,
                new ArrayList<>(),
                false
        );

        docRef.set(refrigeratorInfo).get(); // Firestore에 저장 (ID 포함)

        return refrigeratorInfo.getRid(); // 생성된 냉장고 ID 반환
    }

    // 냉장고 삭제
    public void deleteRefrigerator(String refrigeratorId, String userId) throws Exception {
        DocumentReference fridgeRef = firestore.collection("Refrigerators").document(refrigeratorId);

        // 냉장고 존재 여부 확인
        DocumentSnapshot fridgeSnapshot = fridgeRef.get().get();
        if (!fridgeSnapshot.exists()) {
            throw new Exception("Refrigerator not found.");
        }

        Refrigerator.RefrigeratorInfo refrigerator = fridgeSnapshot.toObject(Refrigerator.RefrigeratorInfo.class);
        if (refrigerator == null) {
            throw new Exception("Failed to parse refrigerator data");
        }

        // 관리자 권한 확인
        if (!refrigerator.getManager().equals(userId)) {
            throw new Exception("Only the manager can delete the refrigerator.");
        }

        // 냉장고 내부의 Foods 컬렉션 삭제
        deleteFoodsInRefrigerator(fridgeRef);

        // 냉장고 삭제
        fridgeRef.delete().get();
    }

    // 냉장고 내부 식품 삭제
    private void deleteFoodsInRefrigerator(DocumentReference fridgeRef) throws Exception {
        CollectionReference foodsRef = fridgeRef.collection("Foods");
        List<QueryDocumentSnapshot> foodSnapshots = foodsRef.get().get().getDocuments();

        // 모든 식품 문서 삭제
        for (QueryDocumentSnapshot foodSnapshot : foodSnapshots) {
            foodSnapshot.getReference().delete().get();
        }
    }

    // 공유 사용자 추가
    public void addSharedUsers(String refrigeratorId, Refrigerator.SharedUsers sharedUsers, String userId) throws Exception {
        DocumentReference fridgeRef = firestore.collection("Refrigerators").document(refrigeratorId);

        // 냉장고 존재 및 관리자 권한 확인
        DocumentSnapshot fridgeSnapshot = fridgeRef.get().get();
        if (!fridgeSnapshot.exists()) {
            throw new Exception("Refrigerator not found");
        }
        Refrigerator.RefrigeratorInfo refrigerator = fridgeSnapshot.toObject(Refrigerator.RefrigeratorInfo.class);
        if (refrigerator == null) {
            throw new Exception("Failed to parse refrigerator data");
        }
        if (!refrigerator.getManager().equals(userId)) {
            throw new Exception("Only the manager can add shared users");
        }

        List<String> sharedUserIds = sharedUsers.getSharedUserIds();
        // 관리자 ID 제거 (중복 방지)
        sharedUserIds.remove(refrigerator.getManager());

        // 공유 대상 사용자 유효성 검사
        for (String sharedUserId : sharedUserIds) {
            QuerySnapshot querySnapshot = firestore.collection("User")
                    .whereEqualTo("id", sharedUserId) // 내부 필드로 조회
                    .get()
                    .get();

            if (querySnapshot.isEmpty()) {
                throw new Exception("User with ID " + sharedUserId + " does not exist.");
            }
        }

        // 공유자 추가 및 공유 상태 업데이트
        fridgeRef.update("sharedUsers", FieldValue.arrayUnion(sharedUserIds.toArray())).get();
        fridgeRef.update("isShared", true).get();
    }

    // 공유 사용자 삭제
    public void removeSharedUsers(String refrigeratorId, Refrigerator.SharedUsers sharedUsers, String userId) throws Exception {
        DocumentReference fridgeRef = firestore.collection("Refrigerators").document(refrigeratorId);

        // 냉장고 존재 및 관리자 권한 확인
        DocumentSnapshot fridgeSnapshot = fridgeRef.get().get();
        if (!fridgeSnapshot.exists()) {
            throw new Exception("Refrigerator not found.");
        }
        Refrigerator.RefrigeratorInfo refrigerator = fridgeSnapshot.toObject(Refrigerator.RefrigeratorInfo.class);
        if (refrigerator == null) {
            throw new Exception("Failed to parse refrigerator data");
        }
        if (!refrigerator.getManager().equals(userId)) {
            throw new Exception("Only the manager can remove shared users.");
        }

        List<String> sharedUserIds = sharedUsers.getSharedUserIds();
        // 공유자 삭제 및 공유 상태 업데이트
        fridgeRef.update("sharedUsers", FieldValue.arrayRemove(sharedUserIds.toArray())).get();

        // 공유 상태 업데이트
        DocumentSnapshot updatedSnapshot = fridgeRef.get().get();
        Refrigerator.RefrigeratorInfo updatedRefrigerator = updatedSnapshot.toObject(Refrigerator.RefrigeratorInfo.class);
        if (updatedRefrigerator == null) {
            throw new Exception("Failed to parse refrigerator data");
        }
        if (updatedRefrigerator.getSharedUsers() == null || updatedRefrigerator.getSharedUsers().isEmpty()) {
            fridgeRef.update("isShared", false).get();
        }
    }

    // 접근 가능한 냉장고 조회
    public List<Refrigerator.RefrigeratorInfoResponse> getAccessibleRefrigerators(String userId) throws Exception {
        List<Refrigerator.RefrigeratorInfo> accessibleRefrigerators = new ArrayList<>();

        // 관리자로 등록된 냉장고
        List<Refrigerator.RefrigeratorInfo> managerFridges = firestore.collection("Refrigerators")
                .whereEqualTo("manager", userId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> {
                    Refrigerator.RefrigeratorInfo fridge = doc.toObject(Refrigerator.RefrigeratorInfo.class);
                    fridge.setRid(doc.getId());
                    return fridge;
                })
                .toList();

        // 공유자로 등록된 냉장고
        List<Refrigerator.RefrigeratorInfo> sharedFridges = firestore.collection("Refrigerators")
                .whereArrayContains("sharedUsers", userId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(doc -> {
                    Refrigerator.RefrigeratorInfo fridge = doc.toObject(Refrigerator.RefrigeratorInfo.class);
                    fridge.setRid(doc.getId());
                    return fridge;
                })
                .toList();

        // 순차적으로 합치기
        accessibleRefrigerators.addAll(managerFridges);
        accessibleRefrigerators.addAll(sharedFridges);

        return accessibleRefrigerators.stream()
                .map(Refrigerator.RefrigeratorInfoResponse::new)
                .toList();
    }
}