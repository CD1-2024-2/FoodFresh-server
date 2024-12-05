package com.cau.foodfresh.enums;

public enum FoodDeletionReason {
    SIMPLE_DELETION,   // 단순 삭제
    CONSUMED,          // 정상 소모
    EXPIRED,           // 유통기한 만료
    SPOILED,           // 신선도 하락
    DONATED,           // 나눔
    OTHER              // 기타
}