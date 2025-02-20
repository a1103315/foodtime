package com.example.foodtime_compose0518

import ExpireScreen
import FoodExpirationScreen
import HolidayScreen
import Signal_Notification
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.foodtime_compose0518.ui.theme.Foodtime0518_Theme
import com.example.foodtime_compose0518.ui.theme.secondaryContainerLight
import com.example.foodtime_compose0518.ui.theme.surfaceContainerLowLight
import androidx.activity.viewModels
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.foodtime_compose0518.MainActivity.Companion.CHANNEL_ID
import com.example.foodtime_compose0518.worker.NotificationReceiver
import com.example.foodtime_compose0518.worker.StockNotification
import com.google.firebase.database.FirebaseDatabase
import setting
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    companion object {
        const val CHANNEL_ID = "default_channel_id"

    }

    private val holidayViewModel: HolidayViewModel by viewModels {
        val database = FoodDatabase.getInstance(application)
        HolidayViewModelFactory(
            dao = database.foodDao,
            holidayDetailDao = database.holidayDetailDao,
            itemDao = database.itemDao

        )
    }
    private val stockViewModel: StockViewModel by viewModels {
        StockViewModelFactory(FoodDatabase.getInstance(application).stockDao,FoodDatabase.getInstance(application).settingDao)
    }
    private val normalViewModel: NormalViewModel by viewModels {
        NormalViewModelFactory(FoodDatabase.getInstance(application).normalDao)
    }
    private val settingViewModel: SettingViewModel by viewModels {
        SettingViewModelFactory(FoodDatabase.getInstance(application).settingDao)
    }
    private val itemViewModel: ItemViewModel by viewModels {
        ItemViewModelFactory(FoodDatabase.getInstance(application).itemDao)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 創建通知頻道
        createNotificationChannel(this)

        // 處理來自通知的啟動
        handleNotificationIntent(intent)

        // 設置定時通知
        setupRepeatingNotification()

        val database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(true)

        setContent {
            Foodtime0518_Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(
                        holidayViewModel,
                        normalViewModel,
                        stockViewModel,
                        settingViewModel,
                        itemViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
            val notificationType = intent.getStringExtra("notification_type")
            notificationType?.let { type ->
                when (type) {
                    "food_expired" -> println("處理通知: 食物過期")
                    "food_expiring" -> println("處理通知: 即將過期")
                    else -> println("處理通知: 其他通知")
                }
            }
    }

    private fun setupRepeatingNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val repeatInterval = 60 * 1000L
        val triggerTime = System.currentTimeMillis() + repeatInterval

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            repeatInterval,
            pendingIntent
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}


data class DrawerMenuItem(
    val route: String,
    val icon: ImageVector,
    val title: String
)
val routeTitleMap = mapOf(
    "ingredients" to "食材庫",
    "holidays" to "節日清單",
    "NormalList" to "常備清單",
    "Expired_food" to "過期食材",
    "logout" to "登出",
    "home_page" to "首頁",
    "addFragment" to "新增食材",
    "Addholiday" to "新增節日",
    "FoodDetail" to "食材資訊",
    "HolidayDetail" to "所需食材",
    "HolidayAddFragment" to "新增食材",
    "NormalListAddFragment" to "常備清單新增食材",
    "setting" to "設定",
    "Signal_Notification" to "燈號通知提醒",
    "Foodexpiration_setting" to "食材到期設定",

    )

val drawerMenuItems = listOf(
    DrawerMenuItem("ingredients", Icons.Default.Menu, "食材庫"),
    DrawerMenuItem("holidays", Icons.Default.FavoriteBorder, "節日清單"),
    DrawerMenuItem("NormalList", Icons.AutoMirrored.Filled.List, "常備清單"),
    DrawerMenuItem("Expired_food", Icons.Default.Delete, "過期食材"),
    DrawerMenuItem("setting", Icons.AutoMirrored.Filled.ExitToApp, "設定")
)


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(
    holidayViewModel: HolidayViewModel,
    normalViewModel: NormalViewModel,
    stockViewModel: StockViewModel,
    settingViewModel: SettingViewModel,
    itemViewModel: ItemViewModel
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val items = drawerMenuItems
    val selectedItem = remember { mutableStateOf(items[0]) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route ?: "ingredients"

    // 检查 route 是否包含特定的参数
    val currentTitle = when {
        currentRoute.contains("FoodDetail/") -> "食材資訊"
        currentRoute.contains("HolidayDetail/") -> "所需食材"
        currentRoute.contains("HolidayAddFragment/") -> "新增食材"
        else -> routeTitleMap[currentRoute] ?: "食材庫"
    }



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(20.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(imageVector = item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                        selected = item.route == currentRoute,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            selectedItem.value = item
                            navController.navigate(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentTitle) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate("home_page") }) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = secondaryContainerLight,
                        )
                    )
                },
            ) { paddingValues ->
                NavHost(navController = navController, startDestination = "home_page", Modifier.padding(paddingValues)) {
                    composable("ingredients") { IngredientsScreen(navController,stockViewModel) }
                    composable("holidays") { HolidayScreen(navController,holidayViewModel) }
                    composable("Addholiday") { HolidayAdd(navController, holidayViewModel) }
                    composable("HolidayDetail/{holidayId}") { backStackEntry ->
                        val holidayId = backStackEntry.arguments?.getString("holidayId")?.toIntOrNull()
                        if (holidayId != null) {
                            HolidayDetailScreen(navController, holidayId, holidayViewModel)
                        }
                    }
                    composable("NormalList") { Normallist(navController, normalViewModel) }
                    composable("HolidayAddFragment/{holidayId}") { backStackEntry ->
                        val holidayId = backStackEntry.arguments?.getString("holidayId")?.toIntOrNull()
                        if (holidayId != null) {
                            HolidayAddFragmentScreen(navController, holidayId, holidayViewModel, itemViewModel = itemViewModel)
                        }
                    }
                    composable("Expired_food") { ExpireScreen(navController,stockViewModel) }
                    composable("home_page") { Home_pageScreen() }
                    composable("logout") { LoginScreen(navController) }
                    composable("addFragment") { AddFragmentScreen(navController, stockViewModel,itemViewModel) }
                    composable("FoodDetail/{stockitemId}") { backStackEntry ->
                        val stockitemId = backStackEntry.arguments?.getString("stockitemId")?.toIntOrNull()
                        if (stockitemId != null) {
                            DetailFragment(navController, stockitemId = stockitemId, stockViewModel)
                        }
                    }
                    composable("NormalListAddFragment") { NormalAddFragment(navController,normalViewModel) }
                    composable("setting") { setting(navController) }
                    composable("Signal_Notification"){Signal_Notification(navController, settingViewModel)}
                    composable("Foodexpiration_setting"){FoodExpirationScreen(navController, settingViewModel)}
                }
            }

        }
    )
}


@Composable
fun Home_pageScreen() {
    TemplateScreen(
        title = "首頁"

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.new_refrigerator),
                contentDescription = "Example Image",
                modifier = Modifier
                    .padding(20.dp)
                    .size(450.dp) // 设置固定大小为 200dp
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(
    title: String,
    backgroundColor: Color = surfaceContainerLowLight,
    content: @Composable () -> Unit
) {
    Scaffold(
        content = {

            Box(modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
                contentAlignment = Alignment.Center) {

                content()
            }
        }
    )
}

