package team13.tetris.config;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// 설정 파일 입출력 담당 클래스
public class SettingsRepository {
    // 여러 위치에서 settings.json 파일을 찾기 시도
    private static final String[] POSSIBLE_PATHS = {
            "app/settings.json", // 프로젝트 루트에서 실행 시
            "settings.json", // app 폴더에서 실행 시
            "./app/settings.json" // 명시적 상대 경로
    };

    public static void save(Settings settings) {
        String savePath = POSSIBLE_PATHS[0]; // 기본 저장 경로
        try (FileWriter writer = new FileWriter(savePath)) {
            new Gson().toJson(settings, writer);
            System.out.println("[SettingsRepository] Saved to " + savePath);
        } catch (IOException e) {
            System.err.println("[SettingsRepository] Save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Settings load() {
        // 여러 경로 시도
        for (String path : POSSIBLE_PATHS) {
            try (FileReader reader = new FileReader(path)) {
                Settings loaded = new Gson().fromJson(reader, Settings.class);
                System.out.println("[SettingsRepository] Loaded from " + path);
                System.out.println("[SettingsRepository] Key mappings - Left: " + loaded.getKeyLeft() +
                        ", Right: " + loaded.getKeyRight() +
                        ", Down: " + loaded.getKeyDown() +
                        ", Rotate: " + loaded.getKeyRotate() +
                        ", Drop: " + loaded.getKeyDrop());
                return loaded;
            } catch (IOException e) {
                // 다음 경로 시도
                continue;
            }
        }

        // 모든 경로에서 실패
        System.out.println("[SettingsRepository] No settings.json found in any location, using defaults");
        return new Settings(); // 기본값으로 새로 생성
    }
}
