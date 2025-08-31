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
# 주문 생성 API 사용 가이드

**Endpoint**  
POST /api/v1/order

장바구니 항목으로 주문을 생성하고, 결제까지 동기 처리합니다.
- 결제가 성공하면 주문이 PAID/COMPLETED 로 확정됩니다.
- 결제가 실패하면 보상 트랜잭션으로 재고 원복 후 주문이 CANCELED/FAILED 로 기록됩니다.

---

## 요청 형식

### Headers
- Content-Type: application/json

### Body (JSON)
{
"items": [
{
"productId": 1,
"productName": "테스트상품A",
"quantity": 2,
"unitPrice": 1000,
"totalPrice": 2000
}
// ... 추가 상품
]
}

### 필드 설명
- productId (number): 상품 PK
- productName (string): 상품명 (로깅/검증용)
- quantity (number): 수량(1 이상)
- unitPrice (number): 단가(원)
- totalPrice (number): 항목별 합계(= quantity * unitPrice)
- 서버는 모든 항목의 totalPrice 합을 결제 금액으로 사용합니다.

⚠️ 실패 규칙: 모든 항목의 totalPrice 합계가 0원이면 결제를 진행하지 않으며 실패 처리됩니다.

---

## 응답 형식 (공통)

- 성공 시: 200 OK
- 실패 시: 비즈니스 에러코드와 함께 비 2xx 상태 코드 반환

### 성공 응답 예시
{
"code": 200,
"message": "Success",
"result": {
"orderId": "OID20250831XXXXXXXXXXXX",
"paymentSuccess": true,
"failureReason": null,
"transactionId": "TID_abc123xyz"
}
}

### 실패 응답 예시
{
"code": "PAYMENT_FAILED",
"message": "결제가 실패했습니다."
}

---

## 예시

### ✅ 성공 사례

Request
curl -X POST http://localhost:8080/api/v1/order \
-H "Content-Type: application/json" \
-d '{
"items": [
{ "productId": 1, "productName": "테스트상품A", "quantity": 1, "unitPrice": 1000, "totalPrice": 1000 },
{ "productId": 3, "productName": "테스트상품B", "quantity": 1, "unitPrice": 1000, "totalPrice": 1000 }
]
}'

Response (200)
{
"code": 200,
"message": "Success",
"result": {
"orderId": "OID20250831F6AFC13EEBAE49C28DF227C169DC10E2",
"paymentSuccess": true,
"failureReason": null,
"transactionId": "TID_20250831_001"
}
}

---

### ❌ 실패 사례 #1 — 총 결제금액이 0원

Request
curl -X POST http://localhost:8080/api/v1/order \
-H "Content-Type: application/json" \
-d '{
"items": [
{ "productId": 1, "productName": "테스트상품A", "quantity": 1, "unitPrice": 0, "totalPrice": 0 },
{ "productId": 3, "productName": "테스트상품B", "quantity": 1, "unitPrice": 0, "totalPrice": 0 }
]
}'

Response (400 Bad Request 권장)
{
"code": "PAYMENT_FAILED",
"message": "결제가 실패했습니다."
}

설명: 총 결제금액이 0원이므로 결제를 진행하지 않고 실패 처리합니다.  
주문은 보상 트랜잭션으로 취소 및 재고 원복됩니다.

---

### ❌ 실패 사례 #2 — 결제 타임아웃/재시도 소진

상황: PG 응답 지연/오류로 재시도(최대 6회) 후에도 완료되지 않음.

Request (성공 사례와 동일 형식)

Response (504 Gateway Timeout 권장)
{
"code": "PAYMENT_TIMEOUT",
"message": "결제가 제한 시간 내에 완료되지 않았습니다."
}

설명: 내부에서 RabbitMQ 재시도 전략을 수행했지만 성공 신호를 받지 못해 타임아웃 처리합니다.  
주문은 취소되고 재고가 원복됩니다.

---

## 참고 사항
- 성공 시 transactionId(= PG tid)가 응답에 포함됩니다.
- 실패 시에는 transactionId 가 null 또는 미포함입니다.
- 실패 응답의 code 는 상황에 따라 PAYMENT_FAILED, PAYMENT_TIMEOUT, PG_BAD_REQUEST, PAYMENT_GATEWAY_ERROR 중 하나가 내려올 수 있습니다.
- 각 실패 케이스에서도 주문/결제 이력 테이블에 상태 이력과 원문(또는 요약) 로그가 저장됩니다.  