package com.cau.foodfresh.controller;

import com.cau.foodfresh.dto.ErrorCode;
import com.cau.foodfresh.dto.Refrigerator;
import com.cau.foodfresh.dto.User;
import com.cau.foodfresh.service.RefrigeratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refrigerator")
public class RefrigeratorController {

    private final RefrigeratorService refrigeratorService;

    public RefrigeratorController(RefrigeratorService refrigeratorService) {
        this.refrigeratorService = refrigeratorService;
    }

    // 냉장고 생성
    @PostMapping("/create")
    public ResponseEntity<Object> createRefrigerator(@RequestBody Refrigerator.NewRefrigerator refrigerator, @RequestParam String userId) {
        try {
            String refrigeratorId = refrigeratorService.createRefrigerator(refrigerator, userId);
            Refrigerator.CreateRefrigeratorResponse createRefrigeratorResponse = new Refrigerator.CreateRefrigeratorResponse(refrigeratorId);
            return ResponseEntity.ok(createRefrigeratorResponse);
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }

    // 냉장고 삭제
    @DeleteMapping("/{refrigeratorId}/delete")
    public ResponseEntity<Object> deleteRefrigerator(@PathVariable String refrigeratorId, @RequestParam String userId) {
        try {
            refrigeratorService.deleteRefrigerator(refrigeratorId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }

    // 공유 사용자 추가
    @PostMapping("/{refrigeratorId}/share/add")
    public ResponseEntity<Object> addSharedUsers(@PathVariable String refrigeratorId, @RequestBody Refrigerator.SharedUsers sharedUsers, @RequestParam String userId) {
        try {
            refrigeratorService.addSharedUsers(refrigeratorId, sharedUsers, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }

    // 공유 사용자 삭제
    @PostMapping("/{refrigeratorId}/share/remove")
    public ResponseEntity<Object> removeSharedUsers(@PathVariable String refrigeratorId, @RequestBody Refrigerator.SharedUsers sharedUsers, @RequestParam String userId) {
        try {
            refrigeratorService.removeSharedUsers(refrigeratorId, sharedUsers, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }

    // 접근 가능한 냉장고 조회
    @GetMapping("/accessible")
    public ResponseEntity<Object> getAccessibleRefrigerators(@RequestParam String userId) {
        try {
            List<Refrigerator.RefrigeratorInfoResponse> refrigerators = refrigeratorService.getAccessibleRefrigerators(userId);
            return ResponseEntity.ok(refrigerators);
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }
}
