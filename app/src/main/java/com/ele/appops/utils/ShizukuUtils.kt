package com.ele.appops.utils

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShizukuUtils {

    private const val SHIZUKU_PERMISSION_REQUEST_CODE = 1001

    /**
     * 检查 Shizuku 是否已安装并正在运行
     */
    fun isShizukuRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查是否已获得 Shizuku 授权
     */
    fun checkPermission(): Boolean {
        return try {
            if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 请求 Shizuku 授权
     */
    fun requestPermission(requestCode: Int = SHIZUKU_PERMISSION_REQUEST_CODE) {
        if (!checkPermission()) {
            Shizuku.requestPermission(requestCode)
        }
    }

    /**
     * 注册授权结果监听
     */
    fun addRequestPermissionResultListener(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.addRequestPermissionResultListener(listener)
    }

    /**
     * 移除授权结果监听
     */
    fun removeRequestPermissionResultListener(listener: Shizuku.OnRequestPermissionResultListener) {
        Shizuku.removeRequestPermissionResultListener(listener)
    }

    /**
     * 执行 Shell 命令
     */
    suspend fun runShellCommand(command: String): ShellResult = withContext(Dispatchers.IO) {
        var process: Process? = null
        try {
            // 在 Shizuku 13.1.5 中，newProcess 是私有的，使用反射调用
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            newProcessMethod.isAccessible = true
            
            process = newProcessMethod.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as Process

            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val error = BufferedReader(InputStreamReader(process.errorStream)).readText()
            val exitCode = process.waitFor()
            ShellResult(exitCode, output, error)
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "Unknown error")
        } finally {
            process?.destroy()
        }
    }
}

data class ShellResult(
    val exitCode: Int,
    val output: String,
    val error: String
)
