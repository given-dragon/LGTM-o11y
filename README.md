# Observability Stack - Caro Demo

Grafana Stack 기반의 프로덕션 레디 관측 가능성(Observability) 인프라. Spring Boot 마이크로서비스를 위한 메트릭, 로그, 트레이스를 수집하고 시각화한다.

## 아키텍처

```
Caro-Demo Application (Spring Boot 4 + OpenTelemetry)
    |
    | OTLP (HTTP :4318)
    v
  Alloy (Unified Collector)
    |
    +---> Loki    (Logs)     :3100
    +---> Mimir   (Metrics)  :9009
    +---> Tempo   (Traces)   :3200
    |
    v
  Grafana (Dashboard)        :3000
    |
    v
  Alertmanager --> Slack
```

### 핵심 컴포넌트

| 서비스 | 역할 | 포트 |
|--------|------|------|
| **Grafana** | 대시보드 및 시각화 | 3000 |
| **Loki** | 로그 수집 및 검색 | 3100 |
| **Tempo** | 분산 트레이싱 | 3200 |
| **Mimir** | 메트릭 저장 (Prometheus 호환) | 9009 |
| **Alloy** | 통합 데이터 수집기 (OTLP) | 12345 |
| **Caro-Demo** | Spring Boot 데모 애플리케이션 | 8080 |

### 멀티 테넌시

모든 백엔드(Loki, Tempo, Mimir)는 `X-Scope-OrgID` 헤더를 통한 멀티 테넌시를 지원한다.

| 테넌트 | 용도 | 메트릭 보존 | 트레이스 보존 |
|--------|------|-------------|---------------|
| `caro-backend-prod` | 프로덕션 환경 | 15일 | 14일 |
| `caro-backend-staging` | 스테이징 환경 | 7일 | 7일 |
| `infra-monitoring` | Alloy 셀프 모니터링 | - | - |

## 빠른 시작

### 사전 요구사항

