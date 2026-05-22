package com.example.travel

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object MultipartUtil {
    fun textPart(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaType())

    fun imagePart(context: Context, uri: Uri, partName: String = "image"): MultipartBody.Part? {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "image/jpeg"
        val ext = when {
            mime.contains("png") -> ".png"
            mime.contains("webp") -> ".webp"
            else -> ".jpg"
        }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val body = bytes.toRequestBody(mime.toMediaType())
        return MultipartBody.Part.createFormData(partName, "landscape$ext", body)
    }
}
