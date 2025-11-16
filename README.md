# Kotlin + Compose MPP 활용한 노트 앱 (해커톤 친화 버전)

요약
- 목적: Kotlin Multiplatform + Compose로 Desktop과 Android에서 동작하는 간단한 오프라인-퍼스트 노트 앱 템플릿.
- 핵심 포인트: MPP UI 경험과 로컬 저장, 버튼 기반의 간단한 동기화(Last-Write-Wins)에 초점.

핵심 요구사항
- 반드시 구현된 항목
  - MPP UI (Desktop + Android) — Compose를 사용한 공통 UI/컴포넌트 재사용
  - 로컬 저장: 앱 재시작 후에도 데이터 유지 (로컬 JSON 파일 또는 플랫폼 저장소)
  - 동기화 버튼: 상단 Sync 버튼으로 간단한 Push/Pull 동작 (Last-Write-Wins, `updatedAt` 기준)

기능 스코프
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

## 빌드 및 실행

### 사전 요구사항
- JDK 17 이상
- Android Studio (Android 앱용)
  - Android SDK 설치 필요
  - `local.properties` 파일에 SDK 경로 설정:
    ```properties
    sdk.dir=/Users/사용자명/Library/Android/sdk
    ```
  - 또는 `ANDROID_HOME` 환경 변수 설정
- Xcode (iOS 앱용, macOS만)
  - Xcode Command Line Tools 설치 필요
  - `xcode-select --install` 실행

### 1. Desktop 앱 실행 (macOS / Windows / Linux)

#### macOS / Linux
```bash
./gradlew :desktopApp:run
```

#### Windows
```bash
gradlew.bat :desktopApp:run
```

**테스트 항목:**
- 노트 추가/수정/삭제
- Sync 버튼 동작
- 앱 재시작 후 데이터 유지 (`~/.compose_notes/notes.json`)

### 2. Android 앱 실행

#### 방법 1: Gradle로 직접 설치
```bash
# Android 에뮬레이터 또는 실제 기기가 연결되어 있어야 함
./gradlew :androidApp:installDebug
```

#### 방법 2: Android Studio에서 실행
1. Android Studio에서 프로젝트 열기
2. `androidApp` 모듈 선택
3. 에뮬레이터 또는 실제 기기 선택
4. Run 버튼 클릭 (⌘R 또는 Shift+F10)

**테스트 항목:**
- 노트 추가/수정/삭제
- Sync 버튼 동작
- 앱 재시작 후 데이터 유지 (앱 내부 저장소)

### 3. iOS 앱 실행 (macOS만 가능)

#### 방법 1: Xcode에서 실행
```bash
# Xcode 프로젝트 열기
open iosApp/iosApp.xcodeproj
```

Xcode에서:
1. 시뮬레이터 선택 (예: iPhone 15)
2. Run 버튼 클릭 (⌘R)
3. 또는 실제 iPhone 연결 후 선택

#### 방법 2: 터미널에서 빌드
```bash
# iOS 프레임워크 빌드
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

**테스트 항목:**
- 노트 추가/수정/삭제
- Sync 버튼 동작
- 앱 재시작 후 데이터 유지

### 4. 모든 플랫폼 빌드 확인

```bash
# Desktop 빌드
./gradlew :desktopApp:build

# Android 빌드
./gradlew :androidApp:assembleDebug

# iOS 프레임워크 빌드 (macOS만)
./gradlew :shared:iosX64Binaries
./gradlew :shared:iosArm64Binaries
```

### 테스트

#### 단위 테스트 실행
```bash
# Desktop 테스트 (JVM)
./gradlew :shared:desktopTest

# 모든 플랫폼 테스트 (Android SDK 필요)
./gradlew test
```

#### 테스트 커버리지
현재 구현된 테스트:
- ✅ **NoteRepositoryTest**: CRUD 기능, 정렬, 소프트 삭제
- ✅ **SyncManagerTest**: Push/Pull, Last-Write-Wins 정책, 충돌 해결
- ✅ **FakeServerTest**: 서버 저장소 기능, LWW 정책

테스트 파일 위치: `shared/src/commonTest/kotlin/com/example/notes/`

## 현재 테스트 상태

### ✅ Desktop (macOS)
- **상태**: 정상 작동
- **실행 방법**: `./gradlew :desktopApp:run`
- **테스트 완료 항목**:
  - ✅ 노트 추가/수정/삭제
  - ✅ Sync 버튼 동작
  - ✅ 데이터 영속화 (`~/.compose_notes/notes.json`)

### ⚠️ Android
- **상태**: Android SDK 설정 필요
- **설정 방법**:
  1. Android Studio 설치
  2. `local.properties` 파일 생성:
     ```properties
     sdk.dir=/Users/사용자명/Library/Android/sdk
     ```
  3. 에뮬레이터 실행 또는 실제 기기 연결
  4. `./gradlew :androidApp:installDebug` 실행

### ⚠️ iOS
- **상태**: Xcode 설정 필요
- **설정 방법**:
  1. Xcode 설치
  2. Command Line Tools 설치: `xcode-select --install`
  3. `open iosApp/iosApp.xcodeproj`로 Xcode에서 열기
  4. 시뮬레이터 또는 실제 iPhone에서 실행

### 📝 Windows Desktop
- **상태**: Windows PC에서 테스트 필요
- **실행 방법**: Windows PC에서 `gradlew.bat :desktopApp:run`
