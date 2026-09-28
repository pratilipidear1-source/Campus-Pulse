package com.campuspulse.app

import android.content.Context
import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

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

data class CampusPlace(
 val name:String,val category:String,val zone:String,val crowd:Int,
 val noise:String,val outlets:String,val climate:String,val description:String,
 val ambientTags:Set<String> = emptySet()
)

private val defaultPlaces=listOf(
 CampusPlace("Studio Green Library","Library","East Quad / Quiet Sanctuary",14,"28 dB · Whisper","92% free","68°F optimal","Deep focus sanctuary with quiet stacks and window pods."),
 CampusPlace("Arrillaga Dining","Dining","Main Dining Hub",84,"72 dB · Busy","18% free","71°F warm","Fast-moving dining hall with the strongest lunch rush."),
 CampusPlace("Tresidder Gym","Gym","Student Center West",71,"64 dB · Active","N/A","69°F cool","Peak workout window with active courts and cardio zones."),
 CampusPlace("CoHo Coffee House","Cafe","South Walk / Student Commons",58,"55 dB · Social","61% free","70°F comfortable","Moderate cafe flow with study and social seating."),
 CampusPlace("Huang Mac Lab","Lab","Engineering Quad",31,"34 dB · Calm","76% free","67°F cool","Quiet computer lab with comfortable workstation availability.")
)

private fun Context.prefs()=getSharedPreferences("campus_pulse_local",Context.MODE_PRIVATE)
private fun loadPlaces(c:Context)=defaultPlaces.map{p->
 val tags=c.prefs().getStringSet("tags_"+p.name,emptySet())?:emptySet()
 p.copy(crowd=c.prefs().getInt("crowd_"+p.name,p.crowd),ambientTags=tags)
}
private fun saveData(c:Context,points:Int,streak:Int,reports:Int,places:List<CampusPlace>){
 val e=c.prefs().edit().putInt("points",points).putInt("streak",streak).putInt("reports",reports)
 places.forEach{
  e.putInt("crowd_"+it.name,it.crowd)
  e.putStringSet("tags_"+it.name,it.ambientTags)
 }
 e.apply()
}
private fun resetData(c:Context){c.prefs().edit().clear().apply()}
private fun todayKey():String=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())
private fun updateLocalStreak(c:Context):Int{
 val p=c.prefs(); val today=todayKey(); val last=p.getString("last_checkin_date",null); val old=p.getInt("streak",0)
 val sdf=SimpleDateFormat("yyyy-MM-dd",Locale.US)
 val newStreak=when{
  last==today->old
  last!=null->{
   val a=sdf.parse(last); val b=sdf.parse(today)
   val days=((b.time-a.time)/(24L*60L*60L*1000L)).toInt()
   if(days==1)old+1 else 1
  }
  else->1
 }
 p.edit().putString("last_checkin_date",today).putInt("streak",newStreak).apply(); return newStreak
}
private fun crowdState(p:Int)=when{p<30->"EMPTY";p<=70->"OKAY";else->"PACKED"}
private fun crowdColor(p:Int)=when{p<30->Sage;p<=70->Butter;else->CoralStrong}
private fun crowdForReport(level:Int)=when(level){0->15;1->50;else->85}
private fun tagSummary(tags:Set<String>):String=if(tags.isEmpty())"No ambient notes yet" else tags.joinToString(" · "){it.substringAfter(" ").ifBlank{it}}

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  setContent{CampusPulseApp(applicationContext)}
 }
}

