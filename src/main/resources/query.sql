# 이 파일은 로직을 작성하기 전에 쿼리로 먼저 확인하기 위한 쿼리 playground 파일 입니다.

# 상품
SELECT *
FROM product
;


# 카테고리
SELECT *
FROM product_category
;

# 대분류
SELECT category_id,
       category
FROM product_category
WHERE upper_category is null
;

# 중분류
SELECT category_id,
       upper_category,
       category
FROM product_category
WHERE upper_category in (SELECT category
                         FROM product_category
                         WHERE upper_category is null)
;

# 검색쿼리
SELECT p.product_id,
       p.product_name,
       p.product_thumbnail,
       p.product_price,
       pc.upper_category,
       pc.category,
       pc.search_text
FROM product p
    INNER JOIN product_category pc on p.category_id = pc.category_id
WHERE 1=1
    AND p.product_name LIKE '%001%'
    AND p.product_price between 1000 and 2000
    AND pc.search_text LIKE '%감자%'
;

-- 동적쿼리 --
SELECT
    p.product_id, p.product_name, p.product_thumbnail, p.product_price,
    pc.upper_category, pc.category, pc.search_text
FROM product p
         JOIN product_category pc ON p.category_id = pc.category_id
WHERE 1=1
  -- 구조화된 필터(AND)
  AND (:minPrice IS NULL OR :maxPrice IS NULL OR p.product_price BETWEEN :minPrice AND :maxPrice)
  AND (:categoryId IS NULL OR p.category_id = :categoryId)
  AND (:isActive IS NULL OR p.is_active = :isActive)

  -- 통합 키워드(q)는 텍스트 필드에만 OR로 적용
  AND (
    :q IS NULL
        OR p.product_name  LIKE CONCAT('%', :q, '%')
        OR pc.category     LIKE CONCAT('%', :q, '%')
        OR pc.upper_category LIKE CONCAT('%', :q, '%')
        OR pc.search_text  LIKE CONCAT('%', :q, '%')
    );


SELECT *
FROM `user`
;

select *
from cart
;

select *
from cart_items
;

ALTER TABLE payment_transaction
    MODIFY tid varchar(100) NULL DEFAULT NULL COMMENT 'PG사 트랜잭션 ID';

ALTER TABLE order_histories
    MODIFY message TEXT comment '변경시 어디서 왜 변경했는지 로그용 필드';