# tsp-msa — Token Service Provider 학습 프로젝트

모바일 결제 도메인의 핵심 개념인 **토큰화**, **멱등성**, **이벤트 기반 MSA**, **웹훅**을  
Spring Boot 멀티모듈 + Kafka + Redis로 직접 구현한 학습용 백엔드 프로젝트입니다.

스노우온카드(SnowOncard)의 CbPP(Cloud-based Payment Platform) 아키텍처를 참고하여 설계했습니다.

---

## 아키텍처

```
클라이언트
    │
    ▼
account-service (8091)        ← JWT 인증 / 계좌 잔액 원자적 차감·충전
tokenization-service (8092)   ← 카드 토큰 발급·디토큰화·라이프사이클
    │
    ▼
transaction-service (8093)    ← 결제 오케스트레이터
    │  1. Redis 멱등성 키 선점 (SETNX)
    │  2. tokenization-service 디토큰화
    │  3. account-service 잔액 차감
    │  4. Kafka에 거래 이벤트 발행
    │
    ▼
notification-service (8094)   ← Kafka 소비 → HMAC 서명 웹훅 발송 → 지수 백오프 재시도
    │
    ▼
merchant-mock-service (8095)  ← 가맹점 웹훅 수신 스텁 (서명 검증 + 중복 방지)
```

**인프라**
- PostgreSQL: 서비스마다 독립 논리 DB (`account_db`, `tokenization_db` 등)
- Apache Kafka: KRaft 모드 (Zookeeper 없음), `transaction-events` 토픽
- Redis: 멱등성 키 캐시

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.3 |
| Build | Gradle 멀티모듈 |
| DB | PostgreSQL 15, Spring Data JPA |
| Message Broker | Apache Kafka 3.7 (KRaft) |
| Cache | Redis 7, Spring Data Redis |
| Auth | Spring Security + JWT (jjwt 0.11.5) |
| Inter-service | Spring RestClient (동기 REST 호출) |
| Webhook 보안 | HmacSHA256 서명 + 상수 시간 비교 |
| 재시도 | @Scheduled + 지수 백오프 |
| 컨테이너 | Docker, Docker Compose |
| CI/CD | GitHub Actions (매트릭스 빌드) |

---

## 핵심 구현 포인트

### 1. 멱등성 (Idempotency) — transaction-service
Stripe 방식의 SETNX 기반 멱등성 키 구현.  
같은 `Idempotency-Key`로 중복 요청이 들어오면 재처리 없이 **캐시된 응답을 그대로 반환**.

```
POST /api/transactions
Header: Idempotency-Key: <uuid>
Body: { "token": "tok_...", "merchantId": "merchant-1", "amount": 10000 }
```

- 첫 요청 → Redis에 `IN_PROGRESS` 선점 → 처리 → `COMPLETED`로 덮어씀
- 재요청 → 캐시된 응답 즉시 반환 (잔액 중복 차감 없음)
- 동시 중복 요청 → 하나는 409 `REQUEST_IN_PROGRESS`

### 2. 원자적 잔액 차감 — account-service
낙관적 락(`@Version`) 대신 **조건부 원자 UPDATE** 사용.  
wms-msa(재고관리 프로젝트)에서 사용한 낙관적 락 방식과 의도적으로 대비되는 설계.

```sql
UPDATE account SET balance = balance - :amount
WHERE id = :id AND balance >= :amount
```

### 3. 결제 거절은 HTTP 에러가 아니다
잔액 부족, 토큰 비활성 등 **비즈니스 거절은 모두 HTTP 200** + `status: FAILED`.  
실제 API 에러(인프라 장애)만 502를 내려줌.

### 4. 웹훅 HMAC 서명 — notification-service
```
Header: X-Signature: <HmacSHA256 hex>
Header: X-Event-Id: <uuid>
```
merchant-mock-service에서 서명 검증 시 **상수 시간 비교**(`MessageDigest.isEqual`)로 타이밍 공격 방어.

