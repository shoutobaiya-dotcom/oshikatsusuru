package com.oshikatsu.schedule

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Kind { SALE, POPUP }

data class Event(
    val id: String,
    val kind: Kind,
    val date: LocalDate,
    val end: LocalDate? = null,
    val title: String,
    val place: String,
    val tags: List<String>,
    val note: String = "",
)

// 今は固定データ。ステップ2で公式サイトからの取得に置き換える
val EVENTS = listOf(
    Event(
        "clinic1", Kind.SALE, LocalDate.of(2026, 11, 27),
        title = "ちいかわクリニック 第一弾",
        place = "ちいかわマーケット(11時〜)/ちいかわらんど/催事会場",
        tags = listOf(
            "ナース姿マスコット12種 各1,650〜1,980円",
            "1,100円ごとにホログラムステッカー",
            "8,800円以上で顔バッジ",
            "3,300円以上でオリジナルBOX配送",
        ),
        note = "購入特典は数量限定です。",
    ),
    Event(
        "clinic2", Kind.SALE, LocalDate.of(2026, 12, 11),
        title = "ちいかわクリニック 第二弾",
        place = "詳細は順次公開予定",
        tags = listOf("発売予定"),
    ),
    Event(
        "sapporo", Kind.POPUP, LocalDate.of(2026, 11, 27), LocalDate.of(2027, 1, 5),
        title = "ポップアップ 札幌アピア", place = "札幌アピア",
        tags = listOf("〜2027/1/5"), note = "予約が必要な場合があります。公式HPで確認を。",
    ),
    Event(
        "sendai", Kind.POPUP, LocalDate.of(2026, 11, 27), LocalDate.of(2026, 12, 20),
        title = "ポップアップ 仙台PARCO1", place = "仙台PARCO1",
        tags = listOf("〜12/20"), note = "予約が必要な場合があります。公式HPで確認を。",
    ),
    Event(
        "kumamoto", Kind.POPUP, LocalDate.of(2026, 11, 27), LocalDate.of(2027, 1, 11),
        title = "ポップアップ アミュプラザくまもと", place = "アミュプラザくまもと 2階 特設会場",
        tags = listOf("〜2027/1/11"), note = "予約が必要な場合があります。公式HPで確認を。",
    ),
)

private val DOW = arrayOf("日", "月", "火", "水", "木", "金", "土")

fun LocalDate.jp(): String = "${monthValue}月${dayOfMonth}日(${DOW[dayOfWeek.value % 7]})"

fun Event.on(day: LocalDate): Boolean = !day.isBefore(date) && !day.isAfter(end ?: date)

fun Event.leftLabel(today: LocalDate): String {
    val diff = ChronoUnit.DAYS.between(today, date)
    val e = end
    if (e != null && diff <= 0 && !e.isBefore(today)) return "開催中"
    return when {
        diff > 0 -> "あと${diff}日"
        diff == 0L -> "今日"
        else -> "終了"
    }
}

fun Event.calendarUrl(): String {
    val p = { d: LocalDate -> d.toString().replace("-", "") }
    val enc = { s: String -> java.net.URLEncoder.encode(s, "UTF-8") }
    return "https://calendar.google.com/calendar/render?action=TEMPLATE" +
        "&text=${enc(title)}&dates=${p(date)}/${p(date.plusDays(1))}&location=${enc(place)}"
}
