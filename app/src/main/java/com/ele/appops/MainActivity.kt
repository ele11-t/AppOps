package com.ele.appops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ele.appops.ui.components.AppList
import com.ele.appops.utils.ShizukuUtils
import com.ele.appops.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkShizukuStatus()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val appList by viewModel.appList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isShizukuReady by viewModel.isShizukuReady.collectAsState()

    val filteredApps = remember(appList, searchQuery) {
        appList.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AppOps Manager") },
                actions = {
                    TextButton(onClick = { viewModel.selectAll(true) }) {
                        Text("全选")
                    }
                    TextButton(onClick = { viewModel.selectAll(false) }) {
                        Text("清空")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.batchSetOps(false) },
                        enabled = isShizukuReady && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("一键禁用")
                    }
                    Button(
                        onClick = { viewModel.batchSetOps(true) },
                        enabled = isShizukuReady && !isLoading
                    ) {
                        Text("一键恢复")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!isShizukuReady) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Shizuku 未就绪或未授权", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { 
                            if (!ShizukuUtils.isShizukuRunning()) {
                                // 引导用户打开 Shizuku (逻辑可以更完善)
                            } else {
                                ShizukuUtils.requestPermission()
                            }
                        }) {
                            Text("申请授权")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("搜索应用名或包名") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AppList(
                apps = filteredApps,
                onToggleSelection = { viewModel.toggleAppSelection(it) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
