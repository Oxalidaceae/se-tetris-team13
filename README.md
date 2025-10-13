# SEOULTECH SE-13 TETRIS

**서울과학기술대학교 컴퓨터공학과 팀 13 테트리스 프로젝트**

JavaFX를 사용한 클래식 테트리스 게임입니다.

## 🎮 게임 소개

이 프로젝트는 Java와 JavaFX를 사용하여 구현된 테트리스 게임입니다. 클래식한 테트리스의 모든 기능을 포함하며, 여러 창의적인 기능들도 지원합니다!

## ✨ 주요 기능

### 🎯 게임플레이

- 클래식 테트리스 게임 메커니즘
- 실시간 점수 및 레벨 표시
- 일시정지 및 재개 기능
- 게임 오버 처리

### ⚙️ 설정 관리

- **창 크기 조절**: Small(400x500), Medium(600x700), Large(800x900)
- **색맹 모드**: 색각이상자를 위한 고대비 색상 테마
- **키 설정**: 모든 게임 키를 사용자 정의 가능
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
- **Data Format**: JSON (설정 및 점수 저장)
- **Testing**: JUnit 5

## 📁 프로젝트 구조

```
se-tetris-team13/
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
│   │   │   │   │   ├── GameManager.java        # 게임 로직 관리
│   │   │   │   │   ├── GameState.java          # 게임 상태 정의
│   │   │   │   │   └── Timer.java              # 게임 타이머
│   │   │   │   ├── input/
│   │   │   │   │   └── KeyInputHandler.java    # 키 입력 처리
│   │   │   │   └── scenes/
│   │   │   │       ├── MainMenuScene.java      # 메인 메뉴
│   │   │   │       ├── GameScene.java          # 게임 화면
│   │   │   │       ├── SettingsScene.java      # 설정 화면
│   │   │   │       ├── ScoreboardScene.java    # 점수판
│   │   │   │       ├── KeySettingsScene.java   # 키 설정
│   │   │   │       └── GameOverScene.java      # 게임 오버
│   │   │   └── resources/
│   │   │       ├── application.css             # 기본 스타일
│   │   │       └── colorblind.css              # 색맹 모드 스타일
│   │   └── test/                               # 단위 테스트
│   ├── build.gradle                            # 빌드 설정
│   ├── settings.json                           # 사용자 설정 파일
│   └── scores.txt                              # 점수 데이터 파일
└── README.md
```

## 🚀 실행 방법

### 요구사항

- Java 17 이상
- Gradle (wrapper 포함)

### 실행 단계

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

## 🎯 게임 조작법

### 기본 키 설정

- **이동**: A (왼쪽), D (오른쪽)
- **회전**: J
- **소프트 드롭**: S
- **하드 드롭**: K
- **일시정지**: P
- **종료**: ESC

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

## 👥 팀 구성

**Team 13 - 서울과학기술대학교 컴퓨터공학과**

각 팀원은 다음 모듈을 담당하여 개발했습니다:

- 게임 로직 & 블럭 시스템
- 조작 입력 & 테스트 코드
- UI/화면 구성 & 설정
- 점수 시스템 & 스코어보드

## 📄 라이선스

MIT License - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

**즐거운 테트리스 게임을 즐기세요! 🎮**
