package com.campuspulse.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Paper=Color(0xFFFBF9F6)
private val Ink=Color(0xFF1B1C1A)
private val Muted=Color(0xFF5C5961)
private val Lavender=Color(0xFFCFC4F5)
private val LavenderDeep=Color(0xFF6E638E)
private val LavenderSoft=Color(0xFFEDE8FF)
private val Coral=Color(0xFFFFDAD3)
private val CoralStrong=Color(0xFFFF8A73)
private val Sage=Color(0xFFB7E4C7)
private val Butter=Color(0xFFFFE6A8)
private val WarmGray=Color(0xFFF1EEEA)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{CampusPulseApp()}}
}

@Composable private fun CampusPulseApp(){
 val context=LocalContext.current
 val prefs=remember{context.getSharedPreferences("campus_pulse_demo",Context.MODE_PRIVATE)}
 var screen by rememberSaveable{mutableStateOf("onboarding")}
 var previousScreen by rememberSaveable{mutableStateOf("home")}
 var placeName by rememberSaveable{mutableStateOf("Studio Green Library")}
 var points by rememberSaveable{mutableIntStateOf(prefs.getInt("points",120))}
 var streak by rememberSaveable{mutableIntStateOf(prefs.getInt("streak",14))}
 var reports by rememberSaveable{mutableIntStateOf(prefs.getInt("reports",0))}
 var crowds by remember{mutableStateOf(basePlaces.associate{it.name to it.crowd})}
 var dataState by rememberSaveable{mutableStateOf("ready")}
 var profileOpen by rememberSaveable{mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 fun saveStats(){prefs.edit().putInt("points",points).putInt("streak",streak).putInt("reports",reports).apply()}
 fun go(s:String){previousScreen=screen;screen=s}
 fun submit(place:String,level:Int,tags:List<String>){
  val old=crowds[place]?:0
  val target=when(level){0->20;1->50;else->85}
  crowds=crowds+(place to ((old*2+target)/3).coerceIn(0,100))
  points+=10;streak+=1;reports+=1;saveStats();placeName=place;screen="detail"
 }
 MaterialTheme(colorScheme=lightColorScheme(background=Paper,surface=Paper,primary=Ink)){
  Surface(Modifier.fillMaxSize(),color=Paper){
   if(profileOpen){Profile(points,streak,reports){profileOpen=false}}
   else when(screen){
    "onboarding"->Onboarding{screen="home"}
    "home"->Home(crowds,dataState,{dataState="loading";scope.launch{delay(700);dataState="ready"}},{placeName=it;screen="detail"},{go("map")},{go("checkin")},{go("pulse")},{profileOpen=true})
    "map"->LiveMap(crowds,{screen="home"},{placeName=it;screen="detail"},{go("checkin")},{go("pulse")},{profileOpen=true})
    "detail"->PlaceDetail(basePlaces.firstOrNull{it.name==placeName}?:basePlaces.first(),crowds[placeName]?:14,{screen="map"},{go("checkin")},{go("home")},{go("pulse")},{profileOpen=true})
    "checkin"->CheckIn(placeName,{screen=previousScreen.ifBlank{"home"}},{place,level,tags->submit(place,level,tags)},{go("home")},{go("map")},{go("pulse")},{profileOpen=true})
    "pulse"->Pulse(points,streak,reports,{go("home")},{go("map")},{go("checkin")},{profileOpen=true})
    else->Home(crowds,dataState,{}, {placeName=it;screen="detail"},{go("map")},{go("checkin")},{go("pulse")},{profileOpen=true})
   }
  }
 }
}

@Composable private fun PulseLogo(modifier:Modifier=Modifier){
 Canvas(modifier){
  val r=size.minDimension
  drawCircle(Lavender,r*.46f,center)
  repeat(4){i->val a=i*Math.PI/2;drawCircle(CoralStrong.copy(.88f),r*.13f,Offset(center.x+cos(a).toFloat()*r*.18f,center.y+sin(a).toFloat()*r*.18f))}
  drawCircle(Ink,r*.16f,center);drawCircle(Paper,r*.08f,center);drawCircle(Ink,r*.035f,center)
 }
}

@Composable private fun Header(title:String,onBack:(()->Unit)?=null,onProfile:(()->Unit)?=null){
 Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically){
  if(onBack!=null)IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back",tint=Ink)}
  PulseLogo(Modifier.size(32.dp))
  Text(title,Modifier.padding(start=8.dp).weight(1f),fontSize=20.sp,fontWeight=FontWeight.ExtraBold)
  if(onProfile!=null)IconButton(onClick=onProfile){Box(Modifier.size(32.dp).clip(CircleShape).background(Ink),contentAlignment=Alignment.Center){Icon(Icons.Default.PersonOutline,null,tint=Paper,modifier=Modifier.size(18.dp))}}
 }
}

