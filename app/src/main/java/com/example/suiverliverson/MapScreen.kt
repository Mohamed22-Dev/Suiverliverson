package com.example.suiverliverson

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.suiverliverson.ui.theme.AppSurface
import com.example.suiverliverson.ui.theme.Blue
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.White
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// ---------- إعدادات يمكنك تعديلها ----------
private const val OSM_OVERLAY_TRANSPARENCY = 0.35f // 0 = OSM فقط، 0.6 = طبقة خفيفة
private const val SIM_SPEED_MPS = 20.0             // سرعة الدراجة (متر/ثانية)، 12 = أبطأ
private const val SIM_TICK_MS = 200L               // كل كم ميلي ثانية تتحرك
private const val PICKUP_AT_FRACTION = 0.45        // نقطة الاستلام عند 45% من المسار
private val DEFAULT_START = LatLng(33.5731, -7.5898)

val darkMapStyleJson = """
[
  {"elementType": "geometry", "stylers": [{"color": "#1d1d2b"}]},
  {"elementType": "labels.text.fill", "stylers": [{"color": "#8ec3b9"}]},
  {"elementType": "labels.text.stroke", "stylers": [{"color": "#1a1a2b"}]},
  {"featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{"color": "#d59563"}]},
  {"featureType": "poi", "elementType": "labels.text.fill", "stylers": [{"color": "#d59563"}]},
  {"featureType": "poi.park", "elementType": "geometry", "stylers": [{"color": "#263c3f"}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"color": "#2c2c3e"}]},
  {"featureType": "road", "elementType": "geometry.stroke", "stylers": [{"color": "#212130"}]},
  {"featureType": "road.highway", "elementType": "geometry", "stylers": [{"color": "#3d3d5c"}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"color": "#0d0d17"}]}
]
""".trimIndent()

// ---------- بلاطات OpenStreetMap بألوان داكنة (الأزقة والشوارع الصغيرة) ----------
// عكس الألوان مع الحفاظ على الماء أزرق والحدائق خضراء
private val OSM_DARK_MATRIX = floatArrayOf(
    0.488f, -1.216f, -0.122f, 0f, 238.8f,
    -0.362f, -0.366f, -0.122f, 0f, 238.8f,
    -0.362f, -1.216f, 0.728f, 0f, 238.8f,
    0f, 0f, 0f, 1f, 0f
)

private class LiversonOsmTiles(
    private val darken: Boolean = true
) : TileProvider {

    private val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix(OSM_DARK_MATRIX))
    }

    override fun getTile(x: Int, y: Int, zoom: Int): Tile? {
        return try {
            val url = URL("https://tile.openstreetmap.org/$zoom/$x/$y.png")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "LiversonApp/1.0 (student project)")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val bytes = conn.inputStream.use { it.readBytes() }

            if (!darken) return Tile(256, 256, bytes)

            val src = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
            Canvas(out).drawBitmap(src, 0f, 0f, paint)

            val stream = ByteArrayOutputStream()
            out.compress(Bitmap.CompressFormat.PNG, 100, stream)
            Tile(256, 256, stream.toByteArray())
        } catch (e: Exception) {
            null
        }
    }
}

// ---------- خطة المسار ----------
class RoutePlan(
    val points: List<LatLng>,
    val cum: DoubleArray,        // المسافة التراكمية (متر) عند كل نقطة
    val pickup: LatLng,
    val delivery: LatLng,
    val pickupMeters: Double     // على أي مسافة تقع نقطة الاستلام
) {
    val total: Double get() = cum.last()
}

private fun haversine(a: LatLng, b: LatLng): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLng / 2).pow(2)
    return 2 * r * asin(sqrt(h))
}

private fun shiftPoint(from: LatLng, meters: Double, angle: Double): LatLng {
    val dLat = meters * cos(angle) / 111_320.0
    val dLng = meters * sin(angle) / (111_320.0 * cos(Math.toRadians(from.latitude)))
    return LatLng(from.latitude + dLat, from.longitude + dLng)
}

// موقع الدراجة بعد قطع مسافة معيّنة + رقم القطعة الحالية (سرعة ثابتة)
private fun locate(points: List<LatLng>, cum: DoubleArray, distance: Double): Pair<LatLng, Int> {
    val d = distance.coerceIn(0.0, cum.last())
    var i = 0
    while (i < cum.size - 2 && cum[i + 1] < d) i++
    val segLen = cum[i + 1] - cum[i]
    val t = if (segLen <= 0.0) 0.0 else (d - cum[i]) / segLen
    val a = points[i]
    val b = points[i + 1]
    val pos = LatLng(
        a.latitude + (b.latitude - a.latitude) * t,
        a.longitude + (b.longitude - a.longitude) * t
    )
    return pos to i
}