@Composable private fun CampusPulseApp(c:Context){
 var screen by rememberSaveable{mutableStateOf("onboarding")}
 var placeName by rememberSaveable{mutableStateOf("Studio Green Library")}
 var places by remember{mutableStateOf(loadPlaces(c))}
 var points by rememberSaveable{mutableIntStateOf(c.prefs().getInt("points",0))}
 var streak by rememberSaveable{mutableIntStateOf(c.prefs().getInt("streak",0))}
 var reports by rememberSaveable{mutableIntStateOf(c.prefs().getInt("reports",0))}
 var lastReportedVenue by rememberSaveable{mutableStateOf("Studio Green Library")}
 var lastReportedLevel by rememberSaveable{mutableIntStateOf(1)}
 fun goPlace(name:String){placeName=name;screen="detail"}
 fun openPicker(){screen="venuePicker"}
 fun submitReport(level:Int,tags:Set<String>){
  places=places.map{if(it.name==placeName)it.copy(crowd=crowdForReport(level),ambientTags=tags)else it}
  points+=10
  reports+=1
  streak=updateLocalStreak(c)
  saveData(c,points,streak,reports,places)
  lastReportedVenue=placeName
  lastReportedLevel=level
  screen="success"
 }
 fun resetAll(){resetData(c);places=defaultPlaces;points=0;streak=0;reports=0;placeName="Studio Green Library";screen="home"}
 MaterialTheme(colorScheme=lightColorScheme(background=Paper,surface=Paper,primary=Ink)){
  Surface(Modifier.fillMaxSize(),color=Paper){
   when(screen){
    "onboarding"->Onboarding{screen="home"}
    "home"->Home(places,{screen="map"},{goPlace(it)},{openPicker()},{screen="profile"},{screen="pulse"}){places=loadPlaces(c)}
    "map"->LiveMap(places,{screen="home"},{goPlace(it)},{openPicker()},{screen="pulse"})
    "detail"->PlaceDetail(places.firstOrNull{it.name==placeName}?:defaultPlaces.first(),{screen="map"},{screen="checkin"})
    "venuePicker"->VenuePicker(places,{screen="home"}){name->placeName=name;screen="checkin"}
    "checkin"->CheckIn(places.firstOrNull{it.name==placeName}?:defaultPlaces.first(),{screen="detail"}){level,tags->submitReport(level,tags)}
    "success"->ReportSuccess(lastReportedVenue,lastReportedLevel,points,{screen="detail"},{screen="venuePicker"})
    "pulse"->PulseScreen(points,streak,reports,{screen="home"},{screen="map"},{openPicker()},{screen="profile"})
    "profile"->ProfileScreen(points,streak,reports,{screen="home"},{screen="map"},{openPicker()},{screen="pulse"}){resetAll()}
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
@Composable private fun Eyebrow(t:String){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(6.dp).clip(CircleShape).background(LavenderDeep));Text(t,Modifier.padding(start=8.dp),fontSize=12.sp,fontWeight=FontWeight.SemiBold,letterSpacing=1.1.sp,color=Muted)}}
@Composable private fun Pill(text:String,onClick:()->Unit){Button(onClick=onClick,shape=RoundedCornerShape(50),colors=ButtonDefaults.buttonColors(containerColor=Ink),contentPadding=PaddingValues(horizontal=20.dp,vertical=10.dp)){Text(text,fontSize=14.sp,fontWeight=FontWeight.SemiBold,color=Paper)}}
@Composable private fun PillTag(text:String,bg:Color,dark:Boolean=false){Row(Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal=10.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(6.dp).clip(CircleShape).background(if(dark)Paper else Ink));Text(text,Modifier.padding(start=6.dp),fontSize=9.sp,fontWeight=FontWeight.Bold,color=if(dark)Paper else Ink)}}
@Composable private fun StepBar(){Row{repeat(3){i->Box(Modifier.width(if(i==0)28.dp else 8.dp).height(6.dp).clip(RoundedCornerShape(50)).background(if(i==0)Ink else Color(0xFFE2DFDE)));if(i<2)Spacer(Modifier.width(6.dp))}}}
@Composable private fun PetalGraphic(modifier:Modifier=Modifier.size(280.dp)){
 Box(modifier,contentAlignment=Alignment.Center){
  repeat(8){i->Box(Modifier.size(114.dp).offset((cos(i*Math.PI/4)*70).dp,(sin(i*Math.PI/4)*70).dp).clip(CircleShape).background(if(i%2==0)Lavender.copy(.64f) else Color(0xFFE7DEFF).copy(.58f)))}
  Box(Modifier.size(112.dp).clip(CircleShape).blur(12.dp).background(Lavender.copy(.8f)))
  Box(Modifier.size(96.dp).clip(CircleShape).background(Color(0xFF30312F)),contentAlignment=Alignment.Center){Box(Modifier.size(10.dp).clip(CircleShape).background(Lavender))}
 }
}

@Composable private fun Onboarding(next:()->Unit){
 Column(Modifier.fillMaxSize().background(Paper).statusBarsPadding().padding(horizontal=20.dp)){
  Row(Modifier.fillMaxWidth().height(64.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow("ONBOARDING");Text("Step 01 / 03",fontSize=11.sp,color=Muted)}
  Box(Modifier.fillMaxWidth().height(285.dp),contentAlignment=Alignment.Center){PetalGraphic(Modifier.size(220.dp))}
  Eyebrow("CAMPUS PULSE RADAR")
  Text("Stop guessing.\nStart knowing.",Modifier.padding(top=11.dp),fontSize=36.sp,lineHeight=39.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-1.8).sp)
  Text("Local crowd radar, seat telemetry, and campus flow delivered straight to your pocket.",Modifier.padding(top=15.dp),fontSize=14.sp,lineHeight=22.sp,color=Muted)
  Row(Modifier.fillMaxWidth().padding(top=28.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){MiniCard("Main Library","18% Full",Sage);MiniCard("Dining Hall","Rush Hour",CoralStrong)}
  Row(Modifier.fillMaxWidth().padding(top=22.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){StepBar();Pill("Next",next)}
 }
}
@Composable private fun RowScope.MiniCard(a:String,b:String,c:Color){Column(Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(16.dp)).background(WarmGray).padding(14.dp),verticalArrangement=Arrangement.SpaceBetween){Text(a,fontSize=11.sp,color=Muted);Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(8.dp).clip(CircleShape).background(c));Text(b,Modifier.padding(start=8.dp),fontSize=13.sp,fontWeight=FontWeight.Bold)}}}

