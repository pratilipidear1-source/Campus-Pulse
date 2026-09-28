package com.campuspulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Paper = Color(0xFFFBF9F6)
private val Ink = Color(0xFF1B1C1A)
private val Muted = Color(0xFF5D5960)
private val Lavender = Color(0xFFCFC4F5)
private val LavenderDark = Color(0xFF615983)
private val Coral = Color(0xFFFFDAD3)
private val CoralStrong = Color(0xFFFF8A73)
private val Sage = Color(0xFFB7E4C7)
private val Butter = Color(0xFFFFE6A8)

data class Venue(val name: String, val area: String, val percent: Int, val state: String, val accent: Color)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CampusPulseApp() }
    }
}

@Composable
private fun CampusPulseApp() {
    var screen by remember { mutableStateOf("onboarding") }
    var selected by remember { mutableStateOf("Green Library") }
    MaterialTheme(colorScheme = lightColorScheme(background = Paper, surface = Paper, primary = Ink)) {
        Surface(Modifier.fillMaxSize(), color = Paper) {
            when (screen) {
                "onboarding" -> Onboarding { screen = "home" }
                "home" -> Home({ screen = "map" }, { selected = it; screen = "detail" }, { screen = "checkin" })
                "map" -> LiveMap({ screen = "home" }, { selected = it; screen = "detail" })
                "detail" -> Detail(selected, { screen = "map" }, { screen = "checkin" })
                else -> CheckIn({ screen = "detail" }, { screen = "home" })
            }
        }
    }
}

@Composable private fun PulseLogo(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawCircle(LavenderDark, size.minDimension * .34f, center)
        drawCircle(Paper, size.minDimension * .25f, center)
        drawCircle(CoralStrong, size.minDimension * .19f, center)
        drawCircle(Paper, size.minDimension * .10f, center)
        drawCircle(Ink, size.minDimension * .055f, center)
    }
}

@Composable private fun Header(title: String, onBack: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Ink) }
        PulseLogo(Modifier.size(34.dp))
        Text(title, Modifier.padding(start = 8.dp).weight(1f), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Icon(Icons.Default.PersonOutline, "Profile", tint = Ink)
    }
}

@Composable private fun Onboarding(next: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ONBOARDING", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text("Step 01 / 03", fontSize = 11.sp, color = Muted)
        }
        Spacer(Modifier.height(38.dp))
        PetalGraphic(Modifier.size(260.dp))
        Spacer(Modifier.height(30.dp))
        Text("CAMPUS PULSE RADAR", Modifier.fillMaxWidth(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Muted)
        Text("Stop guessing.\nStart knowing.", Modifier.fillMaxWidth().padding(top = 10.dp), fontSize = 38.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold)
        Text("Real-time campus crowd radar, place telemetry, and quick check-ins delivered straight to your pocket.", Modifier.fillMaxWidth().padding(top = 14.dp), fontSize = 14.sp, lineHeight = 22.sp, color = Muted)
        Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniCard("Main Library", "18% Full", Sage)
            MiniCard("Dining Hall", "Rush Hour", Coral)
        }
        Spacer(Modifier.height(36.dp))
        Button(next, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text("Enter Campus Pulse", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable private fun PetalGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        repeat(8) { i ->
            val angle = Math.PI * i / 4
            val x = center.x + kotlin.math.cos(angle).toFloat() * size.minDimension * .22f
            val y = center.y + kotlin.math.sin(angle).toFloat() * size.minDimension * .22f
            drawCircle(if (i % 2 == 0) Lavender.copy(.65f) else Coral.copy(.7f), size.minDimension * .16f, androidx.compose.ui.geometry.Offset(x, y))
        }
        drawCircle(Ink, size.minDimension * .18f, center)
        drawCircle(Lavender, size.minDimension * .035f, center)
    }
}

