package com.mmocal.app.data

import android.content.Context
import com.mmocal.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

data class RepoState(
    val events: List<GameEvent>,
    val dataVersion: Int,
    val loading: Boolean,
    val source: String
)

data class RemoteInfo(
    val json: String,
    val dataVersion: Int,
    val appCode: Int,
    val appName: String,
    val apkUrl: String
)

object GamesRepository {

    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day)

    private val defaults: List<GameEvent> = listOf(
        GameEvent(
            id = "quinfall",
            title = "The Quinfall",
            developer = "Vawraek Technology",
            date = d(2026, 2, 6),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC),
            model = AccessModel.B2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2294660/7f61cc7df834b3b684ba63d33d0a14b491a69d22/header.jpg?t=1773702812",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2294660/ss_5df1b87428b0f29f61cb74b411045662e68640a2.1920x1080.jpg?t=1773702812",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2294660/ss_bcfbbb08be80c857485f0de7c910ec1b4dba80a6.1920x1080.jpg?t=1773702812",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2294660/ss_d16e83ee9cdc8eb0d00989dc6a60e7e02ffbcc52.1920x1080.jpg?t=1773702812"
            ),
            iconRes = R.drawable.steam_quinfall,
            description = "Крупномасштабный открытый мир без экранов загрузки, класс-система с свободной комбинацией веток навыков, экшн-бой с уклонением и отменой навыков. Полный релиз в Steam после раннего доступа."
        ),
        GameEvent(
            id = "rok-classic",
            title = "Ragnarok Origin Classic",
            developer = "Gravity",
            date = d(2026, 3, 26),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.MOBILE),
            model = AccessModel.F2P,
            iconRes = R.drawable.play_roc,
            description = "Возвращение к оригинальному Ragnarok Online: 2D-изометрия, карты, джобы и система карт, пересобранные на современный клиент с кросс-платформенным прогрессом."
        ),
        GameEvent(
            id = "nTE",
            title = "Neverness to Everness",
            developer = "Hotta Studio",
            date = d(2026, 4, 29),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.MOBILE, Platform.PLAYSTATION),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4508340/2865c1f08217110a549e039a81a6461d8dc3c1d0/header.jpg?t=1790722094",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4508340/b0b3fc2d349d956c2e0f12cc06d454e2c1f424aa/ss_b0b3fc2d349d956c2e0f12cc06d454e2c1f424aa.1920x1080.jpg?t=1790722094",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4508340/ccbd3770ad07099395806a6286e39cfab2c64d51/ss_ccbd3770ad07099395806a6286e39cfab2c64d51.1920x1080.jpg?t=1790722094",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4508340/f2423fcacb5bd4db76076f75ac60af579bec8dd8/ss_f2423fcacb5bd4db76076f75ac60af579bec8dd8.1920x1080.jpg?t=1790722094"
            ),
            iconRes = R.drawable.steam_nte,
            description = "Открытый мир в духе городского мегаполиса, гача-RPG с MMO-элементами. Глобальный запуск 29 апреля, позже вышла в Steam, Epic и Samsung."
        ),
        GameEvent(
            id = "sao-echoes",
            title = "Sword Art Online: Echoes of Aincrad",
            developer = "Game Studio Inc.",
            date = d(2026, 7, 10),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            model = AccessModel.B2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2244210/25fb6350451b21ca824562c8d1eebe89091347cc/header.jpg?t=1791165838",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2244210/4e631acbbd04158ff72f0c727ed3ac5b8c030ffc/ss_4e631acbbd04158ff72f0c727ed3ac5b8c030ffc.1920x1080.jpg?t=1791165838",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2244210/84aae791793dca0e234ed509e64c6ed4759ccd76/ss_84aae791793dca0e234ed509e64c6ed4759ccd76.1920x1080.jpg?t=1791165838",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2244210/6e1a35fbd3758c1c550c329de77a56c5d968ae37/ss_6e1a35fbd3758c1c550c329de77a56c5d968ae37.1920x1080.jpg?t=1791165838"
            ),
            iconRes = R.drawable.steam_sao,
            description = "ММО по вселенной SAO с прогрессом в башне Аинкрада. Один из немногих релизов 2026 года, вышедших одновременно на PC и консолях."
        ),
        GameEvent(
            id = "bellatores-cbt",
            title = "Bellatores — корейский ЗБТ",
            developer = "NYOU",
            date = d(2026, 7, 30),
            type = EventType.BETA,
            platforms = setOf(Platform.PC),
            model = AccessModel.UNKNOWN,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3397040/82799d310214bc9d512926b9c83b7837d173c2a2/header.jpg?t=1754226056",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3397040/ad418734516ca8d0e74de91d7b3c8f66068eb9c3/ss_ad418734516ca8d0e74de91d7b3c8f66068eb9c3.1920x1080.jpg?t=1754226056",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3397040/c24c2d19ce150569e1f2187b112af632295a4716/ss_c24c2d19ce150569e1f2187b112af632295a4716.1920x1080.jpg?t=1754226056",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3397040/2c4a5f47979378e78b6bb2be8b8fde80d0e987d4/ss_2c4a5f47979378e78b6bb2be8b8fde80d0e987d4.1920x1080.jpg?t=1754226056"
            ),
            iconRes = R.drawable.steam_bellatores,
            note = "Пятидневный ЗБТ в Steam, только Корея",
            description = "Корейский closed beta test медивальной ММО Bellatores в Steam: 30 июля – 3 августа. Два из пяти Великих Домов, не-таргетовый бой и прогрессия без классов и уровней."
        ),
        GameEvent(
            id = "eq-legends",
            title = "EverQuest Legends",
            developer = "Game Jawn",
            date = d(2026, 7, 28),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC),
            model = AccessModel.SUBSCRIPTION,
            iconUrl = "https://www-cdn.everquest.com/images/logos/eq_logo_clear.png?v=3866388639",
            description = "Classic+ версия EverQuest, ориентированная на соло-прохождение. Стоимость $19.99 плюс подписка."
        ),
        GameEvent(
            id = "stars-reach",
            title = "Stars Reach",
            developer = "Playable Worlds",
            date = d(2026, 8, 18),
            type = EventType.EARLY_ACCESS,
            platforms = setOf(Platform.PC),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1925650/fd1ff2ba489eee371a4b1b9466ed7b109bbabab8/header.jpg?t=1787078415",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1925650/a15a27782feae62f2cd0b5cdf28fa4fbd1101599/ss_a15a27782feae62f2cd0b5cdf28fa4fbd1101599.1920x1080.jpg?t=1787078415",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1925650/dd5b65f6303321306684178545f14700dc9c6069/ss_dd5b65f6303321306684178545f14700dc9c6069.1920x1080.jpg?t=1787078415",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1925650/920027b80fdae38dbad3bb2e2bf3ebd4e8093ff3/ss_920027b80fdae38dbad3bb2e2bf3ebd4e8093ff3.1920x1080.jpg?t=1787078415"
            ),
            iconRes = R.drawable.steam_starsreach,
            description = "Сайфай-песочница от Рэфа Костера, ведущего дизайнера Ultima Online. Процедурно генерируемые планеты, terraforming и игроко-управляемая экономика. Платный ранний доступ, полный запуск как F2P впереди."
        ),
        GameEvent(
            id = "eclipse",
            title = "Eclipse: The Awakening",
            developer = "NPIXEL",
            date = d(2026, 9, 10),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.MOBILE),
            model = AccessModel.F2P,
            description = "MMOLite от Smilegate: роль персонажа определяется оружием, а не классом, система Sanctuary качает ресурсы даже офлайн. Пока только Южная Корея."
        ),
        GameEvent(
            id = "aria-eternal",
            title = "Legends of Aria Eternal",
            developer = "Emergent Worlds",
            date = d(2026, 9, 10),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC),
            model = AccessModel.B2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3944440/8aa1793addeda3d654b1cac90eecd1c159af9b6a/header.jpg?t=1791060561",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3944440/7e9670cf9f8ac656e09ffb05637ab1328da74d19/ss_7e9670cf9f8ac656e09ffb05637ab1328da74d19.1920x1080.jpg?t=1791060561",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3944440/4a506aa2a5ae30318d6e79281918557018719b83/ss_4a506aa2a5ae30318d6e79281918557018719b83.1920x1080.jpg?t=1791060561",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3944440/d54c408c3f4a40b5fbce7ec7609fa4908efb5590/ss_d54c408c3f4a40b5fbce7ec7609fa4908efb5590.1920x1080.jpg?t=1791060561"
            ),
            iconRes = R.drawable.steam_aria,
            description = "Навыковая full-loot песочница вернулась под руководством нового студийного дома оригинального автора. Любой владелец может поднять легитимный сервер."
        ),
        GameEvent(
            id = "dragonwilds",
            title = "RuneScape: Dragonwilds",
            developer = "Jagex",
            date = d(2026, 9, 15),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX, Platform.SWITCH),
            model = AccessModel.B2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1374490/3e27915a647dbf05e17f2ff98d33c9a0765da539/header.jpg?t=1791278745",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1374490/8f802b85a8f1002967f4943f76e1d645d1ffb061/ss_8f802b85a8f1002967f4943f76e1d645d1ffb061.1920x1080.jpg?t=1791278745",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1374490/ae64662c060ee16121ab3783bd39efb21f8827a1/ss_ae64662c060ee16121ab3783bd39efb21f8827a1.1920x1080.jpg?t=1791278745",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1374490/d16ec143fa679838d2a88c25798becde17990500/ss_d16ec143fa679838d2a88c25798becde17990500.1920x1080.jpg?t=1791278745"
            ),
            iconRes = R.drawable.steam_dragonwilds,
            description = "Сёрвайвл-крафтинг по мотивам RuneScape вышел из раннего доступа в 1.0 на PC, PS5, Xbox Series и Nintendo Switch 2 — первый RuneScape на нинтендо-железе."
        ),
        GameEvent(
            id = "aniimo",
            title = "Aniimo",
            developer = "Pawprint Studio",
            date = d(2026, 9, 16),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX, Platform.MOBILE),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4126040/bf9b76d25ac135baf8f313d9a095e754fd2e950b/header.jpg?t=1790849962",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4126040/c6c6dfe1260dd953b538e83298e257c4ca3c2e9a/ss_c6c6dfe1260dd953b538e83298e257c4ca3c2e9a.1920x1080.jpg?t=1790849962",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4126040/25c0c7e6c5c5fd530c056ee032b7ba366fb81c0c/ss_25c0c7e6c5c5fd530c056ee032b7ba366fb81c0c.1920x1080.jpg?t=1790849962",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4126040/a2c7b9293be7978b904bb672d49ede44d5bb52f7/ss_a2c7b9293be7978b904bb672d49ede44d5bb52f7.1920x1080.jpg?t=1790849962"
            ),
            iconRes = R.drawable.steam_aniimo,
            description = "Большой запуск сентября: ловля существ и превращение в них для полётов, ныряния и подкопов. Более 40 миллионов предрегистраций, кросс-платформенный прогресс."
        ),
        GameEvent(
            id = "architect",
            title = "Architect: Land of Exiles",
            developer = "Aqua Tree",
            date = d(2026, 9, 16),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.MOBILE),
            model = AccessModel.F2P,
            description = "UE5-ММО с не-таргетовым боем и пятью классами, тяжёлой автоматизацией и офлайн-режимом. Глобальный регион открыт отдельно от корейского сервиса."
        ),
        GameEvent(
            id = "velmora",
            title = "Velmora Online",
            developer = "Beledrian",
            date = d(2026, 9, 18),
            type = EventType.EARLY_ACCESS,
            platforms = setOf(Platform.PC),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4026300/95ede1d1beabe00ca7523097585fb85f53e35d33/header.jpg?t=1789750653",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4026300/db959bcfc3d9c80aa63c2adc28e08b5d99d97ef5/ss_db959bcfc3d9c80aa63c2adc28e08b5d99d97ef5.1920x1080.jpg?t=1789750653",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4026300/60ef2fa00eeac94a8f14f9a71aeb5c8817691695/ss_60ef2fa00eeac94a8f14f9a71aeb5c8817691695.1920x1080.jpg?t=1789750653",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4026300/a41e4d18752156cae3d20ee309c0a0492a4cd314/ss_a41e4d18752156cae3d20ee309c0a0492a4cd314.1920x1080.jpg?t=1789750653"
            ),
            iconRes = R.drawable.steam_velmora,
            description = "3D-трибьют Argentum Online от аргентинского соло-разработчика: девять классов, клановые территории и PvP за точки. Early Access рассчитан на 12–24 месяца."
        ),
        GameEvent(
            id = "laryen",
            title = "Laryen",
            developer = "WITHCENTER",
            date = d(2026, 9, 21),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.MAC, Platform.MOBILE),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142990/03f66d215dd8d831f0511cb32f4adb9539b5bfd6/header.jpg?t=1791383261",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142990/77885dfd68ca3391b6abb4a0710d8567aae5cd51/ss_77885dfd68ca3391b6abb4a0710d8567aae5cd51.1920x1080.jpg?t=1791383261",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142990/dcaebd37e3c83092f2b213cde738d7c92e11cbf6/ss_dcaebd37e3c83092f2b213cde738d7c92e11cbf6.1920x1080.jpg?t=1791383261",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142990/9f9e6c571cb1d05823aba5472d3dc8199036eebb/ss_9f9e6c571cb1d05823aba5472d3dc8199036eebb.1920x1080.jpg?t=1791383261"
            ),
            iconRes = R.drawable.steam_laryen,
            description = "2.5D изометрическая сайфай-ММО: киборги отбирают у робо-ИИ города, вдохновлённые Нью-Дели, Лахором, Даккой и Манилой. Голосовой ИИ-компаньон Лария."
        ),
        GameEvent(
            id = "scapewatch",
            title = "Scapewatch: Idle MMO",
            developer = "Puzzle Drop Studios",
            date = d(2026, 9, 25),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.BROWSER, Platform.MOBILE),
            model = AccessModel.B2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4671380/c3195a5ab8b2d8d36b9bf166f6db8c5bd26d0439/header.jpg?t=1790357247",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4671380/f55093336bb3326e83f65858c3255d9978d0e3a8/ss_f55093336bb3326e83f65858c3255d9978d0e3a8.1920x1080.jpg?t=1790357247",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4671380/b49d69548426fed1b4743d9473d3efd604fb0485/ss_b49d69548426fed1b4743d9473d3efd604fb0485.1920x1080.jpg?t=1790357247",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4671380/4cf88d86a51df24da80621b25a150781c67b32da/ss_4cf88d86a51df24da80621b25a150781c67b32da.1920x1080.jpg?t=1790357247"
            ),
            iconRes = R.drawable.steam_scapewatch,
            description = "Idle-ММО в духе RuneScape: 30 навыков качаются серверной очередью задач даже офлайн, плюс кланы, рейды и хайскоры."
        ),
        GameEvent(
            id = "aion2-lst",
            title = "AION 2 — Launch Scale Test",
            developer = "NCSoft",
            date = d(2026, 9, 17),
            type = EventType.BETA,
            platforms = setOf(Platform.PC),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/10aa4b096ebe6af0d1dd7882e5ba004271aef6f7/header.jpg?t=1791205225",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/b453274cca4a9f7db77c1fb40d8863a14e36c0e1/ss_b453274cca4a9f7db77c1fb40d8863a14e36c0e1.1920x1080.jpg?t=1791205225",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/a96f14cfe58c052192f5425a154f389069ddd7ca/ss_a96f14cfe58c052192f5425a154f389069ddd7ca.1920x1080.jpg?t=1791205225",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/465ab5d757ed9966074098dc1a57af1a297397f8/ss_465ab5d757ed9966074098dc1a57af1a297397f8.1920x1080.jpg?t=1791205225"
            ),
            iconRes = R.drawable.steam_aion2,
            note = "Открытый двухдневный тест без ключей",
            description = "Финальный масштабный тест серверов за две недели до раннего доступа: открыт для всех в Steam и PURPLE, максимальный уровень ограничен 37."
        ),
        GameEvent(
            id = "aion2-ea",
            title = "AION 2 — Founder's Early Access",
            developer = "NCSoft",
            date = d(2026, 9, 30),
            type = EventType.EARLY_ACCESS,
            platforms = setOf(Platform.PC),
            model = AccessModel.MIXED,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/10aa4b096ebe6af0d1dd7882e5ba004271aef6f7/header.jpg?t=1791205225",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/b453274cca4a9f7db77c1fb40d8863a14e36c0e1/ss_b453274cca4a9f7db77c1fb40d8863a14e36c0e1.1920x1080.jpg?t=1791205225",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/a96f14cfe58c052192f5425a154f389069ddd7ca/ss_a96f14cfe58c052192f5425a154f389069ddd7ca.1920x1080.jpg?t=1791205225",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/465ab5d757ed9966074098dc1a57af1a297397f8/ss_465ab5d757ed9966074098dc1a57af1a297397f8.1920x1080.jpg?t=1791205225"
            ),
            iconRes = R.drawable.steam_aion2,
            note = "Только для владельцев Founder's Pack ($24.99 / $49.99 / $99.99)",
            description = "Пятидневный ранний доступ глобальных серверов AION 2 для покупателей пакетов основателя. Все три пакета дают одинаковые даты старта."
        ),
        GameEvent(
            id = "aion2",
            title = "AION 2",
            developer = "NCSoft",
            date = d(2026, 10, 5),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/10aa4b096ebe6af0d1dd7882e5ba004271aef6f7/header.jpg?t=1791205225",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/b453274cca4a9f7db77c1fb40d8863a14e36c0e1/ss_b453274cca4a9f7db77c1fb40d8863a14e36c0e1.1920x1080.jpg?t=1791205225",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/a96f14cfe58c052192f5425a154f389069ddd7ca/ss_a96f14cfe58c052192f5425a154f389069ddd7ca.1920x1080.jpg?t=1791205225",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3393110/465ab5d757ed9966074098dc1a57af1a297397f8/ss_465ab5d757ed9966074098dc1a57af1a297397f8.1920x1080.jpg?t=1791205225"
            ),
            iconRes = R.drawable.steam_aion2,
            note = "Глобальный запуск в Steam и PURPLE",
            description = "Главный релиз осени: Unreal Engine 5, мир в 36 раз крупнее оригинала, полёты как ключевая механика и Abyss-PvP. Одновременный запуск в Америке, Европе и Азии, бесплатный для всех."
        ),
        GameEvent(
            id = "msc-founders",
            title = "MapleStory Classic World — Founder's Access",
            developer = "Nexon",
            date = d(2026, 10, 6),
            type = EventType.EARLY_ACCESS,
            platforms = setOf(Platform.PC, Platform.MAC),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142560/b70b9fceef8e25c68a070c3f08a5aca1dcad6fb4/header.jpg?t=1791330252",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142560/5f8c4395617c97211838b6b257a2632615cd8c1f/ss_5f8c4395617c97211838b6b257a2632615cd8c1f.1920x1080.jpg?t=1791330252",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142560/99bdb7d52572de865d109c4c763dcedede859442/ss_99bdb7d52572de865d109c4c763dcedede859442.1920x1080.jpg?t=1791330252",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142560/579c72a4d457324ae4e40a9c7cb97ada97f6a6ad/ss_579c72a4d457324ae4e40a9c7cb97ada97f6a6ad.1920x1080.jpg?t=1791330252"
            ),
            note = "Прогресс переносится в полный релиз",
            description = "Старт доступа для покупателей Founder's Package — возвращение к до-биг-банговому MapleStory: остров Мейпл и Виктория, четыре базовых класса."
        ),
        GameEvent(
            id = "msc-launch",
            title = "MapleStory Classic World",
            developer = "Nexon",
            date = d(2026, 10, 21),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.MAC),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142560/b70b9fceef8e25c68a070c3f08a5aca1dcad6fb4/header.jpg?t=1791330252",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142560/5f8c4395617c97211838b6b257a2632615cd8c1f/ss_5f8c4395617c97211838b6b257a2632615cd8c1f.1920x1080.jpg?t=1791330252",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142560/99bdb7d52572de865d109c4c763dcedede859442/ss_99bdb7d52572de865d109c4c763dcedede859442.1920x1080.jpg?t=1791330252",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/5142560/579c72a4d457324ae4e40a9c7cb97ada97f6a6ad/ss_579c72a4d457324ae4e40a9c7cb97ada97f6a6ad.1920x1080.jpg?t=1791330252"
            ),
            note = "Grand Launch — играть сможет каждый",
            description = "Полный бесплатный запуск классического MapleStory без опыта и силовых предметов в кэш-шопе — только косметика и удобство."
        ),
        GameEvent(
            id = "broken-ranks-android",
            title = "Broken Ranks — Android",
            developer = "Whitemoon Games",
            date = d(2026, 10, 21),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.MOBILE),
            model = AccessModel.F2P,
            iconRes = R.drawable.play_brokenranks,
            note = "Пошаговая ММО на Android, предрегистрация открыта",
            description = "Grimdark-ММО с пошаговым боем переносится на Android. Версия для ПК уже работает, мобильный релиз — 21 октября."
        ),
        GameEvent(
            id = "loftia",
            title = "Loftia",
            developer = "Loftia Team",
            date = d(2026, 11, 3),
            type = EventType.EARLY_ACCESS,
            platforms = setOf(Platform.PC),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2320980/header.jpg?t=1791432295",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2320980/ss_ddaefddeca7a2d6ce2f365defdacd42d93ff6e46.1920x1080.jpg?t=1791432295",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2320980/731c9c0511024853c8b5f964b64f2ed63b5d0018/ss_731c9c0511024853c8b5f964b64f2ed63b5d0018.1920x1080.jpg?t=1791432295",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2320980/947ef55c506e443b56dba3cd81c07efa34ab5ebc/ss_947ef55c506e443b56dba3cd81c07efa34ab5ebc.1920x1080.jpg?t=1791432295"
            ),
            iconRes = R.drawable.steam_loftia,
            note = "Cozy-ММО про создание города мечты",
            description = "Уютная ММО с фокусом на строительстве, сообществе и повседневной жизни. Дата раннего доступа подтверждена на 3 ноября."
        ),
        GameEvent(
            id = "wow-forever",
            title = "WoW: Forever",
            developer = "Blizzard",
            date = d(2026, 11, 4),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.MAC),
            model = AccessModel.SUBSCRIPTION,
            iconUrl = "https://blz-contentstack-images.akamaized.net/v3/assets/blt9c12f249ac15c7ec/bltb5a24e5ab1e2cfb0/6a88e3589b942efdb6f74110/wow-thumbnail-homepage.jpg",
            note = "Свежие классики-серверы, включены в подписку WoW",
            description = "Запуск новых классических серверов Blizzard. Свежая экономика без установленного золотого запаса; Blizzard ужесточает борьбу с покупкой золота за реальные деньги."
        ),
        GameEvent(
            id = "poe2-10",
            title = "Path of Exile 2 — релиз 1.0",
            developer = "Grinding Gear Games",
            date = d(2026, 12, 11),
            type = EventType.LAUNCH,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2694490/24eeddcbda17903f03d819588757e40845f8115f/header.jpg?t=1787697213",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2694490/f4676d0b868f78aef63d15833a1d96afc76dc8d2/ss_f4676d0b868f78aef63d15833a1d96afc76dc8d2.1920x1080.jpg?t=1787697213",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2694490/1ce58962d3ffb1a824a42dd2ca0ead64c7597fdf/ss_1ce58962d3ffb1a824a42dd2ca0ead64c7597fdf.1920x1080.jpg?t=1787697213",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2694490/07c21f3e884059d85bc2ed0213d71cd99a5499b5/ss_07c21f3e884059d85bc2ed0213d71cd99a5499b5.1920x1080.jpg?t=1787697213"
            ),
            iconRes = R.drawable.steam_poe2,
            note = "Старт в 20:00 UTC, новая лига сбрасывает экономику",
            description = "Полноценный релиз Path of Exile 2 после длительного раннего доступа. Первые дни лиги — пик цен на орбы и валюту из-за сброса торговой экономики."
        ),
        GameEvent(
            id = "archeage-chronicles",
            title = "ArcheAge Chronicles — Early Access",
            developer = "XL Games",
            date = null,
            windowLabel = "Q4 2026",
            type = EventType.EARLY_ACCESS,
            confirmed = false,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            model = AccessModel.MIXED,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3218230/header.jpg?t=1789535623",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3218230/ss_68ffe4125cf76539f9193a4fadfa9c7361ea52f9.1920x1080.jpg?t=1789535623",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3218230/ss_b4f19cf7cd7ac9af51c08e19bac6134bb491abb6.1920x1080.jpg?t=1789535623",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3218230/ss_483dc2f86fd1a09a9550625c6a3feae45a3910a5.1920x1080.jpg?t=1789535623"
            ),
            iconRes = R.drawable.steam_archeage,
            description = "Возвращение в Аурию на UE5: экшн-бой, сюжет, исследование, крафт, жильё и торговля. Целевое окно — платный ранний доступ в четвёртом квартале, точной даты нет."
        ),
        GameEvent(
            id = "bellatores",
            title = "Bellatores",
            developer = "NYOU",
            date = null,
            windowLabel = "2026",
            type = EventType.LAUNCH,
            confirmed = false,
            platforms = setOf(Platform.PC),
            model = AccessModel.UNKNOWN,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3397040/82799d310214bc9d512926b9c83b7837d173c2a2/header.jpg?t=1754226056",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3397040/ad418734516ca8d0e74de91d7b3c8f66068eb9c3/ss_ad418734516ca8d0e74de91d7b3c8f66068eb9c3.1920x1080.jpg?t=1754226056",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3397040/c24c2d19ce150569e1f2187b112af632295a4716/ss_c24c2d19ce150569e1f2187b112af632295a4716.1920x1080.jpg?t=1754226056",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3397040/2c4a5f47979378e78b6bb2be8b8fde80d0e987d4/ss_2c4a5f47979378e78b6bb2be8b8fde80d0e987d4.1920x1080.jpg?t=1754226056"
            ),
            iconRes = R.drawable.steam_bellatores,
            note = "В Steam «Coming soon» после летних ЗБТ",
            description = "Медиевальная open-world ММО про пять благородных Домов: не-таргетовый бой, прогрессия без классов и уровней, крафтовая экономика. Дата релиза не объявлена, после ЗБТ в июле и очевидной волны тестов."
        ),
        GameEvent(
            id = "eternal-tombs",
            title = "Eternal Tombs",
            developer = "Triune Studios",
            date = null,
            windowLabel = "Начало 2027",
            type = EventType.LAUNCH,
            confirmed = false,
            platforms = setOf(Platform.PC),
            model = AccessModel.UNKNOWN,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1668340/28ea5bac40b4fe39f1d3e79165bf246a46fabe94/header_alt_assets_0.jpg?t=1791304352",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1668340/6e576183ae4f22e3837c043ddd8711caa098dbc4/ss_6e576183ae4f22e3837c043ddd8711caa098dbc4.1920x1080.jpg?t=1791304352",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1668340/0ca051f49c04d41fa6af97573edecf13b6767e02/ss_0ca051f49c04d41fa6af97573edecf13b6767e02.1920x1080.jpg?t=1791304352",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1668340/573cccaa8b06c23dc39cae3c417142ed4a485ebf/ss_573cccaa8b06c23dc39cae3c417142ed4a485ebf.1920x1080.jpg?t=1791304352"
            ),
            iconRes = R.drawable.steam_eternaltombs,
            description = "Хардкорная ММО с фокусом на опасность и потерю прогресса. Демо на October Steam Fest запускается в октябре и продолжится до релиза."
        ),
        GameEvent(
            id = "chrono-odyssey",
            title = "Chrono Odyssey",
            developer = "NPIXEL",
            date = null,
            windowLabel = "Q2 2027",
            type = EventType.LAUNCH,
            confirmed = false,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            model = AccessModel.UNKNOWN,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2873440/de48f2d77db5eb14226739d563fdd68332c39e30/header.jpg?t=1788925974",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2873440/ss_e751db5e229413f520c06c6acd845b3585da8cae.1920x1080.jpg?t=1788925974",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2873440/ss_aab3d8e7659e7dd916099abb51380e66cb526f25.1920x1080.jpg?t=1788925974",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2873440/ss_4c66126229c0c2e674ad4f02e66c163b468caea5.1920x1080.jpg?t=1788925974"
            ),
            iconRes = R.drawable.steam_chrono,
            description = "Амбициозная ММО Kakao Games, неоднократно переносившаяся. В августе 2026 дата сдвинута с Q1 на второй квартал 2027 года."
        ),
        GameEvent(
            id = "crimson-desert",
            title = "Crimson Desert",
            developer = "Pearl Abyss",
            date = null,
            windowLabel = "Q4 2026",
            type = EventType.LAUNCH,
            confirmed = false,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            model = AccessModel.B2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3321460/236f3814be7a97d86831800691b6096d449222a8/header.jpg?t=1789020763",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3321460/667d1763ae26137aafbc3140963621f530b43289/ss_667d1763ae26137aafbc3140963621f530b43289.1920x1080.jpg?t=1789020763",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3321460/b154a083ff9a746c71a1513334042e1bb9403a8b/ss_b154a083ff9a746c71a1513334042e1bb9403a8b.1920x1080.jpg?t=1789020763",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3321460/286938f408f5f0c6409f49584f33e8d497433123/ss_286938f408f5f0c6409f49584f33e8d497433123.1920x1080.jpg?t=1789020763"
            ),
            note = "Главный AAA-релиз года",
            description = "Флагман Pearl Abyss на BlackSpace Engine: открытый мир континента Пайвел, экшн-бой с захватами и окружением, сюжетная кампания наёмника Клиффа плюс MMO-активности. Главный претендент на звание AAA-MMORPG года."
        ),
        GameEvent(
            id = "ashes-of-creation",
            title = "Ashes of Creation",
            developer = "Intrepid Studios",
            date = null,
            windowLabel = "2027",
            type = EventType.EARLY_ACCESS,
            confirmed = false,
            platforms = setOf(Platform.PC),
            model = AccessModel.SUBSCRIPTION,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4124950/fe6e847b6846e1e3734843a236ae2c9b3817e3de/header.jpg?t=1773949146",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4124950/a31ee47c30783b9fc9c10c227ee0462f6fb8e6a1/ss_a31ee47c30783b9fc9c10c227ee0462f6fb8e6a1.1920x1080.jpg?t=1773949146",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4124950/03cd89ffcab0427902ca353b7677ece94968d253/ss_03cd89ffcab0427902ca353b7677ece94968d253.1920x1080.jpg?t=1773949146",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4124950/dab1ee8ca13942b427770c0c6e30d54a63b866ba/ss_dab1ee8ca13942b427770c0c6e30d54a63b866ba.1920x1080.jpg?t=1773949146"
            ),
            note = "Alpha Two идёт, вайпы",
            description = "Классическая AAA-MMORPG с нодами, караванами и осадами: мир меняется от действий игроков. Сейчас в стадии Alpha Two с периодическими вайпами, полный ранний доступ ожидается в 2027."
        ),
        GameEvent(
            id = "dune-awakening-ch2",
            title = "Dune: Awakening — Глава 2",
            developer = "Funcom",
            date = null,
            windowLabel = "Q4 2026",
            type = EventType.LAUNCH,
            confirmed = false,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            model = AccessModel.B2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1172710/c23906beee3fdf1cd48980f340a2b76a3590d765/header_alt_assets_9.jpg?t=1790856134",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1172710/c4f058fb805eefa9f07f1cb3a695a9534ba1920a/ss_c4f058fb805eefa9f07f1cb3a695a9534ba1920a.1920x1080.jpg?t=1790856134",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1172710/ae8340a47868d75e3d29ee6038eb4159ee13dd5c/ss_ae8340a47868d75e3d29ee6038eb4159ee13dd5c.1920x1080.jpg?t=1790856134",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1172710/ss_8616aceb30a8b01979c16a5aea46f9ef587f7ff3.1920x1080.jpg?t=1790856134"
            ),
            description = "Сурвайвал-MMO по Дюне: Арракис, спайс, песчаные черви и гильдейские войны. После PC-релиза 2025 года выходят консоли и вторая глава с новым контентом эндгейма."
        ),
        GameEvent(
            id = "tl-expansion",
            title = "Throne and Liberty — дополнение",
            developer = "NCSoft",
            date = null,
            windowLabel = "Q4 2026",
            type = EventType.LAUNCH,
            confirmed = false,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2429640/497cc638fa3186f1aeefddaff89eaaf6f0c2e7dd/header.jpg?t=1787851215",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2429640/9ead63ab900cce6a79501f9cc7995d0654ee4b46/ss_9ead63ab900cce6a79501f9cc7995d0654ee4b46.1920x1080.jpg?t=1787851215",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2429640/38605158143335d34933cb3768338356cfb4bb5c/ss_38605158143335d34933cb3768338356cfb4bb5c.1920x1080.jpg?t=1787851215",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2429640/beb4bf53c3088b16661943fe9f0daff1c45377d9/ss_beb4bf53c3088b16661943fe9f0daff1c45377d9.1920x1080.jpg?t=1787851215"
            ),
            description = "Флагманская F2P-MMORPG NCSoft / Amazon Games: осады замков, морфы в животных, кросс-плей PC и консолей. Осенью ожидается крупное дополнение с новой зоной и рейдом."
        ),
        GameEvent(
            id = "pax-dei-launch",
            title = "Pax Dei",
            developer = "Mainframe Industries",
            date = null,
            windowLabel = "2026",
            type = EventType.LAUNCH,
            confirmed = false,
            platforms = setOf(Platform.PC),
            model = AccessModel.MIXED,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1995520/de17115127459af00a30c24bdd516af4d5f40fd9/header.jpg?t=1789563288",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1995520/aca997eebc30ae8116a83e2798ceea4deabd56c4/ss_aca997eebc30ae8116a83e2798ceea4deabd56c4.1920x1080.jpg?t=1789563288",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1995520/242b269841762873efa60af0f6e395091f3c7f8d/ss_242b269841762873efa60af0f6e395091f3c7f8d.1920x1080.jpg?t=1789563288",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1995520/5b9e53bfd47fb9803321a83c810a2eed2689bcca/ss_5b9e53bfd47fb9803321a83c810a2eed2689bcca.1920x1080.jpg?t=1789563288"
            ),
            description = "Социальная песочница в средневековом мире: клановые земли, строительство деревень, крафтовая экономика и PvP за долины. Вышла в ранний доступ в 2024, полный релиз ожидается в 2026."
        ),
        GameEvent(
            id = "soulframe",
            title = "Soulframe",
            developer = "Digital Extremes",
            date = null,
            windowLabel = "2027",
            type = EventType.BETA,
            confirmed = false,
            platforms = setOf(Platform.PC, Platform.PLAYSTATION, Platform.XBOX),
            model = AccessModel.F2P,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4095380/3fb8986f870e6c87bb0da141b590b667056c5e59/header.jpg?t=1783724341",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4095380/6db41c46256a6a5de9d6b690a165764f8d6e8c5f/ss_6db41c46256a6a5de9d6b690a165764f8d6e8c5f.1920x1080.jpg?t=1783724341",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4095380/a05d1061794948be6d1fc8a75be71bead51eb196/ss_a05d1061794948be6d1fc8a75be71bead51eb196.1920x1080.jpg?t=1783724341",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4095380/f6dc348355aa38eb1b54303297b5b0785d2eea4a/ss_f6dc348355aa38eb1b54303297b5b0785d2eea4a.1920x1080.jpg?t=1783724341"
            ),
            description = "Кооперативная фэнтези-MMO от авторов Warframe: медленный ближний бой, духи предков и процедурные подземелья. Закрытые прелюдии идут, открытая бета ожидается в 2027."
        ),
        GameEvent(
            id = "gw3-beta",
            title = "Guild Wars 3 — бета",
            developer = "ArenaNet",
            date = null,
            windowLabel = "Осень 2027",
            type = EventType.BETA,
            confirmed = false,
            platforms = setOf(Platform.PC),
            model = AccessModel.UNKNOWN,
            iconUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4743930/99b550da44eff3a9ba1fb82d17d1fac4ebbcd8ea/header.jpg?t=1781101414",
            screenshots = listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4743930/9f98bf41411abf4dfa77b3331c7f0bca25aeca77/ss_9f98bf41411abf4dfa77b3331c7f0bca25aeca77.1920x1080.jpg?t=1781101414",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4743930/3d3d6e936fe71b7ae3de9768c4aa106ad646e3a7/ss_3d3d6e936fe71b7ae3de9768c4aa106ad646e3a7.1920x1080.jpg?t=1781101414",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/4743930/fc47ffaea45b20aa07f88054524e33e3101fc395/ss_fc47ffaea45b20aa07f88054524e33e3101fc395.1920x1080.jpg?t=1781101414"
            ),
            iconRes = R.drawable.steam_gw3,
            description = "Официально анонсированный экшн-сиквел с упором на перемещение, моментум-бой и ветвящиеся скиллы. Бета — осенью 2027 года."
        )
    ).sortedBy { it.date ?: LocalDate.of(2099, 1, 1) }

    const val DATA_URL = "https://mmocal-data.surge.sh/data.json"

    // Свой канал обновлений: этот файл правится в репозитории (имя/код/APK),
    // приложение подхватит новую версию само. DATA_URL остаётся запасным.
    const val UPDATE_JSON_URL =
        "https://raw.githubusercontent.com/Sargarus666/PlayGround/main/mmo-calendar/version.json"
    private const val UPDATE_JSON_FALLBACK_URL =
        "https://raw.githubusercontent.com/Sargarus666/PlayGround/opencode/37813996751/mmo-calendar/version.json"

    /**
     * Встроенный патч медиаданных: закрывает игры, у которых в remote data.json
     * пустые iconUrl/screenshots (иначе в списке буквы-заглушки вместо иконок).
     * Каждая ссылка проверена (HTTP 200 + content-type image). Применяется поверх remote
     * и кэша: заполняет только пустые поля, ничего не затирает.
     */
    private val mediaPatch: Map<String, Pair<String?, List<String>>> = buildMap {
        put("architect", Pair(
            "https://architectgb.drimage.com/assets/common/images/meta_image.jpg?v=1",
            listOf(
                "https://d1blk5vyk1w9fz.cloudfront.net/banner/82bb966337f5496eb33bf44ae3e7f429.png",
                "https://d1blk5vyk1w9fz.cloudfront.net/banner/46adce6a772e4fc8bcef405aef880c8a.png",
                "https://d1blk5vyk1w9fz.cloudfront.net/banner/d340daf2dc6b466bb89027f79a7b1d4d.png",
            )
        ))
        put("eclipse", Pair(
            "https://static-pubcomm.onstove.com/live/template/STOVE_ECLIPSE/common/icon.png",
            listOf(
                "https://static-pubcomm.onstove.com/live/template/STOVE_ECLIPSE/common/img_og_ko.jpg",
                "https://static-pubcomm.onstove.com/live/template/STOVE_ECLIPSE/banner/08cadab57440434ebc22d15b5f9cc8e3.png",
                "https://static-pubcomm.onstove.com/live/template/stove_eclipse/210/1/meta/img_og_ko.jpg",
            )
        ))
        put("rok-classic", Pair(
            "https://file.joymaker.com/game/rooc/web/1200X630en.jpg",
            listOf(
                "https://play-lh.googleusercontent.com/0cn0bTZFSa3iH2qMAqyjW7zxnvi-MhM6OgsqggRfr0qMGB4uuy-Zbq2q7IqSRMpJz33ietOTt_KprpjNlV7YBSE=w1052-h592",
                "https://play-lh.googleusercontent.com/T4VJqCGqFS_UfYksJqxIfdKFpIB1aCJfhzAWrVa46sFLPx7Htw54AEKLLpm5cSP5x5zsO6BTJntvRh9SUtvnkA=w1052-h592",
                "https://play-lh.googleusercontent.com/hZlYrwqN-2Qc-9UhQtmoycnfreNIr5NpiDauH-_p7oJ710s01vBObymnSnKiFtgboYgrVv7UvjatT9Lg5TaY=w1052-h592",
            )
        ))
        put("broken-ranks-android", Pair(
            "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1475870/header.jpg",
            listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1475870/ss_57a92805b6099bd4666cfc91efe7d26a5b5ec445.1920x1080.jpg?t=1747995796",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1475870/ss_f4479e7c46604ffcd829d7c6b31d4cdef0f3a967.1920x1080.jpg?t=1747995796",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1475870/ss_ef8f03cf57f65090a66ad35946fee922aebf4bbb.1920x1080.jpg?t=1747995796",
            )
        ))
        put("eq-legends", Pair(
            null,
            listOf(
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/205710/ss_0d93697a60bbd2b08260c9aea625b41d1a18b8ba.1920x1080.jpg?t=1779211487",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/205710/ss_55732612116a82dda8864e8514b7fd855946b2e9.1920x1080.jpg?t=1779211487",
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/205710/ss_c7024ee8a2609c6ee25ce61a11b2cd89c85ae1de.1920x1080.jpg?t=1779211487",
            )
        ))
        put("wow-forever", Pair(
            null,
            listOf(
                "https://blz-contentstack-images.akamaized.net/v3/assets/blt3452e3b114fab0cd/blt01562e0bb296fa2c/676256b74fdea3f5820c0672/S7JGAPLAOK5X1600204786872.jpg",
                "https://blz-contentstack-images.akamaized.net/v3/assets/blt3452e3b114fab0cd/blt02f7711bf8d938d2/675cdaaa8c2c1d5855faae6a/KZ0MUBHT5DE91534444135645.jpg",
                "https://blz-contentstack-images.akamaized.net/v3/assets/blt3452e3b114fab0cd/blt0682186da313f2b4/6761fb5c79b16342850d844f/R9QS6M11G70K1600380813759.jpg",
            )
        ))
    }

    private val defaultsById: Map<String, GameEvent> = defaults.associateBy { it.id }

    private val _state = MutableStateFlow(
        RepoState(defaults, 0, loading = false, source = "local")
    )
    val state: StateFlow<RepoState> = _state.asStateFlow()

    private var base: List<GameEvent> = defaults
    private var dataVersion: Int = 0
    private var userEvents: List<GameEvent> = emptyList()
    private var mmoBase: List<GameEvent> = emptyList()
    private var mmoCount: Int = 0
    private var cachePrefs: android.content.SharedPreferences? = null

    val events: List<GameEvent> get() = _state.value.events

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("remote_cache", Context.MODE_PRIVATE)
        cachePrefs = prefs
        Mmo13Source.init(context)
        SteamSource.init(context)
        val cached = prefs.getString("json", null)
        val version = prefs.getInt("data_version", 0)
        if (cached != null) {
            runCatching { parseJson(cached) }.onSuccess { (list, _) ->
                base = list
                dataVersion = version
            }
        }
        rebuild()
    }

    fun setUserEvents(list: List<GameEvent>) {
        userEvents = list
        rebuild()
    }

    // Не MMO / протухшие записи из remote-базы. Правится одной строкой.
    private val excludedIds = setOf("crimson-desert")

    private fun rebuild() {
        val cleanBase = base.filter { it.id !in excludedIds }
        val enriched = cleanBase.map { e -> enrichFromMmo(e, mmoBase) }
        val fresh = mmoBase.filter { m ->
            !Mmo13Source.isExcluded(m.title) &&
                Mmo13Source.passesGate(m.title, m.rating100) &&
                enriched.none { Mmo13Source.titlesMatch(m.title, it.title) }
        }
        val merged = (enriched + fresh + userEvents)
            .sortedBy { it.date ?: LocalDate.of(2099, 1, 1) }
        val src = buildList {
            if (base !== defaults) add("remote v$dataVersion") else add("local")
            if (mmoCount > 0) add("тесты: $mmoCount")
        }.joinToString(" + ")
        _state.value = RepoState(
            events = merged,
            dataVersion = dataVersion,
            loading = false,
            source = src
        )
    }

    private fun enrichFromMmo(e: GameEvent, tests: List<GameEvent>): GameEvent {
        val hit = tests.firstOrNull { Mmo13Source.titlesMatch(it.title, e.title) } ?: return e
        return e.copy(
            rating100 = e.rating100 ?: hit.rating100,
            aaa = e.aaa || hit.aaa,
            iconUrl = e.iconUrl ?: hit.iconUrl,
            genre = e.genre.ifBlank { hit.genre },
            publisher = e.publisher.ifBlank { hit.publisher },
            region = e.region.ifBlank { hit.region }
        )
    }

    fun mmoLastSync(): Long = cachePrefs?.getLong("mmo13_ts", 0) ?: 0

    suspend fun refreshMmo13(force: Boolean): Boolean {
        val prefs = cachePrefs ?: return false
        val stale = System.currentTimeMillis() - prefs.getLong("mmo13_ts", 0) > 24L * 60 * 60 * 1000
        if (!force && !stale && mmoBase.isNotEmpty()) return false
        _state.value = _state.value.copy(loading = true)
        return try {
            val list = Mmo13Source.fetchAll()
            mmoBase = list
            mmoCount = list.size
            prefs.edit().putLong("mmo13_ts", System.currentTimeMillis()).apply()
            rebuild()
            true
        } catch (_: Exception) {
            false
        } finally {
            if (!force) _state.value = _state.value.copy(loading = false)
        }
    }

    suspend fun fetchRemote(): RemoteInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(DATA_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 15000
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val root = JSONObject(text)
            RemoteInfo(
                json = text,
                dataVersion = root.optInt("dataVersion", 0),
                appCode = root.optJSONObject("appVersion")?.optInt("code", 0) ?: 0,
                appName = root.optJSONObject("appVersion")?.optString("name") ?: "",
                apkUrl = root.optJSONObject("appVersion")?.optString("apkUrl") ?: ""
            )
        }.getOrNull()
    }

    // Проверка обновлений самого приложения: свой version.json в репозитории
    // имеет приоритет, data.json на surge — запасной канал. Побеждает больший код.
    suspend fun fetchUpdateInfo(): RemoteInfo? = withContext(Dispatchers.IO) {
        val fromData = fetchRemote()
        val fromRepo = fetchVersionJson(UPDATE_JSON_URL)
            ?: fetchVersionJson(UPDATE_JSON_FALLBACK_URL)
        val best = listOfNotNull(fromData, fromRepo).maxByOrNull { it.appCode }
        if (best != null && best.appCode > 0) best else fromData
    }

    private fun fetchVersionJson(url: String): RemoteInfo? = runCatching {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        val root = JSONObject(text)
        RemoteInfo(
            json = "{}",
            dataVersion = 0,
            appCode = root.optInt("code", 0),
            appName = root.optString("name", ""),
            apkUrl = root.optString("apkUrl", "")
        )
    }.getOrNull()

    fun applyRemote(info: RemoteInfo) {
        val (list, version) = parseJson(info.json)
        if (version < dataVersion && version != 0) return
        base = list
        dataVersion = info.dataVersion
        cachePrefs?.edit()
            ?.putString("json", info.json)
            ?.putInt("data_version", info.dataVersion)
            ?.apply()
        rebuild()
    }

    suspend fun refresh(): Boolean {
        _state.value = _state.value.copy(loading = true)
        return try {
            val info = fetchRemote()
            var changed = false
            if (info != null && info.dataVersion > dataVersion) {
                applyRemote(info)
                changed = true
            }
            if (refreshMmo13(force = false)) changed = true
            changed
        } finally {
            _state.value = _state.value.copy(loading = false)
        }
    }

    private fun parseJson(text: String): Pair<List<GameEvent>, Int> {
        val root = JSONObject(text)
        val version = root.optInt("dataVersion", 0)
        val arr: JSONArray = root.getJSONArray("events")
        val list = ArrayList<GameEvent>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getString("id")
            val dateStr = o.optString("date").takeIf { it.isNotBlank() }
            val window = o.optString("windowLabel").takeIf { it.isNotBlank() }
            val platforms = mutableSetOf<Platform>()
            val pArr = o.optJSONArray("platforms") ?: JSONArray()
            for (j in 0 until pArr.length()) {
                runCatching { platforms.add(Platform.valueOf(pArr.getString(j))) }
            }
            list.add(
                GameEvent(
                    id = id,
                    title = o.getString("title"),
                    developer = o.optString("developer"),
                    date = dateStr?.let { LocalDate.parse(it) },
                    windowLabel = window,
                    type = runCatching { EventType.valueOf(o.getString("type")) }
                        .getOrDefault(EventType.LAUNCH),
                    confirmed = o.optBoolean("confirmed", true),
                    platforms = platforms,
                    model = runCatching { AccessModel.valueOf(o.optString("model", "UNKNOWN")) }
                        .getOrDefault(AccessModel.UNKNOWN),
                    description = o.optString("description"),
                    note = o.optString("note").takeIf { it.isNotBlank() },
                    iconRes = defaultsById[id]?.iconRes,
                    iconUrl = o.optString("iconUrl").takeIf { it.isNotBlank() }
                        ?: mediaPatch[id]?.first,
                    screenshots = runCatching {
                        val sArr = o.optJSONArray("screenshots") ?: JSONArray()
                        (0 until sArr.length()).map { sArr.getString(it) }
                    }.getOrDefault(emptyList())
                        .ifEmpty { mediaPatch[id]?.second ?: emptyList() }
                )
            )
        }
        require(list.isNotEmpty()) { "empty events" }
        return list to version
    }

    fun byId(id: String): GameEvent? = _state.value.events.find { it.id == id }

    fun onDate(date: LocalDate): List<GameEvent> = _state.value.events.filter { it.date == date }

    fun inMonth(year: Int, month: Int): List<GameEvent> =
        _state.value.events.filter { it.date?.year == year && it.date.monthValue == month }

    fun upcoming(today: LocalDate): List<GameEvent> =
        _state.value.events.filter { it.date != null && !it.date.isBefore(today) }

    fun recent(today: LocalDate, days: Long = 45): List<GameEvent> =
        _state.value.events.filter { e ->
            e.date != null && e.date.isBefore(today) &&
                java.time.temporal.ChronoUnit.DAYS.between(e.date, today) <= days
        }.sortedByDescending { it.date }

    fun undated(): List<GameEvent> = _state.value.events.filter { it.date == null }

    fun nextBigLaunch(today: LocalDate): GameEvent? =
        upcoming(today).firstOrNull { it.type == EventType.LAUNCH && it.confirmed && !it.custom }

    fun onDay(date: LocalDate): List<GameEvent> = _state.value.events.filter { it.date == date }
}
