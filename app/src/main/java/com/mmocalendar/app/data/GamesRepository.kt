package com.mmocalendar.app.data

import java.time.LocalDate

enum class MmoStatus(val label: String) {
    RELEASED("Вышла"),
    EARLY_ACCESS("Ранний доступ"),
    SOON("Скоро"),
    TARGET("Окно релиза"),
    TBA("TBA"),
    CANCELLED("Заморожена")
}

data class MmoGame(
    val id: String,
    val title: String,
    val developer: String,
    val publisher: String,
    /** Человекочитаемая дата для UI: "5 окт 2026", "Q4 2026", "TBA" */
    val dateLabel: String,
    /** Для сортировки и обратного отсчёта. null = TBA */
    val sortDate: LocalDate?,
    val status: MmoStatus,
    val platforms: List<String>,
    /** F2P / B2P / P2P / Подписка */
    val monetization: String,
    val genre: String,
    val description: String,
    /** 1..5 уровень хайпа */
    val hype: Int,
    val site: String
)

object GamesRepository {
    val games: List<MmoGame> = listOf(
        MmoGame(
            id = "dune",
            title = "Dune: Awakening",
            developer = "Funcom",
            publisher = "Funcom",
            dateLabel = "10 июн 2025",
            sortDate = LocalDate.of(2025, 6, 10),
            status = MmoStatus.RELEASED,
            platforms = listOf("PC", "PS5", "Xbox"),
            monetization = "B2P",
            genre = "Survival MMO",
            description = "Выживалка-MMO по Дюне на Арракисе: открытый мир, спайс, песчаные черви, строительство баз, политика Ландсраада и рейды в глубокой пустыне. Главный AAA-релиз 2025 года в жанре.",
            hype = 5,
            site = "https://www.duneawakening.com"
        ),
        MmoGame(
            id = "quinfall",
            title = "The Quinfall",
            developer = "Vawraek Technology",
            publisher = "Vawraek",
            dateLabel = "6 фев 2026",
            sortDate = LocalDate.of(2026, 2, 6),
            status = MmoStatus.RELEASED,
            platforms = listOf("PC"),
            monetization = "B2P",
            genre = "Песочница MMORPG",
            description = "Турецкая песочница с огромным бесшовным миром, кораблями, крафтом и PvP. Вышла из раннего доступа в феврале 2026. Нишевый хит для фанатов олдскула.",
            hype = 3,
            site = "https://store.steampowered.com/app/2356640/"
        ),
        MmoGame(
            id = "crimson",
            title = "Crimson Desert",
            developer = "Pearl Abyss",
            publisher = "Pearl Abyss",
            dateLabel = "19 мар 2026",
            sortDate = LocalDate.of(2026, 3, 19),
            status = MmoStatus.RELEASED,
            platforms = listOf("PC", "PS5", "Xbox", "Mac"),
            monetization = "B2P",
            genre = "Open-World Action / MMO-элементы",
            description = "Флагман Pearl Abyss (авторы Black Desert). Сюжетный open-world на континенте Пайвел: Клифф и Серые Гривы. Вышла 19 марта 2026, предзагрузка за 48 часов. Главный релиз весны.",
            hype = 5,
            site = "https://crimsondesert.pearlabyss.com"
        ),
        MmoGame(
            id = "wow-midnight",
            title = "WoW: Midnight",
            developer = "Blizzard",
            publisher = "Blizzard",
            dateLabel = "мар 2026",
            sortDate = LocalDate.of(2026, 3, 3),
            status = MmoStatus.RELEASED,
            platforms = listOf("PC"),
            monetization = "B2P + Подписка",
            genre = "MMORPG / Дополнение",
            description = "Второе дополнение Worldsoul Saga: Кель'Талас, Плеть, housing. Обязательный релиз для всех фанатов WoW.",
            hype = 5,
            site = "https://worldofwarcraft.com"
        ),
        MmoGame(
            id = "tl-frozen",
            title = "Throne and Liberty: Frozen Divide",
            developer = "NCSOFT",
            publisher = "Amazon Games",
            dateLabel = "25 июн 2026",
            sortDate = LocalDate.of(2026, 6, 25),
            status = MmoStatus.RELEASED,
            platforms = listOf("PC", "PS5", "Xbox"),
            monetization = "F2P",
            genre = "MMORPG",
            description = "Крупное расширение T&L: снежный регион, рейды, осады. Игра live с октября 2024, лучшая F2P-точка входа на консолях.",
            hype = 4,
            site = "https://www.playthroneandliberty.com"
        ),
        MmoGame(
            id = "gw2-voe",
            title = "Guild Wars 2: Visions of Eternity",
            developer = "ArenaNet",
            publisher = "NCSOFT",
            dateLabel = "28 окт 2025",
            sortDate = LocalDate.of(2025, 10, 28),
            status = MmoStatus.RELEASED,
            platforms = listOf("PC"),
            monetization = "B2P",
            genre = "MMORPG / Дополнение",
            description = "Новое дополнение GW2: острова, элитные спеки, рейды. Эталон B2P без подписки.",
            hype = 4,
            site = "https://www.guildwars2.com"
        ),
        MmoGame(
            id = "stars-reach",
            title = "Stars Reach",
            developer = "Playable Worlds (Раф Костер)",
            publisher = "Playable Worlds",
            dateLabel = "18 авг 2026 · EA",
            sortDate = LocalDate.of(2026, 8, 18),
            status = MmoStatus.EARLY_ACCESS,
            platforms = listOf("PC"),
            monetization = "Paid EA",
            genre = "Sci-Fi Песочница",
            description = "Песочница от создателя Ultima Online Рафа Костера. Живая симуляция планет, терраформинг. В платном раннем доступе в Steam с августа 2026.",
            hype = 4,
            site = "https://store.steampowered.com"
        ),
        MmoGame(
            id = "blue-protocol-rs",
            title = "Blue Protocol: Star Resonance",
            developer = "Bandai Namco / Tencent (Bokura)",
            publisher = "Tencent",
            dateLabel = "окт 2025 · CN / 2026 глобал",
            sortDate = LocalDate.of(2025, 10, 8),
            status = MmoStatus.EARLY_ACCESS,
            platforms = listOf("PC", "Mobile"),
            monetization = "F2P",
            genre = "Anime MMORPG",
            description = "Перезапуск Blue Protocol под крылом Tencent. Аниме-экшен MMO, Китай — октябрь 2025, глобал ожидается в 2026. Главный кандидат в топ для любителей гача-эстетики.",
            hype = 4,
            site = "https://www.blueprotocol.jp"
        ),
        MmoGame(
            id = "aion2",
            title = "AION 2",
            developer = "NCSOFT",
            publisher = "NCSOFT",
            dateLabel = "5 окт 2026",
            sortDate = LocalDate.of(2026, 10, 5),
            status = MmoStatus.SOON,
            platforms = listOf("PC"),
            monetization = "Уточняется",
            genre = "MMORPG",
            description = "Полноценный сиквел Aion на UE5: крылья, фракции Элиос/Асмодея, осады, данжи. Ранний доступ для Founder's Pack с 30 сен, глобальный F2P-запуск 5 окт 2026 в Steam и Purple. БЛИЖАЙШИЙ AAA-релиз!",
            hype = 5,
            site = "https://aion2.plaync.com"
        ),
        MmoGame(
            id = "wow-forever",
            title = "WoW: Forever",
            developer = "Blizzard",
            publisher = "Blizzard",
            dateLabel = "4 ноя 2026",
            sortDate = LocalDate.of(2026, 11, 4),
            status = MmoStatus.SOON,
            platforms = listOf("PC"),
            monetization = "Подписка",
            genre = "MMORPG / Новая ветка",
            description = "Третья ветка WoW, анонсирована на BlizzCon 2026: развитие классического мира 2004 года, новые зоны и рейды (Barrow Deeps, Hyjal). Бета 17 сен, резервация ников 27 окт, запуск 4 ноя. Включена в подписку.",
            hype = 5,
            site = "https://worldofwarcraft.com"
        ),
        MmoGame(
            id = "archeage-chronicles",
            title = "ArcheAge Chronicles",
            developer = "XL Games",
            publisher = "Kakao Games",
            dateLabel = "Q4 2026 · EA",
            sortDate = LocalDate.of(2026, 11, 15),
            status = MmoStatus.TARGET,
            platforms = listOf("PC", "PS5", "Xbox"),
            monetization = "Paid EA",
            genre = "MMORPG",
            description = "Наследник ArcheAge (раньше ArcheAge 2). Действие спустя 50 лет. Платный ранний доступ в Steam в Q4 2026, демо Next Fest 19–26 окт. Паблишер вернулся к XL Games.",
            hype = 5,
            site = "https://store.steampowered.com"
        ),
        MmoGame(
            id = "corepunk",
            title = "Corepunk",
            developer = "Artificial Core",
            publisher = "Artificial Core",
            dateLabel = "Q4 2026",
            sortDate = LocalDate.of(2026, 12, 1),
            status = MmoStatus.TARGET,
            platforms = listOf("PC"),
            monetization = "B2P",
            genre = "MMORPG / MOBA-камера",
            description = "Изометрическая MMO с туманом войны как в MOBA. Долгострой, сейчас в EA, полный релиз заявлен на Q4 2026.",
            hype = 3,
            site = "https://store.steampowered.com"
        ),
        MmoGame(
            id = "eve-vanguard",
            title = "EVE Vanguard",
            developer = "CCP / Fenris Creations",
            publisher = "CCP",
            dateLabel = "10 ноя 2026 · Alpha",
            sortDate = LocalDate.of(2026, 11, 10),
            status = MmoStatus.SOON,
            platforms = listOf("PC"),
            monetization = "Paid Alpha",
            genre = "MMOFPS",
            description = "Шутер во вселенной EVE. Сквозная 24/7 Alpha с 10 ноя 2026 в EVE Launcher (платный доступ, 3 карты). Steam EA / бета — Q3 2027.",
            hype = 4,
            site = "https://www.evevanguard.com"
        ),
        MmoGame(
            id = "endfield",
            title = "Arknights: Endfield",
            developer = "Hypergryph",
            publisher = "Hypergryph",
            dateLabel = "22 янв 2026",
            sortDate = LocalDate.of(2026, 1, 22),
            status = MmoStatus.RELEASED,
            platforms = listOf("PC", "Mobile", "PS5"),
            monetization = "F2P (gacha)",
            genre = "Online Action RPG",
            description = "Спин-офф Arknights: строительство базы, автоматизация, кооп. Релиз январь 2026. Не чистая MMORPG, но крупная онлайн-игра для трекера.",
            hype = 4,
            site = "https://endfield.hypergryph.com"
        ),
        MmoGame(
            id = "ffxiv-evercold",
            title = "FFXIV: Evercold (8.0)",
            developer = "Square Enix",
            publisher = "Square Enix",
            dateLabel = "янв 2027",
            sortDate = LocalDate.of(2027, 1, 15),
            status = MmoStatus.TARGET,
            platforms = listOf("PC", "PS5", "Xbox"),
            monetization = "B2P + Подписка",
            genre = "MMORPG / Дополнение",
            description = "Восьмое дополнение FFXIV, анонс на FanFest Anaheim 2026: Четвёртое Отражение, новые джобы Reborn/Evolved. Цель — январь 2027.",
            hype = 5,
            site = "https://finalfantasyxiv.com"
        ),
        MmoGame(
            id = "chrono",
            title = "Chrono Odyssey",
            developer = "Chrono Studio",
            publisher = "Kakao Games",
            dateLabel = "Q2 2027",
            sortDate = LocalDate.of(2027, 5, 15),
            status = MmoStatus.TARGET,
            platforms = listOf("PC", "PS5", "Xbox"),
            monetization = "B2P",
            genre = "Action MMORPG",
            description = "Тёмное фэнтези на UE5 с манипуляцией временем: заморозка врагов, откат HP. После смешанной беты июня 2025 игру трижды переносили: Q4 2026 → Q1 2027 → Q2 2027. Новый тест — конец 2026.",
            hype = 5,
            site = "https://store.steampowered.com"
        ),
        MmoGame(
            id = "soulframe",
            title = "Soulframe",
            developer = "Digital Extremes",
            publisher = "Digital Extremes",
            dateLabel = "2026–2027 · Preludes",
            sortDate = LocalDate.of(2026, 12, 15),
            status = MmoStatus.EARLY_ACCESS,
            platforms = listOf("PC", "PS5"),
            monetization = "F2P",
            genre = "Co-op Fantasy MMO",
            description = "Фэнтезийная сестра Warframe: медленный ближний бой, духи природы, открытый мир. Сейчас в фазе Preludes (Founder-паки), полный релиз ждут в 2026–2027.",
            hype = 4,
            site = "https://www.soulframe.com"
        ),
        MmoGame(
            id = "paxdei",
            title = "Pax Dei",
            developer = "Mainframe Industries",
            publisher = "Mainframe",
            dateLabel = "EA с 2024",
            sortDate = LocalDate.of(2024, 6, 18),
            status = MmoStatus.EARLY_ACCESS,
            platforms = listOf("PC"),
            monetization = "B2P",
            genre = "Social Sandbox MMO",
            description = "Социальная песочница: кланы, строительство деревень, PvE/PvP-зоны. В раннем доступе с июня 2024, полный релиз ориентировочно 2026.",
            hype = 3,
            site = "https://www.paxdei.com"
        ),
        MmoGame(
            id = "ashes",
            title = "Ashes of Creation",
            developer = "Intrepid Studios",
            publisher = "Intrepid Studios",
            dateLabel = "Заморожена · 2026",
            sortDate = null,
            status = MmoStatus.CANCELLED,
            platforms = listOf("PC"),
            monetization = "—",
            genre = "MMORPG",
            description = "Самый амбициозный долгострой (ноды, караваны, осады). В феврале 2026 снята с продажи в Steam после закрытия студии Intrepid. Статус в трекере — заморожена, чтобы ты не ждал зря.",
            hype = 2,
            site = "https://ashesofcreation.com"
        ),
        MmoGame(
            id = "gw3",
            title = "Guild Wars 3",
            developer = "ArenaNet",
            publisher = "NCSOFT",
            dateLabel = "Бета осень 2027",
            sortDate = LocalDate.of(2027, 10, 1),
            status = MmoStatus.TBA,
            platforms = listOf("PC", "PS5"),
            monetization = "B2P",
            genre = "MMORPG",
            description = "Анонс Summer Game Fest 2026: приквел на UE5 за 1000+ лет до GW1, континент Орр. Seamless-мир, паркур, отрываемые части боссов. Первая бета — осень 2027, релиз — 2028.",
            hype = 5,
            site = "https://www.guildwars.com"
        ),
        MmoGame(
            id = "riot-mmo",
            title = "Riot MMO (LoL Universe)",
            developer = "Riot Games",
            publisher = "Riot Games",
            dateLabel = "TBA",
            sortDate = null,
            status = MmoStatus.TBA,
            platforms = listOf("PC"),
            monetization = "TBA",
            genre = "MMORPG",
            description = "Безымянная MMO по вселенной League of Legends (Рунтерра). Подтверждена страницей вакансий Riot. Дат и платформ нет — главный 'слон в комнате' жанра.",
            hype = 5,
            site = "https://www.riotgames.com"
        ),
        MmoGame(
            id = "lotr-amazon",
            title = "The Lord of the Rings MMO",
            developer = "Amazon Games + Embracer",
            publisher = "Amazon Games",
            dateLabel = "TBA · 2027+",
            sortDate = null,
            status = MmoStatus.TBA,
            platforms = listOf("PC", "PS5", "Xbox"),
            monetization = "TBA",
            genre = "MMORPG",
            description = "Новая MMO по Властелину Колец от Amazon (после отмены версии 2021 года). В активной разработке, окно — не раньше 2027–2028.",
            hype = 4,
            site = "https://www.amazongames.com"
        ),
        MmoGame(
            id = "ananta",
            title = "Ananta (Project Mugen)",
            developer = "NetEase / Naked Rain",
            publisher = "NetEase",
            dateLabel = "нач. 2027",
            sortDate = LocalDate.of(2027, 2, 15),
            status = MmoStatus.TARGET,
            platforms = listOf("PC", "Mobile", "PS5"),
            monetization = "F2P",
            genre = "Urban Open-World RPG / MMO-элементы",
            description = "Городская аниме-игра: паркур, паутина, машины, кооп и MMO-активности. Китайский хит ожидания, глобал ждут в начале 2027.",
            hype = 4,
            site = "https://www.ananta.game"
        ),
        MmoGame(
            id = "etl-ec",
            title = "Eternal Tombs",
            developer = "Triune Studios",
            publisher = "Triune Studios",
            dateLabel = "нач. 2027",
            sortDate = LocalDate.of(2027, 3, 1),
            status = MmoStatus.TARGET,
            platforms = listOf("PC"),
            monetization = "Подписка",
            genre = "Hardcore Sandbox MMORPG",
            description = "Хардкорная песочница с живыми мастерами мира (Tomb Masters): ручные ивенты, погода, боссы. Лут только ингредиентами, экипировка — крафт. Подписка + только косметика. Демо — окт 2026.",
            hype = 3,
            site = "https://store.steampowered.com"
        ),
    )

    fun daysUntil(game: MmoGame, today: LocalDate = LocalDate.now()): Long? {
        val d = game.sortDate ?: return null
        return d.toEpochDay() - today.toEpochDay()
    }

    fun countdownLabel(game: MmoGame, today: LocalDate = LocalDate.now()): String {
        if (game.status == MmoStatus.CANCELLED) return "Заморожена"
        if (game.status == MmoStatus.RELEASED || game.status == MmoStatus.EARLY_ACCESS) return game.status.label
        val d = game.sortDate ?: return game.status.label
        val diff = d.toEpochDay() - today.toEpochDay()
        return when {
            diff < 0 -> "Вышла?"
            diff == 0L -> "Сегодня!"
            diff == 1L -> "Завтра!"
            diff < 30 -> "Через $diff дн."
            diff < 365 -> "Через ${diff / 30} мес."
            else -> game.dateLabel
        }
    }
}
