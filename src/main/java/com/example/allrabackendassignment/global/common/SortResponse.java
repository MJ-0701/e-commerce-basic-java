package com.example.allrabackendassignment.global.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SortResponse {
    private List<SortOrder> orders;

    public static SortResponse from(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return empty();
        }
        List<SortOrder> list = sort.stream()
                .map(o -> SortOrder.builder()
                        .property(o.getProperty())
                        .direction(SortOrder.Direction.valueOf(o.getDirection().name()))
                        .ignoreCase(o.isIgnoreCase())
                        .nullHandling(
                                switch (o.getNullHandling()) {
                                    case NATIVE -> SortOrder.NullHandling.NATIVE;
                                    case NULLS_FIRST -> SortOrder.NullHandling.NULLS_FIRST;
                                    case NULLS_LAST -> SortOrder.NullHandling.NULLS_LAST;
                                }
                        )
                        .build()
                )
                .collect(Collectors.toList());
        return SortResponse.builder().orders(list).build();
    }

    public static SortResponse empty() {
        return SortResponse.builder().orders(Collections.emptyList()).build();
    }
}