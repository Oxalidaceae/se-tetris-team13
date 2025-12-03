[![Java CI with Gradle - test automation](https://github.com/Oxalidaceae/se-tetris-team13/actions/workflows/test.yml/badge.svg)](https://github.com/Oxalidaceae/se-tetris-team13/actions/workflows/test.yml)
[![Release Windows Executable](https://github.com/Oxalidaceae/se-tetris-team13/actions/workflows/release-windows.yml/badge.svg)](https://github.com/Oxalidaceae/se-tetris-team13/actions/workflows/release-windows.yml)

# SEOULTECH SE-13 TETRIS

**서울과학기술대학교 컴퓨터공학과 팀 13 테트리스 프로젝트**

JavaFX를 사용한 클래식 테트리스 게임입니다.

## 📥 다운로드

### Windows 사용자 (권장)

**Java 설치 없이 바로 실행 가능한 Windows 실행 파일을 다운로드하세요!**

👉 [최신 릴리즈 다운로드](https://github.com/Oxalidaceae/se-tetris-team13/releases/latest)

1. `Tetris-Windows-v*.*.*.zip` 파일 다운로드
2. 압축 해제
3. `Tetris.exe` 더블클릭으로 실행

> ✅ JRE가 포함되어 있어 별도 설치 불필요  
> ✅ 아이콘이 적용된 실행 파일  
> ✅ Windows 10 이상 지원

## 🎮 게임 소개

이 프로젝트는 Java와 JavaFX를 사용하여 구현된 테트리스 게임입니다. 클래식한 테트리스의 모든 기능을 포함하며, 여러 창의적인 기능들도 지원합니다!

## ✨ 주요 기능

### 🎯 게임 모드

#### 싱글 플레이 모드
- **Classic Mode**: 클래식 테트리스 게임 메커니즘
- **Item Mode**: 특수 아이템 블록으로 전략적 플레이
- **Timer Mode**: 제한 시간 내 최고 점수 달성

#### 멀티플레이 모드
- **Local Versus (1v1)**: 같은 컴퓨터에서 2인 대전
- **Online PVP (1v1)**: 네트워크로 1:1 대전
- **Squad PVP (3인)**: 네트워크로 3인 배틀로얄

### 🎮 게임플레이 기능

- 실시간 점수 및 레벨 표시
- 일시정지 및 재개 기능
- 다음 블록 미리보기
- 고스트 피스 (블록 착지 위치 표시)
- 게임 오버 처리

### ⚙️ 설정 관리

- **창 크기 조절**: Small(400x500), Medium(600x700), Large(800x900)
- **색맹 모드**: 색각이상자를 위한 고대비 색상 테마 및 패턴
- **키 설정**: Player 1, Player 2 모든 게임 키를 개별 설정 가능
- **폰트**: 레트로 게임 느낌의 고정폭 폰트 (Consolas, Courier New)
- **설정 지속성**: JSON 파일을 통한 설정 자동 저장/로드

### 📊 점수 시스템

- 하이스코어 저장 및 관리
- 점수 순위표 (상위 10개)
- 점수 통계 (총 게임 수, 최고 점수, 평균 점수)
- 점수 데이터 초기화 기능

### 🎨 사용자 인터페이스

- 직관적인 메뉴 시스템
- 키보드 내비게이션 지원 (방향키로 버튼 이동)
- 반응형 레이아웃
- 접근성 고려 설계

## 🛠️ 기술 스택

- **Language**: Java 17
- **UI Framework**: JavaFX 17.0.12
- **Build Tool**: Gradle 9.0.0
- **Packaging**: jpackage (Badass Runtime Plugin 1.13.0)
- **Data Format**: JSON (설정 및 점수 저장)
- **Testing**: JUnit 5, TestFX (Monocle)
- **CI/CD**: GitHub Actions

## 📁 프로젝트 구조

```
se-tetris-team13/
├── .github/
│   └── workflows/
│       ├── test.yml                            # CI 테스트 자동화
│       └── release-windows.yml                 # Windows 배포 자동화
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/team13/tetris/
│   │   │   │   ├── App.java                    # 메인 애플리케이션
│   │   │   │   ├── SceneManager.java           # 씬 관리자
│   │   │   │   ├── config/
│   │   │   │   │   ├── Settings.java           # 설정 모델
│   │   │   │   │   └── SettingsRepository.java # 설정 저장/로드
│   │   │   │   ├── data/
│   │   │   │   │   └── ScoreBoard.java         # 점수 관리
│   │   │   │   ├── game/
│   │   │   │   │   ├── logic/
│   │   │   │   │   │   └── GameEngine.java     # 게임 엔진
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Board.java          # 게임 보드
│   │   │   │   │   │   └── Tetromino.java      # 테트로미노 블록
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── GameController.java         # 싱글 게임 컨트롤러
│   │   │   │   │   │   ├── VersusGameController.java   # 로컬 대전 컨트롤러
│   │   │   │   │   │   ├── NetworkGameController.java  # 온라인 1v1 컨트롤러
│   │   │   │   │   │   ├── SquadGameController.java    # Squad 3인 컨트롤러
│   │   │   │   │   │   └── GameStateListener.java      # 게임 상태 리스너
│   │   │   │   │   └── util/
│   │   │   │   │       └── AsciiBoardRenderer.java     # ASCII 보드 렌더러
│   │   │   │   ├── input/
│   │   │   │   │   └── KeyInputHandler.java    # 키 입력 처리
│   │   │   │   ├── network/
│   │   │   │   │   ├── client/
│   │   │   │   │   │   └── TetrisClient.java   # 네트워크 클라이언트
│   │   │   │   │   ├── server/
│   │   │   │   │   │   ├── TetrisServer.java   # 1v1 서버
│   │   │   │   │   │   └── TetrisSquadServer.java  # Squad 서버
│   │   │   │   │   ├── protocol/
│   │   │   │   │   │   └── *Message.java       # 네트워크 메시지 프로토콜
│   │   │   │   │   └── listener/
│   │   │   │   │       └── *MessageListener.java   # 메시지 리스너
│   │   │   │   └── scenes/
│   │   │   │       ├── MainMenuScene.java      # 메인 메뉴
│   │   │   │       ├── ModeSelectionScene.java # 모드 선택
│   │   │   │       ├── MultiModeSelectionScene.java  # 멀티 모드 선택
│   │   │   │       ├── GameScene.java          # 싱글 게임 화면
│   │   │   │       ├── VersusGameScene.java    # 로컬 대전 화면
│   │   │   │       ├── NetworkGameScene.java   # 온라인 1v1 화면
│   │   │   │       ├── SquadGameScene.java     # Squad 게임 화면
│   │   │   │       ├── NetworkLobbyScene.java  # 온라인 대기실
│   │   │   │       ├── SquadNetworkLobbyScene.java  # Squad 대기실
│   │   │   │       ├── GameOverScene.java      # 게임 오버
│   │   │   │       ├── SquadResultScene.java   # Squad 결과 화면
│   │   │   │       ├── SettingsScene.java      # 설정 화면
│   │   │   │       ├── KeySettingsScene.java   # 키 설정
│   │   │   │       ├── ScoreboardScene.java    # 점수판
│   │   │   │       └── ConfirmScene.java       # 확인 다이얼로그
│   │   │   └── resources/
│   │   │       ├── application.css             # 기본 스타일
│   │   │       ├── colorblind.css              # 색맹 모드 스타일
│   │   │       └── icon.ico                    # Windows 아이콘
│   │   └── test/
│   │       └── java/team13/tetris/             # 단위 테스트
│   │           ├── config/
│   │           ├── data/
│   │           ├── game/
│   │           │   ├── controller/
│   │           │   └── logic/
│   │           └── scenes/
│   ├── build.gradle                            # 빌드 설정 (jpackage 포함)
│   ├── settings.json                           # 사용자 설정 파일
│   └── scores.txt                              # 점수 데이터 파일
├── gradle/
│   └── wrapper/                                # Gradle wrapper
├── gradlew                                     # Gradle wrapper 스크립트 (Unix)
├── gradlew.bat                                 # Gradle wrapper 스크립트 (Windows)
├── settings.gradle                             # Gradle 설정
└── README.md
```

## 🚀 실행 방법

### Windows 사용자 (권장)

**가장 쉬운 방법!** [릴리즈 페이지](https://github.com/Oxalidaceae/se-tetris-team13/releases/latest)에서 Windows 실행 파일을 다운로드하세요.

#### 최소 요구사항

- Windows 10 이상의 운영체제
- 500MB 이상의 저장공간
- 1GB 이상의 RAM
- 1.2GHz 이상의 프로세서

### 개발자 또는 다른 OS 사용자 전용 설명명

#### 요구사항

- Java 17 이상
- Gradle (wrapper 포함)

#### 실행 단계

1. **저장소 클론**

   ```bash
   git clone https://github.com/Oxalidaceae/se-tetris-team13.git
   cd se-tetris-team13
   ```

2. **애플리케이션 실행**

   ```bash
   # Windows
   .\gradlew run

   # macOS/Linux
   ./gradlew run
   ```

3. **빌드 (선택사항)**

   ```bash
   # Windows
   .\gradlew build

   # macOS/Linux
   ./gradlew build
   ```

4. **Windows 실행 파일 생성 (선택사항)**

   ```bash
   # Windows에서만 가능
   .\gradlew jpackage

   # 생성 위치: app/build/jpackage/Tetris/Tetris.exe
   ```

## 🎯 게임 조작법

### 기본 키 설정 (Player 1)

- **왼쪽 이동**: A
- **오른쪽 이동**: D
- **소프트 드롭**: S
- **회전**: W
- **하드 드롭**: SPACE
- **일시정지**: ESC

### Player 2 키 설정 (로컬 대전 모드)

- **왼쪽 이동**: ← (LEFT)
- **오른쪽 이동**: → (RIGHT)
- **소프트 드롭**: ↓ (DOWN)
- **회전**: ↑ (UP)
- **하드 드롭**: / (SLASH)

> 💡 모든 키는 설정 메뉴에서 변경 가능합니다.

### 메뉴 내비게이션

- **방향키**: 버튼 간 이동
- **Enter**: 선택
- **Tab**: 다음 버튼으로 이동

## 🧪 테스트 실행

```bash
# 모든 테스트 실행
.\gradlew test

# 테스트 결과 확인
.\gradlew test --info
```

테스트 커버리지:

- 게임 로직 (GameEngine, Board, Tetromino)
- 설정 관리 (Settings, SettingsRepository)
- 점수 시스템 (ScoreBoard)
- 네트워크 프로토콜 (Message 클래스들)
- UI 씬 (GameScene, VersusGameScene 등)
- 컨트롤러 (JavaFX headless 테스트)

## 🚢 배포

### 자동 배포 (GitHub Actions)

`main` 브랜치에 버전 태그를 푸시하면 자동으로 Windows 실행 파일이 빌드되고 GitHub Release에 업로드됩니다.

```bash
# develop에서 main으로 병합
git checkout main
git merge develop

# 버전 태그 생성 및 푸시
git tag v1.0.0
git push origin main --tags
```

워크플로우가 자동으로:

1. 프로젝트 빌드 및 테스트
2. jpackage로 Windows 실행 파일 생성
3. 압축 및 GitHub Release 생성
4. 설치 가이드와 함께 파일 업로드

### 수동 배포

로컬에서 Windows 실행 파일을 직접 생성할 수 있습니다:

```bash
.\gradlew jpackage
```

생성된 파일 위치: `app/build/jpackage/Tetris/`

## 🌐 네트워크 모드

### 온라인 PVP (1v1)
- 호스트 또는 참가로 게임 시작
- 실시간 보드 상태 동기화
- 공격 라인 전송 및 수신
- 게임 모드 선택 (Normal/Item/Timer)
- 일시정지/재개 동기화

### Squad PVP (3인)
- 3인 배틀로얄 모드
- 무작위 공격 대상 선택
- 탈락 순서 추적
- 최종 순위 표시

### 네트워크 기능
- 로비 시스템 (준비 상태 확인)
- 5초 카운트다운 후 게임 시작
- 연결 끊김 감지 및 처리
- 최근 접속 IP 저장

## 👥 팀 구성

**Team 13 - 서울과학기술대학교 컴퓨터공학과**

각 팀원은 다음 모듈을 담당하여 개발했습니다:

- 게임 로직 & 블록 시스템
- 조작 입력 & 테스트 코드
- UI/화면 구성 & 설정
- 점수 시스템 & 스코어보드
- 네트워크 시스템 & 멀티플레이

## 📄 라이선스

MIT License - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

**즐거운 테트리스 게임을 즐기세요! 🎮**