// نقطة الاستلام تُوضع على المسار نفسه، والتسليم هو نهاية المسار
private fun makePlan(rawPoints: List<LatLng>): RoutePlan {
    val pts = if (rawPoints.size >= 2) rawPoints else listOf(rawPoints.first(), rawPoints.first())
    val cum = DoubleArray(pts.size)
    for (i in 1 until pts.size) cum[i] = cum[i - 1] + haversine(pts[i - 1], pts[i])
    val pickupMeters = cum.last() * PICKUP_AT_FRACTION
    val pickup = locate(pts, cum, pickupMeters).first
    return RoutePlan(pts, cum, pickup, pts.last(), pickupMeters)
}

private class RouteResult(val points: List<LatLng>, val distance: Double)

// طلب مسار واحد من OSRM (الخطوط تتبع الشوارع الحقيقية)
private fun requestRoute(from: LatLng, to: LatLng): RouteResult? {
    return try {
        val url = URL(
            "https://router.project-osrm.org/route/v1/driving/" +
                    "${from.longitude},${from.latitude};${to.longitude},${to.latitude}" +
                    "?overview=full&geometries=geojson"
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "LiversonApp/1.0 (student project)")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        val text = conn.inputStream.bufferedReader().use { it.readText() }

        val route = JSONObject(text).getJSONArray("routes").getJSONObject(0)
        val line = route.getJSONObject("geometry").getJSONArray("coordinates")
        val pts = (0 until line.length()).map { i ->
            val c = line.getJSONArray(i)
            LatLng(c.getDouble(1), c.getDouble(0)) // [lon, lat]
        }
        if (pts.size < 2) null else RouteResult(pts, route.getDouble("distance"))
    } catch (e: Exception) {
        null
    }
}

// نجرّب 3 وجهات ونختار المسار الأكثر استقامة (نسبة مسافة الطريق إلى المسافة المباشرة)
// الاتجاهات ثابتة لنفس رقم الطلب، فيبقى المسار نفسه
suspend fun fetchRoutePlan(anchor: LatLng, orderId: Any?): RoutePlan {
    return withContext(Dispatchers.IO) {
        val rnd = Random(orderId.hashCode())
        var best: RouteResult? = null
        var bestRatio = Double.MAX_VALUE
        var firstDest: LatLng? = null

        for (k in 0 until 3) {
            val dist = 1500.0 + rnd.nextDouble() * 1000.0
            val angle = rnd.nextDouble() * 2 * Math.PI
            val dest = shiftPoint(anchor, dist, angle)
            if (firstDest == null) firstDest = dest

            val r = requestRoute(anchor, dest)
            if (r != null) {
                val straight = haversine(r.points.first(), r.points.last())
                val ratio = if (straight > 0) r.distance / straight else Double.MAX_VALUE
                if (ratio < bestRatio) {
                    best = r
                    bestRatio = ratio
                }
                if (bestRatio < 1.35) break
            }
        }

        val chosen = best
        if (chosen != null) {
            makePlan(chosen.points)
        } else {
            // بدون إنترنت: خط مستقيم مؤقت
            makePlan(listOf(anchor, firstDest ?: shiftPoint(anchor, 1800.0, 0.8)))
        }
    }
}

// نقطة بداية ثابتة: تُحفظ أول مرة ولا تتغير إلا إذا ابتعدت أكثر من 5 كم
@SuppressLint("MissingPermission")
private fun stableAnchor(context: Context, hasPermission: Boolean): LatLng {
    val prefs = context.getSharedPreferences("map_sim", Context.MODE_PRIVATE)
    val saved = prefs.getString("lat", null)?.toDoubleOrNull()?.let { lat ->
        prefs.getString("lng", null)?.toDoubleOrNull()?.let { lng -> LatLng(lat, lng) }
    }

    var gps: LatLng? = null
    if (hasPermission) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) gps = LatLng(loc.latitude, loc.longitude)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val chosen = when {
        saved != null && (gps == null || haversine(saved, gps) < 5000.0) -> saved
        gps != null -> gps
        else -> DEFAULT_START
    }

    if (gps != null && chosen == gps && chosen != saved) {
        prefs.edit()
            .putString("lat", chosen.latitude.toString())
            .putString("lng", chosen.longitude.toString())
            .apply()
    }
    return chosen
}

fun getMotorcycleIcon(context: Context): BitmapDescriptor {
    return try {
        MapsInitializer.initialize(context)
        val bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor("#007BFF")
            isAntiAlias = true
        }
        canvas.drawCircle(40f, 40f, 36f, paint)
        val textPaint = Paint().apply {
            textSize = 42f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("🛵", 40f, 54f, textPaint)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
    }
}

