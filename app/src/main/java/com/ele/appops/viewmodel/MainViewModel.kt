package com.ele.appops.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ele.appops.model.AppInfo
import com.ele.appops.utils.ShizukuUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
        checkShizukuStatus()
    }

    private val _appList = MutableStateFlow<List<AppInfo>>(emptyList())
    val appList: StateFlow<List<AppInfo>> = _appList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isShizukuReady = MutableStateFlow(false)
    val isShizukuReady: StateFlow<Boolean> = _isShizukuReady.asStateFlow()

    private val opsToManage = listOf(
        // 标准 Op 名称或 OPSTR 标识符
        "RUN_IN_BACKGROUND",
        "RUN_ANY_IN_BACKGROUND",
        "WAKE_LOCK",
        "SCHEDULE_EXACT_ALARM",
        "BOOT_COMPLETED",
        "START_FOREGROUND",
        "MONITOR_HIGH_POWER_LOCATION",
        "SYSTEM_ALERT_WINDOW",
        "READ_CLIPBOARD",
        "ACTIVITY_RECOGNITION",
        "MONITOR_LOCATION",
        "BLUETOOTH_SCAN",
        "BLUETOOTH_CONNECT",
        "FINE_LOCATION",
        "COARSE_LOCATION",
        "GPS",
        // 尝试使用标准的 android: 前缀标识符以增加兼容性
        "android:fine_location",
        "android:coarse_location",
        "android:mock_location",
        "android:monitor_location",
        "android:monitor_high_power_location",
        "android:gps"
    )

    init {
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        checkShizukuStatus()
        loadApplications()
    }

    override fun onCleared() {
        super.onCleared()
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }

    fun checkShizukuStatus() {
        _isShizukuReady.value = ShizukuUtils.isShizukuRunning() && ShizukuUtils.checkPermission()
    }

    fun loadApplications() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                val pm = getApplication<Application>().packageManager
                val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }

                installedApps.map {
                    val status = if (_isShizukuReady.value) {
                        checkAppOpsStatus(it.packageName)
                    } else "Unknown"

                    AppInfo(
                        label = it.loadLabel(pm).toString(),
                        packageName = it.packageName,
                        icon = it.loadIcon(pm),
                        isSystemApp = false,
                        opStatus = status
                    )
                }.sortedBy { it.label.lowercase() }
            }
            _appList.value = apps
            _isLoading.value = false
        }
    }

    private suspend fun checkAppOpsStatus(packageName: String): String {
        // 我们通过检查 RUN_IN_BACKGROUND 作为一个代表性的状态
        val result = ShizukuUtils.runShellCommand("appops get $packageName RUN_IN_BACKGROUND")
        return when {
            result.output.contains("allow") -> "Allowed"
            result.output.contains("ignore") || result.output.contains("deny") -> "Ignored"
            else -> "Unknown"
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleAppSelection(packageName: String) {
        _appList.value = _appList.value.map {
            if (it.packageName == packageName) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun selectAll(select: Boolean) {
        _appList.value = _appList.value.map { it.copy(isSelected = select) }
    }

    fun batchSetOps(allow: Boolean) {
        val selectedApps = _appList.value.filter { it.isSelected }
        if (selectedApps.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            // 在较新的 Android 版本中，使用 deny 比 ignore 限制更严格且更易观察
            val mode = if (allow) "allow" else "deny"
            selectedApps.forEach { app ->
                opsToManage.forEach { op ->
                    ShizukuUtils.runShellCommand("appops set ${app.packageName} $op $mode")
                }
            }
            // 操作完成后重新加载列表以更新状态
            loadApplications()
        }
    }
}
