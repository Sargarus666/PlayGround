package com.playground.vkmini.data

import java.util.concurrent.TimeUnit

object DemoData {

    private fun minutesAgo(minutes: Long): Long =
        System.currentTimeMillis() / 1000 - TimeUnit.MINUTES.toSeconds(minutes)

    val profile = Profile(
        id = 1,
        name = "Алексей Соколов",
        photo = "https://i.pravatar.cc/200?img=12",
        status = "меньше интерфейса, больше жизни",
        city = "Москва",
        counters = mapOf(
            "friends" to 248,
            "followers" to 1240,
            "photos" to 87,
            "audios" to 312,
            "videos" to 14,
        ),
    )

    fun feed(): List<FeedPost> = listOf(
        FeedPost(
            id = 1,
            author = "Тима Ким",
            avatar = "https://i.pravatar.cc/100?img=13",
            date = minutesAgo(14),
            text = "Собрал ленту из трёх экранов и ни одного баннера. Оказывается, так тоже можно.",
            image = "https://picsum.photos/seed/vkmini1/900/600",
            likes = 128,
            comments = 14,
            reposts = 6,
        ),
        FeedPost(
            id = 2,
            author = "Дизайн-клуб",
            avatar = "https://i.pravatar.cc/100?img=32",
            date = minutesAgo(48),
            text = "Material You на Android 13: один цвет обоев — и весь интерфейс уже другой. Динамическая палитра, аккуратные отступы, маленькие кнопки.",
            image = "https://picsum.photos/seed/vkmini2/900/600",
            likes = 342,
            comments = 27,
            reposts = 19,
        ),
        FeedPost(
            id = 3,
            author = "Лена Ветрова",
            avatar = "https://i.pravatar.cc/100?img=47",
            date = minutesAgo(120),
            text = "Утро, кофе и плейлист без рекомендаций. Просто музыка, которую я выбрала сама.",
            image = null,
            likes = 56,
            comments = 8,
            reposts = 1,
        ),
        FeedPost(
            id = 4,
            author = "Музыка на каждый день",
            avatar = "https://i.pravatar.cc/100?img=8",
            date = minutesAgo(300),
            text = "Тихий плейлист на вечер: инструментал, лоу-фай и немного джаза.",
            image = "https://picsum.photos/seed/vkmini3/900/600",
            likes = 501,
            comments = 43,
            reposts = 72,
        ),
        FeedPost(
            id = 5,
            author = "Аня Смирнова",
            avatar = "https://i.pravatar.cc/100?img=25",
            date = minutesAgo(600),
            text = "Переставила телеграм-каналы в отдельную папку, удалила лишние уведомления. Дышится легче.",
            image = null,
            likes = 74,
            comments = 11,
            reposts = 3,
        ),
        FeedPost(
            id = 6,
            author = "VK Mini",
            avatar = null,
            date = minutesAgo(900),
            text = "Демо-режим: лента, чаты, музыка и профиль работают на тестовых данных. Токен не нужен.",
            image = null,
            likes = 12,
            comments = 0,
            reposts = 0,
        ),
    )

    fun conversations(): List<Conversation> = listOf(
        Conversation(1001, "Тима Ким", "https://i.pravatar.cc/100?img=13", "Скинул макет, глянь вечером", minutesAgo(4), 2),
        Conversation(1002, "Аня Смирнова", "https://i.pravatar.cc/100?img=25", "Договорились 👌", minutesAgo(38), 0),
        Conversation(1003, "Дизайн-клуб", "https://i.pravatar.cc/100?img=32", "Фото", minutesAgo(95), 1),
        Conversation(1004, "Мама", "https://i.pravatar.cc/100?img=44", "Позвони, как освободишься", minutesAgo(180), 0),
        Conversation(1005, "Лена Ветрова", "https://i.pravatar.cc/100?img=47", "Плейлист топ, особенно третий", minutesAgo(420), 0),
        Conversation(1006, "Музыка на каждый день", "https://i.pravatar.cc/100?img=8", "Новый сборник уже в ленте", minutesAgo(1440), 0),
    )

    fun history(peerId: Long): List<ChatMessage> {
        val base = System.currentTimeMillis() / 1000
        val script = when (peerId) {
            1001L -> listOf(
                false to "Привет! Собрал лёгкий вариант интерфейса, без лишнего.",
                true to "О, давай посмотрю. Кнопки реально меньше стали?",
                false to "Да, нижняя панель 60 dp, карточки компактные.",
                false to "Скинул макет, глянь вечером",
            )
            1002L -> listOf(
                false to "Привет! Сможешь завтра в 11?",
                true to "Да, вполне.",
                false to "Отлично, тогда до встречи",
                true to "Договорились 👌",
            )
            1003L -> listOf(
                false to "Выложили подборку про динамические цвета.",
                true to "Уже вижу в ленте, спасибо!",
            )
            else -> listOf(
                false to "Привет! Как дела?",
                true to "Всё хорошо, а у тебя?",
                false to "Тоже отлично 🙂",
            )
        }
        return script.mapIndexed { index, (out, text) ->
            ChatMessage(
                id = index.toLong(),
                peerId = peerId,
                out = out,
                text = text,
                date = base - (script.size - index) * 120L,
            )
        }
    }

    fun tracks(): List<Track> = listOf(
        Track(1, "Mira", "Night Drive", 214, null),
        Track(2, "Low Light", "Тихий район", 187, null),
        Track(3, "Сеня Лав", "Пусто и легко", 242, null),
        Track(4, "Kite", "Paper Planes", 198, null),
        Track(5, "Оля Мир", "Небо в кармане", 226, null),
        Track(6, "Ambient Room", "Soft Focus", 301, null),
        Track(7, "Джаз на кухне", "Кофе в 8 утра", 175, null),
        Track(8, "Nova", "After Rain", 233, null),
    )
}