### 5. 지수 백오프 재시도 — notification-service
웹훅 발송 실패 시 `webhook_delivery` 테이블에 기록 후  
`@Scheduled` 폴러가 **1분 → 2분 → 4분 → 8분...** 간격으로 최대 6회 재시도.

---

## 로컬 실행 방법

**사전 조건**: Docker Desktop 실행 중

```bash
# 1. 전체 빌드
./gradlew clean build -x test

# 2. JAR 복사 (Dockerfile이 각 서비스 폴더의 app.jar를 사용함)
for s in account-service tokenization-service transaction-service notification-service merchant-mock-service; do
  cp $s/build/libs/$s-1.0-SNAPSHOT.jar $s/app.jar
done

# 3. 전체 스택 실행
docker compose up -d --build
```

---

## E2E 테스트 흐름

```bash
# 1. 계정 생성
curl -X POST http://localhost:8091/api/accounts \
  -H 'Content-Type: application/json' \
  -d '{"ownerName":"홍길동","initialBalance":100000}'

# 2. 토큰 발급 (실카드번호는 마스킹 후 폐기 — 저장 안 함)
curl -X POST http://localhost:8092/api/tokens/issue \
  -H 'Content-Type: application/json' \
  -d '{"accountId":1,"pan":"4111111111111234","deviceId":"my-phone"}'

# 3. 가맹점 웹훅 등록
curl -X POST http://localhost:8094/api/merchants \
  -H 'Content-Type: application/json' \
  -d '{"merchantId":"shop-1","webhookUrl":"http://merchant-mock-service:8095/webhook/receive","webhookSecret":"my-secret"}'

# 4. 결제 실행
curl -X POST http://localhost:8093/api/transactions \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: order-001' \
  -d '{"token":"tok_...","merchantId":"shop-1","amount":15000}'
# → {"status":"COMPLETED","newBalance":85000.0, ...}

# 5. 동일 키로 재요청 → 잔액 변화 없이 동일 응답 반환
curl -X POST http://localhost:8093/api/transactions \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: order-001' \
  -d '{"token":"tok_...","merchantId":"shop-1","amount":15000}'

# 6. 웹훅 수신 확인
curl http://localhost:8095/api/received-events
curl http://localhost:8094/api/webhook-deliveries
```

---

## 서비스 포트 요약

| 서비스 | 포트 | 역할 |
|--------|------|------|
| account-service | 8091 | 계좌 관리, JWT 인증 |
| tokenization-service | 8092 | 토큰 발급·관리 |
| transaction-service | 8093 | 결제 처리 (핵심) |
| notification-service | 8094 | 웹훅 발송 |
| merchant-mock-service | 8095 | 웹훅 수신 테스트 |
| PostgreSQL | 5432 | DB (5개 논리 DB) |
| Kafka | 9092 | 이벤트 브로커 |
| Redis | 6379 | 멱등성 키 캐시 |

---

## CI/CD

`main` 브랜치에 push 시 GitHub Actions가 자동 실행됩니다.

```
build-and-push (5개 서비스 병렬 매트릭스 빌드)
    └─ JDK 21 빌드 → Docker 이미지 → Docker Hub push

deploy
    └─ EC2 SSH → docker compose pull → docker compose up -d
```

**필요한 GitHub Secrets**

| Secret | 설명 |
|--------|------|
| `DOCKERHUB_USERNAME` | Docker Hub 아이디 |
| `DOCKERHUB_TOKEN` | Docker Hub 액세스 토큰 |
| `SERVER_IP` | EC2 IP 주소 |
| `SERVER_USER` | EC2 접속 유저명 (ubuntu) |
| `SSH_KEY` | EC2 SSH 프라이빗 키 |
| `JWT_SECRET` | JWT 서명 시크릿 (32자 이상) |

---

## 참고 아키텍처

SnowOncard CbPP(Cloud-based Payment Platform)의 TSP(Token Service Provider) 컴포넌트를  
학습 목적으로 단순화하여 구현하였습니다.

- [SnowOncard](https://www.snowoncard.com) — 국내 Apple Pay(현대카드), 티머니 태그리스, Taiwan Pay 구축
