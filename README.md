# 🛍️ Product API (검색) — 테스트 가이드

> 상품 검색 API 테스트용 가이드입니다.  
> `POST` 요청으로 호출하며, 정렬은 `sort=필드,방향` 형식(띄어쓰기 금지)으로 전달합니다.

---

## ✅ Endpoint
- **URL**: `http://localhost:8080/api/v1/product/search?page=0&size=10&sort=createdAt,desc&sort=productPrice,asc`
- **Method**: `POST`
- **Headers**
  - `Content-Type: application/json`
  - (선택) `Accept: application/json`

> ⚠️ 주의: `sort=productPrice,asc` 처럼 **쉼표 뒤에 공백이 있으면 안 됩니다.**  
> `sort=productPrice, asc` → ❌ 오류 발생 (`No property ' asc' found for type 'Product'`)

---

## 📦 Request Body (JSON)
```json
{
  "q": "감자",
  "name": null,
  "categoryText": null,
  "minPrice": 1000,
  "maxPrice": 5000,
  "categoryId": null,
  "isActive": true
}
```

### 필드 설명
- q: 통합 키워드(한글/영문) — product_name, category.search_text 등 텍스트 필드 OR 검색
- name: 상품명 필터
- categoryText: 카테고리 한글 검색어(예: “감자”, “정육”)
- minPrice / maxPrice: 가격 범위
- categoryId: leaf 카테고리 ID (예: 6..14)
- isActive: 판매 활성 여부

---

## 🧪 호출 예시

**cURL**
```bash
curl -X POST "http://localhost:8080/api/v1/product/search?page=0&size=10&sort=createdAt,desc&sort=productPrice,asc" \
  -H "Content-Type: application/json" \
  -d '{
    "q": "감자",
    "name": null,
    "categoryText": null,
    "minPrice": 1000,
    "maxPrice": 5000,
    "categoryId": null,
    "isActive": true
  }'
```

**HTTPie**
```bash
http POST "http://localhost:8080/api/v1/product/search?page=0&size=10&sort=createdAt,desc&sort=productPrice,asc" \
  Content-Type:application/json \
  q=감자 name:=null categoryText:=null minPrice:=1000 maxPrice:=5000 categoryId:=null isActive:=true
```

**Postman**
- Method: POST
- URL: 위 Endpoint
- Headers: Content-Type: application/json
- Body (raw / JSON): 위 Request Body 그대로
- Query Params:
  - page=0
  - size=10
  - sort=createdAt,desc
  - sort=productPrice,asc

---

## 🔁 응답 예시
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "content": [
      {
        "productId": 10,
        "productName": "Product 010",
        "productThumbnail": "https://example.com/thumbs/p10.jpg",
        "productPrice": 2000,
        "upperCategory": "VEG",
        "category": "POTATO",
        "searchText": "감자",
        "isActive": true
      }
    ],
    "number": 0,
    "size": 10,
    "totalElements": 3,
    "totalPages": 1,
    "empty": false
  }
}
```

---

## ❗️자주 나는 오류 & 해결
- `No property ' asc' found for type 'Product'`  
  → 원인: `sort=productPrice, asc`처럼 쉼표 뒤 공백 존재  
  → 해결: `sort=productPrice,asc` 로 수정
- 400/500 파라미터 파싱 에러  
  → QueryString/Body JSON 형식 및 `Content-Type` 확인

---

# 🛒 Cart API — 테스트 가이드

> 장바구니 API 테스트 가이드입니다.  
> 모든 응답은 `ResponseObject` 포맷으로 내려갑니다.

---

## ✅ API Endpoints

### 1) 장바구니 담기 (POST)
- **URL**: `http://localhost:8080/api/v1/cart/{userId}`
- **Method**: `POST`
- **Body (JSON)**
```json
{
  "productId": 1,
  "quantity": 10
}
```
**Response**
```json
{
  "code": 200,
  "message": "Success",
  "result": "상품이 저장되었습니다."
}
```

---

### 2) 장바구니 조회 (GET, Slice 페이징)
- **URL**: `http://localhost:8080/api/v1/cart/{userId}?page=0&size=10`
- **Method**: `GET`
- **Query Params**
  - page (기본 0)
  - size (페이지 크기)
  - sort (선택: 기본 최신순 정렬, 예: createdAt,ASC)
    **Response**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "content": [
      {
        "productName": "Product 001",
        "productThumbnail": "https://example.com/thumbs/p1.jpg",
        "productId": 1,
        "isActive": false,
        "productPrice": 1100,
        "quantity": 10
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "hasNext": false
  }
}
```

---

### 3) 장바구니 상품 삭제 (DELETE)
- **URL**: `http://localhost:8080/api/v1/cart/{userId}?productId=1`
- **Method**: `DELETE`
  **Response**

---

### 4) 장바구니 수량 업데이트 (PUT)
- **URL**: `http://localhost:8080/api/v1/cart/{userId}?productId=1`
- **Method**: `PUT`
- **Body (JSON)**
```json
{
  "quantity": 100
}
```
**Response**
```json
{
  "code": 200,
  "message": "Success",
  "result": 100
}
```

---

### 5) 상품 수량 증가 (PATCH)
- **URL**: `http://localhost:8080/api/v1/cart/{userId}/increase?productId=1`
- **Method**: `PATCH`
  **Response**
```json
{
  "code": 200,
  "message": "Success",
  "result": 101
}
```

---

### 6) 상품 수량 감소 (PATCH)
- **URL**: `http://localhost:8080/api/v1/cart/{userId}/decrease?productId=1`
- **Method**: `PATCH`
  **Response**
```json
{
  "code": 200,
  "message": "Success",
  "result": 100
}
```