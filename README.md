## 🚀 3시간만에 만든 Keyword AI Example Generator

> **Spring Batch**와 **OpenAI GPT-4o-mini**를 활용하여 대용량 단어 데이터(Excel/CSV)를 분석하고,  
> 학습용 예문과 뜻을 생성하여 JSON으로 변환하는 **고성능 배치 프로그램**입니다.

---

## ⚡️ Performance (성능 리포트)

**Spring Batch의 멀티스레드(ThreadPool 10개)** 전략을 도입하여, I/O Bound 작업인 AI API 호출 속도를 극대화했습니다.

| 데이터 규모 | 총 소요 시간 | 건당 처리 속도 (Throughput) | 비고 |
| :--- | :--- | :--- | :--- |
| **10건** | 약 4.7초 | **0.47초 / 건** | 초기 예열 포함 |
| **100건** | 약 45.3초 | **0.45초 / 건** | 안정화 단계 |
| **10,000건** | **18분 3.3초** | **0.108초 / 건** | **🚀 풀 퍼포먼스 (최적화)** |

* **측정 환경:** M1/M2 Mac, Local Server
* **AI 모델:** GPT-4o-mini (Batch API 아님, 실시간 API 병렬 호출)

---

## 🛠 Tech Stack

* **Java:** 17
* **Framework:** Spring Boot 3.3.x, Spring Batch 5.x
* **AI:** OpenAI API (`gpt-4o-mini`)
* **Library:**
    * **EasyExcel:** 대용량 엑셀 스트리밍 읽기 (메모리 최적화)
    * **Lombok:** 보일러플레이트 제거
* **Build Tool:** Gradle

---

## 🏗 Architecture

### 1. Batch Flow
`Job` → `Step (Chunk 10)` → `Reader` → `Processor (Async)` → `Writer`

1.  **Reader (Synchronized):** `EasyExcel`을 사용하여 1만 건 이상의 데이터를 메모리 과부하 없이 스트리밍 방식으로 읽습니다.
2.  **Processor (Multi-threaded):** `ThreadPoolTaskExecutor` (Pool Size 10)를 사용하여 **GPT API를 동시에 10건씩 호출**합니다.
3.  **Writer (Synchronized):** 병렬 처리된 결과를 순서 꼬임 없이 하나의 JSON 파일로 안전하게 저장합니다.

### 2. Key Features
* **내결함성 (Fault Tolerance):**
    * 네트워크 불안정으로 API 호출 실패 시 **자동 3회 재시도(Retry)**.
    * 복구 불가능한 에러 발생 시, 배치를 중단하지 않고 **Skip** 후 별도 에러 로그 파일(`error_words.txt`)에 기록.
* **실시간 모니터링:**
    * 콘솔 로그를 통해 실시간 진행률(%)과 처리 건수를 확인 가능.
* **파일 관리:**
    * 요청마다 `UUID`를 부여하여 결과 파일 덮어쓰기 방지 및 이력 관리.

---

## 🚀 How to Run

### 1. Prerequisites
* Java 17 이상
* OpenAI API Key 발급

### 2. Setup
`src/main/resources/application.yml` 혹은 환경변수에 API Key를 등록합니다.

```yaml
gpt:
  secretKey: ${GPT_SECRET_KEY} # 시스템 환경변수 설정 필요
```

## Result

path : root/result/...json
<img width="1801" height="772" alt="image" src="https://github.com/user-attachments/assets/ffd87719-1390-49d3-87a4-85e1cc95ad94" />
<img width="984" height="239" alt="image" src="https://github.com/user-attachments/assets/3d06f584-fb57-4134-a3bd-ce0ff8138606" />

