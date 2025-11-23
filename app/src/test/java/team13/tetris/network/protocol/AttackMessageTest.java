package team13.tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttackMessageTest {

    @Test
    @DisplayName("표준 공격 메시지 생성 테스트")
    void testCreateStandardAttack() {
        AttackMessage message = AttackMessage.createStandardAttack("TestPlayer", 2);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.ATTACK_SENT, message.getType(), "메시지 타입이 ATTACK_SENT여야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals(2, message.getSourceLines(), "원본 라인 수가 올바르게 설정되어야 함");
        assertEquals(2, message.getAttackLines(), "공격 라인 수가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("커스텀 공격 메시지 생성 테스트")
    void testCreateCustomAttack() {
        AttackMessage message = new AttackMessage("TestPlayer", 3, 4, null);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.ATTACK_SENT, message.getType(), "메시지 타입이 ATTACK_SENT여야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals(3, message.getSourceLines(), "원본 라인 수가 올바르게 설정되어야 함");
        assertEquals(4, message.getAttackLines(), "공격 라인 수가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("잘못된 원본 라인 수로 생성 시 예외 발생")
    void testInvalidSourceLines() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new AttackMessage("TestPlayer", 0, 1, null);
                },
                "원본 라인 수가 0 이하일 때 예외가 발생해야 함");

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new AttackMessage("TestPlayer", 11, 1, null);
                },
                "원본 라인 수가 10 초과일 때 예외가 발생해야 함");
    }

    @Test
    @DisplayName("잘못된 공격 라인 수로 생성 시 예외 발생")
    void testInvalidAttackLines() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new AttackMessage("TestPlayer", 2, -1, null);
                },
                "공격 라인 수가 음수일 때 예외가 발생해야 함");

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new AttackMessage("TestPlayer", 2, 11, null);
                },
                "공격 라인 수가 10 초과일 때 예외가 발생해야 함");
    }

    @Test
    @DisplayName("표준 공격 계산 로직 테스트")
    void testStandardAttackCalculation() {
        // 1줄 클리어 → 0 공격
        AttackMessage attack1 = AttackMessage.createStandardAttack("Player", 1);
        assertEquals(0, attack1.getAttackLines(), "1줄 클리어 시 공격 라인은 0이어야 함");

        // 2줄 클리어 → 2 공격
        AttackMessage attack2 = AttackMessage.createStandardAttack("Player", 2);
        assertEquals(2, attack2.getAttackLines(), "2줄 클리어 시 공격 라인은 2여야 함");

        // 3줄 클리어 → 3 공격
        AttackMessage attack3 = AttackMessage.createStandardAttack("Player", 3);
        assertEquals(3, attack3.getAttackLines(), "3줄 클리어 시 공격 라인은 3이어야 함");

        // 4줄 클리어 → 4 공격 (테트리스)
        AttackMessage attack4 = AttackMessage.createStandardAttack("Player", 4);
        assertEquals(4, attack4.getAttackLines(), "4줄 클리어 시 공격 라인은 4여야 함");
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void testToString() {
        AttackMessage message = new AttackMessage("TestPlayer", 3, 4, null);
        String toString = message.toString();

        assertNotNull(toString, "toString 결과가 null이 아니어야 함");
        assertTrue(toString.contains("3"), "toString에 원본 라인 수가 포함되어야 함");
        assertTrue(toString.contains("4"), "toString에 공격 라인 수가 포함되어야 함");
        assertTrue(toString.contains("TestPlayer"), "toString에 발신자 ID가 포함되어야 함");
    }
}
