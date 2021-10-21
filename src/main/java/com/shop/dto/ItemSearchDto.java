package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemSearchDto {

    // 현재 시간과 상품 등록일을 비교해서 상품 데이터 조회
    private String searchDateType;

    private ItemSellStatus searchSellStatus;

    // 상품 조회 시 어떤 유형으로 조회할지 선택
    // 상품명 or 상품 등록자 아이디
    private String searchBy;

    // 조회할 검색어 저장
    private String searchQuery = "";
}
