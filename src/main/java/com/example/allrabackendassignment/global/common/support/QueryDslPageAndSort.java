package com.example.allrabackendassignment.global.common.support;

import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Collections;
import java.util.List;

public class QueryDslPageAndSort {

    private final EntityManager entityManager;
    private final Class<?> clazz;

    public QueryDslPageAndSort(EntityManager entityManager, Class<?> clazz) {
        this.entityManager = entityManager;
        this.clazz = clazz;
    }

    private Querydsl getQuerydsl() {
        PathBuilder<?> builder = new PathBuilderFactory().create(clazz);
        return new Querydsl(entityManager, builder);
    }

    private Querydsl getQuerydslWithSort(Class<?> clazz) {
        PathBuilder<?> builder = new PathBuilderFactory().create(clazz);
        return new Querydsl(entityManager, builder);
    }

    public <T> PageImpl<T> getPageImpl(Pageable pageable, JPQLQuery<T> query) {
        long totalCount = query.fetchCount();
        List<T> results = getQuerydsl()
                .applyPagination(pageable, query)
                .fetch();
        return new PageImpl<>(results, pageable, totalCount);
    }

    public <T> Page<T> getCustomPageImpl(Pageable pageable, JPQLQuery<T> query) {
        List<T> results = getQuerydsl()
                .applyPagination(pageable, query)
                .fetch();
        return PageableExecutionUtils.getPage(results, pageable, query::fetchCount);
    }

    public <T> PageImpl<T> getPageImpl(Pageable pageable, JPQLQuery<T> query, long count) {
        List<T> results = getQuerydsl()
                .applyPagination(pageable, query)
                .fetch();
        return new PageImpl<>(results, pageable, count);
    }

    public <T> PageImpl<T> getRandomPageImpl(Pageable pageable, JPQLQuery<T> query) {
        long totalCount = query.fetchCount();
        List<T> results = getQuerydsl()
                .applyPagination(pageable, query)
                .fetch();
        Collections.shuffle(results);
        return new PageImpl<>(results, pageable, totalCount);
    }

    public <T> JPAQuery<T> withPageable(JPAQuery<T> query, Pageable pageable) {
        return query.limit(pageable.getPageSize()).offset(pageable.getOffset());
    }

    public <T> PageImpl<T> getPageImplWithSort(Pageable pageable, JPQLQuery<T> query, Class<?> clazz) {
        long totalCount = query.fetchCount();
        List<T> results = getQuerydslWithSort(clazz)
                .applyPagination(pageable, query)
                .fetch();
        return new PageImpl<>(results, pageable, totalCount);
    }

    public <T> Slice<T> getSlicePageImpl(Pageable pageable, JPQLQuery<T> query) {
        List<T> results = getQuerydsl()
                .applyPagination(pageable, query)
                .fetch();
        boolean hasNext = results.size() >= pageable.getPageSize();
        return new SliceImpl<>(results, pageable, hasNext);
    }
}
