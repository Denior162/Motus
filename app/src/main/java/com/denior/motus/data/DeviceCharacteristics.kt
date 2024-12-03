package com.denior.motus.data

data class DeviceCharacteristics(
    val uuid: String,
    val value: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceCharacteristics
        return uuid == other.uuid && value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}