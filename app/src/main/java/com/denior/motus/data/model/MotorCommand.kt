package com.denior.motus.data.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class MotorCommand(
    val targetAngle: Int,   // 32-bit signed integer (-360 to 360)
    val rpm: Int            // 16-bit unsigned integer (1-60)
) {
    fun toByteArray(): ByteArray {
        return ByteBuffer.allocate(6)  // 4 bytes angle + 2 bytes RPM
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(targetAngle.coerceIn(-360, 360))
            .putShort(rpm.coerceIn(0, 60).toShort()) 
            .array()
    }
}