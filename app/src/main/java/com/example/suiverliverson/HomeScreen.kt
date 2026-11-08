package com.example.suiverliverson

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.suiverliverson.ui.theme.AppBackground
import com.example.suiverliverson.ui.theme.DarkGray
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.RealWhite
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    deliveryManager: DeliveryManager,
    onOpenDrawer: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onOrderClick: (Order) -> Unit = {}
) {
    val s = LocalStrings.current
    val context = LocalContext.current

    var account by remember { mutableStateOf(AccountStore(context).current()) }
    LaunchedEffect(Unit) {
        account = AccountStore(context).current()
    }

    val imageUriString = account?.imageUri
    var online by remember { mutableStateOf(true) }

    var dynamicCity by remember { mutableStateOf(s.casablanca) }

    LaunchedEffect(Unit) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea
                    if (!city.isNullOrBlank()) dynamicCity = city
                }
            }
        } catch (e: Exception) {
            dynamicCity = s.casablanca
        }
    }

    var availableOrders by remember { mutableStateOf(generateRandomOrdersList()) }
    var incomingOrder by remember { mutableStateOf<Order?>(null) }
    var infoDialogOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(online) {
        if (online) {
            delay(3000L)
            if (incomingOrder == null) {
                incomingOrder = generateRandomIncomingOrder()
            }
        }
        while (online) {
            delay(20 * 60 * 1000L)
            if (incomingOrder == null) {
                incomingOrder = generateRandomIncomingOrder()
            }
        }
    }

    var countdownSeconds by remember(incomingOrder) { mutableIntStateOf(30) }

    LaunchedEffect(incomingOrder) {
        if (incomingOrder != null) {
            countdownSeconds = 30
            while (countdownSeconds > 0) {
                delay(1000L)
                countdownSeconds--
            }
            incomingOrder = null
        }
    }

    val userBitmap = remember(imageUriString) {
        if (!imageUriString.isNullOrEmpty()) {
            try {
                if (imageUriString.startsWith("content://")) {
                    context.contentResolver.openInputStream(Uri.parse(imageUriString))?.use {
                        BitmapFactory.decodeStream(it)
                    }
                } else {
                    BitmapFactory.decodeFile(imageUriString)
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val switchAlignmentBias by animateFloatAsState(
        targetValue = if (online) 1f else -1f,
        animationSpec = tween(durationMillis = 220),
        label = "switch"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Red)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(RealWhite.copy(alpha = 0.2f))
                                    .clickable { onOpenDrawer() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (userBitmap != null) {
                                    Image(
                                        bitmap = userBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(R.drawable.avatar),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Good evening / مساء الخير", color = RealWhite.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text(
                                    "Hello, ${account?.firstName ?: "Driver"}",
                                    color = RealWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RealWhite.copy(alpha = 0.2f))
                                .clickable { onNavigateToNotifications() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = RealWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = RealWhite.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (online) Green else RealWhite.copy(alpha = 0.5f))
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        if (online) s.youAreOnline else s.youAreOffline,
                                        color = RealWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        if (online) s.readyForOrders else s.goOnlineToStart,
                                        color = RealWhite.copy(alpha = 0.75f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(28.dp)
                                    .clip(CircleShape)
                                    .background(if (online) Green else RealWhite.copy(alpha = 0.3f))
                                    .clickable { online = !online }
                                    .padding(3.dp),
                                contentAlignment = BiasAlignment(switchAlignmentBias, 0f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(RealWhite)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-16).dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RealWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Today's earnings / أرباح اليوم", color = LightGray, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "$${"%.2f".format(deliveryManager.totalEarnings.let { if (it <= 0) 186.00 else it })}",
                                color = White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Red, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = DarkGray)
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Red, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("${deliveryManager.completedTrips.let { if (it <= 0) 112 else it }}", color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Trips", color = LightGray, fontSize = 11.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text("%.1f".format(deliveryManager.rating), color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Rating", color = LightGray, fontSize = 11.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Red, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(dynamicCity, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Location", color = LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = s.newTripRequests,
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { availableOrders = generateRandomOrdersList() }) {
                        Text(s.seeAll, color = Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(10.dp))

                availableOrders.forEach { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = RealWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    InitialsAvatar(name = order.customerName)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(order.customerName, color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("${order.id} • ${order.distanceKm} km", color = LightGray, fontSize = 12.sp)
                                    }
                                }
                                Text(
                                    "$${"%.2f".format(order.price)}",
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = LightGray, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(s.fromLabel.format(order.pickup), color = LightGray, fontSize = 12.sp)
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { infoDialogOrder = order },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(22.dp)
                                ) {
                                    Text(s.info, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Button(
                                    onClick = {
                                        deliveryManager.acceptOrder(order)
                                        onNavigateToMap()
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                                    shape = RoundedCornerShape(22.dp)
                                ) {
                                    Text(s.accept, color = RealWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        // نافذة Bottom Sheet للطلبيات الجديدة
        incomingOrder?.let { order ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { incomingOrder = null },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = RealWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(DarkGray)
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0F2F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "New Delivery Request",
                                    color = White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE0F2F1))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = Red,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "0:${if (countdownSeconds < 10) "0$countdownSeconds" else countdownSeconds}",
                                        color = Red,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                InitialsAvatar(name = order.customerName)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        order.customerName,
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(order.id, color = LightGray, fontSize = 12.sp)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Price", color = LightGray, fontSize = 11.sp)
                                Text(
                                    "$${"%.2f".format(order.price)}",
                                    color = White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkGray.copy(alpha = 0.5f))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.Top) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Red)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(24.dp)
                                                .background(LightGray.copy(alpha = 0.5f))
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Pickup", color = LightGray, fontSize = 11.sp)
                                        Text(
                                            order.pickup,
                                            color = White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Green)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Drop-off", color = LightGray, fontSize = 11.sp)
                                        Text(
                                            order.client,
                                            color = White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkGray)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "🛒 ${order.orderType}",
                                    color = White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkGray)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "🛣️ ${order.distanceKm} km",
                                    color = White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { incomingOrder = null },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGray),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(s.reject, color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Button(
                                onClick = {
                                    deliveryManager.acceptOrder(order)
                                    incomingOrder = null
                                    onNavigateToMap()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Red),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(s.accept, color = RealWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    infoDialogOrder?.let { order ->
        Dialog(onDismissRequest = { infoDialogOrder = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RealWhite)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = s.incomingOrder,
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InitialsAvatar(name = order.customerName)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(order.customerName, color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${order.id} • ${order.distanceKm} km", color = LightGray, fontSize = 12.sp)
                        }
                        Text("$${"%.2f".format(order.price)}", color = Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(s.fromLabel.format(order.pickup), color = LightGray, fontSize = 13.sp)
                    Text(s.toLabel.format(order.client), color = LightGray, fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { infoDialogOrder = null },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(s.close, color = White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                deliveryManager.acceptOrder(order)
                                infoDialogOrder = null
                                onNavigateToMap()
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Red),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(s.accept, color = RealWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InitialsAvatar(name: String) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text(initials.ifEmpty { "DR" }, color = Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
