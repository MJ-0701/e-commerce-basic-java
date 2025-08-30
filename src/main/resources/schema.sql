-- product_img_details_mapping
CREATE TABLE IF NOT EXISTS product_img_details_mapping (
                                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                           product_img_details_id BIGINT NOT NULL,
                                                           product_id BIGINT NOT NULL,
                                                           CONSTRAINT fk_pim_details
                                                               FOREIGN KEY (product_img_details_id) REFERENCES product_img_details(id)
                                                                   ON UPDATE CASCADE ON DELETE CASCADE,
                                                           CONSTRAINT fk_pim_product
                                                               FOREIGN KEY (product_id) REFERENCES product(product_id)
                                                                   ON UPDATE CASCADE ON DELETE CASCADE,
                                                           INDEX idx_pim_product_id (product_id),
                                                           INDEX idx_pim_details_id (product_img_details_id)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin;

-- product_img_details
CREATE TABLE IF NOT EXISTS product_img_details (
                                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   type VARCHAR(10) DEFAULT 'DETAIL' COMMENT 'DETAIL(상품상세), ASSET(유의사항 이미지 등)',
                                                   img_url TEXT DEFAULT NULL COMMENT '과제용 이므로 이미지가 업로드 됐다고 가정',
                                                   INDEX idx_pid_type (type)
) COMMENT='상품 상세 이미지 및 asset'
    ENGINE=InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_bin;

-- product_category
CREATE TABLE IF NOT EXISTS product_category (
                                                category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                upper_category VARCHAR(30) DEFAULT NULL COMMENT '상위 카테고리',
                                                category VARCHAR(50) NOT NULL COMMENT '하위 카테고리',
                                                search_text VARCHAR(100) NOT NULL DEFAULT '검색필드',
                                                INDEX idx_pc_category (category),
                                                INDEX idx_pc_search_text (search_text),
                                                FULLTEXT INDEX ft_pc_search_text (search_text)
) COMMENT='상품 카테고리 테이블'
    ENGINE=InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_bin;

-- product
CREATE TABLE IF NOT EXISTS product (
                                       product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       product_name VARCHAR(100) NOT NULL,
                                       product_price INT UNSIGNED NOT NULL DEFAULT 1000,
                                       product_thumbnail TEXT DEFAULT NULL,
                                       category_id BIGINT,
                                       is_active BOOLEAN DEFAULT TRUE,
                                       stock INT UNSIGNED DEFAULT 100,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       CONSTRAINT fk_product_category
                                           FOREIGN KEY (category_id) REFERENCES product_category(category_id)
                                               ON UPDATE CASCADE ON DELETE SET NULL,
                                       INDEX idx_product_category_id (category_id)
) COMMENT='상품 테이블'
    ENGINE=InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_bin;

-- 안전하게 재생성하려면 먼저 기존 객체 제거 (필요 시)
DROP TRIGGER IF EXISTS trg_user_after_insert_create_cart;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS `user`;

-- user
CREATE TABLE `user` (
                        user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL
) COMMENT = '과제용 유저 테이블'
    ENGINE=InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin
;

-- cart (1:1 보장을 위해 user_id NOT NULL + UNIQUE + FK)
CREATE TABLE IF NOT EXISTS cart (
                                    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    user_id BIGINT NOT NULL
) COMMENT = '카트 테이블'
    ENGINE=InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin
;

-- 제약조건: 1) user_id는 유니크(1:1) 2) FK로 연결(삭제/수정 전파)
ALTER TABLE cart
    ADD CONSTRAINT uq_cart_user UNIQUE (user_id),
    ADD CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
            REFERENCES `user` (user_id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

-- 사용 중이면 안전하게 삭제
DROP TRIGGER IF EXISTS code_interview.trg_user_after_insert_create_cart;
-- 유저 생성 시 자동으로 카트 생성 트리거
-- 트리거 (단일문; DELIMITER/BEGIN..END 없음)
CREATE TRIGGER code_interview.trg_user_after_insert_create_cart
    AFTER INSERT ON `user`
    FOR EACH ROW
    INSERT IGNORE INTO cart (user_id) VALUES (NEW.user_id);



CREATE TABLE IF NOT EXISTS cart_items(
    id bigint auto_increment primary key,
    cart_id bigint references cart(cart_id) on delete cascade ,
    product_id bigint references product(product_id) on update cascade ,
    quantity int unsigned default 1,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,

    INDEX idx_cart_items_product_id (product_id),
    INDEX idx_cart_items_cart_id (cart_id)
) COMMENT '장바구니에 담기 테이블'
    ENGINE = InnoDB
    DEFAULT CHAR SET = utf8mb4
    COLLATE = utf8mb4_bin
;
