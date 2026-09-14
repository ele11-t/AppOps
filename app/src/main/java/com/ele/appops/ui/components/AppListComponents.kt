package com.ele.appops.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.ele.appops.model.AppInfo

@Composable
fun AppListItem(
    app: AppInfo,
    onToggleSelection: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelection(app.packageName) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = app.icon
        if (icon != null) {
            Image(
                bitmap = icon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = app.label, style = MaterialTheme.typography.titleMedium)
            Text(text = app.packageName, style = MaterialTheme.typography.bodySmall)
            
            val statusColor = when (app.opStatus) {
                "Allowed" -> MaterialTheme.colorScheme.primary
                "Ignored" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outline
            }
            val statusText = when (app.opStatus) {
                "Allowed" -> "状态：已恢复"
                "Ignored" -> "状态：已禁用"
                else -> "状态：未知"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor
            )
        }
        Checkbox(
            checked = app.isSelected,
            onCheckedChange = { onToggleSelection(app.packageName) }
        )
    }
}

@Composable
fun AppList(
    apps: List<AppInfo>,
    onToggleSelection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(apps, key = { it.packageName }) { app ->
            AppListItem(app = app, onToggleSelection = onToggleSelection)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
