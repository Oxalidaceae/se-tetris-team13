package team13.tetris.game.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.Queue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 최대 10줄 공격 제한 기능 테스트 헬퍼 메서드 로직을 직접 테스트 */
class AttackLimitTest {

    // addAttackWithLimit 로직을 테스트용으로 복제
    private void addAttackWithLimit(Queue<int[][]> incomingQueue, int[][] newPattern) {
        // 현재 큐에 있는 총 줄 수 계산
        int currentTotalLines = 0;
        for (int[][] pattern : incomingQueue) {
            currentTotalLines += pattern.length;
        }

        int newLines = newPattern.length;

        // 이미 10줄이 차 있는 경우 무시
        if (currentTotalLines >= 10) {
            return;
        }

        // 추가하면 10줄을 넘는 경우, 제일 아래쪽 부분을 잘라냄
        if (currentTotalLines + newLines > 10) {
            int allowedLines = 10 - currentTotalLines;
            int[][] trimmedPattern = new int[allowedLines][newPattern[0].length];
            // 위쪽 부분만 복사 (아래쪽 버림)
            System.arraycopy(newPattern, 0, trimmedPattern, 0, allowedLines);
            incomingQueue.add(trimmedPattern);
        } else {
            // 10줄 이하면 전체 추가
            incomingQueue.add(newPattern);
        }
    }

    @Test
    @DisplayName("10줄 미만 공격 추가")
    void testAddAttackUnderLimit() {
        Queue<int[][]> queue = new LinkedList<>();

        // 3줄 패턴
        int[][] pattern3 = new int[3][10];
        // 4줄 패턴
        int[][] pattern4 = new int[4][10];

        addAttackWithLimit(queue, pattern3);
        addAttackWithLimit(queue, pattern4);

        assertEquals(2, queue.size(), "2개의 패턴이 추가되어야 함");

        int totalLines = 0;
        for (int[][] pattern : queue) {
            totalLines += pattern.length;
        }
        assertEquals(7, totalLines, "총 7줄이 추가되어야 함");
    }

    @Test
    @DisplayName("정확히 10줄 추가")
    void testAddAttackExactLimit() {
        Queue<int[][]> queue = new LinkedList<>();

        // 10줄 패턴
        int[][] pattern10 = new int[10][10];

        addAttackWithLimit(queue, pattern10);

        assertEquals(1, queue.size(), "1개의 패턴이 추가되어야 함");
        assertEquals(10, queue.peek().length, "10줄 패턴이 추가되어야 함");
    }

    @Test
    @DisplayName("10줄 초과 시 무시")
    void testAddAttackOverLimitIgnored() {
        Queue<int[][]> queue = new LinkedList<>();

        // 10줄 먼저 추가
        int[][] pattern10 = new int[10][10];
        addAttackWithLimit(queue, pattern10);

        // 추가로 3줄 시도
        int[][] pattern3 = new int[3][10];
        addAttackWithLimit(queue, pattern3);

        assertEquals(1, queue.size(), "10줄이 차있으면 추가되지 않아야 함");
        assertEquals(10, queue.peek().length, "여전히 10줄이어야 함");
    }

