package team13.tetris.audio;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

// 테트리스 게임의 사운드를 관리하는 싱글톤 클래스
public class SoundManager {
    private static SoundManager instance;

    private MediaPlayer bgmPlayer;
    private Map<String, String> soundPaths;

    private double bgmVolume = 0.3; // BGM은 30% 소리
    private double effectVolume = 0.5; // 효과음은 50% 소리

    private boolean bgmEnabled = true; // BGM이 켜져있음
    private boolean effectEnabled = true; // 효과음이 켜져있음

    // 게임 BGM 번갈아 재생용 변수
    private int currentGameBgmIndex = 0; // 1 또는 2
    private boolean isGameBgmMode = false; // 게임 BGM 모드인지 확인
    private String currentPlayingBgmId = null; // 현재 재생 중인 BGM ID 추적

    // 효과음 중복 재생 방지용
    private Map<String, MediaPlayer> activeEffects = new HashMap<>();

    private SoundManager() {
        soundPaths = new HashMap<>();
        initializeSoundPaths();
    }

    // SoundManager의 싱글톤 인스턴스를 반환합니다.
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // 사운드 파일 경로를 초기화합니다.
    private void initializeSoundPaths() {
        // BGM - 메뉴 화면용 (메인 메뉴, 설정 등)
        soundPaths.put("menu_bgm", "/katyusha.mp3");

        // BGM - 게임 내 플레이용
        soundPaths.put("game_bgm_1", "/tetris.mp3");
        soundPaths.put("game_bgm_2", "/loginska.mp3");

        // 효과음
        soundPaths.put("rotate", "/rotate.mp3");
        soundPaths.put("line_clear", "/line-clear.mp3");
        soundPaths.put("hard_drop", "/hard-drop.mp3");
        soundPaths.put("win", "/win.mp3");
        soundPaths.put("lose", "/lose.mp3");
        soundPaths.put("time", "/time.mp3");
    }

    // 메뉴 화면용 BGM 재생
    public void playMenuBGM() {
        isGameBgmMode = false; // 메뉴 모드로 전환

        // 이미 메뉴 BGM이 재생 중이면 그대로 유지
        if ("menu_bgm".equals(currentPlayingBgmId) && bgmPlayer != null) {
            return; // 계속 재생
        }

        playBGM("menu_bgm", true); // 무한 반복
    }

    // 게임 플레이 BGM 재생 (첫 시작은 랜덤, 이후 번갈아가며 재생)
    public void playGameBGM() {
        isGameBgmMode = true; // 게임 BGM 모드 활성화

        // 첫 시작이면 랜덤으로 선택
        if (currentGameBgmIndex == 0) {
            currentGameBgmIndex = Math.random() < 0.5 ? 1 : 2;
        }

        playGameBGMByIndex(currentGameBgmIndex);
    }

    // 특정 게임 BGM 재생 (번갈아가기 시작점 설정)
    public void playGameBGM(int index) {
        isGameBgmMode = true;
        if (index == 1 || index == 2) {
            currentGameBgmIndex = index;
            playGameBGMByIndex(index);
        } else {
            playGameBGM(); // 잘못된 인덱스면 랜덤
        }
    }

    // 게임 BGM을 인덱스로 재생하고, 끝나면 다음 BGM으로 전환
    private void playGameBGMByIndex(int index) {
        String bgmId = "game_bgm_" + index;
        playBGM(bgmId, false); // 한 번만 재생 (끝나면 다음 곡으로)
    }

