-- ---------- User (10) ----------
INSERT INTO user (name) VALUES
                            ('김민수'),
                            ('이서연'),
                            ('박지훈'),
                            ('최유진'),
                            ('정우영'),
                            ('한지민'),
                            ('오세훈'),
                            ('유나영'),
                            ('장도윤'),
                            ('서지호');

-- ---------- Product Category (14) ----------
-- 상위(1~5)는 필터용, leaf(6~14)만 product에 매핑
INSERT INTO product_category (upper_category, category) VALUES
                                                            (NULL, 'VEG'),
                                                            (NULL, 'SEAFOOD'),
                                                            (NULL, 'MEAT'),
                                                            (NULL, 'FRUIT'),
                                                            (NULL, 'GRAIN'),
                                                            ('VEG',      'POTATO'),  -- id=6
                                                            ('VEG',      'CARROT'),  -- 7
                                                            ('SEAFOOD',  'FISH'),    -- 8
                                                            ('SEAFOOD',  'SHRIMP'),  -- 9
                                                            ('MEAT',     'PORK'),    -- 10
                                                            ('MEAT',     'BEEF'),    -- 11
                                                            ('FRUIT',    'APPLE'),   -- 12
                                                            ('FRUIT',    'BANANA'),  -- 13
                                                            ('GRAIN',    'RICE');    -- 14

-- ---------- Product (50) ----------
-- leaf 카테고리만 순환: 6..14 → 반복
INSERT INTO product (product_name, product_price, product_thumbnail, category_id) VALUES
                                                                                      ('Product 001', 1100, 'https://example.com/thumbs/p1.jpg',  6),
                                                                                      ('Product 002', 1200, 'https://example.com/thumbs/p2.jpg',  7),
                                                                                      ('Product 003', 1300, 'https://example.com/thumbs/p3.jpg',  8),
                                                                                      ('Product 004', 1400, 'https://example.com/thumbs/p4.jpg',  9),
                                                                                      ('Product 005', 1500, 'https://example.com/thumbs/p5.jpg',  10),
                                                                                      ('Product 006', 1600, 'https://example.com/thumbs/p6.jpg',  11),
                                                                                      ('Product 007', 1700, 'https://example.com/thumbs/p7.jpg',  12),
                                                                                      ('Product 008', 1800, 'https://example.com/thumbs/p8.jpg',  13),
                                                                                      ('Product 009', 1900, 'https://example.com/thumbs/p9.jpg',  14),
                                                                                      ('Product 010', 2000, 'https://example.com/thumbs/p10.jpg', 6),
                                                                                      ('Product 011', 2100, 'https://example.com/thumbs/p11.jpg', 7),
                                                                                      ('Product 012', 2200, 'https://example.com/thumbs/p12.jpg', 8),
                                                                                      ('Product 013', 2300, 'https://example.com/thumbs/p13.jpg', 9),
                                                                                      ('Product 014', 2400, 'https://example.com/thumbs/p14.jpg', 10),
                                                                                      ('Product 015', 2500, 'https://example.com/thumbs/p15.jpg', 11),
                                                                                      ('Product 016', 2600, 'https://example.com/thumbs/p16.jpg', 12),
                                                                                      ('Product 017', 2700, 'https://example.com/thumbs/p17.jpg', 13),
                                                                                      ('Product 018', 2800, 'https://example.com/thumbs/p18.jpg', 14),
                                                                                      ('Product 019', 2900, 'https://example.com/thumbs/p19.jpg', 6),
                                                                                      ('Product 020', 3000, 'https://example.com/thumbs/p20.jpg', 7),
                                                                                      ('Product 021', 3100, 'https://example.com/thumbs/p21.jpg', 8),
                                                                                      ('Product 022', 3200, 'https://example.com/thumbs/p22.jpg', 9),
                                                                                      ('Product 023', 3300, 'https://example.com/thumbs/p23.jpg', 10),
                                                                                      ('Product 024', 3400, 'https://example.com/thumbs/p24.jpg', 11),
                                                                                      ('Product 025', 3500, 'https://example.com/thumbs/p25.jpg', 12),
                                                                                      ('Product 026', 3600, 'https://example.com/thumbs/p26.jpg', 13),
                                                                                      ('Product 027', 3700, 'https://example.com/thumbs/p27.jpg', 14),
                                                                                      ('Product 028', 3800, 'https://example.com/thumbs/p28.jpg', 6),
                                                                                      ('Product 029', 3900, 'https://example.com/thumbs/p29.jpg', 7),
                                                                                      ('Product 030', 4000, 'https://example.com/thumbs/p30.jpg', 8),
                                                                                      ('Product 031', 4100, 'https://example.com/thumbs/p31.jpg', 9),
                                                                                      ('Product 032', 4200, 'https://example.com/thumbs/p32.jpg', 10),
                                                                                      ('Product 033', 4300, 'https://example.com/thumbs/p33.jpg', 11),
                                                                                      ('Product 034', 4400, 'https://example.com/thumbs/p34.jpg', 12),
                                                                                      ('Product 035', 4500, 'https://example.com/thumbs/p35.jpg', 13),
                                                                                      ('Product 036', 4600, 'https://example.com/thumbs/p36.jpg', 14),
                                                                                      ('Product 037', 4700, 'https://example.com/thumbs/p37.jpg', 6),
                                                                                      ('Product 038', 4800, 'https://example.com/thumbs/p38.jpg', 7),
                                                                                      ('Product 039', 4900, 'https://example.com/thumbs/p39.jpg', 8),
                                                                                      ('Product 040', 5000, 'https://example.com/thumbs/p40.jpg', 9),
                                                                                      ('Product 041', 5100, 'https://example.com/thumbs/p41.jpg', 10),
                                                                                      ('Product 042', 5200, 'https://example.com/thumbs/p42.jpg', 11),
                                                                                      ('Product 043', 5300, 'https://example.com/thumbs/p43.jpg', 12),
                                                                                      ('Product 044', 5400, 'https://example.com/thumbs/p44.jpg', 13),
                                                                                      ('Product 045', 5500, 'https://example.com/thumbs/p45.jpg', 14),
                                                                                      ('Product 046', 5600, 'https://example.com/thumbs/p46.jpg', 6),
                                                                                      ('Product 047', 5700, 'https://example.com/thumbs/p47.jpg', 7),
                                                                                      ('Product 048', 5800, 'https://example.com/thumbs/p48.jpg', 8),
                                                                                      ('Product 049', 5900, 'https://example.com/thumbs/p49.jpg', 9),
                                                                                      ('Product 050', 6000, 'https://example.com/thumbs/p50.jpg', 10);

