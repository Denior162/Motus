package com.denior.motus

/*class MotorPowerTest {

    @Test
    fun `constructor should create valid object with default values`() {
        val motorPower = MotorPower()
        assertEquals(0, motorPower.adjustedPower)
        assertEquals("MotorPower(power=0, direction=false)", motorPower.toString())
    }

    @Test
    fun `constructor should create valid object with custom values`() {
        val motorPower = MotorPower(15, true)
        assertEquals(15, motorPower.adjustedPower)
        assertEquals("MotorPower(power=15, direction=true)", motorPower.toString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw exception when power is above 21`() {
        MotorPower(22)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw exception when power is below 0`() {
        MotorPower(-1)
    }

    @Test
    fun `toByte should correctly encode power and direction`() {
        val testCases = listOf(
            Triple(0, false, 0.toByte()),    // 0000 0000
            Triple(1, false, 2.toByte()),    // 0000 0010
            Triple(1, true, 3.toByte()),     // 0000 0011
            Triple(21, false, 42.toByte()),  // 0010 1010
            Triple(21, true, 43.toByte())    // 0010 1011
        )

        testCases.forEach { (power, direction, expected) ->
            val motorPower = MotorPower(power, direction)
            assertEquals(expected, motorPower.toByte())
        }
    }

    @Test
    fun `fromByte should correctly decode power and direction`() {
        val testCases = listOf(
            Triple(0.toByte(), 0, false),
            Triple(2.toByte(), 1, false),
            Triple(3.toByte(), 1, true),
            Triple(42.toByte(), 21, false),
            Triple(43.toByte(), 21, true)
        )

        testCases.forEach { (byte, expectedPower, expectedDirection) ->
            val motorPower = MotorPower.fromByte(byte)
            assertEquals(expectedPower, motorPower.adjustedPower)
            assertEquals(
                "MotorPower(power=$expectedPower, direction=$expectedDirection)",
                motorPower.toString()
            )
        }
    }

    @Test
    fun `fromByte should coerce power values to valid range`() {
        val highByte = 0xFF.toByte()
        val highMotorPower = MotorPower.fromByte(highByte)
        assertEquals(21, highMotorPower.adjustedPower)
        val lowByte = 0x00.toByte()
        val lowMotorPower = MotorPower.fromByte(lowByte)
        assertEquals(0, lowMotorPower.adjustedPower)
    }

    @Test
    fun `adjustedPower setter should enforce valid range`() {
        val motorPower = MotorPower()
        motorPower.adjustedPower = 0
        assertEquals(0, motorPower.adjustedPower)

        motorPower.adjustedPower = 21
        assertEquals(21, motorPower.adjustedPower)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `adjustedPower setter should throw exception for values above 21`() {
        val motorPower = MotorPower()
        motorPower.adjustedPower = 22
    }

    @Test(expected = IllegalArgumentException::class)
    fun `adjustedPower setter should throw exception for values below 0`() {
        val motorPower = MotorPower()
        motorPower.adjustedPower = -1
    }

    @Test
    fun `reverse should change direction`() {
        val motorPower = MotorPower(10, false)
        motorPower.reverse()
        assertEquals("MotorPower(power=10, direction=true)", motorPower.toString())
    }

    @Test
    fun `stop should set power to zero`() {
        val motorPower = MotorPower(10, true)
        motorPower.stop()
        assertEquals(0, motorPower.adjustedPower)
    }

    @Test
    fun `plus operator should increment power within bounds`() {
        val motorPower = MotorPower(10)
        val result = motorPower + 5
        assertEquals(15, result.power)

        val maxPower = MotorPower(20)
        val limitedResult = maxPower + 5
        assertEquals(21, limitedResult.power)
    }
}
 */