    // BGM을 재생합니다
    private void playBGM(String soundId, boolean loop) {
        if (!bgmEnabled) return;

        // 같은 BGM이 이미 재생 중이면 그대로 유지
        if (soundId.equals(currentPlayingBgmId) && bgmPlayer != null) {
            return; // 계속 재생
        }

        String path = soundPaths.get(soundId);
        if (path == null) {
            System.err.println("BGM을 찾을 수 없음: " + soundId);
            return;
        }

        try {
            // 이전 BGM 정리
            stopBGM();

            // 새 BGM 재생
            Media media = new Media(getClass().getResource(path).toExternalForm());
            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setVolume(bgmVolume);
            currentPlayingBgmId = soundId; // 현재 재생 중인 BGM ID 저장

            if (loop) {
                // 무한 반복 모드
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } else {
                // 한 번만 재생 후 다음 곡으로 (게임 BGM 모드)
                bgmPlayer.setOnEndOfMedia(
                        () -> {
                            if (isGameBgmMode) {
                                // 다음 BGM으로 전환 (1 -> 2, 2 -> 1)
                                currentGameBgmIndex = (currentGameBgmIndex == 1) ? 2 : 1;
                                playGameBGMByIndex(currentGameBgmIndex);
                            }
                        });
            }

            bgmPlayer.play();
        } catch (Exception e) {
            System.err.println("BGM 재생 실패: " + soundId + " - " + e.getMessage());
        }
    }

    // 현재 재생 중인 BGM을 정지합니다.
    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
        currentPlayingBgmId = null; // 재생 중인 BGM ID 초기화
    }

    // 현재 재생 중인 BGM을 일시정지합니다.
    public void pauseBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.pause();
        }
    }

    // 일시정지된 BGM을 재개합니다.
    public void resumeBGM() {
        if (bgmPlayer != null && bgmEnabled) {
            bgmPlayer.play();
        }
    }

    // 효과음을 재생합니다.
    public void playEffect(String soundId) {
        playEffect(soundId, false);
    }

    // 효과음을 재생합니다 (forceRestart 옵션)
    public void playEffect(String soundId, boolean forceRestart) {
        if (!effectEnabled) return;

        // 이미 재생 중인 같은 효과음 확인
        MediaPlayer existingPlayer = activeEffects.get(soundId);
        if (existingPlayer != null) {
            if (existingPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                if (!forceRestart) {
                    return; // 재생 중이면 스킵 (forceRestart가 false일 때)
                }
                // forceRestart가 true면 기존 재생 중단
                existingPlayer.stop();
            }
            // 재생이 끝났거나 멈춘 플레이어는 정리
            existingPlayer.dispose();
            activeEffects.remove(soundId);
        }

        String path = soundPaths.get(soundId);
        if (path == null) {
            System.err.println("효과음을 찾을 수 없음: " + soundId);
            return;
        }

        try {
            Media media = new Media(getClass().getResource(path).toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(effectVolume);

            // 활성 효과음으로 등록
            activeEffects.put(soundId, player);

            player.play();

            // 재생 완료 후 자동으로 메모리 해제
            player.setOnEndOfMedia(
                    () -> {
                        player.stop();
                        player.dispose();
                        activeEffects.remove(soundId); // 활성 목록에서 제거
                    });
        } catch (Exception e) {
            System.err.println("효과음 재생 실패: " + soundId + " - " + e.getMessage());
        }
    }

    // BGM 볼륨을 설정합니다.
    public void setBGMVolume(double volume) {
        this.bgmVolume = Math.max(0.0, Math.min(1.0, volume));
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(this.bgmVolume);
        }
    }

    // 효과음 볼륨을 설정합니다.
    public void setEffectVolume(double volume) {
        this.effectVolume = Math.max(0.0, Math.min(1.0, volume));
    }

    // BGM 활성화/비활성화를 설정합니다.
    public void setBGMEnabled(boolean enabled) {
        this.bgmEnabled = enabled;
        if (!enabled) {
            stopBGM();
        }
    }

    // 효과음 활성화/비활성화를 설정합니다.
    public void setEffectEnabled(boolean enabled) {
        this.effectEnabled = enabled;
    }

    // 현재 BGM 볼륨을 반환합니다.
    public double getBGMVolume() {
        return bgmVolume;
    }

    // 현재 효과음 볼륨을 반환합니다.
    public double getEffectVolume() {
        return effectVolume;
    }

    // BGM이 활성화되어 있는지 확인합니다.
    public boolean isBGMEnabled() {
        return bgmEnabled;
    }

    // 효과음이 활성화되어 있는지 확인합니다.
    public boolean isEffectEnabled() {
        return effectEnabled;
    }

    // 모든 사운드 리소스를 정리합니다. (게임 종료 시 호출)
    public void cleanup() {
        stopBGM();
        soundPaths.clear();
    }
}
