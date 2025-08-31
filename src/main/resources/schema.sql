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
                                       CONSTRAINT product_stock_managed CHECK ( stock >= 0 )
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
    updated_at timestamp default current_timestamp on update current_timestamp
) COMMENT '장바구니에 담기 테이블'
    ENGINE = InnoDB
    DEFAULT CHAR SET = utf8mb4
    COLLATE = utf8mb4_bin
;


-- order

DROP TABLE IF EXISTS code_interview.orders;

CREATE Table IF NOT EXISTS orders(
                                     id bigint auto_increment primary key,
                                     order_id varchar(100) not null comment '주문번호 : OIDUUIDYYMMDD',
                                     order_name varchar(100) not null,
                                     user_name varchar(100) not null comment '주문자',
                                     user_id bigint not null references user(user_id),
                                     order_status varchar(20) default 'CREATED' comment '주문상태 -> 주문생성, 결제완료, 주문취소',
                                     total_amount int unsigned not null comment '총 결제금액',
                                     paid_amount int unsigned not null comment '실제 결제금액 (쿠폰, 프로모션 등 할인을 받았다면 로직 적용이 됐다고 가정)',
                                     canceled_amount int unsigned default 0 comment '주문취소 금액 (전체환불, 부분환불 고려)',
                                     is_fully_canceled boolean default false comment '전체환불 여부',
                                     created_at timestamp default current_timestamp,
                                     updated_at timestamp default current_timestamp on update current_timestamp

) comment '주문 테이블'
    ENGINE = InnoDB
    DEFAULT CHAR SET = utf8mb4
    COLLATE = utf8mb4_bin
;

DROP TABLE IF EXISTS code_interview.order_items;

CREATE TABLE IF NOT EXISTS order_items(
    id bigint auto_increment primary key,
    order_id bigint references orders(id),
    product_id bigint not null references product(product_id),
    quantity int unsigned not null comment '주문수량',
    unit_price int unsigned not null comment '상품 개별가격',
    total_price int unsigned not null comment '상품 총 가격 = quantity * unit_price',
    created_at timestamp default current_timestamp
) COMMENT '주문 상품'
    ENGINE = InnoDB
    DEFAULT CHAR SET = utf8mb4
    COLLATE = utf8mb4_bin
;

CREATE TABLE IF NOT EXISTS order_histories(
    id bigint auto_increment primary key,
    order_id bigint references orders(id),
    status varchar(20) default 'CREATED' comment '주문상태 -> 주문생성, 결제완료, 주문취소',
    message varchar(20) default '주문 생성' comment '변경시 어디서 왜 변경했는지 로그용 필드',
    changed_by varchar(20) default 'SYSTEM' comment '주문생성 : SYSTEM, 결제완료 : PG, 주문취소 : USER OR ADMIN',
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp

) comment '주문이력 관리 테이블'
    ENGINE = InnoDB
    DEFAULT CHAR SET = utf8mb4
    COLLATE = utf8mb4_bin
;


-- Payment
CREATE TABLE IF NOT EXISTS payment_transaction(
    id bigint auto_increment primary key,
    order_id bigint references orders(id),
    tid varchar(100) not null comment 'PG사 트랜잭션 ID',
    pg_code varchar(50) default 'credit_cart' comment '결제수단(신용카드, 무통장입금, 인터넷뱅킹, 카카오페이, 네이버페이, 애플페이, 토스페이, 페이북, 지역화폐 등등)',
    amount int unsigned not null,
    discount_amount int unsigned default 0 comment '할인받은 금액',
    use_point int unsigned default 0 comment '포인트 사용금액',
    coupon_discount_amount int unsigned default 0 comment '쿠폰 할인금액',
    created_at timestamp default current_timestamp,

    INDEX idx_payment_transaction_pg_code (pg_code)

) comment '결제 트랜잭션 테이블',
    ENGINE = InnoDB
    DEFAULT CHAR SET = utf8mb4
    COLLATE = utf8mb4_bin
;

CREATE TABLE IF NOT EXISTS payment_tx_histories(
    id bigint auto_increment primary key,
    transaction_id bigint references payment_transaction(id),
    status varchar(50) not null default 'REQUEST' comment 'REQUEST : 결제요청, COMPLETE : 결제완료, FAIL : 결제실패',
    raw_data json default null comment 'PG사 응답 RAW DATA'
) comment '결제 이력관리 테이블'
    ENGINE = InnoDB
    DEFAULT CHAR SET = utf8mb4
    COLLATE = utf8mb4_bin
;

CREATE TABLE IF NOT EXISTS payment_cancellations(
    id bigint auto_increment primary key,
    transaction_id bigint references payment_transaction(id),
    cancel_amount int unsigned not null comment '취소금액',
    cancel_reason varchar(200) not null default '단순변심' comment '취소사유',
    cancel_status varchar(100) not null default 'CANCEL_REQUESTED' comment '주문 취소 접수상태 -> CANCEL_REQUESTED : 취소접수, CANCEL_COMPLETE : 취소완료, CANCEL_REJECT : 취소거부',
    cancel_type varchar(50) not null default 'FULL' comment 'FULL : 전체환불, PARTIAL : 부분환불'
) comment '환불 테이블'
    ENGINE = InnoDB
    DEFAULT CHAR SET = utf8mb4
    COLLATE = utf8mb4_bin
;