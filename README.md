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

필드 설명
	•	q: 통합 키워드(한글/영문) — product_name, category.search_text 등 텍스트 필드 OR 검색
	•	name: 상품명 필터
	•	categoryText: 카테고리 한글 검색어(예: “감자”, “정육”)
	•	minPrice / maxPrice: 가격 범위
	•	categoryId: leaf 카테고리 ID (예: 6..14)
	•	isActive: 판매 활성 여부

⸻

🧪 호출 예시

cURL

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

HTTPie

http POST "http://localhost:8080/api/v1/product/search?page=0&size=10&sort=createdAt,desc&sort=productPrice,asc" \
  Content-Type:application/json \
  q=감자 name:=null categoryText:=null minPrice:=1000 maxPrice:=5000 categoryId:=null isActive:=true

Postman
	•	Method: POST
	•	URL: 위 Endpoint
	•	Headers: Content-Type: application/json
	•	Body (raw / JSON): 위 Request Body 그대로
	•	Query Params
	•	page=0
	•	size=10
	•	sort=createdAt,desc
	•	sort=productPrice,asc

⸻

🔁 응답 예시

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
      },
      {
        "productId": 28,
        "productName": "Product 028",
        "productThumbnail": "https://example.com/thumbs/p28.jpg",
        "productPrice": 3800,
        "upperCategory": "VEG",
        "category": "POTATO",
        "searchText": "감자",
        "isActive": true
      },
      {
        "productId": 37,
        "productName": "Product 037",
        "productThumbnail": "https://example.com/thumbs/p37.jpg",
        "productPrice": 4700,
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
    "empty": false,
    "sort": {
      "orders": [
        {
          "property": "createdAt",
          "direction": "DESC",
          "ignoreCase": false,
          "nullHandling": "NATIVE"
        },
        {
          "property": "productPrice",
          "direction": "ASC",
          "ignoreCase": false,
          "nullHandling": "NATIVE"
        }
      ]
    }
  }
}


⸻

❗️자주 나는 오류 & 해결
	•	No property ' asc' found for type 'Product'
→ sort=productPrice, asc처럼 쉼표 뒤 공백이 있을 때 발생
→ 공백 제거: sort=productPrice,asc
	•	400/500 응답이며 파라미터 파싱 에러 발생 시
→ QueryString과 Body 모두 JSON 형식/타입 재확인, Content-Type이 application/json인지 확인
