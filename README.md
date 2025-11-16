# Kotlin + Compose MPP 오프라인-퍼스트 노트 앱 (해커톤 친화 버전)

요약
- 목적: Kotlin Multiplatform + Compose로 Desktop과 Android에서 동작하는 간단한 오프라인-퍼스트 노트 앱을 구현해보는 해커톤/학습용 템플릿.
- 핵심 포인트: MPP UI 경험과 로컬 저장, 버튼 기반의 간단한 동기화(Last-Write-Wins)에 초점.
- 구현 범위는 가볍게 잡아 핵심만 실제 동작하도록 함. 고급 동기화/충돌 해결은 설계 수준으로만 다룸.

핵심 요구사항 (이 레포 기준)
- 반드시 구현된 항목
  - MPP UI (Desktop + Android) — Compose를 사용한 공통 UI/컴포넌트 재사용
  - 로컬 저장: 앱 재시작 후에도 데이터 유지 (로컬 파일/JSON 또는 플랫폼 저장소)
  - 동기화 버튼: 상단 Sync 버튼으로 간단한 Push/Pull 동작 (Last-Write-Wins, `updatedAt` 기준)
- 설계·문서로만 다루는 항목
  - 이벤트 로그 기반 동기화(설계 문서)
  - 고급 충돌 해결 전략(CRDT/OT 등) — 아이디어/확장 노트

기능 스코프(실제 구현된 최소한)
1. 노트 CRUD
   - 노트 목록 보기, 노트 생성/수정/삭제
2. 오프라인-퍼스트
   - 로컬 JSON 파일 또는 플랫폼 파일 저장소에 직렬화하여 보관
   - 네트워크가 없어도 동작
3. 동기화 버튼(간단 LWW)
   - ‘서버’는 데모용으로 같은 기기 내의 `server.json` 또는 in-memory fake server
   - Push: 로컬의 수정된 노트를 서버의 노트와 `updatedAt` 비교 후 덮어쓰기
   - Pull: 서버의 노트를 로컬에 병합 (역시 `updatedAt` 기반)

데이터 모델 (예시)
- Note
  - id: String (UUID)
  - title: String
  - content: String
  - updatedAt: Long (epoch millis)
  - deleted: Boolean (선택; 소프트 삭제를 원할 때 사용)

아키텍처 개요
- 모듈 구조
  - `shared/` (commonMain): 모델, repository 인터페이스, 직렬화(kotlinx.serialization), 동기화 로직(공통)
  - `androidApp/`: Android Compose UI, 플랫폼 저장소 구현
  - `desktopApp/`: Desktop Compose UI, 플랫폼 저장소 구현
- 저장소 구현
  - LocalStore: 플랫폼별 구현을 통해 `List<Note>`를 JSON으로 저장/복원
  - SyncManager: 간단한 Push/Pull 및 LWW 병합 로직 제공
- UI
  - Compose MPP 공통 컴포넌트(노트 목록, 편집기, 상단바의 Sync 버튼)
  - 상태 관리는 ViewModel(또는 간단한 상태 홀더)을 사용

동기화(간단 버전) 상세
- 서버 모델: 데모 목적의 `server.json` 파일(같은 기기) 또는 인메모리 fake server
- Push (로컬 → 서버)
  - 로컬의 각 노트를 서버의 동일 ID 노트와 비교
  - 로컬의 `updatedAt`이 더 최신이면 서버에 덮어쓰기
- Pull (서버 → 로컬)
  - 서버의 노트를 로컬에 병합, `updatedAt` 기반으로 최신 항목 채택
- 충돌 정책
  - Last-Write-Wins(LWW): 타임스탬프가 최신인 쪽을 채택
  - 제약: 분산 시스템 환경에서 완전한 일관성/충돌 해결 보장은 아님 — 해커톤/데모 목적

고급 설계(문서 수준)
- 이벤트 로그 기반 동기화
  - 모든 로컬 변경을 이벤트로 기록하고, 이벤트를 서버와 교환하여 재생(replay)
  - 장점: 변경 이력 보존, 충돌 재현 가능
  - 단점: 로그 크기 관리, 스냅샷 전략 필요
- CRDT/OT 기반 충돌 해결
  - 텍스트 필드 수준의 CRDT 또는 OT 적용으로 병합 충돌 최소화
  - 구현 난이도 및 데이터 구조 복잡성 증가

프로젝트 구조(예시)
- `shared/` - 공통 모델 및 로직
- `androidApp/` - Android 관련 코드
- `desktopApp/` - Desktop 관련 코드
- `readme_images/` - 문서 이미지

빌드 및 실행 (macOS 기준)
- IntelliJ(또는 Android Studio)에서 프로젝트 열기
- Desktop 실행
  ```bash
  ./gradlew :desktopApp:run
  ```
- Android 실행(에뮬레이터 또는 연결된 디바이스 필요)
  ```bash
  ./gradlew :androidApp:installDebug
  ```
- 테스트
  ```bash
  ./gradlew test
  ```

개발 노트 & 구현 팁
- 로컬 저장: `kotlinx.serialization`으로 `List<Note>`를 JSON 직렬화하여 파일에 저장
- 동기화: `SyncManager.push()`와 `SyncManager.pull()`을 제공하고 내부에서 `updatedAt`으로 비교
- UI: Compose로 빠르게 화면 구성, 상태는 공통 ViewModel(또는 platform-specific ViewModel)으로 관리

확장 아이디어(우선순위)
1. SQLDelight/Realm 등으로 로컬 DB 전환(영속성/쿼리 개선)
2. 이벤트 로그 기반 동기화 구현(서버와 이벤트 교환)
3. CRDT 기반 병합(필드 수준 또는 문서 수준)
4. 간단한 REST 서버로 전환하여 여러 기기 동기화 지원

라이선스
- 기본: MIT (필요에 따라 변경)

문의
- 레포 유지보수자: 프로젝트 소유자(로컬 환경)

---
이 README는 해커톤/학습용으로 "핵심은 Kotlin + Compose MPP 사용 경험"에 초점을 맞추고, 복잡한 동기화는 최소한의 동작(LWW)만 구현하도록 설계되어 있습니다.

