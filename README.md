## 🚀 키워드를 이용한 ai 예제 생성기

> **Spring Batch**와 **OpenAI gpt-4o-mini **를 활용하여 대용량 단어 데이터(Excel/CSV)를 분석하고,  
> 학습용 예문과 뜻을 생성하여 JSON으로 변환하는 **고성능 배치 프로그램**입니다.

---

## ⚡️ Performance (성능 리포트)

**Spring Batch의 멀티스레드(ThreadPool 10개)** 전략을 도입하여, I/O Bound 작업인 AI API 호출 속도를 극대화했습니다.

| 데이터 규모 | 총 소요 시간 | 건당 처리 속도 (Throughput) | 비고 |
| :--- | :--- | :--- | :--- |
| **10건** | 약 4.7초 | **0.47초 / 건** | 초기 예열 포함 |
| **100건** | 약 45.3초 | **0.45초 / 건** | 안정화 단계 |
| **2700건** |약 18분 3초 | **0.4초 / 건** | **🚀 풀 퍼포먼스 (최적화)** |

* **측정 환경:** M1/M2 Mac, Local Server
* **AI 모델:** gpt-4o-mini (Batch API 아님, 실시간 API 병렬 호출)

---

## 🛠 Tech Stack

* **Java:** 17
* **Framework:** Spring Boot 3.3.x, Spring Batch 5.x
* **AI:** OpenAI API (test :`gpt-4o-mini`)
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
    * 요청마다 `timestamp`를 부여하여 결과 파일 덮어쓰기 방지 및 이력 관리.

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
### 3. ⚡️ Execution Guide (Postman)

서버를 실행하고 **Postman**을 사용하여 파일을 업로드하면 배치가 즉시 실행됩니다.

**Step 1. 서버 실행 (Start Server)**
* IntelliJ에서 `AiWordApplication`을 실행합니다 (Run).
* 콘솔 로그에 아래 문구가 뜨면 준비 완료입니다.
  > `✅ Started AiWordApplication in ... seconds`

**Step 2. API 요청 설정 (Configure Request)**
Postman을 열고 아래 순서대로 설정합니다.

1.  **Method:** `POST`
2.  **URL:** `http://localhost:8080/api/batch/upload`
3.  **Body 설정:**
    * 탭 메뉴에서 **[Body]** → **[form-data]** 선택
    * **Key:** `file` 입력
        * *(⚠️ 중요: Key 이름 위에 마우스를 올리고, 우측 드롭다운에서 **Text**를 **File**로 변경해야 합니다)*
    * **Value:** `Select Files` 버튼을 눌러 **`n1.csv`** 파일 선택

**Step 3. 전송 및 확인 (Send & Check)**
* 우측 상단의 **[Send]** 버튼을 클릭합니다.
* **결과:** `200 OK` 응답 확인 후, IntelliJ 콘솔에서 **실시간 배치 로그**를 모니터링하세요.



## 📂 Result & Output
배치 작업이 완료되면 결과 파일은 아래 경로에 자동으로 생성됩니다.

### 📁 Directory Structure
```bash
root
└── result
    ├── keyword_data_20241203_011742.json  # ✅ 변환 완료된 결과 파일 (timestamp 포함)
    └── error_words.txt             # ⚠️ 처리 실패한 단어 목록 (Skip Log)
```

### 📸 Execution Screenshots
1. JSON Output Example
<img src="https://github.com/user-attachments/assets/ffd87719-1390-49d3-87a4-85e1cc95ad94" width="100%" alt="JSON Output Screenshot">
2. Batch Console Log
<img src="https://github.com/user-attachments/assets/3d06f584-fb57-4134-a3bd-ce0ff8138606" width="100%" alt="Console Log Screenshot">