    @Test
    @DisplayName("10줄 초과 시 아래쪽 잘라냄")
    void testAddAttackTrimBottom() {
        Queue<int[][]> queue = new LinkedList<>();

        // 7줄 먼저 추가
        int[][] pattern7 = new int[7][10];
        addAttackWithLimit(queue, pattern7);

        // 5줄 추가 시도 (총 12줄이 되므로 3줄만 추가되어야 함)
        int[][] pattern5 = new int[5][10];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                pattern5[i][j] = i + 1; // 각 줄마다 다른 값
            }
        }

        addAttackWithLimit(queue, pattern5);

        assertEquals(2, queue.size(), "2개의 패턴이 있어야 함");

        int totalLines = 0;
        for (int[][] pattern : queue) {
            totalLines += pattern.length;
        }
        assertEquals(10, totalLines, "총 10줄이어야 함");

        // 마지막 패턴이 3줄로 잘렸는지 확인
        int[][] lastPattern = null;
        for (int[][] pattern : queue) {
            lastPattern = pattern;
        }
        assertEquals(3, lastPattern.length, "마지막 패턴이 3줄로 잘렸어야 함");

        // 위쪽 3줄만 보존되었는지 확인 (값이 1, 2, 3이어야 함)
        assertEquals(1, lastPattern[0][0], "첫 번째 줄 값 확인");
        assertEquals(2, lastPattern[1][0], "두 번째 줄 값 확인");
        assertEquals(3, lastPattern[2][0], "세 번째 줄 값 확인");
    }

    @Test
    @DisplayName("여러 번 공격으로 누적")
    void testMultipleAttacksAccumulate() {
        Queue<int[][]> queue = new LinkedList<>();

        // 2줄씩 5번 추가
        for (int i = 0; i < 5; i++) {
            int[][] pattern2 = new int[2][10];
            addAttackWithLimit(queue, pattern2);
        }

        assertEquals(5, queue.size(), "5개의 패턴이 추가되어야 함");

        int totalLines = 0;
        for (int[][] pattern : queue) {
            totalLines += pattern.length;
        }
        assertEquals(10, totalLines, "총 10줄이어야 함");

        // 추가 시도 (무시되어야 함)
        int[][] pattern2 = new int[2][10];
        addAttackWithLimit(queue, pattern2);

        assertEquals(5, queue.size(), "여전히 5개여야 함");
    }

    @Test
    @DisplayName("빈 큐에 추가")
    void testAddToEmptyQueue() {
        Queue<int[][]> queue = new LinkedList<>();

        // 5줄 패턴
        int[][] pattern5 = new int[5][10];
        addAttackWithLimit(queue, pattern5);

        assertEquals(1, queue.size(), "1개의 패턴이 추가되어야 함");
        assertEquals(5, queue.peek().length, "5줄 패턴이 추가되어야 함");
    }

    @Test
    @DisplayName("8줄 + 3줄 시도 - 2줄만 추가")
    void testEightPlusTwoLimit() {
        Queue<int[][]> queue = new LinkedList<>();

        // 8줄 먼저 추가
        int[][] pattern8 = new int[8][10];
        addAttackWithLimit(queue, pattern8);

        // 3줄 추가 시도 (총 11줄이 되므로 2줄만 추가되어야 함)
        int[][] pattern3 = new int[3][10];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 10; j++) {
                pattern3[i][j] = i + 100;
            }
        }

        addAttackWithLimit(queue, pattern3);

        assertEquals(2, queue.size(), "2개의 패턴이 있어야 함");

        int totalLines = 0;
        for (int[][] pattern : queue) {
            totalLines += pattern.length;
        }
        assertEquals(10, totalLines, "총 10줄이어야 함");

        // 마지막 패턴이 2줄로 잘렸는지 확인
        int[][] lastPattern = null;
        for (int[][] pattern : queue) {
            lastPattern = pattern;
        }
        assertEquals(2, lastPattern.length, "마지막 패턴이 2줄로 잘렸어야 함");
        assertEquals(100, lastPattern[0][0], "첫 번째 줄 값 확인");
        assertEquals(101, lastPattern[1][0], "두 번째 줄 값 확인");
    }

    @Test
    @DisplayName("9줄 + 5줄 시도 - 1줄만 추가")
    void testNinePlusFiveLimit() {
        Queue<int[][]> queue = new LinkedList<>();

        // 9줄 먼저 추가
        int[][] pattern9 = new int[9][10];
        addAttackWithLimit(queue, pattern9);

        // 5줄 추가 시도 (총 14줄이 되므로 1줄만 추가되어야 함)
        int[][] pattern5 = new int[5][10];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                pattern5[i][j] = i + 200;
            }
        }

        addAttackWithLimit(queue, pattern5);

        assertEquals(2, queue.size(), "2개의 패턴이 있어야 함");

        int totalLines = 0;
        for (int[][] pattern : queue) {
            totalLines += pattern.length;
        }
        assertEquals(10, totalLines, "총 10줄이어야 함");

        // 마지막 패턴이 1줄로 잘렸는지 확인
        int[][] lastPattern = null;
        for (int[][] pattern : queue) {
            lastPattern = pattern;
        }
        assertEquals(1, lastPattern.length, "마지막 패턴이 1줄로 잘렸어야 함");
        assertEquals(200, lastPattern[0][0], "첫 번째 줄 값 확인");
    }
}
