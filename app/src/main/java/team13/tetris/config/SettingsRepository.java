package team13.tetris.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// 설정 파일 입출력 담당 클래스
public class SettingsRepository {
    private static final String FILE_PATH = "settings.json"; // 루트 기준

    public static void save(Settings settings) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            new Gson().toJson(settings, writer);
            System.out.println("[SettingsRepository] Saved to " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("[SettingsRepository] Save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Settings load() {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            return new Gson().fromJson(reader, Settings.class);
        } catch (IOException e) {
            System.out.println("[SettingsRepository] No settings.json found, using defaults");
            return new Settings(); // 기본값으로 새로 생성
        } catch (JsonSyntaxException e) {
            System.err.println("[SettingsRepository] Corrupted settings.json, using defaults: " + e.getMessage());
            return new Settings(); // 손상된 파일이면 기본값 사용
        }
    }
}
