package com.example.allrabackendassignment.global.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SortOrder {
    public enum Direction { ASC, DESC }
    public enum NullHandling { NATIVE, NULLS_FIRST, NULLS_LAST }

    private String property;
    private Direction direction;
    private boolean ignoreCase;
    private NullHandling nullHandling;
}