-- db/schema-mysql.sql

-- Session / safety
SET NAMES utf8mb4;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- ------- DROP (children -> parents) -------

DROP TRIGGER IF EXISTS trg_user_after_insert_create_cart;

-- payments
DROP TABLE IF EXISTS payment_cancellations;
DROP TABLE IF EXISTS payment_tx_histories;
DROP TABLE IF EXISTS payment_transaction;

-- orders
DROP TABLE IF EXISTS order_histories;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;

-- cart
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS cart;

-- products
DROP TABLE IF EXISTS product_img_details_mapping;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS product_category;
DROP TABLE IF EXISTS product_img_details;

-- users
DROP TABLE IF EXISTS `user`;

-- ------- CREATE (parents -> children) -------

-- users
CREATE TABLE `user` (
                        user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name    VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='과제용 유저 테이블';

-- cart (user 1:1)
CREATE TABLE cart (
                      cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      user_id BIGINT NOT NULL,
                      UNIQUE KEY uq_cart_user (user_id),
                      CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES `user`(user_id)
                          ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='카트 테이블';

-- trigger AFTER cart table exists
CREATE TRIGGER trg_user_after_insert_create_cart
    AFTER INSERT ON `user` FOR EACH ROW
    INSERT IGNORE INTO cart (user_id) VALUES (NEW.user_id);

-- product category (parent)
CREATE TABLE product_category (
                                  category_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  upper_category  VARCHAR(30)  DEFAULT NULL COMMENT '상위 카테고리',
                                  category        VARCHAR(50)  NOT NULL COMMENT '하위 카테고리',
                                  search_text     VARCHAR(100) NOT NULL DEFAULT '검색필드',
                                  INDEX idx_pc_category (category),
                                  INDEX idx_pc_search_text (search_text),
                                  FULLTEXT INDEX ft_pc_search_text (search_text)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='상품 카테고리 테이블';

-- product_img_details (parent)
CREATE TABLE product_img_details (
                                     id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     type    VARCHAR(10) DEFAULT 'DETAIL' COMMENT 'DETAIL(상품상세), ASSET(유의사항 등)',
                                     img_url TEXT DEFAULT NULL COMMENT '과제용: 업로드 된 것으로 가정',
                                     INDEX idx_pid_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='상품 상세 이미지 및 asset';

-- product (parent)
CREATE TABLE product (
                         product_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                         product_name      VARCHAR(100) NOT NULL,
                         product_price     INT UNSIGNED NOT NULL DEFAULT 1000,
                         product_thumbnail TEXT DEFAULT NULL,
                         category_id       BIGINT,
                         is_active         BOOLEAN DEFAULT TRUE,
                         stock             INT UNSIGNED DEFAULT 100,
                         created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         CONSTRAINT product_stock_managed CHECK (stock >= 0),
                         CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category(category_id)
                             ON UPDATE CASCADE ON DELETE SET NULL,
                         INDEX idx_product_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='상품 테이블';

-- mapping (child of product_img_details & product)  <-- now both parents exist
CREATE TABLE product_img_details_mapping (
                                             id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             product_img_details_id BIGINT NOT NULL,
                                             product_id             BIGINT NOT NULL,
                                             CONSTRAINT fk_pim_details FOREIGN KEY (product_img_details_id) REFERENCES product_img_details(id)
                                                 ON UPDATE CASCADE ON DELETE CASCADE,
                                             CONSTRAINT fk_pim_product  FOREIGN KEY (product_id) REFERENCES product(product_id)
                                                 ON UPDATE CASCADE ON DELETE CASCADE,
                                             INDEX idx_pim_product_id (product_id),
                                             INDEX idx_pim_details_id (product_img_details_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='상품-이미지 매핑';

-- cart_items (child of cart & product)
CREATE TABLE cart_items (
                            id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                            cart_id    BIGINT NOT NULL,
                            product_id BIGINT NOT NULL,
                            quantity   INT UNSIGNED DEFAULT 1,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)    REFERENCES cart(cart_id)
                                ON UPDATE CASCADE ON DELETE CASCADE,
                            CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES product(product_id)
                                ON UPDATE CASCADE ON DELETE RESTRICT,
                            INDEX idx_cart_items_cart_id (cart_id),
                            INDEX idx_cart_items_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='장바구니 담기';

-- orders (parent)
CREATE TABLE orders (
                        id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                        order_id          VARCHAR(100) NOT NULL COMMENT '주문번호 : OIDUUIDYYMMDD',
                        order_name        VARCHAR(100) NOT NULL,
                        user_name         VARCHAR(100) NOT NULL COMMENT '주문자',
                        user_id           BIGINT NOT NULL,
                        order_status      VARCHAR(20)  DEFAULT 'CREATED',
                        total_amount      INT UNSIGNED NOT NULL,
                        paid_amount       INT UNSIGNED NOT NULL,
                        canceled_amount   INT UNSIGNED DEFAULT 0,
                        is_fully_canceled BOOLEAN DEFAULT FALSE,
                        created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES `user`(user_id)
                            ON UPDATE CASCADE ON DELETE RESTRICT,
                        UNIQUE KEY uq_orders_order_id (order_id),
                        INDEX idx_orders_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='주문 테이블';

-- order_items (child)
CREATE TABLE order_items (
                             id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                             order_id    BIGINT NOT NULL,
                             product_id  BIGINT NOT NULL,
                             quantity    INT UNSIGNED NOT NULL,
                             unit_price  INT UNSIGNED NOT NULL,
                             total_price INT UNSIGNED NOT NULL,
                             created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id)
                                 ON UPDATE CASCADE ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES product(product_id)
                                 ON UPDATE CASCADE ON DELETE RESTRICT,
                             INDEX idx_order_items_order_id (order_id),
                             INDEX idx_order_items_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='주문 상품';

-- order_histories (child)
CREATE TABLE order_histories (
                                 id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 order_id   BIGINT NOT NULL,
                                 status     VARCHAR(20)  DEFAULT 'CREATED',
                                 message    TEXT,
                                 changed_by VARCHAR(20)  DEFAULT 'SYSTEM',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_order_histories_order FOREIGN KEY (order_id) REFERENCES orders(id)
                                     ON UPDATE CASCADE ON DELETE CASCADE,
                                 INDEX idx_order_histories_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='주문 이력';

-- payment_transaction (parent)
CREATE TABLE payment_transaction (
                                     id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     order_id                BIGINT NOT NULL,
                                     tid                     VARCHAR(100) DEFAULT NULL,
                                     pg_code                 VARCHAR(50)  DEFAULT 'credit_card',
                                     amount                  INT UNSIGNED NOT NULL,
                                     discount_amount         INT UNSIGNED DEFAULT 0,
                                     use_point               INT UNSIGNED DEFAULT 0,
                                     coupon_discount_amount  INT UNSIGNED DEFAULT 0,
                                     created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_payment_tx_order FOREIGN KEY (order_id) REFERENCES orders(id)
                                         ON UPDATE CASCADE ON DELETE CASCADE,
                                     INDEX idx_payment_transaction_pg_code (pg_code),
                                     INDEX idx_payment_transaction_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='결제 트랜잭션';

-- payment_tx_histories (child)
CREATE TABLE payment_tx_histories (
                                      id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      transaction_id BIGINT NOT NULL,
                                      status         VARCHAR(50) NOT NULL DEFAULT 'REQUEST',
                                      created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT fk_payment_hist_tx FOREIGN KEY (transaction_id) REFERENCES payment_transaction(id)
                                          ON UPDATE CASCADE ON DELETE CASCADE,
                                      INDEX idx_payment_hist_tx_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='결제 이력';

-- payment_cancellations (child)
CREATE TABLE payment_cancellations (
                                       id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       transaction_id BIGINT NOT NULL,
                                       cancel_amount  INT UNSIGNED NOT NULL,
                                       cancel_reason  VARCHAR(200) NOT NULL DEFAULT '단순변심',
                                       cancel_status  VARCHAR(100) NOT NULL DEFAULT 'CANCEL_REQUESTED',
                                       cancel_type    VARCHAR(50)  NOT NULL DEFAULT 'FULL',
                                       created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       CONSTRAINT fk_payment_cancel_tx FOREIGN KEY (transaction_id) REFERENCES payment_transaction(id)
                                           ON UPDATE CASCADE ON DELETE CASCADE,
                                       INDEX idx_payment_cancel_tx_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='환불 테이블';

-- restore FK checks
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;