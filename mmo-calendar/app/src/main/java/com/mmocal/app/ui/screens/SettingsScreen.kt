package com.mmocal.app.ui.screens

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mmocal.app.BuildConfig
import com.mmocal.app.data.AccentPalette
import com.mmocal.app.data.BackupHelper
import com.mmocal.app.data.AccountsStore
import com.mmocal.app.data.AppStyle
import com.mmocal.app.data.GamesRepository
import com.mmocal.app.data.RemoteInfo
import com.mmocal.app.data.SettingsStore
import com.mmocal.app.data.ThemeMode
import com.mmocal.app.notifications.Reminders
import kotlinx.coroutines.launch

private val themeOptions = listOf(
    ThemeMode.SYSTEM to "Система",
    ThemeMode.LIGHT to "Светлая",
    ThemeMode.DARK to "Тёмная"
)

private val styleOptions = listOf(
    AppStyle.MATERIAL_YOU to "Material You",
    AppStyle.CLASSIC to "Классическая",
    AppStyle.MONO to "Монохром"
)

@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    onNotificationsToggled: (Boolean) -> Unit,
    onAccountChanged: () -> Unit
) {
    val context = LocalContext.current
    val themeMode by settingsStore.themeMode.collectAsState()
    val style by settingsStore.style.collectAsState()
    val colorIndex by settingsStore.colorIndex.collectAsState()
    val notifications by settingsStore.notifications.collectAsState()
    val autoUpdate by settingsStore.autoUpdate.collectAsState()
    val account by AccountsStore.current.collectAsState()
    val repo by GamesRepository.state.collectAsState()
    val mmoProgress by com.mmocal.app.data.Mmo13Source.progress.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            settingsStore.setNotifications(true)
            onNotificationsToggled(true)
        }
    }

    val scope = rememberCoroutineScope()
    var checkBusy by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<RemoteInfo?>(null) }
    var checkStatus by remember { mutableStateOf<String?>(null) }
    var onlineNow by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) { onlineNow = AccountsStore.checkOnline() }

    fun manualCheck() {
        scope.launch {
            checkBusy = true
            checkStatus = "Проверка…"
            val info = GamesRepository.fetchUpdateInfo()
            var mmoOk = false
            try {
                mmoOk = GamesRepository.refreshMmo13(force = true)
            } catch (_: Exception) {
            }
            checkBusy = false
            updateInfo = info
            checkStatus = if (info == null && !mmoOk) {
                "Не удалось проверить (нет сети)"
            } else {
                if (info != null && info.dataVersion > GamesRepository.state.value.dataVersion
                    && info.dataVersion > 0
                ) {
                    runCatching { GamesRepository.applyRemote(info) }
                }
                if (info != null && info.appCode > BuildConfig.VERSION_CODE) {
                    "Доступна версия ${info.appName}"
                } else if (mmoOk) {
                    "Данные обновлены: сервер + тесты ок"
                } else {
                    "Данные актуальны (тесты: кэш)"
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        item {
            val current = account
            SettingsCard(title = "Аккаунт") {
                if (current == null) {
                    AccountForm(
                        onResult = { error ->
                            if (error == null) onAccountChanged()
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = current,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            val syncTs = AccountsStore.lastSync(current)
                            Text(
                                text = when (onlineNow) {
                                    true -> "Онлайн-аккаунт: синхронизировано " +
                                        if (syncTs > 0) formatSyncTime(syncTs) else "только что"
                                    false -> "Офлайн: для синхронизации подключите интернет"
                                    null -> "Проверка соединения…"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = {
                            AccountsStore.logout()
                            onAccountChanged()
                        }) { Text("Выйти") }
                    }
                }
            }
        }

        item {
            BackupCard(
                onExport = {
                    val uri = BackupHelper.export(context)
                    if (uri != null) {
                        BackupHelper.shareBackup(context, uri)
                    } else {
                        Toast.makeText(context, "Не удалось создать копию", Toast.LENGTH_SHORT).show()
                    }
                },
                onImport = { uri ->
                    val error = BackupHelper.importBackup(context, uri)
                    if (error == null) {
                        onAccountChanged()
                        Toast.makeText(context, "Копия восстановлена", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        item {
            SettingsCard(title = "Обновление") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Автообновление",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Проверять данные при запуске приложения",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoUpdate,
                        onCheckedChange = { settingsStore.setAutoUpdate(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = !checkBusy) { manualCheck() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (checkBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Проверить обновления",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Проверить обновления",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        val status = mmoProgress
                            ?: checkStatus
                            ?: "Нажмите, чтобы проверить данные и версию"
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (updateInfo != null && updateInfo!!.appCode > BuildConfig.VERSION_CODE) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val url = updateInfo!!.apkUrl
                            if (url.isNotBlank()) {
                                downloadApk(
                                    context,
                                    url,
                                    onStarted = {
                                        checkStatus = "Скачивание ${updateInfo!!.appName}: откройте файл из уведомлений, чтобы установить"
                                    }
                                )
                            } else {
                                Toast.makeText(
                                    context, "Ссылка на скачивание ещё не доступна", Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Скачать обновление ${updateInfo!!.appName}")
                    }
                }
                Spacer(Modifier.height(10.dp))
                val mmoTs = GamesRepository.mmoLastSync()
                val serverVer = updateInfo?.let { " · на сервере: ${it.appName} (${it.appCode})" } ?: ""
                Text(
                    text = "Приложение: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})$serverVer",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Данные: ${repo.source} · ${repo.events.size} событий" +
                        if (mmoTs > 0) " · тесты: ${formatSyncTime(mmoTs)}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SettingsCard(title = "Тема") {
                SegmentedRow(
                    options = themeOptions.map { it.second },
                    selectedIndex = themeOptions.indexOfFirst { it.first == themeMode },
                    onSelect = { index -> settingsStore.setThemeMode(themeOptions[index].first) }
                )
            }
        }

        item {
            SettingsCard(title = "Стиль") {
                SegmentedRow(
                    options = styleOptions.map { it.second },
                    selectedIndex = styleOptions.indexOfFirst { it.first == style },
                    onSelect = { index -> settingsStore.setStyle(styleOptions[index].first) }
                )
            }
        }

        item {
            SettingsCard(title = "Основной цвет") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AccentPalette.colors.forEachIndexed { index, color ->
                        val selected = index == colorIndex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color.value))
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { settingsStore.setColorIndex(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = color.name,
                                    tint = if (color.value > 0xFF888888) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = AccentPalette.at(colorIndex).name +
                        if (style == AppStyle.MATERIAL_YOU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                            " · на Android 12+ применяются динамические цвета Material You"
                        else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SettingsCard(title = "Уведомления") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Напоминания о релизах",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "За 3 дня до выхода игр из избранного или ближайшего релиза",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notifications,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                    ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    settingsStore.setNotifications(true)
                                    onNotificationsToggled(true)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                settingsStore.setNotifications(false)
                                Reminders.cancel(context)
                                onNotificationsToggled(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        item {
            SettingsCard(title = "О приложении") {
                InfoRow("Версия", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                InfoRow("Разработчик", "Sargarus")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Календарь релизов ММО: ЗБТ, ОБТ, ранний доступ и полные запуски. Данные подтягиваются с сервера, даты могут уточняться издателями.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun downloadApk(context: Context, url: String, onStarted: () -> Unit = {}) {
    val ok = runCatching {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("MMO Calendar")
            setDescription("Скачивание обновления")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "mmo-calendar.apk")
            setAllowedOverMetered(true)
        }
        dm.enqueue(request)
    }.isSuccess
    if (ok) {
        onStarted()
        Toast.makeText(context, "Скачивание началось (Загрузки)", Toast.LENGTH_SHORT).show()
    } else {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            Toast.makeText(context, "Открываю ссылку в браузере", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Не удалось начать загрузку", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun AccountForm(onResult: (String?) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Пароль") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))
    error?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(6.dp))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            enabled = !busy,
            onClick = {
                scope.launch {
                    busy = true
                    val err = AccountsStore.register(email, password)
                    busy = false
                    error = err
                    if (err == null) onResult(null)
                }
            }
        ) { Text("Регистрация") }
        Button(
            enabled = !busy,
            onClick = {
                scope.launch {
                    busy = true
                    val err = AccountsStore.login(email, password)
                    busy = false
                    error = err
                    if (err == null) onResult(null)
                }
            }
        ) { Text("Войти") }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Регистрация и вход работают только онлайн: соединение проверяется с сервером данных. Избранное и настройки привязаны к аккаунту.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun BackupCard(
    onExport: () -> Unit,
    onImport: (Uri) -> Unit
) {
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) onImport(uri) }
    SettingsCard(title = "Резервная копия") {
        Text(
            text = "Аккаунт онлайн: избранное и настройки привязаны к аккаунту. Для переноса на другое устройство сохраните копию ниже.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f)) {
                Text("Сохранить")
            }
            Button(onClick = { picker.launch("*/*") }, modifier = Modifier.weight(1f)) {
                Text("Восстановить")
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SegmentedRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

private fun formatSyncTime(ts: Long): String = runCatching {
    val dt = java.time.Instant.ofEpochMilli(ts).atZone(java.time.ZoneId.systemDefault())
    dt.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm"))
}.getOrDefault("")

@Composable
private fun InfoRow(label: String, value: String) {    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}