package com.example.travel

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object SmsCodeType {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PASSWORD = "password"
    const val DELETE = "delete"
    const val FORGOT = "forgot"
}

private val PHONE_REGEX = Regex("^1\\d{10}$")

/**
 * 发送验证码并在弹窗中展示
 */
suspend fun sendSmsCodeAndShow(
    context: Context,
    phone: String,
    type: String,
    onError: (String) -> Unit = {}
): Boolean {
    val normalized = phone.trim()
    if (!PHONE_REGEX.matches(normalized)) {
        onError("请输入正确的11位手机号")
        return false
    }
    val res = requestSmsCode(normalized, type)
    return if (res != null && res.code == 200 && res.data != null && !res.data.smsCode.isNullOrBlank()) {
        showSmsCodeDialog(context, res.data.smsCode, res.data.expiresInSeconds)
        true
    } else {
        onError(res?.message?.takeIf { it.isNotBlank() } ?: "发送失败，请重启后端后重试")
        false
    }
}

private suspend fun requestSmsCode(phone: String, type: String): ApiResponse<SmsSendResponse>? {
    val api = NetworkClient.apiService
    val attempts = listOf<suspend () -> ApiResponse<SmsSendResponse>>(
        { api.sendSmsCode(phone, type) },
        { api.sendSmsCodeRegister(phone, type) }
    )
    for (call in attempts) {
        try {
            val res = call()
            if (res.code == 200 && res.data?.smsCode != null) {
                return res
            }
            if (res.message?.isNotBlank() == true && res.code != 200) {
                return res
            }
        } catch (_: Exception) {
            // 尝试下一个地址
        }
    }
    return null
}

fun showSmsCodeDialog(context: Context, code: String, expiresInSeconds: Int) {
    val minutes = if (expiresInSeconds > 0) expiresInSeconds / 60 else 5
    val message = "您的验证码是：$code\n（${minutes} 分钟内有效）"
    val activity = context as? android.app.Activity
    if (activity != null) {
        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle("验证码")
                .setMessage(message)
                .setPositiveButton("知道了", null)
                .show()
        }
    } else {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