@Composable private fun Home(places:List<CampusPlace>,onMap:()->Unit,onPlace:(String)->Unit,onCheck:()->Unit,onProfile:()->Unit,onPulse:()->Unit,onRefresh:()->Unit){
 var loading by remember{mutableStateOf(false)}
 var error by remember{mutableStateOf(false)}
 val scope=rememberCoroutineScope()
 Scaffold(bottomBar={BottomNav("home",{},onMap,onCheck,onPulse)},containerColor=Paper){p->
  Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(horizontal=20.dp)){
   Header("Campus Pulse",onProfile=onProfile)
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow("OVERVIEW");Row(verticalAlignment=Alignment.CenterVertically){PillTag("LOCAL DEMO",Sage);IconButton(onClick={loading=true;error=false;scope.launch{delay(450);onRefresh();loading=false}}){Icon(Icons.Default.Refresh,"Refresh",tint=Ink)}}}
   when{
    loading->LoadingState()
    error->ErrorState{error=false}
    places.isEmpty()->EmptyState{onRefresh()}
    else->{
     Text("hey, campus scout",Modifier.padding(top=4.dp),fontSize=36.sp,fontWeight=FontWeight.ExtraBold)
     Text("your campus flow, in one glance",fontSize=14.sp,color=Muted)
     Spacer(Modifier.height(20.dp))
     val busiest=places.maxByOrNull{it.crowd}?:places.first()
     HomeHero(busiest){onPlace(busiest.name)}
     Spacer(Modifier.height(14.dp))
     Row(horizontalArrangement=Arrangement.spacedBy(14.dp)){ActionTile("Check in","update a crowd report",Butter,Icons.Default.LocationOn,onCheck);ActionTile("Live Map","explore campus zones",Ink,Icons.Default.Map,onMap,true)}
     Spacer(Modifier.height(24.dp));Section("LIVE NOW","local demo state")
     places.forEach{VenueRow(it,onPlace)}
     Spacer(Modifier.height(20.dp));Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFFE9E3F8)).clickable{onPulse()}.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Your campus contribution",fontSize=15.sp,fontWeight=FontWeight.Bold);Text("Open Pulse to see points, streak and reports.",fontSize=12.sp,color=Muted,modifier=Modifier.padding(top=4.dp))};Icon(Icons.Default.ChevronRight,"Open Pulse",tint=Ink)}
     Spacer(Modifier.height(24.dp))
    }
   }
  }
 }
}
@Composable private fun LoadingState(){Column(Modifier.fillMaxWidth().padding(top=50.dp),horizontalAlignment=Alignment.CenterHorizontally){CircularProgressIndicator(color=Ink);Text("Reading your local campus pulse…",Modifier.padding(top=14.dp),fontSize=13.sp,color=Muted)}}
@Composable private fun ErrorState(retry:()->Unit){Column(Modifier.fillMaxWidth().padding(top=50.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.ErrorOutline,null,tint=CoralStrong,modifier=Modifier.size(44.dp));Text("Couldn’t refresh the pulse",Modifier.padding(top=12.dp),fontSize=19.sp,fontWeight=FontWeight.Bold);Text("The local demo state is still safe on this device.",Modifier.padding(top=5.dp),fontSize=12.sp,color=Muted);Pill("Try again",retry)}}
@Composable private fun EmptyState(retry:()->Unit){Column(Modifier.fillMaxWidth().padding(top=50.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.CloudOff,null,tint=LavenderDeep,modifier=Modifier.size(44.dp));Text("No campus places yet",Modifier.padding(top=12.dp),fontSize=19.sp,fontWeight=FontWeight.Bold);Text("Restore the local demo venue list.",Modifier.padding(top=5.dp),fontSize=12.sp,color=Muted);Pill("Restore",retry)}}

