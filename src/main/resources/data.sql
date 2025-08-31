-- data.sql (RESET & SEED for API testing)  // DML only, 한블록

-- 🔄 전부 초기화 (자식 → 부모 순서, FK 잠깐 해제)
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE payment_cancellations;
TRUNCATE TABLE payment_tx_histories;
TRUNCATE TABLE payment_transaction;

TRUNCATE TABLE order_histories;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;

TRUNCATE TABLE cart_items;
TRUNCATE TABLE cart;

TRUNCATE TABLE product_img_details_mapping;
TRUNCATE TABLE product;
TRUNCATE TABLE product_category;
TRUNCATE TABLE product_img_details;

TRUNCATE TABLE `user`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1) User (트리거로 카트 자동생성)
INSERT INTO `user` (name) VALUES
                              ('김민수'),('이서연'),('박지훈'),('최유진'),('정우영'),
                              ('한지민'),('오세훈'),('유나영'),('장도윤'),('서지호');

-- 2) Product Category (search_text 포함)
--   상위카테고리 5개(1..5) + 말단카테고리 9개(6..14)
INSERT INTO product_category (upper_category, category, search_text) VALUES
                                                                         (NULL, 'VEG',     '야채'),
                                                                         (NULL, 'SEAFOOD', '해산물'),
                                                                         (NULL, 'MEAT',    '정육'),
                                                                         (NULL, 'FRUIT',   '과일'),
                                                                         (NULL, 'GRAIN',   '곡식'),
                                                                         ('VEG',     'POTATO', '감자'),     -- 6
                                                                         ('VEG',     'CARROT', '당근'),     -- 7
                                                                         ('SEAFOOD', 'FISH',   '생선'),     -- 8
                                                                         ('SEAFOOD', 'SHRIMP', '새우'),     -- 9
                                                                         ('MEAT',    'PORK',   '돼지고기'), -- 10
                                                                         ('MEAT',    'BEEF',   '소고기'),   -- 11
                                                                         ('FRUIT',   'APPLE',  '사과'),     -- 12
                                                                         ('FRUIT',   'BANANA', '바나나'),   -- 13
                                                                         ('GRAIN',   'RICE',   '쌀');       -- 14

-- 3) Product (50개)  // leaf 카테고리 6..14 순환
INSERT INTO product (product_name, product_price, product_thumbnail, category_id, is_active, stock) VALUES
                                                                                                        ('Product 001',1100,'https://example.com/thumbs/p1.jpg',6,  TRUE, 10),
                                                                                                        ('Product 002',1200,'https://example.com/thumbs/p2.jpg',7,  FALSE,0),
                                                                                                        ('Product 003',1300,'https://example.com/thumbs/p3.jpg',8,  TRUE, 3),
                                                                                                        ('Product 004',1400,'https://example.com/thumbs/p4.jpg',9,  TRUE, 2),
                                                                                                        ('Product 005',1500,'https://example.com/thumbs/p5.jpg',10, FALSE,1),
                                                                                                        ('Product 006',1600,'https://example.com/thumbs/p6.jpg',11, TRUE, 41),
                                                                                                        ('Product 007',1700,'https://example.com/thumbs/p7.jpg',12, TRUE, 65),
                                                                                                        ('Product 008',1800,'https://example.com/thumbs/p8.jpg',13, TRUE, 230),
                                                                                                        ('Product 009',1900,'https://example.com/thumbs/p9.jpg',14, FALSE,0),
                                                                                                        ('Product 010',2000,'https://example.com/thumbs/p10.jpg',6,  TRUE, 500),
                                                                                                        ('Product 011',2100,'https://example.com/thumbs/p11.jpg',7,  TRUE, 150),
                                                                                                        ('Product 012',2200,'https://example.com/thumbs/p12.jpg',8,  FALSE,0),
                                                                                                        ('Product 013',2300,'https://example.com/thumbs/p13.jpg',9,  TRUE, 260),
                                                                                                        ('Product 014',2400,'https://example.com/thumbs/p14.jpg',10, TRUE, 300),
                                                                                                        ('Product 015',2500,'https://example.com/thumbs/p15.jpg',11, FALSE,0),
                                                                                                        ('Product 016',2600,'https://example.com/thumbs/p16.jpg',12, TRUE, 85),
                                                                                                        ('Product 017',2700,'https://example.com/thumbs/p17.jpg',13, TRUE, 440),
                                                                                                        ('Product 018',2800,'https://example.com/thumbs/p18.jpg',14, TRUE, 120),
                                                                                                        ('Product 019',2900,'https://example.com/thumbs/p19.jpg',6,  FALSE,0),
                                                                                                        ('Product 020',3000,'https://example.com/thumbs/p20.jpg',7,  TRUE, 270),
                                                                                                        ('Product 021',3100,'https://example.com/thumbs/p21.jpg',8,  TRUE, 390),
                                                                                                        ('Product 022',3200,'https://example.com/thumbs/p22.jpg',9,  FALSE,0),
                                                                                                        ('Product 023',3300,'https://example.com/thumbs/p23.jpg',10, TRUE, 180),
                                                                                                        ('Product 024',3400,'https://example.com/thumbs/p24.jpg',11, TRUE, 240),
                                                                                                        ('Product 025',3500,'https://example.com/thumbs/p25.jpg',12, TRUE, 310),
                                                                                                        ('Product 026',3600,'https://example.com/thumbs/p26.jpg',13, FALSE,0),
                                                                                                        ('Product 027',3700,'https://example.com/thumbs/p27.jpg',14, TRUE, 470),
                                                                                                        ('Product 028',3800,'https://example.com/thumbs/p28.jpg',6,  TRUE, 140),
                                                                                                        ('Product 029',3900,'https://example.com/thumbs/p29.jpg',7,  FALSE,0),
                                                                                                        ('Product 030',4000,'https://example.com/thumbs/p30.jpg',8,  TRUE, 220),
                                                                                                        ('Product 031',4100,'https://example.com/thumbs/p31.jpg',9,  TRUE, 360),
                                                                                                        ('Product 032',4200,'https://example.com/thumbs/p32.jpg',10, TRUE, 95),
                                                                                                        ('Product 033',4300,'https://example.com/thumbs/p33.jpg',11, FALSE,0),
                                                                                                        ('Product 034',4400,'https://example.com/thumbs/p34.jpg',12, TRUE, 410),
                                                                                                        ('Product 035',4500,'https://example.com/thumbs/p35.jpg',13, TRUE, 275),
                                                                                                        ('Product 036',4600,'https://example.com/thumbs/p36.jpg',14, FALSE,0),
                                                                                                        ('Product 037',4700,'https://example.com/thumbs/p37.jpg',6,  TRUE, 330),
                                                                                                        ('Product 038',4800,'https://example.com/thumbs/p38.jpg',7,  TRUE, 420),
                                                                                                        ('Product 039',4900,'https://example.com/thumbs/p39.jpg',8,  TRUE, 260),
                                                                                                        ('Product 040',5000,'https://example.com/thumbs/p40.jpg',9,  FALSE,0),
                                                                                                        ('Product 041',5100,'https://example.com/thumbs/p41.jpg',10, TRUE, 160),
                                                                                                        ('Product 042',5200,'https://example.com/thumbs/p42.jpg',11, TRUE, 350),
                                                                                                        ('Product 043',5300,'https://example.com/thumbs/p43.jpg',12, FALSE,0),
                                                                                                        ('Product 044',5400,'https://example.com/thumbs/p44.jpg',13, TRUE, 240),
                                                                                                        ('Product 045',5500,'https://example.com/thumbs/p45.jpg',14, TRUE, 480),
                                                                                                        ('Product 046',5600,'https://example.com/thumbs/p46.jpg',6,  FALSE,0),
                                                                                                        ('Product 047',5700,'https://example.com/thumbs/p47.jpg',7,  TRUE, 200),
                                                                                                        ('Product 048',5800,'https://example.com/thumbs/p48.jpg',8,  TRUE, 300),
                                                                                                        ('Product 049',5900,'https://example.com/thumbs/p49.jpg',9,  TRUE, 90),
                                                                                                        ('Product 050',6000,'https://example.com/thumbs/p50.jpg',10, FALSE,0);

