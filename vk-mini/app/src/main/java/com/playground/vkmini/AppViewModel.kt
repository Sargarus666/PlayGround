package com.playground.vkmini

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.webkit.CookieManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playground.vkmini.data.ChatMessage
import com.playground.vkmini.data.Conversation
import com.playground.vkmini.data.DemoData
import com.playground.vkmini.data.FeedPost
import com.playground.vkmini.data.Profile
import com.playground.vkmini.data.TokenStore
import com.playground.vkmini.data.Track
import com.playground.vkmini.data.VkApi
import com.playground.vkmini.data.VkApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = TokenStore(application)
    private var api: VkApi? = null

    var ready by mutableStateOf(false)
        private set
    var booting by mutableStateOf(false)
        private set
    var demo by mutableStateOf(false)
        private set
    var authLoading by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    var profile by mutableStateOf<Profile?>(null)
        private set
    var profileLoading by mutableStateOf(false)
        private set

    var feed by mutableStateOf<List<FeedPost>>(emptyList())
        private set
    var feedLoading by mutableStateOf(false)
        private set
    var feedRefreshing by mutableStateOf(false)
        private set
    var feedError by mutableStateOf<String?>(null)
        private set
    var feedHasMore by mutableStateOf(false)
        private set
    private var feedFrom: String? = null
    private var feedLoaded = false

    var conversations by mutableStateOf<List<Conversation>>(emptyList())
        private set
    var conversationsLoading by mutableStateOf(false)
        private set
    var conversationsError by mutableStateOf<String?>(null)
        private set
    private var conversationsLoaded = false

    var selectedConversation by mutableStateOf<Conversation?>(null)
        private set
    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set
    var chatLoading by mutableStateOf(false)
        private set
    var chatError by mutableStateOf<String?>(null)
        private set
    var sending by mutableStateOf(false)
        private set
    private var chatPeerId: Long? = null

    var tracks by mutableStateOf<List<Track>>(emptyList())
        private set
    var musicLoading by mutableStateOf(false)
        private set
    var musicError by mutableStateOf<String?>(null)
        private set
    private var musicLoaded = false

    var currentTrack by mutableStateOf<Track?>(null)
        private set
    var playing by mutableStateOf(false)
        private set
    var positionMs by mutableStateOf(0)
        private set
    var durationMs by mutableStateOf(0)
        private set

    private var player: MediaPlayer? = null
    private var ticker: Job? = null

    init {
        if (store.demo) {
            startDemo()
        } else {
            val saved = store.token
            if (saved != null) {
                api = VkApi(saved)
                booting = true
                viewModelScope.launch {
                    try {
                        profile = api?.profile()
                        ready = true
                    } catch (e: VkApiException) {
                        if (e.code == 5 || e.code == 18) {
                            store.token = null
                            api = null
                            authError = e.message
                        } else {
                            ready = true
                        }
                    } catch (e: Exception) {
                        ready = true
                    } finally {
                        booting = false
                    }
                }
            }
        }
    }

    fun login(rawToken: String) {
        val token = rawToken.trim()
        if (token.isBlank()) {
            authError = "Вставь access_token"
            return
        }
        if (authLoading) return
        authLoading = true
        authError = null
        viewModelScope.launch {
            try {
                val candidate = VkApi(token)
                val loaded = candidate.profile()
                api = candidate
                profile = loaded
                store.token = token
                store.demo = false
                demo = false
                resetContent()
                ready = true
            } catch (e: Exception) {
                authError = VkApi.friendlyMessage(e)
            } finally {
                authLoading = false
            }
        }
    }

    fun enterDemo() {
        releasePlayer()
        store.demo = true
        store.token = null
        api = null
        startDemo()
    }

    private fun startDemo() {
        demo = true
        ready = true
        profile = DemoData.profile
        feed = DemoData.feed()
        feedLoaded = true
        feedHasMore = false
        feedError = null
        conversations = DemoData.conversations()
        conversationsLoaded = true
        conversationsError = null
        tracks = DemoData.tracks()
        musicLoaded = true
        musicError = null
    }

    fun logout() {
        releasePlayer()
        store.clear()
        api = null
        ready = false
        demo = false
        profile = null
        authError = null
        selectedConversation = null
        chatPeerId = null
        resetContent()
    }

    private fun resetContent() {
        feed = emptyList()
        feedFrom = null
        feedLoaded = false
        feedHasMore = false
        feedError = null
        conversations = emptyList()
        conversationsLoaded = false
        conversationsError = null
        messages = emptyList()
        chatError = null
        tracks = emptyList()
        musicLoaded = false
        musicError = null
        currentTrack = null
        playing = false
        positionMs = 0
        durationMs = 0
    }

    fun clearAuthError() {
        authError = null
    }

    fun consumeMessage() {
        message = null
    }

    fun refreshProfile() {
        if (demo || profileLoading) return
        val client = api ?: return
        profileLoading = true
        viewModelScope.launch {
            try {
                profile = client.profile()
            } catch (e: Exception) {
                message = VkApi.friendlyMessage(e)
            } finally {
                profileLoading = false
            }
        }
    }

    fun ensureFeed() {
        if (!feedLoaded) loadFeed()
    }

    fun loadFeed(refresh: Boolean = false) {
        if (feedLoading) return
        val client = api
        if (!demo && client == null) return
        feedLoading = true
        if (refresh) feedRefreshing = true
        feedError = null
        viewModelScope.launch {
            try {
                if (demo) {
                    delay(300)
                    feed = DemoData.feed()
                    feedHasMore = false
                    feedLoaded = true
                } else {
                    val startFrom = if (refresh) null else feedFrom
                    val (items, next) = client!!.feed(startFrom)
                    feed = if (refresh || feed.isEmpty()) items else feed + items
                    feedFrom = next
                    feedHasMore = next != null
                    feedLoaded = true
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                feedError = VkApi.friendlyMessage(e)
            } finally {
                feedLoading = false
                feedRefreshing = false
            }
        }
    }

    fun ensureConversations() {
        if (!conversationsLoaded) loadConversations()
    }

    fun loadConversations(refresh: Boolean = false) {
        if (conversationsLoading) return
        val client = api
        if (!demo && client == null) return
        conversationsLoading = true
        conversationsError = null
        viewModelScope.launch {
            try {
                if (demo) {
                    delay(300)
                    conversations = DemoData.conversations()
                } else {
                    conversations = client!!.conversations()
                }
                conversationsLoaded = true
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                conversationsError = VkApi.friendlyMessage(e)
            } finally {
                conversationsLoading = false
            }
        }
    }

    fun openChat(conversation: Conversation) {
        selectedConversation = conversation
        conversations = conversations.map {
            if (it.peerId == conversation.peerId) it.copy(unread = 0) else it
        }
        loadHistory(conversation.peerId)
        if (!demo) {
            val client = api ?: return
            viewModelScope.launch {
                runCatching { client.markRead(conversation.peerId) }
            }
        }
    }

    fun closeChat() {
        selectedConversation = null
        chatPeerId = null
        messages = emptyList()
        chatError = null
        if (!demo) loadConversations()
    }

    fun loadHistory(peerId: Long) {
        chatPeerId = peerId
        chatLoading = true
        chatError = null
        messages = emptyList()
        viewModelScope.launch {
            try {
                if (demo) {
                    delay(250)
                    if (chatPeerId == peerId) messages = DemoData.history(peerId)
                } else {
                    val history = api?.history(peerId) ?: emptyList()
                    if (chatPeerId == peerId) messages = history
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (chatPeerId == peerId) chatError = VkApi.friendlyMessage(e)
            } finally {
                if (chatPeerId == peerId) chatLoading = false
            }
        }
    }

    fun sendMessage(text: String) {
        val conversation = selectedConversation ?: return
        val value = text.trim()
        if (value.isEmpty() || sending) return
        val now = System.currentTimeMillis() / 1000
        val local = ChatMessage(-System.nanoTime(), conversation.peerId, true, value, now)
        messages = messages + local
        sending = true
        viewModelScope.launch {
            try {
                if (demo) {
                    delay(250)
                } else {
                    api?.send(conversation.peerId, value)
                }
                conversations = conversations.map {
                    if (it.peerId == conversation.peerId) {
                        it.copy(lastText = value, date = now)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                messages = messages.filterNot { it.id == local.id }
                message = VkApi.friendlyMessage(e)
            } finally {
                sending = false
            }
        }
    }

    fun ensureMusic() {
        if (!musicLoaded) loadTracks()
    }

    fun loadTracks() {
        if (musicLoading) return
        val client = api
        if (!demo && client == null) return
        musicLoading = true
        musicError = null
        viewModelScope.launch {
            try {
                if (demo) {
                    delay(300)
                    tracks = DemoData.tracks()
                } else {
                    val owner = profile?.id ?: client!!.profile().id
                    tracks = client!!.tracks(owner)
                }
                musicLoaded = true
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                musicError = VkApi.friendlyMessage(e)
            } finally {
                musicLoading = false
            }
        }
    }

    fun toggleTrack(track: Track) {
        if (track.url.isNullOrBlank()) {
            message = if (demo) {
                "Демо-режим: воспроизведение отключено"
            } else {
                "VK не отдал ссылку на этот трек"
            }
            return
        }
        if (currentTrack?.id == track.id && player != null) {
            togglePlay()
            return
        }
        releasePlayer()
        try {
            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(track.url)
                setOnPreparedListener { prepared ->
                    durationMs = prepared.duration
                    prepared.start()
                    playing = true
                    startTicker()
                }
                setOnCompletionListener {
                    playing = false
                    positionMs = 0
                    ticker?.cancel()
                }
                setOnErrorListener { _, _, _ ->
                    playing = false
                    message = "Не удалось воспроизвести трек"
                    true
                }
                prepareAsync()
            }
            player = mediaPlayer
            currentTrack = track
            positionMs = 0
            durationMs = track.duration * 1000
            playing = false
        } catch (e: Exception) {
            releasePlayer()
            message = "Не удалось воспроизвести трек"
        }
    }

    private fun togglePlay() {
        val mediaPlayer = player ?: return
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playing = false
                ticker?.cancel()
            } else {
                mediaPlayer.start()
                playing = true
                startTicker()
            }
        } catch (e: Exception) {
            message = "Плеер недоступен"
        }
    }

    fun seekTo(fraction: Float) {
        val mediaPlayer = player ?: return
        val total = if (durationMs > 0) durationMs else mediaPlayer.duration
        if (total <= 0) return
        val target = (fraction.coerceIn(0f, 1f) * total).toInt()
        mediaPlayer.seekTo(target)
        positionMs = target
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                player?.let {
                    runCatching { positionMs = it.currentPosition }
                }
                delay(500)
            }
        }
    }

    private fun releasePlayer() {
        ticker?.cancel()
        ticker = null
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        playing = false
        positionMs = 0
        currentTrack = null
    }

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }
}
