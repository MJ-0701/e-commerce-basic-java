package com.example.allrabackendassignment.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.allrabackendassignment.domain.cart.entity.Cart;
import com.example.allrabackendassignment.domain.cart.entity.CartItems;
import com.example.allrabackendassignment.domain.cart.repository.CartItemsRepository;
import com.example.allrabackendassignment.domain.cart.repository.CartRepository;
import com.example.allrabackendassignment.global.common.SliceResponse;
import com.example.allrabackendassignment.web.dto.internal.cart.request.CartItemUpdateRequestDto;
import com.example.allrabackendassignment.web.dto.internal.cart.request.CartItemsRequestDto;
import com.example.allrabackendassignment.web.dto.internal.cart.response.CartItemsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemsRepository cartItemsRepository;

    @InjectMocks
    private CartService cartService;

    @Nested
    @DisplayName("saveCartItems")
    class SaveCartItems {

        @Test
        @DisplayName("카트가 존재하면 아이템 저장 후 성공 메시지 반환")
        void save_success() {
            // given
            Long userId = 100L;
            Long cartId = 10L;

            Cart cart = Cart.builder()
                    .cart_id(cartId)
                    .user_id(userId)
                    .build();

            CartItemsRequestDto req = new CartItemsRequestDto(777L, 3);

            given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));
            // save는 굳이 반환 검증 필요 없다면 any로 ok
            given(cartItemsRepository.save(any(CartItems.class)))
                    .willAnswer(invocation -> invocation.getArgument(0)); // 그대로 반환

            // when
            String result = cartService.saveCartItems(userId, req);

            // then
            assertThat(result).isEqualTo("상품이 저장되었습니다.");

            // 저장된 엔티티 내용 검증 (cartId, productId, quantity)
            ArgumentCaptor<CartItems> captor = ArgumentCaptor.forClass(CartItems.class);
            verify(cartItemsRepository).save(captor.capture());
            CartItems saved = captor.getValue();
            assertThat(saved.getCartId()).isEqualTo(cartId);
            assertThat(saved.getProductId()).isEqualTo(777L);
            assertThat(saved.getQuantity()).isEqualTo(3);

            verify(cartRepository).findByUserId(userId);
            verifyNoMoreInteractions(cartRepository, cartItemsRepository);
        }

        @Test
        @DisplayName("카트가 없으면 NoSuchElementException")
        void save_cartNotFound() {
            // given
            Long userId = 100L;
            CartItemsRequestDto req = new CartItemsRequestDto(777L, 3);
            given(cartRepository.findByUserId(userId)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> cartService.saveCartItems(userId, req))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Cart 생성 안됨");

            verify(cartRepository).findByUserId(userId);
            verifyNoInteractions(cartItemsRepository);
        }
    }

    @Nested
    @DisplayName("getCartItems")
    class GetCartItems {

        @Test
        @DisplayName("Slice -> SliceResponse 매핑 결과 검증 (record DTO)")
        void get_slice_success() {
            // given
            Long userId = 42L;
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id"));

            List<CartItemsResponse> content = List.of(
                    new CartItemsResponse("P-1000", "thumb-1000.png", 1000L, true, 9900, 2),
                    new CartItemsResponse("P-1001", "thumb-1001.png", 1001L, true, 12900, 1)
            );
            Slice<CartItemsResponse> slice = new SliceImpl<>(content, pageable, true);

            given(cartItemsRepository.userCartItemList(pageable, userId)).willReturn(slice);

            // when
            SliceResponse<CartItemsResponse> response = cartService.getCartItems(pageable, userId);

            // then
            assertThat(response.getContent()).hasSize(2);
            // ★ 레코드 접근자는 getId()가 아니라 productId()
            assertThat(response.getContent().get(0).productId()).isEqualTo(1000L);
            assertThat(response.getContent().get(0).productName()).isEqualTo("P-1000");
            assertThat(response.getPageNumber()).isEqualTo(0);
            assertThat(response.getPageSize()).isEqualTo(2);
            assertThat(response.isHasNext()).isTrue();

            verify(cartItemsRepository).userCartItemList(pageable, userId);
            verifyNoMoreInteractions(cartItemsRepository);
            verifyNoInteractions(cartRepository);
        }

        @Nested
        @DisplayName("productIncrease / productDecrease / updateProductQuantity / deleteProduct")
        class Mutations {

            @Test
            @DisplayName("수량 증가")
            void increase() {
                // given
                Long userId = 1L;
                Long productId = 100L;
                Long cartId = 10L;

                Cart cart = Cart.builder().cart_id(cartId).user_id(userId).build();
                CartItems item = CartItems.builder()
                        .id(999L)
                        .cartId(cartId)
                        .productId(productId)
                        .quantity(1)
                        .build();

                given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));
                given(cartItemsRepository.findByCartId(cartId, productId)).willReturn(item);

                // when
                cartService.productIncrease(userId, productId);

                // then
                assertThat(item.getQuantity()).isEqualTo(2);

                verify(cartRepository).findByUserId(userId);
                verify(cartItemsRepository).findByCartId(cartId, productId);
                verifyNoMoreInteractions(cartRepository, cartItemsRepository);
            }

            @Test
            @DisplayName("수량 감소")
            void decrease() {
                // given
                Long userId = 1L;
                Long productId = 100L;
                Long cartId = 10L;

                Cart cart = Cart.builder().cart_id(cartId).user_id(userId).build();
                CartItems item = CartItems.builder()
                        .id(999L)
                        .cartId(cartId)
                        .productId(productId)
                        .quantity(3)
                        .build();

                given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));
                given(cartItemsRepository.findByCartId(cartId, productId)).willReturn(item);

                // when
                cartService.productDecrease(userId, productId);

                // then
                assertThat(item.getQuantity()).isEqualTo(2);

                verify(cartRepository).findByUserId(userId);
                verify(cartItemsRepository).findByCartId(cartId, productId);
                verifyNoMoreInteractions(cartRepository, cartItemsRepository);
            }

            @Test
            @DisplayName("수량 지정 업데이트")
            void updateQuantity() {
                // given
                Long userId = 1L;
                Long productId = 100L;
                Long cartId = 10L;

                Cart cart = Cart.builder().cart_id(cartId).user_id(userId).build();
                CartItems item = CartItems.builder()
                        .id(999L)
                        .cartId(cartId)
                        .productId(productId)
                        .quantity(3)
                        .build();

                given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));
                given(cartItemsRepository.findByCartId(cartId, productId)).willReturn(item);

                CartItemUpdateRequestDto cartItemUpdateRequestDto = new CartItemUpdateRequestDto(10);
                // when
                cartService.updateProductQuantity(userId, productId, cartItemUpdateRequestDto);

                // then
                assertThat(item.getQuantity()).isEqualTo(7);

                verify(cartRepository).findByUserId(userId);
                verify(cartItemsRepository).findByCartId(cartId, productId);
                verifyNoMoreInteractions(cartRepository, cartItemsRepository);
            }

            @Test
            @DisplayName("상품 삭제")
            void deleteProduct() {
                // given
                Long userId = 1L;
                Long productId = 100L;
                Long cartId = 10L;

                Cart cart = Cart.builder().cart_id(cartId).user_id(userId).build();
                CartItems item = CartItems.builder()
                        .id(999L)
                        .cartId(cartId)
                        .productId(productId)
                        .quantity(3)
                        .build();

                given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));
                given(cartItemsRepository.findByCartId(cartId, productId)).willReturn(item);

                // when
                cartService.deleteProduct(userId, productId);

                // then
                verify(cartRepository).findByUserId(userId);
                verify(cartItemsRepository).findByCartId(cartId, productId);
                verify(cartItemsRepository).delete(item);
                verifyNoMoreInteractions(cartRepository, cartItemsRepository);
            }

            @Test
            @DisplayName("카트 미존재 시 예외")
            void mutations_cartNotFound() {
                // given
                Long userId = 1L;
                given(cartRepository.findByUserId(userId)).willReturn(Optional.empty());

                // when / then
                assertThatThrownBy(() -> cartService.productIncrease(userId, 100L))
                        .isInstanceOf(NoSuchElementException.class);

                assertThatThrownBy(() -> cartService.productDecrease(userId, 100L))
                        .isInstanceOf(NoSuchElementException.class);

                CartItemUpdateRequestDto cartItemUpdateRequestDto = new CartItemUpdateRequestDto(5);
                assertThatThrownBy(() -> cartService.updateProductQuantity(userId, 100L, cartItemUpdateRequestDto))
                        .isInstanceOf(NoSuchElementException.class);

                assertThatThrownBy(() -> cartService.deleteProduct(userId, 100L))
                        .isInstanceOf(NoSuchElementException.class);

                verify(cartRepository, times(4)).findByUserId(userId);
                verifyNoInteractions(cartItemsRepository);
            }
        }
    }
}