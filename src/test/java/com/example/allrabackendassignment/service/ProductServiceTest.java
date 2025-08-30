package com.example.allrabackendassignment.service;

import com.example.allrabackendassignment.domain.product.repository.ProductRepository;
import com.example.allrabackendassignment.global.common.PageResponse;
import com.example.allrabackendassignment.global.common.SortResponse;
import com.example.allrabackendassignment.web.dto.internal.product.request.ProductSearchConditionDto;
import com.example.allrabackendassignment.web.dto.internal.product.response.ProductSearchResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class ProductServiceTest {

    @Test
    @DisplayName("서비스가 레포지토리에 위임하고, 기대 페이지를 그대로 반환한다 (기댓값 : 결과값 상세 비교)")
    void getProductList_delegatesToRepository_andReturnsExpectedPage() {
        // given
        ProductRepository mockRepo = Mockito.mock(ProductRepository.class);
        ProductService service = new ProductService(mockRepo);

        var expectedCond = new ProductSearchConditionDto("감자", null, null, 1000, 2000, 6L, true);
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));

        // 기대 결과(기대값)
        var expectedDto = new ProductSearchResponseDto(
                1L, "감자 001 박스", "http://img/p1.jpg", 1500, "VEG", "POTATO", "감자 potato", true
        );
        Page<ProductSearchResponseDto> expectedPage =
                new PageImpl<>(List.of(expectedDto), expectedPageable, 1);

        // mock 동작: repo가 호출되면 기대 페이지를 그대로 반환
        Mockito.when(mockRepo.searchProduct(any(), any())).thenReturn(expectedPage);

        // when (실제 호출)
        PageResponse<ProductSearchResponseDto> actual = service.getProductList(expectedCond, expectedPageable);

        // then (기댓값 : 결과값 비교)

        // 1) 인자 캡쳐로 repository 호출 검증 + 기대 인자와 동일한지 확인
        ArgumentCaptor<ProductSearchConditionDto> condCaptor = ArgumentCaptor.forClass(ProductSearchConditionDto.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(mockRepo).searchProduct(condCaptor.capture(), pageableCaptor.capture());
        assertThat(condCaptor.getValue()).as("전달된 검색 조건").isEqualTo(expectedCond);
        assertThat(pageableCaptor.getValue()).as("전달된 Pageable").isEqualTo(expectedPageable);

        // 2) 페이지 메타 비교
        assertThat(actual.getTotalElements())
                .as("총 개수(expected vs actual)")
                .isEqualTo(expectedPage.getTotalElements());
        assertThat(actual.getNumber()).as("page index").isEqualTo(expectedPage.getNumber());
        assertThat(actual.getSize()).as("page size").isEqualTo(expectedPage.getSize());
        assertThat(actual.getSort())
                .usingRecursiveComparison()
                .isEqualTo(SortResponse.from(expectedPage.getSort()));

        // 3) 콘텐츠(리스트) 비교
        assertThat(actual.getContent())
                .as("페이지 콘텐츠(expected vs actual)")
                .containsExactlyElementsOf(expectedPage.getContent());

        // 4) 첫 요소 필드-by-필드 비교
        assertThat(actual.getContent().get(0))
                .as("첫 번째 결과 DTO 상세 비교")
                .usingRecursiveComparison()
                .isEqualTo(expectedDto);
    }
}