fun getCompactMarkerIcon(context: Context, emoji: String, colorHex: String): BitmapDescriptor {
    return try {
        MapsInitializer.initialize(context)
        val bitmap = Bitmap.createBitmap(60, 60, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor(colorHex)
            isAntiAlias = true
        }
        canvas.drawCircle(30f, 30f, 26f, paint)
        val textPaint = Paint().apply {
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(emoji, 30f, 40f, textPaint)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        BitmapDescriptorFactory.defaultMarker()
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(deliveryManager: DeliveryManager) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val activeOrder = deliveryManager.currentActiveOrder

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // نقطة البداية الثابتة
    val anchor by produceState<LatLng?>(initialValue = null, hasLocationPermission) {
        value = stableAnchor(context, hasLocationPermission)
    }

    // مسافة الدراجة المقطوعة (بالمتر) وخطة المسار
    var traveled by remember { mutableDoubleStateOf(0.0) }
    var plan by remember { mutableStateOf<RoutePlan?>(null) }
    var isDeliveredShowDialog by remember { mutableStateOf(false) }

    LaunchedEffect(activeOrder?.id, anchor) {
        val a = anchor ?: return@LaunchedEffect
        traveled = 0.0
        isDeliveredShowDialog = false
        plan = null
        plan = fetchRoutePlan(a, activeOrder?.id)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_START, 14f)
    }

    LaunchedEffect(anchor) {
        anchor?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    // عرض المسار كاملاً عند وصول الخطة
    LaunchedEffect(plan) {
        val p = plan ?: return@LaunchedEffect
        try {
            val bounds = LatLngBounds.builder().apply { p.points.forEach { include(it) } }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 140))
        } catch (e: Exception) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(p.points.first(), 15f))
        }
    }

    val mapProperties = remember(hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapStyleOptions = try { MapStyleOptions(darkMapStyleJson) } catch (e: Exception) { null }
        )
    }

    val mapUiSettings = remember(hasLocationPermission) {
        MapUiSettings(
            myLocationButtonEnabled = hasLocationPermission,
            zoomControlsEnabled = false
        )
    }

    // طبقة OpenStreetMap (الأزقة والشوارع الصغيرة) بألوان داكنة
    val osmTiles = remember { LiversonOsmTiles() }

    val motorcycleIcon = remember(context) { getMotorcycleIcon(context) }
    val pickupMarkerIcon = remember(context) { getCompactMarkerIcon(context, "🏪", "#E91E63") }
    val deliveryMarkerIcon = remember(context) { getCompactMarkerIcon(context, "🏠", "#28A745") }

    // حركة بسرعة ثابتة، والاستلام عند وصول الدراجة لنقطة المتجر
    // (تتحرك فقط عندما لا تكون الحالة IDLE، أي بعد قبول الطلب)
    LaunchedEffect(deliveryManager.deliveryStage, plan) {
        val p = plan ?: return@LaunchedEffect
        if (deliveryManager.deliveryStage == DeliveryStage.IDLE) return@LaunchedEffect

        val step = SIM_SPEED_MPS * SIM_TICK_MS / 1000.0
        while (traveled < p.total) {
            delay(SIM_TICK_MS)
            traveled = min(traveled + step, p.total)

            if (traveled >= p.pickupMeters && deliveryManager.deliveryStage == DeliveryStage.PICKUP_EN_ROUTE) {
                deliveryManager.completePickup()
                Toast.makeText(context, s.orderStarted, Toast.LENGTH_SHORT).show()
            }

            if (traveled >= p.total && deliveryManager.deliveryStage == DeliveryStage.ORDER_PICKED_UP) {
                isDeliveredShowDialog = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            // طبقة OpenStreetMap فوق خريطة Google (تحت المسار والعلامات)
            TileOverlay(
                tileProvider = osmTiles,
                transparency = OSM_OVERLAY_TRANSPARENCY
            )

            plan?.let { p ->
                val (bikePos, segIndex) = locate(p.points, p.cum, traveled)

                // الجزء المتبقي من المسار فقط: يُمسح الخط خلف الدراجة أثناء تقدمها
                Polyline(
                    points = listOf(bikePos) + p.points.subList(segIndex + 1, p.points.size),
                    color = Blue,
                    width = 10f
                )

                Marker(
                    state = remember(p) { MarkerState(position = p.pickup) },
                    icon = pickupMarkerIcon,
                    title = activeOrder?.pickup ?: "الاستلام"
                )

                Marker(
                    state = remember(p) { MarkerState(position = p.delivery) },
                    icon = deliveryMarkerIcon,
                    title = activeOrder?.client ?: "التوصيل"
                )

                val bikeState = remember(p) { MarkerState(position = p.points.first()) }
                bikeState.position = bikePos
                Marker(
                    state = bikeState,
                    icon = motorcycleIcon,
                    title = "الدراجة النارية 🛵"
                )
            }
        }

        // نص حقوق OpenStreetMap (مطلوب قانونياً)
        Text(
            text = "© OpenStreetMap contributors",
            color = Color.White,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 6.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )

        // للتشخيص فقط: يعرض مرحلة التوصيل الحالية (احذفه لاحقاً)
        Text(
            text = "● ${deliveryManager.deliveryStage}",
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )

        activeOrder?.let { order ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppSurface)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.customerName,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Green)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$${"%.2f".format(order.price)}",
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (isDeliveredShowDialog) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 30.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Green)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = s.orderDeliveredConfirm,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            deliveryManager.completeDelivery()
                            isDeliveredShowDialog = false
                            traveled = 0.0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(s.confirmer, color = Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}