@Composable private fun MiniCard(title: String, value: String, accent: Color) {
    Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(Color(0xFFF2F0ED)).padding(14.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
        Text(title, Modifier.padding(top = 10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, Modifier.padding(top = 3.dp), fontSize = 12.sp, color = Muted)
    }
}

private val venues = listOf(
    Venue("Green Library", "East Wing • 3F", 14, "EMPTY", Sage),
    Venue("Main Canteen", "Central Hub", 72, "PACKED", Coral),
    Venue("Campus Gym", "Sports Complex", 48, "OKAY", Butter),
    Venue("Printer Shop", "Academic Block", 31, "OKAY", Lavender),
    Venue("CS Labs", "North Block • 2F", 19, "EMPTY", Sage)
)

@Composable private fun Home(onMap: () -> Unit, onPlace: (String) -> Unit, onCheckIn: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Paper)) {
        Header("Live Radar")
        LazyColumn(Modifier.weight(1f).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("GOOD AFTERNOON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                Text("Where is the campus breathing?", Modifier.padding(top = 5.dp), fontSize = 30.sp, lineHeight = 33.sp, fontWeight = FontWeight.ExtraBold)
                Box(Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(26.dp)).background(Lavender.copy(.55f)).padding(20.dp)) {
                    Column {
                        Text("CAMPUS PULSE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                        Text("Mostly calm right now.", Modifier.padding(top = 8.dp), fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Library and labs have the most breathing room.", Modifier.padding(top = 6.dp), fontSize = 13.sp, color = Muted)
                        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Tag("5 places live", Color.White.copy(.8f)); Tag("Updated now", Color.White.copy(.8f))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Nearby pulse", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("View map", Modifier.clickable(onClick = onMap), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LavenderDark)
                }
            }
            items(venues) { venue -> VenueCard(venue) { onPlace(venue.name) } }
            item {
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallAction("Check in", Icons.Default.AddCircleOutline, onCheckIn)
                    SmallAction("Map", Icons.Default.Map, onMap)
                }
            }
        }
    }
}

@Composable private fun VenueCard(venue: Venue, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFFF2F0ED)).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(venue.accent.copy(.75f)), contentAlignment = Alignment.Center) { Text(venue.percent.toString() + "%", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold) }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(venue.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(venue.area, Modifier.padding(top = 3.dp), fontSize = 11.sp, color = Muted)
        }
        Tag(venue.state, Color.White.copy(.85f))
    }
}