@Composable private fun HomeHero(place:CampusPlace,onClick:()->Unit){
 Box(Modifier.fillMaxWidth().height(245.dp).clip(RoundedCornerShape(28.dp)).background(Lavender).clickable(onClick=onClick)){
  Box(Modifier.size(190.dp).offset(190.dp,(-45).dp).clip(CircleShape).blur(16.dp).background(Color.White.copy(.35f)))
  Column(Modifier.fillMaxSize().padding(24.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){PillTag("FEATURED VENUE",Color.White.copy(.55f));PillTag(place.crowd.toString()+"% "+crowdState(place.crowd),crowdColor(place.crowd))};Spacer(Modifier.height(18.dp));Text(place.name,fontSize=29.sp,fontWeight=FontWeight.ExtraBold);Text(place.description,Modifier.padding(top=8.dp),fontSize=14.sp,color=Muted);Spacer(Modifier.weight(1f));Text("LOCAL DEMO · tap for details",fontSize=11.sp,color=Muted)}
 }
}
@Composable private fun RowScope.ActionTile(title:String,sub:String,bg:Color,icon:ImageVector,onClick:()->Unit,dark:Boolean=false){
 Column(Modifier.weight(1f).height(170.dp).clip(RoundedCornerShape(24.dp)).background(bg).clickable(onClick=onClick).padding(20.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Icon(icon,null,tint=if(dark)Paper else Ink,modifier=Modifier.size(18.dp));PillTag(if(dark)"EXPLORE" else "QUICK",if(dark)Color.White.copy(.12f) else Color.White.copy(.55f),dark)};Spacer(Modifier.weight(1f));Text(title,fontSize=21.sp,fontWeight=FontWeight.Bold,color=if(dark)Paper else Ink);Text(sub,fontSize=11.sp,color=if(dark)Color.White.copy(.7f) else Muted,modifier=Modifier.padding(top=4.dp))}
}
@Composable private fun Section(a:String,b:String){Row(Modifier.fillMaxWidth().padding(bottom=10.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(a,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp);Text(b,fontSize=11.sp,color=Muted)}}
@Composable private fun VenueRow(place:CampusPlace,onPlace:(String)->Unit){
 val bg=crowdColor(place.crowd)
 Row(Modifier.fillMaxWidth().padding(bottom=10.dp).clip(RoundedCornerShape(20.dp)).background(bg.copy(.45f)).clickable{onPlace(place.name)}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
  Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(bg.copy(.8f)),contentAlignment=Alignment.Center){Text(place.crowd.toString()+"%",fontSize=13.sp,fontWeight=FontWeight.ExtraBold)}
  Column(Modifier.weight(1f).padding(start=14.dp)){Text(place.name,fontSize=16.sp,fontWeight=FontWeight.Bold);Text(place.category+" · "+place.zone,fontSize=11.sp,color=Muted,modifier=Modifier.padding(top=3.dp))}
  PillTag(crowdState(place.crowd),Color.White.copy(.85f))
 }
}

@Composable private fun LiveMap(places:List<CampusPlace>,onHome:()->Unit,onPlace:(String)->Unit,onCheck:()->Unit,onPulse:()->Unit){
 Scaffold(bottomBar={BottomNav("map",onHome,{},onCheck,onPulse)},containerColor=Paper){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())){
  Header("Aura Heatmap",onBack=onHome);Column(Modifier.padding(horizontal=20.dp)){
   Eyebrow("LOCAL CAMPUS MAP");Text("Find the quiet pocket.",Modifier.padding(top=6.dp),fontSize=30.sp,fontWeight=FontWeight.ExtraBold)
   Text("Tap a marker or venue card to inspect it.",Modifier.padding(top=5.dp),fontSize=12.sp,color=Muted)
   BoxWithConstraints(Modifier.fillMaxWidth().height(390.dp).padding(top=16.dp).clip(RoundedCornerShape(30.dp)).background(Ink)){
    val mapWidth=maxWidth.value;val mapHeight=390f
    Canvas(Modifier.fillMaxSize()){
     places.forEachIndexed{i,v->val x=.15f+(i*.17f);val y=.22f+(i%2)*.28f;drawCircle(crowdColor(v.crowd).copy(.45f),55f+v.crowd*.42f,Offset(size.width*x,size.height*y))}
     for(i in 0..7)drawLine(Color.White.copy(.07f),Offset(i*size.width/7f,0f),Offset(i*size.width/7f,size.height),1f)
     for(i in 0..8)drawLine(Color.White.copy(.06f),Offset(0f,i*size.height/8f),Offset(size.width,i*size.height/8f),1f)
    }
    Row(Modifier.align(Alignment.TopStart).padding(14.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){PillTag("EMPTY",Sage);PillTag("OKAY",Butter);PillTag("PACKED",Coral)}
    places.forEachIndexed{i,v->val x=.15f+(i*.17f);val y=.22f+(i%2)*.28f;MapMarker(v.name,x,y,crowdColor(v.crowd),mapWidth,mapHeight){onPlace(v.name)}}
   }
   Spacer(Modifier.height(18.dp));Section("NEARBY PULSE","tap a zone");places.forEach{VenueRow(it,onPlace)};Spacer(Modifier.height(25.dp))
  }
 }}
}
@Composable private fun MapMarker(label:String,x:Float,y:Float,c:Color,mapWidth:Float,mapHeight:Float,onClick:()->Unit){
 Column(Modifier.offset(x=(mapWidth*x-17).dp,y=(mapHeight*y-17).dp).clickable(onClick=onClick),horizontalAlignment=Alignment.CenterHorizontally){
  Box(Modifier.size(34.dp).clip(CircleShape).background(c),contentAlignment=Alignment.Center){Icon(Icons.Default.LocationOn,null,tint=Ink,modifier=Modifier.size(18.dp))}
  Text(label,fontSize=8.sp,fontWeight=FontWeight.Bold,color=Paper,modifier=Modifier.padding(top=2.dp))
 }
}

