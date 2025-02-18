package com.denior.motus.data.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class MotorCommand(
    val targetAngle: Int, val rpm: Int
) {
    fun toByteArray(): ByteArray {
        return ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(targetAngle.coerceIn(-360, 360)).putShort(rpm.coerceIn(0, 60).toShort()).array()
    }
}