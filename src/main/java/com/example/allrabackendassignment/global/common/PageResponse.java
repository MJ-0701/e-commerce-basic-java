package com.example.allrabackendassignment.global.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {
    private List<T> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean empty;
    private SortResponse sort;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .empty(page.isEmpty())
                .sort(SortResponse.from(page.getSort()))
                .build();
    }

    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .content(Collections.emptyList())
                .number(0)
                .size(0)
                .totalElements(0L)
                .totalPages(0)
                .empty(true)
                .sort(SortResponse.empty())
                .build();
    }
}