@Composable private fun Eyebrow(t:String){
 Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(6.dp).clip(CircleShape).background(LavenderDeep));Text(t,Modifier.padding(start=8.dp),fontSize=12.sp,fontWeight=FontWeight.SemiBold,letterSpacing=1.1.sp,color=Muted)}
}

@Composable private fun StepBar(){
 Row{repeat(3){i->Box(Modifier.width(if(i==0)28.dp else 8.dp).height(6.dp).clip(RoundedCornerShape(50)).background(if(i==0)Ink else Color(0xFFE2DFDE)));if(i<2)Spacer(Modifier.width(6.dp))}}
}

@Composable private fun PetalGraphic(modifier:Modifier=Modifier.size(280.dp)){
 Box(modifier,contentAlignment=Alignment.Center){
  repeat(8){i->Box(Modifier.size(114.dp).offset((cos(i*Math.PI/4)*70).dp,(sin(i*Math.PI/4)*70).dp).clip(CircleShape).background(if(i%2==0)Lavender.copy(.64f) else Color(0xFFE7DEFF).copy(.58f)))}
  Box(Modifier.size(112.dp).clip(CircleShape).blur(12.dp).background(Lavender.copy(.8f)))
  Box(Modifier.size(96.dp).clip(CircleShape).background(Color(0xFF30312F)),contentAlignment=Alignment.Center){Box(Modifier.size(10.dp).clip(CircleShape).background(Lavender))}
 }
}

@Composable private fun Onboarding(next:()->Unit){
 Column(Modifier.fillMaxSize().background(Paper).padding(horizontal=20.dp)){
  Row(Modifier.fillMaxWidth().height(64.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow("ONBOARDING");Text("Step 01 / 03",fontSize=11.sp,color=Muted)}
  Box(Modifier.fillMaxWidth().height(360.dp),contentAlignment=Alignment.Center){PetalGraphic()}
  Column(Modifier.padding(top=16.dp)){
   Eyebrow("CAMPUS PULSE RADAR")
   Text("Stop guessing.\nStart knowing.",Modifier.padding(top=11.dp),fontSize=36.sp,lineHeight=39.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-1.8).sp)
   Text("Real-time campus crowd radar, seat telemetry, and mess velocity delivered straight to your pocket.",Modifier.padding(top=15.dp).width(320.dp),fontSize=14.sp,lineHeight=22.sp,color=Muted)
  }
  Row(Modifier.fillMaxWidth().padding(top=28.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   PreviewCard("Main Library • Floor 2","18% Full",Sage);PreviewCard("Dining Hall • Hub B","Rush Hour",CoralStrong)
  }
  Row(Modifier.fillMaxWidth().padding(top=36.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){StepBar();Pill("Next",next)}
 }
}

@Composable private fun RowScope.PreviewCard(a:String,b:String,c:Color){
 Column(Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(16.dp)).background(WarmGray).padding(14.dp),verticalArrangement=Arrangement.SpaceBetween){Text(a,fontSize=11.sp,color=Muted,maxLines=1);Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(8.dp).clip(CircleShape).background(c));Text(b,Modifier.padding(start=8.dp),fontSize=13.sp,fontWeight=FontWeight.Bold)}}
}