@Composable private fun SmallAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(onClick, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Ink)) {
        Icon(icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun LiveMap(onBack: () -> Unit, onPlace: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(Paper)) {
        Header("Aura Heatmap", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Text("LIVE CAMPUS MAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
            Text("Find the quiet pocket.", Modifier.padding(top = 6.dp), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Box(Modifier.fillMaxWidth().height(420.dp).padding(top = 18.dp).clip(RoundedCornerShape(30.dp)).background(Color(0xFFE8E4DF))) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Sage.copy(.65f), size.minDimension * .17f, androidx.compose.ui.geometry.Offset(size.width*.30f, size.height*.32f))
                    drawCircle(Coral.copy(.9f), size.minDimension * .20f, androidx.compose.ui.geometry.Offset(size.width*.68f, size.height*.55f))
                    drawCircle(Lavender.copy(.9f), size.minDimension * .15f, androidx.compose.ui.geometry.Offset(size.width*.48f, size.height*.75f))
                }
                MapPin("Library • 14%", Alignment.TopStart, onPlace)
                MapPin("Canteen • 72%", Alignment.CenterEnd, onPlace)
                MapPin("CS Labs • 19%", Alignment.BottomCenter, onPlace)
            }
            Spacer(Modifier.height(18.dp))
            venues.take(3).forEach { VenueCard(it) { onPlace(it.name) } }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable private fun MapPin(text: String, alignment: Alignment, onPlace: (String) -> Unit) {
    Box(Modifier.fillMaxSize().padding(22.dp), contentAlignment = alignment) {
        Text(text, Modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(.92f)).clickable { onPlace(text.substringBefore(" •")) }.padding(10.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun Detail(place: String, onBack: () -> Unit, onCheckIn: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Paper)) {
        Header("Place Detail", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(Lavender.copy(.55f)).padding(22.dp)) {
                Column {
                    Text("ZONE: EAST QUAD / LIVE TELEMETRY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                    Text(place, Modifier.padding(top = 20.dp), fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                    Row(verticalAlignment = Alignment.Bottom) { Text("14", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold); Text("% occupied", Modifier.padding(bottom = 9.dp, start = 6.dp), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = LavenderDark) }
                    Text("Quiet right now • Updated 1 min ago", Modifier.padding(top = 8.dp), fontSize = 13.sp, color = Muted)
                }
            }
            Text("Today's Crowd Curve", Modifier.padding(top = 22.dp), fontSize = 21.sp, fontWeight = FontWeight.Bold)
            CrowdChart()
            Text("Floor Breakdown", Modifier.padding(top = 22.dp), fontSize = 21.sp, fontWeight = FontWeight.Bold)
            listOf("1F Social & Commons" to "58% • Okay", "2F Stacks & Periodicals" to "24% • Chill", "3F Deep Silent Zone" to "14% • Empty", "B1 Tech & Media Lab" to "10% • Empty").forEach { InfoRow(it.first, it.second) }
            Text("Community Buzz", Modifier.padding(top = 22.dp), fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Quote("North-facing window pods have incredible afternoon sunshine right now.")
            Quote("Printing station on 2F was just restocked.")
            Button(onCheckIn, Modifier.fillMaxWidth().padding(top = 18.dp).height(56.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Ink)) {
                Icon(Icons.Default.AddCircleOutline, null); Spacer(Modifier.width(8.dp)); Text("Check in here", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable private fun CrowdChart() {
    Canvas(Modifier.fillMaxWidth().height(150.dp).padding(top = 14.dp)) {
        val values = listOf(.18f,.25f,.34f,.55f,.43f,.22f,.18f,.64f,.82f,.52f,.30f,.16f)
        val w = size.width / values.size
        values.forEachIndexed { i, v ->
            drawRoundRect(if (i == 5) CoralStrong else LavenderDark.copy(.65f), androidx.compose.ui.geometry.Offset(i*w+5, size.height-v*size.height), androidx.compose.ui.geometry.Size(w-10, v*size.height), 7f, 7f)
        }
    }
}

@Composable private fun InfoRow(title: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(top = 9.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xFFF2F0ED)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold); Tag(value, Color.White.copy(.85f))
    }
}

@Composable private fun Quote(text: String) {
    Box(Modifier.fillMaxWidth().padding(top = 9.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xFFF2F0ED)).padding(15.dp)) { Text("“" + text + "”", fontSize = 12.sp, lineHeight = 18.sp, color = Muted) }
}

@Composable private fun CheckIn(onBack: () -> Unit, onDone: () -> Unit) {
    var selected by remember { mutableStateOf(1) }
    val choices = listOf("Chill / Empty", "Moderate / Okay", "Slammed / Packed")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Paper)) {
        Header("Check In", onBack)
        Column(Modifier.padding(horizontal = 18.dp)) {
            Box(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFFE3DED8)), contentAlignment = Alignment.Center) { PetalGraphic(Modifier.size(150.dp)) }
            Text("How’s the crowd right now?", Modifier.padding(top = 22.dp), fontSize = 32.sp, lineHeight = 35.sp, fontWeight = FontWeight.ExtraBold)
            Text("Your quick report helps other students choose a better time.", Modifier.padding(top = 8.dp), fontSize = 14.sp, color = Muted)
            Text("Crowd level", Modifier.padding(top = 22.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            choices.forEachIndexed { i, label ->
                Row(Modifier.fillMaxWidth().padding(top = 9.dp).clip(RoundedCornerShape(20.dp)).background(if (selected == i) Lavender.copy(.65f) else Color(0xFFF2F0ED)).clickable { selected = i }.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(if (i==0) "< 30% occupancy" else if (i==1) "30–70% occupancy" else "> 70% occupancy", Modifier.padding(top = 3.dp), fontSize = 11.sp, color = Muted)
                    }
                    Icon(if (selected == i) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null)
                }
            }
            Text("Ambient tags", Modifier.padding(top = 22.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Tag("🔌 Outlets open", Color(0xFFF2F0ED)); Tag("❄️ AC cool", Color(0xFFF2F0ED)) }
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Tag("🤫 Quiet", Color(0xFFF2F0ED)); Tag("🔊 Rising noise", Color(0xFFF2F0ED)) }
            Row(Modifier.fillMaxWidth().padding(top = 20.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFEDE8FF)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = CoralStrong)
                Column(Modifier.padding(start = 9.dp).weight(1f)) { Text("14-day Scout Streak", fontSize = 13.sp, fontWeight = FontWeight.Bold); Text("+10 Pulse Points", fontSize = 11.sp, color = Muted) }
                Tag("Active", Color.White)
            }
            Button(onDone, Modifier.fillMaxWidth().padding(top = 20.dp).height(58.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text("Submit Report  •  +10 Pulse Points", fontWeight = FontWeight.Bold) }
            Text("Report auto-expires after 45 minutes.", Modifier.fillMaxWidth().padding(vertical = 14.dp), fontSize = 11.sp, color = Muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable private fun Tag(text: String, color: Color) {
    Text(text, Modifier.clip(RoundedCornerShape(50)).background(color).padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
}
