알겠습니다 🙏 불필요한 첨언은 전부 빼고, 그대로 복붙해도 되는 순수 README.md 블록만 드리겠습니다.

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


# 🤖 AI Assistant Notes

이 영역은 AI Assistant와의 협업 과정을 정리한 부분입니다.

## 해결하려던 문제
MySQL 기반 상품/이미지 매핑 스키마 설계와 더미 데이터 생성, JPA(Entity/DTO) 매핑 방식, QueryDSL 기반 통합 검색 쿼리/DTO 설계를 진행하면서 올바른 구조와 코드 예시를 정리하고 싶었다.

## 대화 요약

### Q: `product_img_details`에 DETAIL만 50개 넣은 예제를 ASSET과 반반 섞어서 만들어달라
**A:** DETAIL과 ASSET을 절반씩 섞어서 INSERT SQL 예시를 만들어주었음.

### Q: `product_img_details_mapping`은 1:1 매핑이 아니라 상품별 3개의 DETAIL, 1개의 ASSET으로 해달라
**A:** 각 상품당 DETAIL×3 + ASSET×1 구조로 매핑 SQL을 제안했고, 같은 카테고리 상품은 동일한 ASSET 이미지를 공유하도록 구성.

### Q: “굳이 product에 mapping PK를 둘 필요 없다”라고 생각이 바뀜
**A:** 동의하며 product에서 img_details_mapping 컬럼을 제거한 새로운 스키마를 제시. 삽입 순환 문제 없이 N:M 구조만으로 충분하다고 설명.

### Q: 전체 더미 데이터 (User, Category, Product, ImgDetails, Mapping) 다시 작성해달라
**A:** 50개 상품, 14개 카테고리, 150개 DETAIL, 20개 ASSET, 200개 매핑이 포함된 풀 시드 SQL을 제시.

### Q: product 테이블의 category_id는 leaf 카테고리(6~14)만 사용해야 한다
**A:** upper_category가 NULL인 상위 카테고리는 product에 매핑되지 않도록 수정된 SQL을 제공.

### Q: product_img_details는 150개 전부 넣지 말고 20개만 생성해서 돌려 쓰자
**A:** DETAIL 11개, ASSET 9개만 만들고, 상품별로 회전 재사용(DETAIL×3 + ASSET×1)하도록 SQL을 제시.

### Q: record 클래스를 QueryDSL 프로젝션 DTO로 쓸 수 있나?
**A:** 가능. `Projections.constructor()` 방식으로 사용 가능하지만 `@QueryProjection`은 record/data class에 적용 불가. 조회 전용 DTO라면 record/data class + constructor projection이 깔끔하다고 정리.

### Q: 동적쿼리 검색 조건 DTO도 record로 쓸 수 있나?
**A:** 가능하지만 Java record는 모든 필드를 생성자로 받아야 해서 선택적 조건이 많은 경우 불편. Kotlin data class는 기본값/엘비스 연산자를 쓸 수 있어 더 자연스럽다고 설명.

### Q: 통합검색 쿼리에서 "감자"를 넣을 때 product_name/price/search_text에 전부 AND 매핑되는 게 맞냐?
**A:** 텍스트 필드 검색은 OR 묶음으로, 가격/카테고리 같은 구조화된 필터는 AND로 분리해야 한다고 정리. QueryDSL 예시도 함께 제시.

### Q: SearchConditionDto는 어떻게 작성하는 게 좋냐?
**A:** Java class + Builder, 혹은 Kotlin data class 형태로 `q`(통합 키워드)와 정형 필터(minPrice, maxPrice, categoryId, isActive 등)를 분리. QueryDSL BooleanBuilder 헬퍼 메서드까지 포함된 DTO 설계안을 제시.

### Q: 내 Kotlin 스타일 예시(NailSetProductRepositoryImpl)를 보여주며 참고하라고 했다
**A:** Java 구현부도 Kotlin 스타일을 따라, DTO는 데이터만 담고 조건 조립은 RepositoryImpl 내부 private 함수에서 하도록 리팩토링된 구현 코드를 제안했다.

---

## 최종 적용 결과
- product 스키마에서 img_details_mapping 컬럼 제거 → 순환 FK 문제 해소.
- User, Category, Product, ProductImgDetails(20개만), Mapping 더미 데이터 세트 완성.
- 조회 전용 DTO는 record/data class + constructor projection으로 사용.
- 검색 조건 DTO는 통합 키워드와 구조화 필터를 분리해 설계, QueryDSL에서 BooleanBuilder로 동적 조건 생성 가능.
- 통합검색에서 텍스트 필드는 OR, 필터는 AND 조합으로 안전하게 처리하도록 구현 방향 확정.  