package com.example.allrabackendassignment.domain.cart.repository.custom;

import com.example.allrabackendassignment.web.dto.internal.cart.response.CartItemsResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CartItemsCustomRepository {

    public Slice<CartItemsResponse> userCartItemList(Pageable pageable, Long userId);
}