data class PlaceData(
 val name:String,val category:String,val zone:String,val crowd:Int,val noise:String,val outlets:String,val temperature:String,val buzz:List<String>
)
private val basePlaces=listOf(
 PlaceData("Studio Green Library","Library","East Quad / Quiet Sanctuary",14,"28 dB · Whisper","92% free","68°F",listOf("North-facing window pods have incredible afternoon sunshine right now.","Printing station on 2F was just restocked.")),
 PlaceData("Arrillaga Dining","Dining","Central Campus / Dining Hub",84,"72 dB · Busy","18% free","70°F",listOf("Lunch queue is moving quickly near Hub B.","Outdoor tables have a few open spots.")),
 PlaceData("Tresidder Gym","Gym","Student Center / Fitness Wing",71,"64 dB · Energetic","42% free","69°F",listOf("Cardio floor is filling up.","Free weights have short waits right now.")),
 PlaceData("CoHo Coffee House","Cafe","Old Union / South Terrace",58,"55 dB · Social","31% free","67°F",listOf("Window seats are turning over quickly.","The west counter has a short line.")),
 PlaceData("Huang Mac Lab","Lab","Engineering Quad / Lab Row",31,"36 dB · Focused","76% free","66°F",listOf("Plenty of open workstations.","Quiet zone is especially calm today."))
)

@Composable private fun Home(crowds:Map<String,Int>,dataState:String,onRefresh:()->Unit,onPlace:(String)->Unit,onMap:()->Unit,onCheckIn:()->Unit,onPulse:()->Unit,onProfile:()->Unit){
 Scaffold(bottomBar={BottomNav("home",{},onMap,onCheckIn,onPulse)},containerColor=Paper){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(horizontal=20.dp)){
 Spacer(Modifier.height(4.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow("OVERVIEW");Row(verticalAlignment=Alignment.CenterVertically){PillTag(if(dataState=="loading")"SYNCING" else "LIVE",if(dataState=="loading")Butter else Sage);IconButton(onClick=onRefresh){Icon(Icons.Default.Refresh,"Refresh",tint=Ink)}}}
 if(dataState=="loading")StatusCard("Refreshing campus pulse…","Local demo data is syncing.",LavenderSoft,Icons.Default.Sync) else if(dataState=="error")StatusCard("Couldn’t refresh","Your saved campus snapshot is still available.",Coral,Icons.Default.Warning) else if(crowds.isEmpty())StatusCard("No venues yet","No campus zones are available in this demo.",WarmGray,Icons.Default.Place)
 Text("hey srijoy,",Modifier.padding(top=8.dp),fontSize=36.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-1.8).sp);Text("make your day easy · local flow updates",fontSize=14.sp,color=Muted);Spacer(Modifier.height(24.dp))
 HomeHero(crowds["Tresidder Gym"]?:71){onPlace("Tresidder Gym")};Spacer(Modifier.height(14.dp));Row(horizontalArrangement=Arrangement.spacedBy(14.dp)){ActionTile("Check in","verify crowd level",Butter,Icons.Default.LocationOn,onCheckIn);ActionTile("Live Map","tap a zone",Ink,Icons.Default.Map,onMap,true)};Spacer(Modifier.height(24.dp));Section("live now","frontend demo telemetry")
 if(crowds.isEmpty())EmptyState("No crowd snapshots yet.","Try refreshing the demo.",onRefresh) else basePlaces.forEach{v->val c=crowds[v.name]?:v.crowd;VenueRow(v.name,c,statusFor(c),onPlace)}
 Spacer(Modifier.height(16.dp));Section("COMMUNITY SNAPSHOT","Meyer Green Lawn");Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFFE9E3F8)),contentAlignment=Alignment.BottomStart){Column(Modifier.padding(18.dp)){Text("Sunset Gathering on Meyer Green",fontSize=15.sp,fontWeight=FontWeight.Bold);Text("outdoor acoustic session · optimal vibes",fontSize=12.sp,color=Muted,modifier=Modifier.padding(top=4.dp))}};Spacer(Modifier.height(24.dp))
}}
@Composable private fun StatusCard(title:String,sub:String,bg:Color,icon:ImageVector){Row(Modifier.fillMaxWidth().padding(top=12.dp).clip(RoundedCornerShape(18.dp)).background(bg).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Ink);Column(Modifier.padding(start=10.dp)){Text(title,fontSize=13.sp,fontWeight=FontWeight.Bold);Text(sub,fontSize=11.sp,color=Muted,modifier=Modifier.padding(top=2.dp))}}}
@Composable private fun EmptyState(title:String,sub:String,onRetry:()->Unit){Column(Modifier.fillMaxWidth().padding(vertical=24.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.CloudOff,null,tint=Muted,modifier=Modifier.size(34.dp));Text(title,Modifier.padding(top=8.dp),fontSize=14.sp,fontWeight=FontWeight.Bold);Text(sub,fontSize=11.sp,color=Muted,modifier=Modifier.padding(top=3.dp));TextButton(onClick=onRetry){Text("Try again")}}}
@Composable private fun HomeHero(crowd:Int,onClick:()->Unit){Box(Modifier.fillMaxWidth().height(252.dp).clip(RoundedCornerShape(28.dp)).background(Lavender).clickable(onClick=onClick)){Box(Modifier.size(192.dp).offset(190.dp,(-46).dp).clip(CircleShape).blur(16.dp).background(Color.White.copy(.35f)));Column(Modifier.fillMaxSize().padding(24.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){PillTag("FEATURED VENUE",Color.White.copy(.55f));PillTag(crowd.toString()+"% "+statusFor(crowd),CoralStrong)};Spacer(Modifier.height(18.dp));Text("Tresidder Gym",fontSize=30.sp,fontWeight=FontWeight.ExtraBold);Text(if(crowd>70)"Peak window · expect a 12 min wait" else "Moderate flow · spots are opening up",fontSize=14.sp,color=Muted,modifier=Modifier.padding(top=8.dp));Spacer(Modifier.weight(1f));Text("NOW · local demo snapshot",fontSize=11.sp,color=Muted)}}}
@Composable private fun RowScope.ActionTile(title:String,sub:String,bg:Color,icon:ImageVector,onClick:()->Unit,dark:Boolean=false){Column(Modifier.weight(1f).height(175.dp).clip(RoundedCornerShape(24.dp)).background(bg).clickable(onClick=onClick).padding(20.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Icon(icon,null,tint=if(dark)Paper else Ink,modifier=Modifier.size(18.dp));PillTag(if(dark)"LIVE" else "QUICK",if(dark)Color.White.copy(.12f) else Color.White.copy(.55f),dark)};Spacer(Modifier.weight(1f));Text(title,fontSize=21.sp,fontWeight=FontWeight.Bold,color=if(dark)Paper else Ink);Text(sub,fontSize=11.sp,color=if(dark)Color.White.copy(.7f) else Muted,modifier=Modifier.padding(top=4.dp))}}
@Composable private fun Section(a:String,b:String){Row(Modifier.fillMaxWidth().padding(bottom=10.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(a,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp);Text(b,fontSize=11.sp,color=Muted)}}
@Composable private fun VenueRow(name:String,percent:Int,state:String,onPlace:(String)->Unit){val bg=when(state){"PACKED"->Coral;"OKAY"->Butter;else->Sage};Row(Modifier.fillMaxWidth().padding(bottom=10.dp).clip(RoundedCornerShape(20.dp)).background(bg.copy(.55f)).clickable{onPlace(name)}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(bg.copy(.8f)),contentAlignment=Alignment.Center){Text(percent.toString()+"%",fontSize=13.sp,fontWeight=FontWeight.ExtraBold)};Column(Modifier.weight(1f).padding(start=14.dp)){Text(name,fontSize=16.sp,fontWeight=FontWeight.Bold);Text("frontend demo telemetry · saved locally",fontSize=11.sp,color=Muted,modifier=Modifier.padding(top=3.dp))};PillTag(state,Color.White.copy(.85f))}}