- Docker & Docker Compose
- [Infisical CLI](https://infisical.com/docs/cli/overview) (`brew install infisical/get-cli/infisical`)
- (선택) JDK 24 + Gradle 8 (caro-demo 로컬 개발 시)

### 실행

```bash
# 1. Infisical 로그인 (최초 1회)
infisical login

# 2. 프로젝트 연결 (최초 1회)
infisical init

# 3. 전체 스택 실행
make up

# 4. 서비스 상태 확인
make ps
```

### 접속

| 서비스 | URL | 인증 |
|--------|-----|------|
| Grafana | http://localhost:3000 | admin / admin |
| Alloy UI | http://localhost:12345 | - |
| Caro-Demo | http://localhost:8080 | - |
| Caro-Demo Actuator | http://localhost:9090/actuator | - |

## 환경 변수

환경 변수는 Infisical에서 관리한다. `.env` 파일을 직접 생성하지 않는다.

### Infisical 경로 구조

| 경로 | 설명 |
|------|------|
| `/lgtm` | LGTM 스택 설정 (포트, Grafana 인증, Alertmanager 등) |
| `/garage` | Garage S3 오브젝트 스토리지 크리덴셜 |

`/lgtm`은 Secret Import로 `/garage`를 포함하므로 `make up` 한 번으로 모든 변수가 주입된다.

### /lgtm 경로

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `GRAFANA_ADMIN_USER` | `admin` | Grafana 관리자 계정 |
| `GRAFANA_ADMIN_PASSWORD` | - | Grafana 관리자 비밀번호 |
| `GRAFANA_PORT` | `3000` | Grafana 포트 |
| `LOKI_PORT` | `3100` | Loki HTTP API 포트 |
| `TEMPO_PORT` | `3200` | Tempo HTTP API 포트 |
| `TEMPO_OTLP_GRPC_PORT` | `4317` | Tempo OTLP gRPC 포트 |
| `MIMIR_PORT` | `9009` | Mimir HTTP API 포트 |
| `ALLOY_PORT` | `12345` | Alloy UI 포트 |
| `ALLOY_OTLP_HTTP_PORT` | `4318` | Alloy OTLP HTTP 수신 포트 |
| `ALERTMANAGER_PORT` | `9093` | Alertmanager 포트 |
| `CARO_DEMO_PORT` | `8080` | Caro-Demo 애플리케이션 포트 |
| `ENVIRONMENT` | `development` | 실행 환경 |

### /garage 경로

| 변수 | 설명 |
|------|------|
| `GARAGE_ENDPOINT` | Garage S3 엔드포인트 URL |
| `GARAGE_LOKI_ACCESS_KEY_ID` | Loki용 접근 키 ID |
| `GARAGE_LOKI_SECRET_ACCESS_KEY` | Loki용 시크릿 키 |
| `GARAGE_MIMIR_ACCESS_KEY_ID` | Mimir용 접근 키 ID |
| `GARAGE_MIMIR_SECRET_ACCESS_KEY` | Mimir용 시크릿 키 |
| `GARAGE_TEMPO_ACCESS_KEY` | Tempo용 접근 키 |
| `GARAGE_TEMPO_SECRET_KEY` | Tempo용 시크릿 키 |
| `GARAGE_METRIC_BEARER_TOKEN` | Garage 메트릭 수집용 Bearer 토큰 |

## 프로젝트 구조

```
o11y/
├── docker-compose.yaml          # 서비스 오케스트레이션
├── Makefile                     # 실행 명령어 래퍼 (infisical run 포함)
├── .infisical.json              # Infisical 프로젝트 연결 설정
├── .env.example                 # 환경 변수 참조 (Infisical 경로 안내)
├── config/
│   ├── alloy/
│   │   └── config.alloy         # OTLP 수신 및 라우팅 설정
│   ├── grafana/
│   │   ├── datasources.yaml     # 데이터소스 정의 (테넌트별)
│   │   ├── dashboards.yaml      # 대시보드 프로비저닝
│   │   └── dashboards/          # 대시보드 JSON 파일
│   ├── loki/
│   │   ├── loki.yaml            # 로그 수집 설정
│   │   └── runtime-config.yaml  # 테넌트별 오버라이드
│   ├── mimir/
│   │   ├── mimir.yaml           # 메트릭 저장 설정
│   │   ├── runtime-config.yaml  # 테넌트별 오버라이드
│   │   ├── rules/               # 알림 규칙 (테넌트별)
│   │   └── alertmanager-tenants/# Alertmanager 설정 (테넌트별)
│   └── tempo/
│       ├── tempo.yaml           # 트레이싱 설정
│       └── runtime-config.yaml  # 테넌트별 오버라이드
├── data/                        # 런타임 데이터 (Docker 볼륨)
└── caro-demo/                   # Spring Boot 데모 애플리케이션
```

## 설정 상세

### Alloy (데이터 수집기)

Alloy는 OTLP 프로토콜로 애플리케이션의 텔레메트리 데이터를 수신하여 각 백엔드로 라우팅한다.

- **수신**: OTLP gRPC (`:4317`), OTLP HTTP (`:4318`)
- **메모리 제한**: 512MB (스파이크 버퍼 20%)
- **배치 처리**: `tenant_id` 기반 테넌트별 배칭
- **재시도 정책**: 1초~30초 간격, 최대 5분

### Loki (로그)

- **스키마**: v13 TSDB (2025-01-01~)
- **보존 기간**: 168시간 (7일)
- **구조화된 메타데이터** 지원
- **패턴 인제스터** 활성화

### Tempo (트레이싱)

- **저장소**: 로컬 파일시스템
- **메트릭 생성기**: 서비스 그래프, 스팬 메트릭 (Mimir로 내보내기)
- **보존 기간**: 프로덕션 14일 / 스테이징 7일

### Mimir (메트릭)

- **타겟**: all, alertmanager
- **저장소**: 파일시스템
- **Ruler**: 1분마다 평가
- **테넌트별 제한**:
  - 프로덕션: 인제스션 10,000 req/s, 최대 시리즈 50,000
  - 스테이징: 인제스션 5,000 req/s, 최대 시리즈 20,000

### Grafana 데이터소스

| 데이터소스 | UID | 테넌트 |
|-----------|-----|--------|
| Loki-Staging | `loki-staging` | `caro-backend-staging` |
| Loki-Prod | `loki-prod` | `caro-backend-prod` |
| Tempo-Staging | `tempo-staging` | `caro-backend-staging` |
| Tempo-Prod | `tempo-prod` | `caro-backend-prod` |
| Mimir-Staging (기본) | `mimir-staging` | `caro-backend-staging` |
| Mimir-Prod | `mimir-prod` | `caro-backend-prod` |
| Mimir-Infra | `mimir-infra` | `infra-monitoring` |

상관 관계 설정:
- Tempo -> Loki: 트레이스에서 관련 로그 조회
- Tempo -> Mimir: 트레이스에서 관련 메트릭 조회
- Exemplar: 메트릭에서 트레이스로 연결

## 알림 (Alerting)

### 알림 규칙

`config/mimir/rules/` 디렉토리에 테넌트별 알림 규칙을 정의한다.

현재 스테이징 환경에 설정된 규칙:

| 규칙 | 조건 | 심각도 |
|------|------|--------|
| `CaroDemoDown` | 2분간 메트릭 수신 없음 | critical |
| `HighHTTPErrorRate` | 5분간 5xx 오류율 > 5% | warning |

### Slack 알림 설정

Slack 알림은 `config/mimir/alertmanager-tenants/` 의 Alertmanager 설정 파일에서 직접 웹훅 URL을 구성한다.

## Caro-Demo 애플리케이션

Spring Boot 4 + Spring Modulith 기반의 플래시카드 학습 플랫폼 데모.

### 기술 스택

- **Kotlin** 2.2 + **Java** 24
- **Spring Boot** 4.0.1 + **Spring Modulith** 2.0.1
- **OpenTelemetry** (spring-boot-starter-opentelemetry)
- **H2** (로컬 개발) / **MySQL** (프로덕션)

### 모듈 구성

| 모듈 | 설명 | 이벤트 |
|------|------|--------|
| **Workbook** | 덱 & 카드 관리 | - |
| **Review** | 간격 반복 학습 (SM-2) | `CardReviewedEvent` 발행 |
| **Ingestion** | AI 기반 카드 생성 | - |
| **Gamification** | 스트릭, 경험치, 뱃지 | `CardReviewedEvent` 구독 |
| **Analytics** | 일일 학습 통계 | `CardReviewedEvent` 구독 |
| **Notification** | 목표 달성 알림 | `CardReviewedEvent` 구독 |
| **Member** | 사용자 관리 | - |
| **Observatory** | 관측 가능성/모니터링 | - |
| **Shared** | 공통 인프라 (보안, 에러 처리, Swagger) | - |

자세한 내용은 [caro-demo/README.md](./caro-demo/README.md) 참조.

## 데이터 흐름

```
[Caro-Demo]
    |
    | OTLP HTTP (metrics, logs, traces)
    v
[Alloy] --- tenant_id 라우팅 (X-Scope-OrgID) ---
    |                    |                    |
    v                    v                    v
[Loki]              [Mimir]              [Tempo]
 로그 저장           메트릭 저장           트레이스 저장
    |                    |                    |
    +--------+-----------+--------------------+
             |
             v
         [Grafana]
     대시보드 & 알림
             |
             v
    [Alertmanager] --> [Slack]
```

## 운영

### 기본 명령어

```bash
make up                      # 스택 시작 (dev 환경)
make down                    # 스택 중지
make restart                 # 스택 재시작
make ps                      # 서비스 상태 확인
make env-check               # 주입되는 환경변수 확인
```

### 환경 전환

```bash
INFISICAL_ENV=staging make up
INFISICAL_ENV=prod make up
```

### 로그 확인

```bash
make logs                    # 전체 서비스 로그
make logs SVC=alloy          # 특정 서비스 로그
make logs SVC=caro-demo
```

### 서비스 재시작

```bash
docker compose restart alloy           # 특정 서비스 재시작
docker compose up -d --force-recreate alloy  # 설정 변경 후 재생성
```

### 데이터 초기화

```bash
# 전체 스택 중지 및 데이터 삭제
make down
rm -rf data/*

# 다시 시작
make up
```

### 리소스 모니터링

```bash
docker stats
```