-- 4) Product Image Details
--   DETAIL 11장 (id=1..11), ASSET 9장 (id=12..20: 카테고리 6..14 대응)
INSERT INTO product_img_details (type, img_url) VALUES
                                                    ('DETAIL','https://example.com/details/d01.jpg'),
                                                    ('DETAIL','https://example.com/details/d02.jpg'),
                                                    ('DETAIL','https://example.com/details/d03.jpg'),
                                                    ('DETAIL','https://example.com/details/d04.jpg'),
                                                    ('DETAIL','https://example.com/details/d05.jpg'),
                                                    ('DETAIL','https://example.com/details/d06.jpg'),
                                                    ('DETAIL','https://example.com/details/d07.jpg'),
                                                    ('DETAIL','https://example.com/details/d08.jpg'),
                                                    ('DETAIL','https://example.com/details/d09.jpg'),
                                                    ('DETAIL','https://example.com/details/d10.jpg'),
                                                    ('DETAIL','https://example.com/details/d11.jpg');

INSERT INTO product_img_details (type, img_url) VALUES
                                                    ('ASSET','https://example.com/assets/c6_a1.jpg'),
                                                    ('ASSET','https://example.com/assets/c7_a1.jpg'),
                                                    ('ASSET','https://example.com/assets/c8_a1.jpg'),
                                                    ('ASSET','https://example.com/assets/c9_a1.jpg'),
                                                    ('ASSET','https://example.com/assets/c10_a1.jpg'),
                                                    ('ASSET','https://example.com/assets/c11_a1.jpg'),
                                                    ('ASSET','https://example.com/assets/c12_a1.jpg'),
                                                    ('ASSET','https://example.com/assets/c13_a1.jpg'),
                                                    ('ASSET','https://example.com/assets/c14_a1.jpg');

-- 5) Mapping
--   DETAIL × 3개씩 라운드로빈(각 상품당 3매핑)
INSERT INTO product_img_details_mapping (product_img_details_id, product_id)
SELECT 1 + ((p.product_id - 1) * 3 + o.o) % 11 AS product_img_details_id,
       p.product_id
FROM product p
    JOIN (SELECT 0 AS o UNION ALL SELECT 1 UNION ALL SELECT 2) o
ORDER BY p.product_id, o.o;

--   ASSET × 1개(카테고리 공유) : category_id 6..14 → 이미지 id 12..20
INSERT INTO product_img_details_mapping (product_img_details_id, product_id)
SELECT 11 + (p.category_id - 5) AS product_img_details_id,
       p.product_id
FROM product p
ORDER BY p.product_id;

-- (옵션) 기본 장바구니 아이템 샘플
-- INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (1,1,2),(1,2,1),(2,2,3);

-- 끝