@Composable private fun LiveMap(crowds:Map<String,Int>,onHome:()->Unit,onPlace:(String)->Unit,onCheckIn:()->Unit,onPulse:()->Unit,onProfile:()->Unit){
 Scaffold(bottomBar={BottomNav("map",onHome,{},onCheckIn,onPulse)},containerColor=Paper){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())){Header("Aura Heatmap",onHome,onProfile);Column(Modifier.padding(horizontal=20.dp)){Eyebrow("LIVE CAMPUS MAP");Text("Find the quiet pocket.",Modifier.padding(top=6.dp),fontSize=30.sp,fontWeight=FontWeight.ExtraBold);Box(Modifier.fillMaxWidth().height(420.dp).padding(top=18.dp).clip(RoundedCornerShape(30.dp)).background(Ink)){Canvas(Modifier.fillMaxSize()){drawCircle(Sage.copy(.55f),90f,Offset(size.width*.25f,size.height*.35f));drawCircle(CoralStrong.copy(.45f),120f,Offset(size.width*.72f,size.height*.55f));drawCircle(Butter.copy(.38f),95f,Offset(size.width*.52f,size.height*.72f));drawCircle(Lavender.copy(.28f),100f,Offset(size.width*.78f,size.height*.25f));for(i in 0..7)drawLine(Color.White.copy(.07f),Offset(i*size.width/7f,0f),Offset(i*size.width/7f,size.height),1f);for(i in 0..8)drawLine(Color.White.copy(.06f),Offset(0f,i*size.height/8f),Offset(size.width,i*size.height/8f),1f)};Row(Modifier.align(Alignment.TopStart).padding(14.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){PillTag("EMPTY",Sage);PillTag("OKAY",Butter);PillTag("PACKED",Coral)};MapMarker("LIBRARY",.22f,.38f,crowds["Studio Green Library"]?:14,Sage){onPlace("Studio Green Library")};MapMarker("DINING",.70f,.25f,crowds["Arrillaga Dining"]?:84,CoralStrong){onPlace("Arrillaga Dining")};MapMarker("GYM",.56f,.70f,crowds["Tresidder Gym"]?:71,CoralStrong){onPlace("Tresidder Gym")}};Spacer(Modifier.height(18.dp));Section("NEARBY PULSE","tap a zone");basePlaces.take(3).forEach{v->val c=crowds[v.name]?:v.crowd;VenueRow(v.name,c,statusFor(c),onPlace)};Spacer(Modifier.height(30.dp))}}}}
