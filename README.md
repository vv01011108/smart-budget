# Smart Budget (AI 재무 코칭 가계부)

월 예산과 고정·변동 지출을 관리하고, 구매 시뮬레이션과 Gemini AI 기반 조언 및 결제 문자 파싱을 제공하는 개인 재무 관리 웹 애플리케이션입니다.

## 배포 주소
- **Frontend (Web):** https://smart-budget-web.onrender.com
- **Backend API:** https://smart-budget-api-mui6.onrender.com
- **Swagger UI:** https://smart-budget-api-mui6.onrender.com/swagger-ui/index.html

## 기술 스택
- **Backend:** Java, Spring Boot, JPA/Hibernate, Gradle, Docker, Swagger (OpenAPI)
- **AI:** Google Gemini API (구매 시뮬레이션 코치, 결제 문자 자동 파싱)
- **Database:** TiDB Cloud (MySQL 호환)
- **Frontend:** HTML, CSS, JavaScript (ES6+), Render Static Site
- **Deployment:** Render (Docker Web Service, Static Site)

## 주요 기능
- **월 예산 및 지출 관리:** 월별 예산 설정, 고정 지출(항목·금액·결제일) 및 변동 지출 CRUD
- **구매 시뮬레이션 & AI 코치:** 예정 지출 입력 시 구매 후 잔여 예산·남은 일수·일일 권장 지출액 계산 및 Gemini AI 재무 조언 제공(장애 시 기본 규칙 기반 Fallback 지원)
- **AI 결제 문자 파싱:** 카드 승인 문자 텍스트를 입력해 금액·가맹점·일시 등 지출 정보 추출

## 아키텍처 및 배포 구성
- **프론트엔드·백엔드 분리 배포:** `frontend/` 정적 리소스는 Render Static Site로, Spring Boot 서버는 `Dockerfile` 기반 Docker Web Service로 분리 배포했습니다.
- **환경별 API·CORS 분기:** 로컬(`localhost`, `127.0.0.1`)과 운영 도메인을 구분해 `API_BASE`를 자동 전환하고, `CORS_ALLOWED_ORIGINS` 환경변수로 허용 출처를 관리합니다.
- **클라우드 DB 연동:** TiDB Cloud에 `smart_budget` 데이터베이스를 구성하고 TLS/SSL 옵션을 적용해 Spring Boot와 연결했습니다.

## 트러블슈팅 및 개선 사항
- **CORS 설정 반영 이슈:** `WebConfig`에서 환경변수 바인딩 필드(`allowedOrigins`) 대신 기존 로컬 도메인 목록이 직접 지정되어 있던 문제를 수정하고, Render에서 `Clear build cache & deploy`로 재빌드해 정상 통신을 확보했습니다.
- **고정 지출 요청 필드 불일치 해결:** 프론트엔드 요청 Payload의 `title` 키와 백엔드 DTO의 `name` 필드 불일치로 발생하던 `400 Bad Request`를 키 통일로 해결했습니다.
- **AI 조언 응답 포맷 정제:** Gemini가 JSON 문자열이나 마크다운 코드블록 형태로 반환하는 경우에도 `"advice"` 본문만 추출하도록 `AiAdviceService`에 파싱 로직을 추가하고, `ObjectMapper` 빈 누락으로 인한 기동 실패를 내부 인스턴스 생성으로 정리했습니다.
- **무료 티어 슬립 특성:** Render 무료 Web Service는 15분간 요청이 없으면 슬립 상태로 전환되며, 재기동 시 첫 요청에 30초~1분 정도 소요될 수 있습니다.