-- ---------- Product Image Details (총 20장만 생성) ----------
-- DETAIL 11장(1..11): 전 상품이 회전 재사용
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

-- ASSET 9장(12..20): leaf 카테고리 6..14 공유용
INSERT INTO product_img_details (type, img_url) VALUES
                                                    ('ASSET','https://example.com/assets/c6_a1.jpg'),   -- id=12
                                                    ('ASSET','https://example.com/assets/c7_a1.jpg'),   -- 13
                                                    ('ASSET','https://example.com/assets/c8_a1.jpg'),   -- 14
                                                    ('ASSET','https://example.com/assets/c9_a1.jpg'),   -- 15
                                                    ('ASSET','https://example.com/assets/c10_a1.jpg'),  -- 16
                                                    ('ASSET','https://example.com/assets/c11_a1.jpg'),  -- 17
                                                    ('ASSET','https://example.com/assets/c12_a1.jpg'),  -- 18
                                                    ('ASSET','https://example.com/assets/c13_a1.jpg'),  -- 19
                                                    ('ASSET','https://example.com/assets/c14_a1.jpg');  -- 20

-- ---------- Mapping ----------
-- DETAIL×3: 11장을 회전 재사용
--   formula: detail_id = 1 + ((product_id-1)*3 + o) % 11,  o ∈ {0,1,2}
INSERT INTO product_img_details_mapping (product_img_details_id, product_id)
SELECT 1 + ((p.product_id - 1) * 3 + o.o) % 11 AS product_img_details_id,
       p.product_id
FROM product p
         JOIN (SELECT 0 AS o UNION ALL SELECT 1 UNION ALL SELECT 2) o
ORDER BY p.product_id, o.o;

-- ASSET×1: leaf 카테고리별 공유
--   ASSET id = 11 + (category_id - 5)  → category 6..14 → asset 12..20
INSERT INTO product_img_details_mapping (product_img_details_id, product_id)
SELECT 11 + (p.category_id - 5) AS product_img_details_id,
       p.product_id
FROM product p
ORDER BY p.product_id;

COMMIT;

-- 인덱스
CREATE INDEX idx_pim_product_id  ON product_img_details_mapping (product_id);
CREATE INDEX idx_pim_details_id  ON product_img_details_mapping (product_img_details_id);
CREATE INDEX idx_pid_type        ON product_img_details (type);