@Composable private fun MapMarker(label:String,x:Float,y:Float,crowd:Int,c:Color,onClick:()->Unit){Column(Modifier.offset(x=(330*x).dp,y=(380*y).dp).clickable(onClick=onClick),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(34.dp).clip(CircleShape).background(c),contentAlignment=Alignment.Center){Icon(Icons.Default.LocationOn,null,tint=Ink,modifier=Modifier.size(18.dp))};Text(label+" · "+crowd+"%",fontSize=9.sp,fontWeight=FontWeight.Bold,color=Paper,modifier=Modifier.padding(top=2.dp))}}

@Composable private fun PlaceDetail(place:PlaceData,crowd:Int,onBack:()->Unit,onCheckIn:()->Unit,onHome:()->Unit,onPulse:()->Unit,onProfile:()->Unit){
 Scaffold(bottomBar={Box(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=10.dp)){Button(onCheckIn,Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(50),colors=ButtonDefaults.buttonColors(containerColor=Ink)){Text("I am here — Check In",fontSize=16.sp,fontWeight=FontWeight.Bold);Text("  +10 pts",fontSize=13.sp)}}},containerColor=Paper){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())){Header(place.name,onBack,onProfile);Column(Modifier.padding(horizontal=20.dp)){Box(Modifier.fillMaxWidth().height(355.dp).clip(RoundedCornerShape(32.dp)).background(LavenderSoft).padding(24.dp)){Column{Eyebrow("ZONE: "+place.zone.uppercase());Text(place.name,Modifier.padding(top=20.dp),fontSize=36.sp,lineHeight=40.sp,fontWeight=FontWeight.ExtraBold);Row(verticalAlignment=Alignment.Bottom){Text(crowd.toString(),fontSize=50.sp,fontWeight=FontWeight.ExtraBold);Text("%",Modifier.padding(bottom=5.dp),fontSize=36.sp);Text(if(crowd<30)" quiet" else if(crowd<70)" flowing" else " busy",fontSize=18.sp,fontWeight=FontWeight.Bold,color=LavenderDeep,modifier=Modifier.padding(start=8.dp,bottom=7.dp))};Row(Modifier.padding(top=14.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){PillTag(place.category,Color.White.copy(.82f));PillTag(statusFor(crowd),Color.White.copy(.82f))};Spacer(Modifier.weight(1f));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){SmallStat(place.noise,"Noise");SmallStat(place.outlets,"Outlets");SmallStat(place.temperature,"AC")}}};Box(Modifier.fillMaxWidth().padding(top=16.dp).clip(RoundedCornerShape(24.dp)).background(LavenderSoft).padding(16.dp)){Column{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Optimal Window Detected",fontSize=12.sp,fontWeight=FontWeight.Bold);Text("LOCAL MODEL",fontSize=10.sp,color=Muted)};Text(if(crowd<30)"Right now is a calm pocket for focused work." else if(crowd<70)"Flow is moderate. You should still find a comfortable spot." else "Crowd is high. Consider waiting for a quieter window.",Modifier.padding(top=8.dp),fontSize=14.sp,lineHeight=20.sp,color=Muted)}};Text("Today's Crowd Curve",Modifier.padding(top=20.dp),fontSize=21.sp,fontWeight=FontWeight.Bold);CrowdChart(crowd);Text("Zone Breakdown",Modifier.padding(top=20.dp),fontSize=21.sp,fontWeight=FontWeight.Bold);InfoRow("Main "+place.category+" zone",crowd.toString()+"% "+statusFor(crowd),if(crowd>70)Coral else if(crowd>30)Butter else Sage);InfoRow("Noise profile",place.noise,WarmGray);InfoRow("Availability",place.outlets,Sage);Text("Community Buzz",Modifier.padding(top=20.dp),fontSize=21.sp,fontWeight=FontWeight.Bold);place.buzz.forEach{Quote(it)};Spacer(Modifier.height(90.dp))}}}}
@Composable private fun CrowdChart(crowd:Int){Canvas(Modifier.fillMaxWidth().height(160.dp).padding(top=12.dp)){val base=listOf(.2f,.25f,.34f,.55f,.45f,.28f,.2f,.7f,.82f,.55f,.3f,.18f);val scale=(crowd/70f).coerceIn(.35f,1.25f);val w=size.width/base.size;base.forEachIndexed{i,v->{val h=(v*scale).coerceIn(.08f,.95f)*size.height;drawRoundRect(if(i==5)CoralStrong else LavenderDeep.copy(.65f),Offset(i*w+5,size.height-h),Size(w-10,h),CornerRadius(6f,6f))}}}
@Composable private fun InfoRow(a:String,b:String,c:Color){Row(Modifier.fillMaxWidth().padding(top=9.dp).clip(RoundedCornerShape(18.dp)).background(c.copy(.45f)).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(a,Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.Bold);PillTag(b,Color.White.copy(.9f))}}
@Composable private fun Quote(t:String){Box(Modifier.fillMaxWidth().padding(top=8.dp).clip(RoundedCornerShape(18.dp)).background(WarmGray).padding(15.dp)){Text("“"+t+"”",fontSize=12.sp,lineHeight=18.sp,color=Muted)}}
@Composable private fun RowScope.SmallStat(a:String,b:String){Column(Modifier.weight(1f)){Text(b,fontSize=10.sp,color=Muted);Text(a,fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=3.dp))}}

@Composable private fun CheckIn(placeName:String,onBack:()->Unit,onDone:(String,Int,List<String>)->Unit,onHome:()->Unit,onMap:()->Unit,onPulse:()->Unit,onProfile:()->Unit){
 var selected by rememberSaveable{mutableIntStateOf(1)}
 val tags=remember{mutableStateListOf<String>()}
 val options=listOf("Chill / Empty" to "Seats everywhere, walk-in ease, whisper quiet.","Moderate / Okay" to "Decent spots, moderate daytime buzz, brisk line.","Slammed / Packed" to "No free tables, long queues, energetic sound level.")
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Paper)){Header("Tactile Telemetry",onBack,onProfile);Box(Modifier.fillMaxWidth().height(300.dp).background(Color(0xFFDDD9D2)),contentAlignment=Alignment.Center){PetalGraphic(Modifier.size(190.dp))};Column(Modifier.padding(horizontal=20.dp)){Row(Modifier.fillMaxWidth().padding(top=16.dp).clip(RoundedCornerShape(14.dp)).background(WarmGray).padding(8.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.LocationOn,null,Modifier.size(16.dp));Text("Demo location check · "+placeName,Modifier.weight(1f).padding(start=8.dp),fontSize=11.sp);PillTag("LOCAL",Sage)};Text("How’s the crowd\nright now?",Modifier.padding(top=22.dp),fontSize=36.sp,lineHeight=39.sp,fontWeight=FontWeight.ExtraBold);Text("Your 1-tap telemetry helps fellow students find the right campus pocket.",Modifier.padding(top=8.dp),fontSize=14.sp,lineHeight=20.sp,color=Muted);Spacer(Modifier.height(24.dp));options.forEachIndexed{i,o->Selector(o.first,o.second,selected==i){selected=i}};Spacer(Modifier.height(22.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Ambient Micro-tags",fontSize=12.sp,fontWeight=FontWeight.Bold);Text("Tap all that apply",fontSize=12.sp,color=Muted)};Row(Modifier.fillMaxWidth().padding(top=10.dp).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("🔌 Outlets open","❄️ AC is frosty","🤫 Pure silence","🔊 Rising noise").forEach{tag->SelectableTag(tag,tags.contains(tag)){if(tags.contains(tag))tags.remove(tag) else tags.add(tag)}}};Row(Modifier.fillMaxWidth().padding(top=20.dp).clip(RoundedCornerShape(18.dp)).background(LavenderSoft).padding(16.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.LocalFireDepartment,null,tint=CoralStrong);Column(Modifier.weight(1f).padding(start=10.dp)){Text("Scout Streak",fontSize=13.sp,fontWeight=FontWeight.Bold);Text("+5 demo bonus multiplier",fontSize=11.sp,color=Muted)};PillTag("Active",Color.White)};Button(onClick={onDone(placeName,selected,tags.toList())},Modifier.fillMaxWidth().padding(top=20.dp).height(58.dp),shape=RoundedCornerShape(50),colors=ButtonDefaults.buttonColors(containerColor=Ink)){Text("Submit Report  •  +10 Pulse Points",fontWeight=FontWeight.Bold)};Text("Saved locally on this device · frontend demo",Modifier.fillMaxWidth().padding(vertical=14.dp),fontSize=10.sp,color=Muted,textAlign=TextAlign.Center);Spacer(Modifier.height(30.dp))}}
@Composable private fun SelectableTag(text:String,selected:Boolean,onClick:()->Unit){Row(Modifier.clip(RoundedCornerShape(50)).background(if(selected)Lavender else WarmGray).clickable(onClick=onClick).padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){if(selected)Icon(Icons.Default.Check,null,tint=Ink,modifier=Modifier.size(14.dp));Text(text,Modifier.padding(start=if(selected)5.dp else 0.dp),fontSize=10.sp,fontWeight=FontWeight.Bold,color=Ink)}}

@Composable private fun Selector(title:String,desc:String,active:Boolean,onClick:()->Unit){Column(Modifier.fillMaxWidth().padding(top=8.dp).clip(RoundedCornerShape(24.dp)).background(if(active)Color(0xFFDED8F4) else WarmGray).clickable(onClick=onClick).padding(24.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){PillTag(if(title.startsWith("Chill"))"< 30% OCCUPANCY" else if(title.startsWith("Moderate"))"30–70% OCCUPANCY" else "> 70% OCCUPANCY",Color.White.copy(.8f));Icon(if(active)Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,null,tint=if(active)Ink else Muted)};Text(title,Modifier.padding(top=12.dp),fontSize=20.sp,fontWeight=FontWeight.Bold);Text(desc,Modifier.padding(top=4.dp),fontSize=13.sp,lineHeight=20.sp,color=Muted)}}
@Composable private fun Pulse(points:Int,streak:Int,reports:Int,onHome:()->Unit,onMap:()->Unit,onCheckIn:()->Unit,onProfile:()->Unit){Scaffold(bottomBar={BottomNav("pulse",onHome,onMap,onCheckIn,{})},containerColor=Paper){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())){Header("Pulse Points",null,onProfile);Column(Modifier.padding(horizontal=20.dp)){Eyebrow("YOUR CAMPUS SIGNAL");Text("Turn your check-ins into momentum.",Modifier.padding(top=7.dp),fontSize=30.sp,fontWeight=FontWeight.ExtraBold);Row(Modifier.fillMaxWidth().padding(top=20.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){MetricCard(points.toString(),"Pulse Points",Lavender);MetricCard("🔥 "+streak,"Scout Streak",Butter)};MetricCard(reports.toString(),"Reports submitted",Sage,Modifier.padding(top=10.dp));Text("Campus leaderboard",Modifier.padding(top=24.dp),fontSize=21.sp,fontWeight=FontWeight.Bold);if(reports==0)EmptyState("No reports yet.","Submit your first crowd report to appear here.",onCheckIn) else listOf("Maya" to 2840,"Rahul" to 2510,"You" to points).sortedByDescending{it.second}.forEachIndexed{i,row->LeaderboardRow(i+1,row.first,row.second)};Text("Recent contribution",Modifier.padding(top=24.dp),fontSize=21.sp,fontWeight=FontWeight.Bold);if(reports==0)Quote("Your first check-in will appear here.") else Quote("You have contributed "+reports+" local crowd reports. Keep the campus pulse useful.");Spacer(Modifier.height(40.dp))}}}}
@Composable private fun MetricCard(value:String,label:String,bg:Color,modifier:Modifier=Modifier){Column(modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(bg).padding(20.dp)){Text(value,fontSize=30.sp,fontWeight=FontWeight.ExtraBold);Text(label,fontSize=12.sp,color=Muted,modifier=Modifier.padding(top=3.dp))}}
@Composable private fun LeaderboardRow(rank:Int,name:String,score:Int){Row(Modifier.fillMaxWidth().padding(top=8.dp).clip(RoundedCornerShape(18.dp)).background(if(name=="You")LavenderSoft else WarmGray).padding(15.dp),verticalAlignment=Alignment.CenterVertically){Text("#"+rank,Modifier.width(40.dp),fontSize=13.sp,fontWeight=FontWeight.Bold);Text(name,Modifier.weight(1f),fontSize=14.sp,fontWeight=FontWeight.Bold);Text(score.toString()+" pts",fontSize=12.sp,color=Muted)}}
@Composable private fun Profile(points:Int,streak:Int,reports:Int,onClose:()->Unit){Column(Modifier.fillMaxSize().background(Paper).verticalScroll(rememberScrollState())){Header("Your Profile",onClose,null);Column(Modifier.padding(horizontal=20.dp)){Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(Ink).padding(24.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(64.dp).clip(CircleShape).background(Lavender),contentAlignment=Alignment.Center){Text("S",fontSize=28.sp,fontWeight=FontWeight.ExtraBold)};Column(Modifier.padding(start=16.dp)){Text("srijoy",fontSize=24.sp,fontWeight=FontWeight.ExtraBold,color=Paper);Text("Campus Pulse scout",fontSize=12.sp,color=Paper.copy(.65f))}}};Spacer(Modifier.height(22.dp));MetricCard(points.toString(),"Pulse Points",Lavender);MetricCard(streak.toString(),"Scout Streak",Butter,Modifier.padding(top=10.dp));MetricCard(reports.toString(),"Reports",Sage,Modifier.padding(top=10.dp));Text("Frontend demo data is saved locally on this device.",Modifier.padding(vertical=18.dp),fontSize=12.sp,color=Muted)}}}

@Composable private fun PillTag(text:String,bg:Color,dark:Boolean=false){Row(Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal=10.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(6.dp).clip(CircleShape).background(if(dark)Paper else Ink));Text(text,Modifier.padding(start=6.dp),fontSize=9.sp,fontWeight=FontWeight.Bold,color=if(dark)Paper else Ink)}}
@Composable private fun Pill(label:String,onClick:()->Unit){Button(onClick=onClick,shape=RoundedCornerShape(50),colors=ButtonDefaults.buttonColors(containerColor=Ink),contentPadding=PaddingValues(horizontal=20.dp,vertical=10.dp)){Text(label,fontSize=14.sp,fontWeight=FontWeight.SemiBold,color=Paper)}}
@Composable private fun BottomNav(active:String,onHome:()->Unit,onMap:()->Unit,onCheckIn:()->Unit,onPulse:()->Unit){NavigationBar(containerColor=Paper){NavigationBarItem(selected=active=="home",onClick=onHome,icon={Icon(Icons.Default.Home,null)},label={Text("Home")});NavigationBarItem(selected=active=="map",onClick=onMap,icon={Icon(Icons.Default.Map,null)},label={Text("Map")});NavigationBarItem(selected=active=="check",onClick=onCheckIn,icon={Icon(Icons.Default.AddCircleOutline,null)},label={Text("Check in")});NavigationBarItem(selected=active=="pulse",onClick=onPulse,icon={Icon(Icons.Default.EmojiEvents,null)},label={Text("Pulse")})}}
private fun statusFor(c:Int)=when{c>=70->"PACKED";c>=30->"OKAY";else->"EMPTY"}
}
}
}
