package com.example.allrabackendassignment.web.controller.cart;

import com.example.allrabackendassignment.global.common.SliceResponse;
import com.example.allrabackendassignment.global.http.ResponseObject;
import com.example.allrabackendassignment.service.CartService;
import com.example.allrabackendassignment.web.dto.internal.cart.request.CartItemUpdateRequestDto;
import com.example.allrabackendassignment.web.dto.internal.cart.request.CartItemsRequestDto;
import com.example.allrabackendassignment.web.dto.internal.cart.response.CartItemsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartRestApiController {

    private final CartService cartService;

    @PostMapping("/{userId}")
    private ResponseEntity<ResponseObject<String>> saveCartItems(
            @PathVariable Long userId,
            @RequestBody CartItemsRequestDto cartItemsRequestDto
    ) {
        return ResponseEntity.ok().body(ResponseObject.of(cartService.saveCartItems(userId, cartItemsRequestDto)));
    }

    @GetMapping("/{userId}")
    private ResponseEntity<ResponseObject<SliceResponse<CartItemsResponse>>> getCartItems(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok().body(ResponseObject.of(cartService.getCartItems(pageable, userId)));
    }

    @DeleteMapping("/{userId}")
    private void deleteCartItem(
            @PathVariable Long userId,
            @RequestParam Long productId
    ) {
        cartService.deleteProduct(userId, productId);
    }

    @PutMapping("/{userId}")
    private ResponseEntity<ResponseObject<Integer>> cartItemUpdate(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestBody CartItemUpdateRequestDto cartItemsRequestDto
    ) {
        return ResponseEntity.ok().body(ResponseObject.of(cartService.updateProductQuantity(userId, productId, cartItemsRequestDto)));
    }

    @PatchMapping("/{userId}/increase")
    private ResponseEntity<ResponseObject<Integer>> increaseCartItem(
            @PathVariable Long userId,
            @RequestParam Long productId
    ) {
        return ResponseEntity.ok().body(ResponseObject.of(cartService.productIncrease(userId, productId)));
    }

    @PatchMapping("/{userId}/decrease")
    private ResponseEntity<ResponseObject<Integer>> decreaseCartItem(
            @PathVariable Long userId,
            @RequestParam Long productId
    ) {
        return ResponseEntity.ok().body(ResponseObject.of(cartService.productDecrease(userId, productId)));
    }


}
