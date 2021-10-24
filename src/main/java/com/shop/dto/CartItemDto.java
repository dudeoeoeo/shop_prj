package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CartItemDto {

    @NotNull(message = "상품 아이디는 필수 입력 값 입니다.")
    private Long ItemId;

    @Min(value = 1, message = "최소 1개의 상품을 장바구니에 담을 수 있습니다.")
    private int count;
}