@Composable private fun PlaceDetail(place:CampusPlace,onBack:()->Unit,onCheck:()->Unit){
 Scaffold(bottomBar={Box(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=10.dp)){Button(onCheck,Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(50),colors=ButtonDefaults.buttonColors(containerColor=Ink)){Text("I am here — Check In",fontSize=16.sp,fontWeight=FontWeight.Bold);Text("  +10 pts",fontSize=13.sp)}}},containerColor=Paper){p->
  Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState())){Header(place.name,onBack);Column(Modifier.padding(horizontal=20.dp)){
   Box(Modifier.fillMaxWidth().height(275.dp).clip(RoundedCornerShape(32.dp)).background(LavenderSoft).padding(24.dp)){Column{Eyebrow(("ZONE: "+place.zone).uppercase());Text(place.name,Modifier.padding(top=18.dp),fontSize=34.sp,lineHeight=40.sp,fontWeight=FontWeight.ExtraBold);Row(verticalAlignment=Alignment.Bottom){Text(place.crowd.toString(),fontSize=50.sp,fontWeight=FontWeight.ExtraBold);Text("%",Modifier.padding(bottom=5.dp),fontSize=36.sp);Text(crowdState(place.crowd).lowercase(),fontSize=18.sp,fontWeight=FontWeight.Bold,color=LavenderDeep,modifier=Modifier.padding(start=8.dp,bottom=7.dp))};Spacer(Modifier.height(12.dp));PillTag(place.category,Color.White.copy(.82f));Spacer(Modifier.weight(1f));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){SmallStat(place.noise,"Noise");SmallStat(place.outlets,"Outlets");SmallStat(place.climate,"Climate")}}}
   Box(Modifier.fillMaxWidth().padding(top=16.dp).clip(RoundedCornerShape(24.dp)).background(LavenderSoft).padding(16.dp)){Column{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Local Demo State",fontSize=12.sp,fontWeight=FontWeight.Bold);Text("FRONTEND ONLY",fontSize=10.sp,color=Muted)};Text("This venue has its own crowd state. Check-in changes it locally and the value persists after restart.",Modifier.padding(top=8.dp),fontSize=14.sp,lineHeight=20.sp,color=Muted)}}
   Text("Demo Crowd Pattern",Modifier.padding(top=20.dp),fontSize=21.sp,fontWeight=FontWeight.Bold);CrowdChart(place.crowd)
   Text("Venue Snapshot",Modifier.padding(top=20.dp),fontSize=21.sp,fontWeight=FontWeight.Bold);InfoRow("Current occupancy",place.crowd.toString()+"% "+crowdState(place.crowd),crowdColor(place.crowd));InfoRow("Zone",place.zone,Lavender);InfoRow("Category",place.category,WarmGray)
   Text("Community Buzz",Modifier.padding(top=20.dp),fontSize=21.sp,fontWeight=FontWeight.Bold);Quote(place.description)
Box(Modifier.fillMaxWidth().padding(top=8.dp).clip(RoundedCornerShape(18.dp)).background(WarmGray).padding(15.dp)){Column{Text("Latest ambient notes",fontSize=12.sp,fontWeight=FontWeight.Bold);Text(tagSummary(place.ambientTags),Modifier.padding(top=5.dp),fontSize=12.sp,color=Muted)}}
Quote("Local demo state · future versions can connect this venue to live reports.")
   Spacer(Modifier.height(90.dp))
  }}
 }
}
@Composable private fun CrowdChart(current:Int){Canvas(Modifier.fillMaxWidth().height(160.dp).padding(top=12.dp)){val base=current/100f;val a=listOf(.18f,.25f,.34f,.55f,base,.28f,.2f,.7f,.82f,.55f,.3f,.18f);val w=size.width/a.size;a.forEachIndexed{i,v->drawRoundRect(if(i==4)CoralStrong else LavenderDeep.copy(.65f),Offset(i*w+5,size.height-v*size.height),Size(w-10,v*size.height),CornerRadius(6f,6f))}}}
@Composable private fun InfoRow(a:String,b:String,c:Color){Row(Modifier.fillMaxWidth().padding(top=9.dp).clip(RoundedCornerShape(18.dp)).background(c.copy(.45f)).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(a,Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.Bold);PillTag(b,Color.White.copy(.9f))}}
@Composable private fun Quote(t:String){Box(Modifier.fillMaxWidth().padding(top=8.dp).clip(RoundedCornerShape(18.dp)).background(WarmGray).padding(15.dp)){Text("“"+t+"”",fontSize=12.sp,lineHeight=18.sp,color=Muted)}}
@Composable private fun RowScope.SmallStat(a:String,b:String){Column(Modifier.weight(1f)){Text(b,fontSize=10.sp,color=Muted);Text(a,fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=3.dp))}}

