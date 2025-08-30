# ---------- User ----------

create table user(
    user_id bigint auto_increment primary key,
    name varchar(100) not null
) comment '더미용 유저 테이블'
    default char set = utf8mb4
    collate = utf8mb4_bin
;

# ---------- Product ----------

create table product_category(
    category_id bigint auto_increment primary key,
    upper_category varchar(30) default null comment '상위 카테고리(ex.VEG(야채),SEAFOOD(해산물), MEAT(정육) ...)',
    category varchar(50) not null comment '해당 카테고리(ex. POTATO(감자), FISH(생선), PORK(돼지고기))'

) comment '상품 카테고리 테이블'
    default char set = utf8mb4
    collate = utf8mb4_bin
;

create table product_img_details(
    id bigint auto_increment primary key,
    type varchar(50) default 'DETAIL' comment 'DETAIL(상품상세), ASSET(유의사항 이미지 등)',
    img_url text default null comment '과제용 이므로 이미지가 업로드 됐다고 가정'

) comment '상품 상세 이미지 및 asset'
    default char set = utf8mb4
    collate = utf8mb4_bin
;

alter table product_img_details modify column type varchar(10);

create table product_img_details_mapping(
    id bigint auto_increment primary key,
    product_img_details_id bigint references product_img_details(id),
    product_id bigint references product(product_id)
);

create table product(
    product_id bigint auto_increment primary key,
    product_name varchar(100) not null,
    product_price int unsigned not null default 1000,
    product_thumbnail text default null comment '쌈네일 이미지 url -> 과제용 이므로 이미지가 업로드 됐다고 가정',
    category_id bigint comment '상품 카테고리' references product_category(category_id) ,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp

) comment '상품 테이블'
    default char set = utf8mb4
    collate = utf8mb4_bin
;
