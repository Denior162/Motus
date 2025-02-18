package com.denior.motus

import com.denior.motus.data.model.MotorCommand
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MotorCommandTest {

    @Test
    fun `test MotorCommand toByteArray and parse back`() {
        val commandsToTest = listOf(
            MotorCommand(targetAngle = 0, rpm = 0),
            MotorCommand(targetAngle = 360, rpm = 60),
            MotorCommand(targetAngle = -360, rpm = 1),
            MotorCommand(targetAngle = 500, rpm = 70),
            MotorCommand(targetAngle = -999, rpm = -10)
        )

        commandsToTest.forEach { cmd ->
            val bytes = cmd.toByteArray()

            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val angleBack = buffer.int
            val rpmBack = buffer.short.toInt()

            val expectedAngle = cmd.targetAngle.coerceIn(-360, 360)
            val expectedRpm = cmd.rpm.coerceIn(0, 60)

            assertEquals("Angle должен быть в пределах [-360..360]", expectedAngle, angleBack)
            assertEquals("RPM должен быть в пределах [0..60]", expectedRpm, rpmBack)
        }
    }

    @Test
    fun `test exact byte representation`() {
        val command = MotorCommand(360, 60)
        val actualBytes = command.toByteArray()
        val expectedBytes = byteArrayOf(0x68, 0x01, 0x00, 0x00, 0x3C, 0x00) 
        // 360 = 0x0168 (little-endian -> 0x68, 0x01, 0x00, 0x00)
        // 60   = 0x3C (little-endian -> 0x3C, 0x00)

        assertArrayEquals(
            "Должны совпадать все байты, включая порядок байтов (little-endian)",
            expectedBytes, 
            actualBytes
        )
    }

    @Test
    fun `test negative angle exact byte representation`() {
        val command = MotorCommand(-360, 0)
        val actualBytes = command.toByteArray()

        // -360 в 32-битном представлении (two's complement) -> 0xFFFFFE98 (little-endian -> 0x98, 0xFE, 0xFF, 0xFF)
        // 0 в 16-битном представлении (little-endian -> 0x00, 0x00)
        val expectedBytes = byteArrayOf(
            0x98.toByte(), 0xFE.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0x00, 0x00
        )

        assertArrayEquals(
            "Неверная сериализация для -360 градусов и скорости 0",
            expectedBytes,
            actualBytes
        )
    }

    @Test
    fun `test zero angle and zero rpm exact byte representation`() {
        val command = MotorCommand(0, 0)
        val actualBytes = command.toByteArray()

        // 0 в 32-битном представлении -> 0x00000000 (little-endian -> 0x00, 0x00, 0x00, 0x00)
        // 0 в 16-битном представлении -> 0x0000 (little-endian -> 0x00, 0x00)
        val expectedBytes = byteArrayOf(
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00
        )

        assertArrayEquals(
            "Неверная сериализация для 0 градусов и скорости 0",
            expectedBytes,
            actualBytes
        )
    }

    @Test
    fun `test boundary and zero values byte representation`() {
        // Проверяем все граничные случаи
        val testCases = listOf(
            TestCase(
                command = MotorCommand(0, 0),
                expectedBytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
                description = "Нулевой угол и нулевая скорость"
            ),
            TestCase(
                command = MotorCommand(-360, 1),
                expectedBytes = byteArrayOf(0x98.toByte(), 0xFE.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x01, 0x00),
                description = "Минимальный угол и минимальная разрешенная скорость"
            ),
            TestCase(
                command = MotorCommand(360, 60),
                expectedBytes = byteArrayOf(0x68, 0x01, 0x00, 0x00, 0x3C, 0x00),
                description = "Максимальный угол и максимальная скорость"
            ),
            TestCase(
                command = MotorCommand(-1, 30),
                expectedBytes = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x1E, 0x00),
                description = "Отрицательный угол близкий к нулю"
            ),
            TestCase(
                command = MotorCommand(1, 30),
                expectedBytes = byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x1E, 0x00),
                description = "Положительный угол близкий к нулю"
            )
        )

        testCases.forEach { testCase ->
            val actualBytes = testCase.command.toByteArray()
            
            assertArrayEquals(
                "Ошибка для случая: ${testCase.description}",
                testCase.expectedBytes,
                actualBytes
            )
        }
    }

    private data class TestCase(
        val command: MotorCommand,
        val expectedBytes: ByteArray,
        val description: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as TestCase
            return command == other.command &&
                    expectedBytes.contentEquals(other.expectedBytes) &&
                    description == other.description
        }

        override fun hashCode(): Int {
            var result = command.hashCode()
            result = 31 * result + expectedBytes.contentHashCode()
            result = 31 * result + description.hashCode()
            return result
        }
    }
}