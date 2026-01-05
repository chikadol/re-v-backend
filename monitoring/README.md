# 모니터링 설정 가이드

## Prometheus & Grafana 설정

### 1. Docker Compose로 실행

```bash
docker-compose up -d prometheus grafana
```

### 2. 접속 정보

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
  - 기본 계정: `admin` / `admin`

### 3. Grafana에서 Prometheus 데이터 소스 설정

1. Grafana에 로그인
2. Configuration > Data Sources > Add data source
3. Prometheus 선택
4. URL: `http://prometheus:9090` (Docker Compose 내부 네트워크)
   - 또는 `http://localhost:9090` (로컬 실행 시)
5. Save & Test

### 4. 주요 메트릭

Spring Boot Actuator가 제공하는 메트릭:
- `http_server_requests_seconds`: HTTP 요청 처리 시간
- `jvm_memory_used_bytes`: JVM 메모리 사용량
- `jvm_gc_pause_seconds`: GC 일시정지 시간
- `hikari_connections_active`: 데이터베이스 연결 풀 상태
- `cache_gets_total`: 캐시 히트/미스 통계

### 5. 로컬 개발 환경에서 실행

Docker Compose 없이 로컬에서 실행하는 경우:

1. Prometheus 설치:
```bash
brew install prometheus
```

2. Prometheus 실행:
```bash
prometheus --config.file=./monitoring/prometheus.yml
```

3. Grafana 설치:
```bash
brew install grafana
```

4. Grafana 실행:
```bash
brew services start grafana
```

### 6. Actuator 엔드포인트

- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Prometheus: http://localhost:8080/actuator/prometheus

