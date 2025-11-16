# Kotlin + Compose MPP 오프라인-퍼스트 노트 앱 (해커톤 친화 버전)

요약
- 목적: Kotlin Multiplatform + Compose로 Desktop과 Android에서 동작하는 간단한 오프라인-퍼스트 노트 앱 템플릿.
- 핵심 포인트: MPP UI 경험과 로컬 저장, 버튼 기반의 간단한 동기화(Last-Write-Wins)에 초점.

핵심 요구사항
- 반드시 구현된 항목
  - MPP UI (Desktop + Android) — Compose를 사용한 공통 UI/컴포넌트 재사용
  - 로컬 저장: 앱 재시작 후에도 데이터 유지 (로컬 JSON 파일 또는 플랫폼 저장소)
  - 동기화 버튼: 상단 Sync 버튼으로 간단한 Push/Pull 동작 (Last-Write-Wins, `updatedAt` 기준)

기능 스코프 (구현된 최소한)
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
  - deleted: Boolean (선택; 소프트 삭제)

아키텍처 개요
- 모듈 구조
  - `shared/` (commonMain): 모델, repository 인터페이스, 직렬화(kotlinx.serialization), 동기화 로직(공통)
  - `androidApp/`: Android 관련 코드 및 플랫폼 저장소 구현
  - `desktopApp/`: Desktop 관련 코드 및 플랫폼 저장소 구현
- 저장소
  - `LocalStore` 인터페이스를 통해 플랫폼별로 `List<Note>`를 JSON 직렬화하여 저장/복원
  - `NoteRepository`는 LocalStore를 래핑하여 CRUD와 캐시/영속화를 담당
- 동기화
  - `SyncManager`는 Push/Pull을 구현하며 LWW(last-write-wins) 정책으로 병합
  - 데모용 `FakeServer`(in-memory) 또는 같은 기기 내의 JSON 파일을 서버로 가정

프로젝트 구조(요약)
- `shared/` - 공통 모델 및 로직
- `androidApp/` - Android 관련 코드
- `desktopApp/` - Desktop 관련 코드
- `readme_images/` - 문서 이미지

빌드 및 실행 (macOS 기준)
- Desktop 실행
```bash
./gradlew :desktopApp:run
```
- Android 빌드(설치/실행은 IDE 또는 디바이스 필요)
```bash
./gradlew :androidApp:installDebug
```

테스트
```bash
./gradlew test
```

설계 노트(간단)
- 동기화는 교육/해커톤 목적의 단순 LWW 구현입니다. 고급 충돌 해결(이벤트 로그, CRDT 등)은 문서 수준으로만 다루며 필요시 확장 가능합니다.

---
(불필요한 라이선스, 개발 노트·구현 팁, 문의, 확장 아이디어 항목은 요청에 따라 제외되었습니다.)
