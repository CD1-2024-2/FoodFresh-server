package com.cau.foodfresh.controller;

import com.cau.foodfresh.dto.ErrorCode;
import com.cau.foodfresh.dto.Food;
import com.cau.foodfresh.enums.FoodDeletionReason;
import com.cau.foodfresh.service.FoodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refrigerator/{refrigeratorId}/food")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    // 식품 추가
    @PostMapping("/add")
    public ResponseEntity<Object> addFood(@PathVariable String refrigeratorId, @RequestBody Food.NewFood food) {
        try {
            String foodId = foodService.addFood(refrigeratorId, food);
            Food.AddFoodResponse addFoodResponse = new Food.AddFoodResponse(foodId);
            return ResponseEntity.ok(addFoodResponse);
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }

    // 식품 조회
    @GetMapping("/list")
    public ResponseEntity<Object> getFoodList(@PathVariable String refrigeratorId) {
        try {
            List<Food.FoodInfoResponse> foodList = foodService.getFoodList(refrigeratorId);
            return ResponseEntity.ok(foodList);
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }

     // 식품 삭제
    @DeleteMapping("/{foodId}/delete")
    public ResponseEntity<Object> deleteFood(
            @PathVariable String refrigeratorId, @PathVariable String foodId, @RequestParam String userId
    ) {
        try {
            foodService.deleteFood(refrigeratorId, foodId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }

    @DeleteMapping("/{foodId}/delete/detail")
    public ResponseEntity<Object> deleteDetailedFood(
            @PathVariable String refrigeratorId, @PathVariable String foodId, @RequestParam String userId, @RequestParam FoodDeletionReason reason
    ) {
        try {
            foodService.deleteDetailedFood(refrigeratorId, foodId, userId, reason);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCode);
        }
    }

}