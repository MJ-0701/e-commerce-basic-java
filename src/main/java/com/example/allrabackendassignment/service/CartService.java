package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.cart.entity.Cart;
import com.example.allrabackendassignment.domain.cart.entity.CartItems;
import com.example.allrabackendassignment.domain.cart.repository.CartItemsRepository;
import com.example.allrabackendassignment.domain.cart.repository.CartRepository;

import com.example.allrabackendassignment.global.common.SliceResponse;
import com.example.allrabackendassignment.web.dto.internal.cart.request.CartItemUpdateRequestDto;
import com.example.allrabackendassignment.web.dto.internal.cart.request.CartItemsRequestDto;
import com.example.allrabackendassignment.web.dto.internal.cart.response.CartItemsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;

    @Transactional
    public String saveCartItems(Long userId, CartItemsRequestDto cartItemsRequestDto) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Cart 생성 안됨"));

        CartItems cartItems = CartItems.builder()
                .cartId(cart.getCart_id())
                .productId(cartItemsRequestDto.productId())
                .quantity(cartItemsRequestDto.quantity())
                .build();

        cartItemsRepository.save(cartItems);

        return "상품이 저장되었습니다.";
    }

    @Transactional(readOnly = true)
    public SliceResponse<CartItemsResponse> getCartItems(Pageable pageable, Long userId) {
        return SliceResponse.from(cartItemsRepository.userCartItemList(pageable, userId));
    }

    @Transactional
    public int productIncrease(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Cart 생성 안됨"));

        CartItems cartItems = cartItemsRepository.findByCartId(cart.getCart_id(), productId);

        cartItems.increaseQuantity();

        return cartItems.getQuantity();
    }

    @Transactional
    public int productDecrease(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Cart 생성 안됨"));

        CartItems cartItems = cartItemsRepository.findByCartId(cart.getCart_id(), productId);

        cartItems.decreaseQuantity();

        return cartItems.getQuantity();
    }

    @Transactional
    public int updateProductQuantity(Long userId, Long productId, CartItemUpdateRequestDto itemUpdateRequestDto) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Cart 생성 안됨"));

        CartItems cartItems = cartItemsRepository.findByCartId(cart.getCart_id(), productId);

        cartItems.updateQuantity(itemUpdateRequestDto.quantity());

        return cartItems.getQuantity();
    }

    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Cart 생성 안됨"));

        CartItems cartItems = cartItemsRepository.findByCartId(cart.getCart_id(), productId);

        if (cartItems == null) {
            throw new NoSuchElementException("이미 삭제되었거나 존재하지 않는 상품입니다.");
        }

        cartItemsRepository.delete(cartItems);
    }

}