@Composable private fun CheckIn(place:CampusPlace,onBack:()->Unit,onDone:(Int,Set<String>)->Unit){
 var selected by rememberSaveable{mutableIntStateOf(1)}
 val options=listOf("Chill / Empty" to "15% · seats easy to find","Moderate / Okay" to "50% · a few good spots","Slammed / Packed" to "85% · queues and tight seating")
 val tags=listOf("🔌 Outlets open","❄️ AC is frosty","🤫 Pure silence","🔊 Rising noise")
 var selectedTags by rememberSaveable{mutableStateOf(place.ambientTags)}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Paper).statusBarsPadding()){
  Header("Tactile Telemetry",onBack);Box(Modifier.fillMaxWidth().height(190.dp).background(Color(0xFFDDD9D2)),contentAlignment=Alignment.Center){PetalGraphic(Modifier.size(135.dp))}
  Column(Modifier.padding(horizontal=20.dp)){
   Spacer(Modifier.height(14.dp))
   Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(WarmGray).padding(10.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.LocationOn,null,Modifier.size(16.dp));Text(place.name,Modifier.weight(1f).padding(start=8.dp),fontSize=12.sp,fontWeight=FontWeight.Bold);PillTag("LOCAL DEMO",Sage)}
   Text("How’s the crowd\nright now?",Modifier.padding(top=18.dp),fontSize=34.sp,lineHeight=37.sp,fontWeight=FontWeight.ExtraBold)
   Text("Choose the condition that best matches what you see. This updates only the local prototype state.",Modifier.padding(top=7.dp),fontSize=13.sp,lineHeight=19.sp,color=Muted)
   Spacer(Modifier.height(14.dp));options.forEachIndexed{i,o->Selector(o.first,o.second,selected==i){selected=i}}
   Spacer(Modifier.height(18.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Ambient Micro-tags",fontSize=12.sp,fontWeight=FontWeight.Bold);Text("Optional",fontSize=12.sp,color=Muted)}
   Row(Modifier.fillMaxWidth().padding(top=10.dp).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){tags.forEach{tag->SelectableTag(tag,selectedTags.contains(tag)){selectedTags=if(selectedTags.contains(tag))selectedTags-tag else selectedTags+tag}}}
   Button({onDone(selected,selectedTags)},Modifier.fillMaxWidth().padding(top=20.dp).height(58.dp),shape=RoundedCornerShape(50),colors=ButtonDefaults.buttonColors(containerColor=Ink)){Icon(Icons.Default.CheckCircle,null,modifier=Modifier.size(18.dp));Text("  Submit Report  •  +10 Pulse Points",fontWeight=FontWeight.Bold)}
   Text("Saved locally on this device · no database or live service used",Modifier.fillMaxWidth().padding(vertical=12.dp),fontSize=10.sp,color=Muted,textAlign=TextAlign.Center);Spacer(Modifier.height(24.dp))
  }
}
}
@Composable private fun VenuePicker(places:List<CampusPlace>,onBack:()->Unit,onContinue:(String)->Unit){
 var selected by rememberSaveable{mutableStateOf(places.firstOrNull()?.name?:"")}
 Scaffold(containerColor=Paper){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(horizontal=20.dp)){
  Header("Choose Venue",onBack);Eyebrow("CHECK-IN");Text("Where are you right now?",Modifier.padding(top=8.dp),fontSize=32.sp,fontWeight=FontWeight.ExtraBold)
  Text("Pick the place you want your local report to update.",Modifier.padding(top=6.dp),fontSize=13.sp,color=Muted);Spacer(Modifier.height(18.dp))
  places.forEach{place->val active=selected==place.name
   Row(Modifier.fillMaxWidth().padding(bottom=10.dp).clip(RoundedCornerShape(22.dp)).background(if(active)LavenderSoft else WarmGray).clickable{selected=place.name}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
    Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(crowdColor(place.crowd).copy(.75f)),contentAlignment=Alignment.Center){Text(place.crowd.toString()+"%",fontSize=12.sp,fontWeight=FontWeight.ExtraBold)}
    Column(Modifier.weight(1f).padding(start=12.dp)){Text(place.name,fontSize=16.sp,fontWeight=FontWeight.Bold);Text(place.category+" · "+crowdState(place.crowd),fontSize=11.sp,color=Muted,modifier=Modifier.padding(top=3.dp))}
    Icon(if(active)Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,null,tint=if(active)Ink else Muted)
   }
  }
  Button({if(selected.isNotBlank())onContinue(selected)},Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(50),colors=ButtonDefaults.buttonColors(containerColor=Ink)){Text("Continue to Report",fontWeight=FontWeight.Bold);Icon(Icons.Default.ArrowForward,null,modifier=Modifier.padding(start=8.dp))}
  Spacer(Modifier.height(25.dp))
 }}
}
@Composable private fun ReportSuccess(venue:String,level:Int,points:Int,onView:()->Unit,onAnother:()->Unit){
 val label=when(level){0->"EMPTY";1->"OKAY";else->"PACKED"};val color=when(level){0->Sage;1->Butter;else->CoralStrong}
 Column(Modifier.fillMaxSize().background(Paper).statusBarsPadding().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){
  Spacer(Modifier.height(65.dp));Box(Modifier.size(88.dp).clip(CircleShape).background(color),contentAlignment=Alignment.Center){Icon(Icons.Default.CheckCircle,null,tint=Ink,modifier=Modifier.size(48.dp))}
  Eyebrow("REPORT SAVED");Text("Nice pulse.",Modifier.padding(top=12.dp),fontSize=38.sp,fontWeight=FontWeight.ExtraBold)
  Text("Your local report updated the demo state for",Modifier.padding(top=7.dp),fontSize=13.sp,color=Muted,textAlign=TextAlign.Center);Text(venue,Modifier.padding(top=3.dp),fontSize=18.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
  Row(Modifier.padding(top=18.dp).clip(RoundedCornerShape(50.dp)).background(color.copy(.55f)).padding(horizontal=16.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Text(label,fontWeight=FontWeight.ExtraBold);Text("  ·  +10 pts",fontSize=12.sp)}
  Text("Total Pulse Points: $points",Modifier.padding(top=14.dp),fontSize=13.sp,color=Muted);Spacer(Modifier.weight(1f))
  Button(onClick=onView,Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(50.dp),colors=ButtonDefaults.buttonColors(containerColor=Ink)){Text("View Updated Venue",fontWeight=FontWeight.Bold)}
  OutlinedButton(onClick=onAnother,Modifier.fillMaxWidth().padding(top=10.dp).height(52.dp),shape=RoundedCornerShape(50.dp)){Text("Report Another Place")};Spacer(Modifier.height(18.dp))
 }
}

@Composable private fun Selector(title:String,desc:String,active:Boolean,onClick:()->Unit){
 Column(Modifier.fillMaxWidth().padding(top=8.dp).clip(RoundedCornerShape(24.dp)).background(if(active)Color(0xFFDED8F4) else WarmGray).clickable(onClick=onClick).padding(24.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){PillTag(if(title.startsWith("Chill"))"< 30% OCCUPANCY" else if(title.startsWith("Moderate"))"30–70% OCCUPANCY" else "> 70% OCCUPANCY",Color.White.copy(.8f));Icon(if(active)Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,null,tint=if(active)Ink else Muted)};Text(title,Modifier.padding(top=12.dp),fontSize=20.sp,fontWeight=FontWeight.Bold);Text(desc,Modifier.padding(top=4.dp),fontSize=13.sp,lineHeight=20.sp,color=Muted)}
}
@Composable private fun SelectableTag(text:String,selected:Boolean,onClick:()->Unit){Row(Modifier.clip(RoundedCornerShape(50)).background(if(selected)Lavender else WarmGray).clickable(onClick=onClick).padding(horizontal=12.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically){if(selected)Icon(Icons.Default.Check,null,tint=Ink,modifier=Modifier.size(13.dp));Text(text,Modifier.padding(start=if(selected)5.dp else 0.dp),fontSize=10.sp,fontWeight=FontWeight.Bold)}}

@Composable private fun PulseScreen(points:Int,streak:Int,reports:Int,onHome:()->Unit,onMap:()->Unit,onCheck:()->Unit,onProfile:()->Unit){
 Scaffold(bottomBar={BottomNav("pulse",onHome,onMap,onCheck,{})},containerColor=Paper){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(horizontal=20.dp)){
  Header("Pulse",onBack=onHome,onProfile=onProfile);Eyebrow("YOUR CAMPUS CONTRIBUTION");Text("Your pulse.",Modifier.padding(top=8.dp),fontSize=36.sp,fontWeight=FontWeight.ExtraBold);Text("Your reports and streak now work as local frontend state.",Modifier.padding(top=6.dp),fontSize=14.sp,color=Muted)
  Spacer(Modifier.height(20.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){MetricCard(points.toString(),"Pulse Points",Lavender);MetricCard("🔥 "+streak,"Day Streak",Coral);MetricCard(reports.toString(),"Reports",Sage)}
  Spacer(Modifier.height(24.dp));Section("LOCAL LEADERBOARD","demo");listOf("You" to points,"Campus Scout" to maxOf(80,points+120),"Pulse Friend" to maxOf(50,points-40)).sortedByDescending{it.second}.forEachIndexed{i,item->LeaderboardRow(i+1,item.first,item.second)}
  Spacer(Modifier.height(20.dp));Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(WarmGray).padding(18.dp)){Text("Frontend-only note",fontWeight=FontWeight.Bold);Text("Points, streak and reports persist locally. The leaderboard is illustrative until a backend is added.",Modifier.padding(top=6.dp),fontSize=12.sp,color=Muted)}
  Spacer(Modifier.height(30.dp))
 }}
}
@Composable private fun MetricCard(value:String,label:String,bg:Color){Column(Modifier.width(108.dp).height(110.dp).clip(RoundedCornerShape(22.dp)).background(bg.copy(.7f)).padding(14.dp),verticalArrangement=Arrangement.SpaceBetween){Text(value,fontSize=23.sp,fontWeight=FontWeight.ExtraBold);Text(label,fontSize=11.sp,color=Muted)}}
@Composable private fun LeaderboardRow(rank:Int,name:String,score:Int){Row(Modifier.fillMaxWidth().padding(bottom=8.dp).clip(RoundedCornerShape(18.dp)).background(WarmGray).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text("#"+rank,fontWeight=FontWeight.ExtraBold,modifier=Modifier.width(42.dp));Text(name,Modifier.weight(1f),fontWeight=FontWeight.Bold);Text(score.toString()+" pts",fontSize=12.sp,color=Muted)}}

@Composable private fun ProfileScreen(points:Int,streak:Int,reports:Int,onHome:()->Unit,onMap:()->Unit,onCheck:()->Unit,onPulse:()->Unit,onReset:()->Unit){
 Scaffold(bottomBar={BottomNav("profile",onHome,onMap,onCheck,onPulse)},containerColor=Paper){p->Column(Modifier.fillMaxSize().padding(p).verticalScroll(rememberScrollState()).padding(horizontal=20.dp)){
  Header("Profile",onBack=onHome);Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(30.dp)).background(Lavender),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(58.dp).clip(CircleShape).background(Ink),contentAlignment=Alignment.Center){Icon(Icons.Default.Person,null,tint=Paper)};Text("Campus Scout",Modifier.padding(top=10.dp),fontSize=22.sp,fontWeight=FontWeight.ExtraBold);Text("Campus Pulse scout",fontSize=12.sp,color=Muted)}}
  Spacer(Modifier.height(18.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){MetricCard(points.toString(),"Points",LavenderSoft);MetricCard("🔥 "+streak,"Streak",Coral);MetricCard(reports.toString(),"Reports",Sage)}
  Spacer(Modifier.height(22.dp));Section("DEMO SETTINGS","local");Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(WarmGray).padding(16.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Storage,null);Column(Modifier.weight(1f).padding(start=12.dp)){Text("Local persistence",fontWeight=FontWeight.Bold);Text("Enabled on this device",fontSize=11.sp,color=Muted)};PillTag("ON",Sage)}
  Button(onClick=onReset,Modifier.fillMaxWidth().padding(top=14.dp).height(52.dp),shape=RoundedCornerShape(50),colors=ButtonDefaults.buttonColors(containerColor=Ink)){Text("Reset demo data")}
  Spacer(Modifier.height(30.dp))
 }}
}

@Composable private fun BottomNav(active:String,onHome:()->Unit,onMap:()->Unit,onCheck:()->Unit,onPulse:()->Unit){
 NavigationBar(containerColor=Paper){
  NavigationBarItem(selected=active=="home",onClick=onHome,icon={Icon(Icons.Default.Home,null)},label={Text("Home")})
  NavigationBarItem(selected=active=="map",onClick=onMap,icon={Icon(Icons.Default.Map,null)},label={Text("Map")})
  NavigationBarItem(selected=active=="check",onClick=onCheck,icon={Icon(Icons.Default.AddCircleOutline,null)},label={Text("Check in")})
  NavigationBarItem(selected=active=="pulse",onClick=onPulse,icon={Icon(Icons.Default.EmojiEvents,null)},label={Text("Pulse")})
 }
}
