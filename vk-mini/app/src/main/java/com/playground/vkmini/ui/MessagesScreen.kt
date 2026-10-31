package com.playground.vkmini.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playground.vkmini.AppViewModel
import com.playground.vkmini.data.ChatMessage
import com.playground.vkmini.data.Conversation

@Composable
fun MessagesScreen(vm: AppViewModel, onOpen: (Conversation) -> Unit) {
    LaunchedEffect(Unit) { vm.ensureConversations() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Сообщения",
            subtitle = if (vm.demo) "тестовые данные" else "личные диалоги и беседы",
            demo = vm.demo,
            actions = {
                CompactIconButton(
                    icon = Icons.Filled.Refresh,
                    contentDescription = "Обновить",
                    onClick = { vm.loadConversations(true) },
                )
            },
        )

        when {
            vm.conversationsError != null && vm.conversations.isEmpty() -> Column(Modifier.padding(12.dp)) {
                InlineError(text = vm.conversationsError!!, onRetry = { vm.loadConversations() })
            }

            vm.conversations.isEmpty() && vm.conversationsLoading -> LoadingState()

            vm.conversations.isEmpty() -> EmptyState(
                icon = MiniIcons.Chat,
                title = "Сообщений нет",
                text = "Диалоги появятся здесь. Проверь права токена на messages.",
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp),
            ) {
                items(vm.conversations, key = { it.peerId }) { conversation ->
                    ConversationRow(conversation) { onOpen(conversation) }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(conversation.avatar, 42.dp, conversation.title)
        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(6.dp))
                if (conversation.unread > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = conversation.unread.coerceAtMost(99).toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 9.sp,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = relativeTime(conversation.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = conversation.lastText.ifBlank { "Нет сообщений" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}

@Composable
fun ChatScreen(vm: AppViewModel, conversation: Conversation) {
    val listState = rememberLazyListState()

    LaunchedEffect(vm.messages.size, vm.chatLoading) {
        if (vm.messages.isNotEmpty() && !vm.chatLoading) {
            listState.scrollToItem(vm.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompactIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                onClick = vm::closeChat,
            )
            Spacer(Modifier.width(2.dp))
            Avatar(conversation.avatar, 30.dp, conversation.title)
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f),
            ) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (vm.demo) "демо-диалог" else "диалог",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

        Box(Modifier.weight(1f)) {
            when {
                vm.chatError != null && vm.messages.isEmpty() -> Column(Modifier.padding(12.dp)) {
                    InlineError(
                        text = vm.chatError!!,
                        onRetry = { vm.loadHistory(conversation.peerId) },
                    )
                }

                vm.chatLoading && vm.messages.isEmpty() -> LoadingState()

                vm.messages.isEmpty() -> EmptyState(
                    icon = MiniIcons.Chat,
                    title = "Тишина",
                    text = "Сообщений пока нет — напиши первым.",
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(vm.messages, key = { it.id }) { message ->
                        MessageBubble(message, showAuthor = conversation.peerId > 2_000_000_000L)
                    }
                }
            }
        }

        ChatInput(
            peerId = conversation.peerId,
            onSend = vm::sendMessage,
            sending = vm.sending,
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, showAuthor: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.out) Arrangement.End else Arrangement.Start,
    ) {
        Column(horizontalAlignment = if (message.out) Alignment.End else Alignment.Start) {
            if (showAuthor && !message.out && !message.author.isNullOrBlank()) {
                Text(
                    text = message.author!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 1.dp),
                )
            }
            Surface(
                color = if (message.out) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                contentColor = if (message.out) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomEnd = if (message.out) 4.dp else 14.dp,
                    bottomStart = if (message.out) 14.dp else 4.dp,
                ),
                modifier = Modifier.widthIn(max = 300.dp),
            ) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = clockTime(message.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInput(peerId: Long, onSend: (String) -> Unit, sending: Boolean) {
    var draft by rememberSaveable(peerId) { mutableStateOf("") }
    var focused by remember { mutableStateOf(false) }
    val canSend = draft.isNotBlank() && !sending

    fun send() {
        if (!canSend) return
        onSend(draft)
        draft = ""
    }

    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(50),
            ) {
                Box(Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                    if (draft.isEmpty() && !focused) {
                        Text(
                            text = "Сообщение",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BasicTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focused = it.isFocused },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { send() }),
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (canSend || sending) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    )
                    .clickable(enabled = canSend, onClick = { send() }),
                contentAlignment = Alignment.Center,
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Отправить",
                        tint = if (canSend) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
