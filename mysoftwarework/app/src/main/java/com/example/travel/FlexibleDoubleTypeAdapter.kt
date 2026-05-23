package com.example.travel

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * 兼容后端将 DECIMAL 序列化为数字或字符串的情况，避免 latitude/longitude 解析为 null。
 */
class FlexibleDoubleTypeAdapter : TypeAdapter<Double?>() {
    override fun write(out: JsonWriter, value: Double?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Double? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.NUMBER -> reader.nextDouble()
            JsonToken.STRING -> reader.nextString().trim().toDoubleOrNull()
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
