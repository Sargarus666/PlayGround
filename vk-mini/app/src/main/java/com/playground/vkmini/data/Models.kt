package com.playground.vkmini.data

data class Profile(
    val id: Long,
    val name: String,
    val photo: String?,
    val status: String?,
    val city: String?,
    val counters: Map<String, Int>,
)

data class FeedPost(
    val id: Long,
    val author: String,
    val avatar: String?,
    val date: Long,
    val text: String,
    val image: String?,
    val likes: Int,
    val comments: Int,
    val reposts: Int,
    val ownerId: Long = 0L,
    val postId: Long = 0L,
    val liked: Boolean = false,
    val canLike: Boolean = true,
)

data class Conversation(
    val peerId: Long,
    val title: String,
    val avatar: String?,
    val lastText: String,
    val date: Long,
    val unread: Int,
)

data class ChatMessage(
    val id: Long,
    val peerId: Long,
    val out: Boolean,
    val text: String,
    val date: Long,
    val author: String? = null,
)

data class Track(
    val id: Long,
    val artist: String,
    val title: String,
    val duration: Int,
    val url: String?,
)

class VkApiException(val code: Int, message: String) : Exception(message)
