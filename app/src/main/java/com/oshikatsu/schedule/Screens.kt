package com.oshikatsu.schedule

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth

private val SaleColor = Color(0xFFF08C00)
private val PopupColor = Color(0xFFE64980)
private fun Kind.color() = if (this == Kind.SALE) SaleColor else PopupColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventCard(ev: Event, today: LocalDate, remind: Boolean, onToggle: () -> Unit) {
    val ctx = LocalContext.current
    val left = ev.leftLabel(today)
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (left == "終了") 0.5f else 1f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    ev.date.jp() + if (ev.end != null) "〜" else "",
                    fontWeight = FontWeight.Black, fontSize = 18.sp,
                )
                Text(
                    left,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 10.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(ev.title, fontWeight = FontWeight.Bold)
            Text(ev.place, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(
                Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ev.tags.forEach {
                    Text(
                        it,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                    )
                }
            }
            if (ev.note.isNotEmpty()) {
                Text(ev.note, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (remind) {
                    Button(onClick = onToggle, modifier = Modifier.weight(1f)) { Text("リマインド中") }
                } else {
                    OutlinedButton(
                        onClick = onToggle, modifier = Modifier.weight(1f),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    ) { Text("リマインドする") }
                }
                OutlinedButton(
                    onClick = {
                        try {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ev.calendarUrl())))
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                ) { Text("カレンダーに追加") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListScreen(mine: Set<String>, onToggle: (String) -> Unit) {
    val today = remember { LocalDate.now() }
    var filter by remember { mutableStateOf("all") }
    val rows = EVENTS
        .filter {
            when (filter) {
                "sale" -> it.kind == Kind.SALE
                "popup" -> it.kind == Kind.POPUP
                "mine" -> it.id in mine
                else -> true
            }
        }
        .sortedBy { it.date }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("推し活スケジュール", fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text(
                "ちいかわの発売日・イベント・購入特典",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "すべて", "sale" to "発売", "popup" to "ポップアップ", "mine" to "リマインド中")
                    .forEach { (k, label) ->
                        FilterChip(selected = filter == k, onClick = { filter = k }, label = { Text(label) })
                    }
            }
        }
        if (rows.isEmpty()) {
            item { Text("該当する予定はありません。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(rows, key = { it.id }) { ev ->
            EventCard(ev, today, ev.id in mine) { onToggle(ev.id) }
        }
    }
}

@Composable
fun CalendarScreen(mine: Set<String>, onToggle: (String) -> Unit) {
    val today = remember { LocalDate.now() }
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var sel by remember { mutableStateOf(today) }
    val offset = month.atDay(1).dayOfWeek.value % 7
    val weeks = (offset + month.lengthOfMonth() + 6) / 7
    val dayEvents = EVENTS.filter { it.on(sel) }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { month = month.minusMonths(1) }) { Text("‹", fontSize = 24.sp) }
                Text("${month.year}年${month.monthValue}月", fontSize = 18.sp, fontWeight = FontWeight.Black)
                TextButton(onClick = { month = month.plusMonths(1) }) { Text("›", fontSize = 24.sp) }
            }
            Row(Modifier.fillMaxWidth()) {
                listOf("日", "月", "火", "水", "木", "金", "土").forEach {
                    Text(
                        it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            for (w in 0 until weeks) {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (c in 0 until 7) {
                        val dayNum = w * 7 + c - offset + 1
                        if (dayNum < 1 || dayNum > month.lengthOfMonth()) {
                            Box(Modifier.weight(1f).height(52.dp))
                        } else {
                            val d = month.atDay(dayNum)
                            val evs = EVENTS.filter { it.on(d) }
                            val selected = d == sel
                            Column(
                                Modifier
                                    .weight(1f).height(52.dp)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(12.dp),
                                    )
                                    .clickable { sel = d }
                                    .padding(top = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    dayNum.toString(), fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    evs.take(3).forEach {
                                        Box(Modifier.size(6.dp).background(it.kind.color(), CircleShape))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(SaleColor, CircleShape))
                    Text(" 発売", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(PopupColor, CircleShape))
                    Text(" ポップアップ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(sel.jp(), fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(top = 18.dp))
            if (dayEvents.isEmpty()) {
                Text("この日の予定はありません。", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(dayEvents, key = { it.id }) { ev ->
            EventCard(ev, today, ev.id in mine) { onToggle(ev.id) }
        }
    }
}
