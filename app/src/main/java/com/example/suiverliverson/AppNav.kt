package com.example.suiverliverson

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.suiverliverson.ui.theme.AppSurface
import com.example.suiverliverson.ui.theme.DarkGray
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.RealWhite
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White

enum class Tab(val route: String, val icon: ImageVector) {
    HOME("home", Icons.Outlined.Home),
    STATS("stats", Icons.Outlined.BarChart),
    MAP("map", Icons.Outlined.Place),
    PROFILE("profile", Icons.Outlined.Person);

    fun label(s: Strings) = when (this) {
        HOME -> s.home
        STATS -> s.stats
        MAP -> s.map
        PROFILE -> s.profile
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val s = LocalStrings.current
    val context = LocalContext.current
    val store = remember { AccountStore(context) }
    val deliveryManager = remember { DeliveryManager() }

    var isDrawerOpen by remember { mutableStateOf(false) }
    var account by remember { mutableStateOf(store.current()) }

    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route
    val showBar = Tab.entries.any { it.route == current }

    LaunchedEffect(current) {
        account = store.current()
    }

    val userBitmap = remember(account?.imageUri) {
        if (!account?.imageUri.isNullOrEmpty()) {
            try {
                val pathStr = account!!.imageUri!!
                if (pathStr.startsWith("content://")) {
                    context.contentResolver.openInputStream(Uri.parse(pathStr))?.use {
                        BitmapFactory.decodeStream(it)
                    }
                } else {
                    BitmapFactory.decodeFile(pathStr)
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                NavigationBar(
                    containerColor = RealWhite,
                    tonalElevation = 0.dp
                ) {
                    Tab.entries.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(Tab.HOME.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label(s),
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Red,
                                selectedTextColor = Red,
                                indicatorColor = Color(0xFFE0F2F1),
                                unselectedIconColor = LightGray,
                                unselectedTextColor = LightGray
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            composable("splash") {
                SplashScreen {
                    val startDest = if (store.current() != null) "home" else "login"
                    nav.navigate(startDest) { popUpTo("splash") { inclusive = true } }
                }
            }
            composable("login") {
                LoginScreen(
                    store = store,
                    onLogin = {
                        nav.navigate("home") { popUpTo("login") { inclusive = true } }
                    },
                    onCreateAccount = { nav.navigate("signup") },
                    onForgotPassword = { nav.navigate("forgot") }
                )
            }
            composable("signup") {
                SignUpScreen(
                    store = store,
                    onBack = { nav.popBackStack() },
                    onSuccess = {
                        nav.navigate("home") { popUpTo("login") { inclusive = true } }
                    }
                )
            }
            composable("forgot") {
                ForgotPasswordScreen(
                    store = store,
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Tab.HOME.route) {
                HomeScreen(
                    deliveryManager = deliveryManager,
                    onOpenDrawer = { isDrawerOpen = true },
                    onNavigateToMap = { nav.navigate(Tab.MAP.route) },
                    onNavigateToNotifications = { nav.navigate("notifications") }
                )
            }
            composable(Tab.STATS.route) {
                StatsScreen()
            }
            composable(Tab.MAP.route) {
                MapScreen(deliveryManager = deliveryManager)
            }
            composable(Tab.PROFILE.route) {
                ProfileScreen(
                    store = store,
                    deliveryManager = deliveryManager,
                    onNavigateToSettings = { nav.navigate("settings") },
                    onNavigateToHistory = { nav.navigate("history") },
                    onLogout = {
                        store.logout()
                        nav.navigate("login") { popUpTo("home") { inclusive = true } }
                    }
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    deliveryManager = deliveryManager,
                    onBack = { nav.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    store = store,
                    onBack = { nav.popBackStack() }
                )
            }
            composable("history") {
                HistoryScreen(
                    deliveryManager = deliveryManager,
                    onBack = { nav.popBackStack() }
                )
            }
        }

        if (isDrawerOpen) {
            Dialog(
                onDismissRequest = { isDrawerOpen = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { isDrawerOpen = false }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(300.dp)
                            .align(Alignment.CenterStart)
                            .background(AppSurface)
                            .clickable(enabled = false) {}
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Red)
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, RealWhite, CircleShape),
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
                                        Text(
                                            "${account?.firstName ?: "Driver"} ${account?.lastName ?: ""}",
                                            color = RealWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "ID: ${account?.id ?: "#DRV1024"}",
                                            color = RealWhite.copy(alpha = 0.8f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            DrawerMenuItem(
                                icon = Icons.Outlined.Home,
                                label = s.home,
                                onClick = {
                                    isDrawerOpen = false
                                    nav.navigate(Tab.HOME.route) { popUpTo(Tab.HOME.route) { inclusive = true } }
                                }
                            )

                            DrawerMenuItem(
                                icon = Icons.Outlined.BarChart,
                                label = s.stats,
                                onClick = {
                                    isDrawerOpen = false
                                    nav.navigate(Tab.STATS.route)
                                }
                            )

                            DrawerMenuItem(
                                icon = Icons.Outlined.Place,
                                label = s.map,
                                onClick = {
                                    isDrawerOpen = false
                                    nav.navigate(Tab.MAP.route)
                                }
                            )

                            DrawerMenuItem(
                                icon = Icons.Outlined.Person,
                                label = s.profile,
                                onClick = {
                                    isDrawerOpen = false
                                    nav.navigate(Tab.PROFILE.route)
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkGray)

                            DrawerMenuItem(
                                icon = Icons.Default.Notifications,
                                label = s.notifications,
                                onClick = {
                                    isDrawerOpen = false
                                    nav.navigate("notifications")
                                }
                            )

                            DrawerMenuItem(
                                icon = Icons.Default.Settings,
                                label = s.settings,
                                onClick = {
                                    isDrawerOpen = false
                                    nav.navigate("settings")
                                }
                            )

                            DrawerMenuItem(
                                icon = Icons.Default.History,
                                label = s.orderHistory,
                                onClick = {
                                    isDrawerOpen = false
                                    nav.navigate("history")
                                }
                            )

                            Spacer(Modifier.weight(1f))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkGray)

                            DrawerMenuItem(
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                label = s.logout,
                                textColor = Red,
                                iconTint = Red,
                                onClick = {
                                    isDrawerOpen = false
                                    store.logout()
                                    nav.navigate("login") { popUpTo("home") { inclusive = true } }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    textColor: Color = White,
    iconTint: Color = Red,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null, tint = iconTint) },
        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
    )
}
