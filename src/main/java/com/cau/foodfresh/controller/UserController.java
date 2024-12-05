package com.cau.foodfresh.controller;

import com.cau.foodfresh.dto.ErrorCode;
import com.cau.foodfresh.dto.Food;
import com.cau.foodfresh.dto.User;
import com.cau.foodfresh.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody User.Signup user) {
        try {
            String docId = userService.registerUser(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody User.Login user) {
        try {
            String id = userService.loginUser(user);
            User.LoginResponse loginResponse = new User.LoginResponse(id);
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorCode);
        }
    }

    // 삭제 식품 정보 조회
    @GetMapping("/{userId}/deleted-foods")
    public ResponseEntity<Object> getDeletedFoodsByUser(@PathVariable String userId) {
        try {
            List<Food.DeletedFoodInfoResponse> deletedFoodList = userService.getDeletedFoods(userId);
            return ResponseEntity.ok(deletedFoodList);
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }
}