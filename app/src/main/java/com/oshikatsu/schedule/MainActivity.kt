package com.oshikatsu.schedule

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val Yellow = Color(0xFFF7C928)
private val LightScheme = lightColorScheme(
    primary = Yellow, onPrimary = Color(0xFF3A2C00),
    primaryContainer = Color(0xFFFFEC9A), onPrimaryContainer = Color(0xFF4A3B12),
    background = Color(0xFFFFF3B8), onBackground = Color(0xFF4A3B12),
    surface = Color(0xFFFFFDF0), onSurface = Color(0xFF4A3B12),
    surfaceVariant = Color(0xFFFFEC9A), onSurfaceVariant = Color(0xFF86743A),
    outline = Color(0xFFF0DC82),
)
private val DarkScheme = darkColorScheme(
    primary = Yellow, onPrimary = Color(0xFF2A2000),
    primaryContainer = Color(0xFF4D431D), onPrimaryContainer = Color(0xFFFFF3C4),
    background = Color(0xFF2A2410), onBackground = Color(0xFFFFF3C4),
    surface = Color(0xFF3A3218), onSurface = Color(0xFFFFF3C4),
    surfaceVariant = Color(0xFF4D431D), onSurfaceVariant = Color(0xFFCDBD80),
    outline = Color(0xFF5A4D22),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("oshi", MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme) {
                App(prefs)
            }
        }
    }
}

@Composable
fun App(prefs: SharedPreferences) {
    var mine by remember { mutableStateOf(prefs.getStringSet("mine", emptySet())!!.toSet()) }
    var tab by remember { mutableIntStateOf(0) }

    val toggle = { id: String ->
        mine = if (id in mine) mine - id else mine + id
        prefs.edit().putStringSet("mine", mine).apply()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(Modifier.padding(pad)) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("リスト") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("カレンダー") })
            }
            if (tab == 0) ListScreen(mine, toggle) else CalendarScreen(mine, toggle)
        }
    }
}
