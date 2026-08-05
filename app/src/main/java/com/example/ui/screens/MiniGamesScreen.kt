package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import com.example.ui.Localizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGamesScreen(viewModel: GameViewModel) {
    val settings by viewModel.settingsState.collectAsState()
    val playerTrader by viewModel.playerTraderState.collectAsState()
    
    val lang = settings?.selectedLanguage ?: "TR"
    val cash = playerTrader?.cash ?: 0.0

    // Life Simulation variables
    val currentHouseId = settings?.currentHouseId ?: "kiralik_kotu"
    val foodPlanId = settings?.foodPlanId ?: 1
    val furnitureBought = settings?.furnitureBought ?: ""
    val ownedCars = settings?.ownedCars ?: ""
    val activeCarId = settings?.activeCarId

    // UI Navigation tabs inside Life Simulator
    var subTab by remember { mutableIntStateOf(0) } // 0 = İşler (Jobs), 1 = Evim (My House), 2 = Garaj & Sürüş (Garage & Drive)

    // Game state managers
    var activeGameId by remember { mutableStateOf<String?>(null) } // "waiter", "electrician", "miner", "driver"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "TR") "YAŞAM & İŞ SİMÜLATÖRÜ" else "LIFE & JOB SIMULATOR",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFFFFC107)
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E2638), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Nakit: $${String.format("%.2f", cash)}",
                                color = Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0E14)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF06090E))
        ) {
            if (activeGameId != null) {
                // Render the interactive mini-games overlay full-screen
                when (activeGameId) {
                    "waiter" -> WaiterGame(lang = lang, onEarn = { amount -> viewModel.earnMiniGameCash(amount, "Garsonluk") }, onClose = { activeGameId = null })
                    "electrician" -> ElectricianGame(lang = lang, onEarn = { amount -> viewModel.earnMiniGameCash(amount, "Elektrikçilik") }, onClose = { activeGameId = null })
                    "miner" -> MinerGame(lang = lang, onEarn = { amount -> viewModel.earnMiniGameCash(amount, "Taş Ocağı İşçiliği") }, onClose = { activeGameId = null })
                    "driver" -> DrivingSimulator(activeCarId = activeCarId ?: "rust_bucket", lang = lang, onEarn = { amount -> viewModel.earnMiniGameCash(amount, "Taksi Sürüşü") }, onClose = { activeGameId = null })
                }
            } else {
                // Render main life simulator tabs
                Column(modifier = Modifier.fillMaxSize()) {
                    // Sub-tab selectors
                    TabRow(
                        selectedTabIndex = subTab,
                        containerColor = Color(0xFF0F141F),
                        contentColor = Color(0xFFFFC107)
                    ) {
                        Tab(
                            selected = subTab == 0,
                            onClick = { subTab = 0 },
                            text = { Text(if (lang == "TR") "İŞ BUL" else "JOBS", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = subTab == 1,
                            onClick = { subTab = 1 },
                            text = { Text(if (lang == "TR") "EVİM" else "MY HOUSE", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = subTab == 2,
                            onClick = { subTab = 2 },
                            text = { Text(if (lang == "TR") "GARAJ" else "GARAGE", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }

                    // Content areas
                    when (subTab) {
                        0 -> JobsTab(lang = lang, onSelectGame = { activeGameId = it })
                        1 -> HouseTab(
                            currentHouseId = currentHouseId,
                            furnitureBought = furnitureBought,
                            foodPlanId = foodPlanId,
                            cash = cash,
                            lang = lang,
                            onBuyFurniture = { id, name, price -> viewModel.buyFurniture(id, name, price) },
                            onRentOrBuyHouse = { id, price, isPurchase -> viewModel.buyOrRentHouse(id, price, isPurchase) },
                            onChangeFood = { id, name -> viewModel.changeFoodPlan(id, name) }
                        )
                        2 -> GarageTab(
                            ownedCars = ownedCars,
                            activeCarId = activeCarId,
                            cash = cash,
                            lang = lang,
                            onBuyCar = { id, name, price -> viewModel.buyCar(id, name, price) },
                            onSelectActiveCar = { id -> viewModel.selectActiveCar(id) },
                            onStartDrive = { activeGameId = "driver" }
                        )
                    }
                }
            }
        }
    }
}

// =================== TAB 0: JOBS (İŞ BUL) ===================

@Composable
fun JobsTab(lang: String, onSelectGame: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "TR") "💼 EL EMEĞİ İŞLER" else "💼 PHYSICAL MANUAL LABOR",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == "TR") 
                            "Trading yapmak için ilk sermayeyi toplaman lazım. Bu işler zordur ve az para kazandırır. Zengin olmak için ticareti öğrenmek tek yol!" 
                            else "You need initial capital to trade. These physical jobs are difficult and pay very little. Learning to trade is the only real way out of poverty!",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        item {
            JobCard(
                title = if (lang == "TR") "🍽️ Lokantada Garsonluk" else "🍽️ Restaurant Waitering",
                desc = if (lang == "TR") "Müşterilerin siparişlerini hızlıca teslim et. Hata yapma!" else "Quickly deliver customer orders from the kitchen. Don't make mistakes!",
                payout = if (lang == "TR") "İşlem Başına: $10" else "Per Delivery: $10",
                difficulty = if (lang == "TR") "Zorluk: Orta" else "Difficulty: Medium",
                btnText = if (lang == "TR") "Garsonluğa Başla" else "Start Waitering",
                onClick = { onSelectGame("waiter") }
            )
        }

        item {
            JobCard(
                title = if (lang == "TR") "⚡ Elektrik Panosu Tamiri" else "⚡ Electric Panel Repair",
                desc = if (lang == "TR") "Renkleri doğru bağlayarak kısa devreleri çöz. Dikkat et çarpılma!" else "Connect scrambled matching colors to fix panels. Watch out for shocks!",
                payout = if (lang == "TR") "Tamirat Başına: $25" else "Per Fix: $25",
                difficulty = if (lang == "TR") "Zorluk: Yüksek" else "Difficulty: High",
                btnText = if (lang == "TR") "Kabloları Bağla" else "Connect Wires",
                onClick = { onSelectGame("electrician") }
            )
        }

        item {
            JobCard(
                title = if (lang == "TR") "⛏️ Taş Ocağında Kazı" else "⛏️ Rock Quarry Mining",
                desc = if (lang == "TR") "Balyozunu kayaya vurarak parçala. Ağır fiziksel güç gerektirir." else "Swing your heavy hammer to break giant stones. Highly physically demanding.",
                payout = if (lang == "TR") "Kaya Başına: $15" else "Per Rock: $15",
                difficulty = if (lang == "TR") "Zorluk: Kolay (Tıklama)" else "Difficulty: Easy (Clicker)",
                btnText = if (lang == "TR") "Kaya Parçala" else "Mine Rocks",
                onClick = { onSelectGame("miner") }
            )
        }
    }
}

@Composable
fun JobCard(
    title: String,
    desc: String,
    payout: String,
    difficulty: String,
    btnText: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111724)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E283C), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, fontSize = 12.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = payout, color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = difficulty, color = Color.LightGray, fontSize = 11.sp)
                }
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = btnText, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// =================== TAB 1: HOUSE (EVİM & EŞYALAR) ===================

@Composable
fun HouseTab(
    currentHouseId: String,
    furnitureBought: String,
    foodPlanId: Int,
    cash: Double,
    lang: String,
    onBuyFurniture: (String, String, Double) -> Unit,
    onRentOrBuyHouse: (String, Double, Boolean) -> Unit,
    onChangeFood: (Int, String) -> Unit
) {
    val furnitureList = furnitureBought.split(",").filter { it.isNotEmpty() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Interactive Isometric 3D Room Render on Compose Canvas!
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101524)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2A44), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (lang == "TR") "🏠 EVİMİN İZOMETRİK 3D GÖRÜNÜMÜ" else "🏠 MY ISOMETRIC 3D HOUSE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    IsometricRoomCanvas(
                        houseId = currentHouseId,
                        furnitureList = furnitureList,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070B11))
                    )
                }
            }
        }

        // 2. Budget Diet Choices Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111724)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (lang == "TR") "🍽️ Yemek Planı (Aylık Kesilir)" else "🍽️ Diet Nutrition Plan (Monthly)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FoodPlanButton(
                            id = 1,
                            name = if (lang == "TR") "Kötü ($50/Ay)" else "Poor ($50/Mo)",
                            desc = if (lang == "TR") "Makarna & Su" else "Instant Ramen",
                            selected = foodPlanId == 1,
                            onClick = { onChangeFood(1, "Kötü Yemekler") },
                            modifier = Modifier.weight(1f)
                        )
                        FoodPlanButton(
                            id = 2,
                            name = if (lang == "TR") "Orta ($200/Ay)" else "Average ($200/Mo)",
                            desc = if (lang == "TR") "Ev Yemekleri" else "Home Cooked",
                            selected = foodPlanId == 2,
                            onClick = { onChangeFood(2, "Ortalama Yemekler") },
                            modifier = Modifier.weight(1f)
                        )
                        FoodPlanButton(
                            id = 3,
                            name = if (lang == "TR") "Lüks ($600/Ay)" else "Luxury ($600/Mo)",
                            desc = if (lang == "TR") "Biftek & Suşi" else "Steaks & Sushi",
                            selected = foodPlanId == 3,
                            onClick = { onChangeFood(3, "Lüks Yemekler") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Furniture Store
        item {
            Text(
                text = if (lang == "TR") "🛋️ Eşya Mağazası (Dizayn Et)" else "🛋️ Furniture Store",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        val furnitureStoreItems = listOf(
            Triple("lux_bed", if (lang == "TR") "🛏️ Lüks Ortopedik Yatak" else "🛏️ Luxury Orthopedic Bed", 400.0),
            Triple("giant_tv", if (lang == "TR") "📺 Dev Plazma Smart TV" else "📺 Giant Smart Plasma TV", 1200.0),
            Triple("leather_sofa", if (lang == "TR") "🛋️ Siyah Deri Koltuk Takımı" else "🛋️ Elegant Leather Sofa", 2500.0),
            Triple("mining_rig", if (lang == "TR") "⚡ Mining Rig (+ $150 Pasif Gelir!)" else "⚡ Crypto Mining Rig (+ $150 Passive Income!)", 5000.0)
        )

        items(furnitureStoreItems) { (id, name, price) ->
            val bought = furnitureList.contains(id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1422), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF1E283C), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Fiyat: $${String.format("%.2f", price)}", color = Color(0xFFFFC107), fontSize = 11.sp)
                }
                if (bought) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1B2E1E), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = if (lang == "TR") "Satın Alındı" else "Purchased", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onBuyFurniture(id, name, price) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF148F46)),
                        shape = RoundedCornerShape(6.dp),
                        enabled = cash >= price,
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(text = if (lang == "TR") "SATIN AL" else "BUY", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Housing Store
        item {
            Text(
                text = if (lang == "TR") "🏘️ Emlak Ofisi (Taşın)" else "🏘️ Real Estate (Relocate)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        val housesList = listOf(
            HouseOption("kiralik_kotu", if (lang == "TR") "🏚️ Gecekondu (Kiralık)" else "🏚️ Slum Shack (Rented)", 0.0, 150.0, 50.0, false),
            HouseOption("kiralik_orta", if (lang == "TR") "🏢 Şehir İçi Apartman (Kiralık)" else "🏢 City Apartment (Rented)", 1000.0, 500.0, 150.0, false),
            HouseOption("satinal_rezidans", if (lang == "TR") "🏙️ Lüks Rezidans (Satın Alınan)" else "🏙️ Luxury Condo (Owned)", 15000.0, 0.0, 250.0, true),
            HouseOption("satinal_villa", if (lang == "TR") "🏰 Ultra Malikane (Satın Alınan)" else "🏰 Ultra Villa (Owned)", 100000.0, 0.0, 500.0, true)
        )

        items(housesList) { house ->
            val isActive = currentHouseId == house.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1422), RoundedCornerShape(10.dp))
                    .border(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = if (isActive) Color(0xFFFFC107) else Color(0xFF1E283C),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = house.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (house.isPurchase) 
                            "Satış Bedeli: $${String.format("%.2f", house.price)} (Kira Yok!)"
                            else "Giriş/Depozito: $${String.format("%.2f", house.price)} | Kira: $${house.rent}/Ay",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Text(text = "Aylık Fatura Gideri: $${house.bills}", color = Color(0xFFFF5252), fontSize = 10.sp)
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x3BFFC107), CircleShape)
                            .border(1.dp, Color(0xFFFFC107), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = if (lang == "TR") "Aktif Evim" else "My Home", color = Color(0xFFFFC107), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onRentOrBuyHouse(house.id, house.price, house.isPurchase) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(6.dp),
                        enabled = cash >= house.price,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (house.isPurchase) 
                                (if (lang == "TR") "SATIN AL" else "BUY") 
                                else (if (lang == "TR") "KİRALA" else "RENT"),
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class HouseOption(
    val id: String,
    val name: String,
    val price: Double,
    val rent: Double,
    val bills: Double,
    val isPurchase: Boolean
)

@Composable
fun FoodPlanButton(
    id: Int,
    name: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0x3D00E676) else Color(0xFF141B2B)
        ),
        modifier = modifier
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) Color(0xFF00E676) else Color(0xFF1E283C),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, color = Color.Gray, fontSize = 9.sp, textAlign = TextAlign.Center)
        }
    }
}

// Draw a stylized vector room inside Canvas
@Composable
fun IsometricRoomCanvas(houseId: String, furnitureList: List<String>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f + 20f

        // 1. Draw floor polygon (Isometric cube base)
        val floorPath = Path().apply {
            moveTo(cx, cy - 60f)      // North
            lineTo(cx + 140f, cy)     // East
            lineTo(cx, cy + 60f)      // South
            lineTo(cx - 140f, cy)     // West
            close()
        }

        val floorBrush = when (houseId) {
            "kiralik_kotu" -> Brush.linearGradient(colors = listOf(Color(0xFF2E2C29), Color(0xFF1F1E1C))) // Cold dark cracked concrete
            "kiralik_orta" -> Brush.linearGradient(colors = listOf(Color(0xFF8D6E63), Color(0xFF4E342E))) // Mahogany laminate wood
            "satinal_rezidans" -> Brush.linearGradient(colors = listOf(Color(0xFF263238), Color(0xFF37474F))) // High-tech carpet
            "satinal_villa" -> Brush.linearGradient(colors = listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC))) // Elegant white marble
            else -> Brush.linearGradient(colors = listOf(Color.DarkGray, Color.Black))
        }
        drawPath(floorPath, brush = floorBrush)
        drawPath(floorPath, color = Color(0x3BFFFFFF), style = Stroke(2f)) // Isometric Grid Outline

        // If Slum (Gecekondu), draw cracks on floor
        if (houseId == "kiralik_kotu") {
            drawLine(Color(0xFF0F1014), Offset(cx - 40f, cy - 10f), Offset(cx + 20f, cy + 20f), strokeWidth = 2f)
            drawLine(Color(0xFF0F1014), Offset(cx - 10f, cy + 10f), Offset(cx - 60f, cy + 25f), strokeWidth = 1.5f)
        }

        // 2. Draw Back Walls (North-West and North-East)
        // Left Wall (North-West)
        val leftWall = Path().apply {
            moveTo(cx, cy - 60f)
            lineTo(cx - 140f, cy)
            lineTo(cx - 140f, cy - 110f)
            lineTo(cx, cy - 170f)
            close()
        }
        val wallColorLeft = when (houseId) {
            "kiralik_kotu" -> Color(0xFF1B1A18)
            "kiralik_orta" -> Color(0xFF424242)
            "satinal_rezidans" -> Color(0xFF1A237E)
            "satinal_villa" -> Color(0xFF0D47A1)
            else -> Color.DarkGray
        }
        drawPath(leftWall, color = wallColorLeft)

        // Right Wall (North-East)
        val rightWall = Path().apply {
            moveTo(cx, cy - 60f)
            lineTo(cx + 140f, cy)
            lineTo(cx + 140f, cy - 110f)
            lineTo(cx, cy - 170f)
            close()
        }
        val wallColorRight = when (houseId) {
            "kiralik_kotu" -> Color(0xFF242220)
            "kiralik_orta" -> Color(0xFF4F4F4F)
            "satinal_rezidans" -> Color(0xFF283593)
            "satinal_villa" -> Color(0xFF1565C0)
            else -> Color.Gray
        }
        drawPath(rightWall, color = wallColorRight)

        // 3. Render Furniture based on purchases
        // BED
        if (furnitureList.contains("lux_bed")) {
            // Draw luxury double bed in the left corner
            val bedPath = Path().apply {
                moveTo(cx - 100f, cy - 25f)
                lineTo(cx - 40f, cy - 50f)
                lineTo(cx - 20f, cy - 40f)
                lineTo(cx - 80f, cy - 15f)
                close()
            }
            drawPath(bedPath, color = Color(0xFF6A1B9A)) // Rich royal violet sheets
            // Bed frame
            drawLine(Color(0xFFFFD54F), Offset(cx - 100f, cy - 10f), Offset(cx - 100f, cy - 25f), strokeWidth = 5f)
            drawLine(Color(0xFFFFD54F), Offset(cx - 80f, cy), Offset(cx - 80f, cy - 15f), strokeWidth = 5f)
        } else {
            // Render dirty sleeping bag on the floor
            val bedPath = Path().apply {
                moveTo(cx - 90f, cy - 20f)
                lineTo(cx - 50f, cy - 35f)
                lineTo(cx - 40f, cy - 30f)
                lineTo(cx - 80f, cy - 15f)
                close()
            }
            drawPath(bedPath, color = Color(0xFF3E2723)) // Dirty dark brown sleeping bag
        }

        // TV
        if (furnitureList.contains("giant_tv")) {
            // Modern television panel on the right wall
            val tvStand = Path().apply {
                moveTo(cx + 40f, cy - 10f)
                lineTo(cx + 90f, cy - 30f)
                lineTo(cx + 105f, cy - 24f)
                lineTo(cx + 55f, cy - 4f)
                close()
            }
            drawPath(tvStand, color = Color(0xFF212121))
            // TV Screen
            drawRect(
                color = Color(0xFF03A9F4), // Glowing neon cyan trading chart showing!
                topLeft = Offset(cx + 65f, cy - 65f),
                size = Size(24f, 40f)
            )
            // Draw visual graph lines on the smart TV
            drawLine(Color(0xFF00E676), Offset(cx + 67f, cy - 45f), Offset(cx + 78f, cy - 55f), strokeWidth = 2f)
            drawLine(Color(0xFF00E676), Offset(cx + 78f, cy - 55f), Offset(cx + 87f, cy - 35f), strokeWidth = 2f)
        }

        // SOFA / COUCH
        if (furnitureList.contains("leather_sofa")) {
            // Drawing stylish dark leather armchair in center
            drawCircle(Color(0xFF1A1A1A), center = Offset(cx, cy + 10f), radius = 16f)
            drawCircle(Color.Black, center = Offset(cx - 10f, cy + 15f), radius = 6f)
            drawCircle(Color.Black, center = Offset(cx + 10f, cy + 15f), radius = 6f)
        }

        // CRYPTO MINING RIG (With interactive blinking fan rings!)
        if (furnitureList.contains("mining_rig")) {
            // Draw blinking green/blue rig boxes in the far corner
            drawRect(
                color = Color(0xFF101010),
                topLeft = Offset(cx - 20f, cy - 85f),
                size = Size(40f, 30f)
            )
            // Animated/Blinking neon cooling fan dots
            val blink = (System.currentTimeMillis() / 400) % 2 == 0L
            drawCircle(if (blink) Color(0xFF00E676) else Color.DarkGray, center = Offset(cx - 12f, cy - 75f), radius = 3.5f)
            drawCircle(if (!blink) Color(0xFF29B6F6) else Color.DarkGray, center = Offset(cx, cy - 75f), radius = 3.5f)
            drawCircle(if (blink) Color(0xFF00E676) else Color.DarkGray, center = Offset(cx + 12f, cy - 75f), radius = 3.5f)
        }
    }
}


// =================== TAB 2: GARAGE (GARAJ & SÜRÜŞ) ===================

@Composable
fun GarageTab(
    ownedCars: String,
    activeCarId: String?,
    cash: Double,
    lang: String,
    onBuyCar: (String, String, Double) -> Unit,
    onSelectActiveCar: (String) -> Unit,
    onStartDrive: () -> Unit
) {
    val carsList = ownedCars.split(",").filter { it.isNotEmpty() }

    val garageCatalog = listOf(
        CarOption("rust_bucket", if (lang == "TR") "🚗 Paslı Tofaş Şase (Murat 124)" else "🚗 Rust Murat 124", 800.0, "Paslı, eski"),
        CarOption("tofas_sahin", if (lang == "TR") "🚘 Modifiyeli Tofaş Şahin" else "🚘 Tuned Tofas Sahin", 2500.0, "Drift Canavarı"),
        CarOption("bmw_m3", if (lang == "TR") "🏎️ Sahibinden Temiz BMW M3" else "🏎️ Clean BMW M3", 25000.0, "Hızlı & Konforlu"),
        CarOption("tesla_model_s", if (lang == "TR") "⚡ Lüks Tesla Model S" else "⚡ Tesla Model S", 75000.0, "Otopilot & Sessiz"),
        CarOption("ferrari", if (lang == "TR") "🐎 Kırmızı Ferrari F40" else "🐎 Red Ferrari F40", 250000.0, "Yırtıcı Güç")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E273A), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "TR") "🚘 ŞEHİR SÜRÜŞÜ & GARAJ" else "🚘 STREET DRIVE & GARAGE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == "TR") 
                            "Sahip olduğunuz arabalarla retro 3D sokaklarda sürüşe çıkabilir, engellerden kaçarak yoldaki paraları toplayabilirsiniz! Sürüş yapmak bakiye kazandırır."
                            else "Drive your cars in retro pseudo-3D city roads, dodge obstacles, and collect floating cash! Driving awards you raw trading balance.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    if (carsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onStartDrive,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (lang == "TR") "ŞEHİRDE SÜRÜŞE ÇIK 🚗💨" else "GO FOR A CITY RUN 🚗💨",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(text = if (lang == "TR") "🛒 Araba Galerisi" else "🛒 Car Dealership", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(garageCatalog) { car ->
            val isOwned = carsList.contains(car.id)
            val isActive = activeCarId == car.id

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1422), RoundedCornerShape(10.dp))
                    .border(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = if (isActive) Color(0xFFFFC107) else Color(0xFF1E283C),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = car.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = car.desc, color = Color.Gray, fontSize = 11.sp)
                    Text(text = "Bedel: $${String.format("%.2f", car.price)}", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x3BFFC107), CircleShape)
                            .border(1.dp, Color(0xFFFFC107), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = if (lang == "TR") "Direksiyonda" else "Active", color = Color(0xFFFFC107), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isOwned) {
                    Button(
                        onClick = { onSelectActiveCar(car.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283C)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(text = if (lang == "TR") "SÜR" else "DRIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onBuyCar(car.id, car.name, car.price) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(6.dp),
                        enabled = cash >= car.price,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = if (lang == "TR") "AL" else "BUY", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class CarOption(
    val id: String,
    val name: String,
    val price: Double,
    val desc: String
)


// =================== INTERACTIVE GAME 1: GARSONLUK (WAITER) ===================

@Composable
fun WaiterGame(
    lang: String,
    onEarn: (Double) -> Unit,
    onClose: () -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var timerSeconds by remember { mutableIntStateOf(30) }
    var currentDishNeeded by remember { mutableStateOf("") }
    val dishes = listOf("🥩", "🍜", "🥗", "🍕")

    var messageText by remember { mutableStateOf("") }
    var showGameOver by remember { mutableStateOf(false) }

    // Pick dynamic dish order
    LaunchedEffect(Unit) {
        currentDishNeeded = dishes.random()
        while (timerSeconds > 0 && lives > 0) {
            delay(1000)
            timerSeconds--
        }
        showGameOver = true
        // Earn cash based on dishes delivered
        onEarn(score * 10.0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showGameOver) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFFFC107), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "🏁 VARDİYA SONU", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFC107))
                    Text(
                        text = if (lang == "TR") 
                            "Bu vardiyada toplam $score siparişi doğru ulaştırdın ve $${score * 10} kazandın!"
                            else "In this shift, you successfully delivered $score dishes and earned $${score * 10}!",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text(text = if (lang == "TR") "KASAYA EKLE & ÇIK" else "CLAIM & CLOSE", color = Color.Black)
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Statistics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Süre: ${timerSeconds}s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Can: ${"❤️".repeat(lives)}", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = if (lang == "TR") "MÜŞTERİNİN İSTEDİĞİ SİPARİŞ:" else "CUSTOMER WANTS THIS DISH:",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                // Large customer bubble
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFF1E273A), CircleShape)
                        .border(4.dp, Color(0xFFFFC107), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = currentDishNeeded, fontSize = 48.sp)
                }

                Text(text = messageText, color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)

                // Plates kitchen counter row
                Text(text = if (lang == "TR") "TEPSİDEN DOĞRU YEMEĞİ BUL VE TIKLA:" else "FIND THE MATCHING DISH TO SERVE:", color = Color.Gray, fontSize = 11.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    dishes.forEach { dish ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF141A28), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF1E283C), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (dish == currentDishNeeded) {
                                        score++
                                        messageText = "+$10 Doğru Servis! 🍔"
                                        currentDishNeeded = dishes.random()
                                    } else {
                                        lives--
                                        messageText = "🚨 Yanlış tabak götürdün, azar yedin!"
                                        if (lives <= 0) {
                                            showGameOver = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = dish, fontSize = 32.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = onClose,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = if (lang == "TR") "Vardiyadan Ayrıl" else "Quit Shift")
                }
            }
        }
    }
}


// =================== INTERACTIVE GAME 2: ELEKTRİK (ELECTRICIAN) ===================

@Composable
fun ElectricianGame(
    lang: String,
    onEarn: (Double) -> Unit,
    onClose: () -> Unit
) {
    var level by remember { mutableIntStateOf(1) }
    var scoreCash by remember { mutableDoubleStateOf(0.0) }
    var selectedLeftIndex by remember { mutableStateOf<Int?>(null) }
    var wireConnections by remember { mutableStateOf(mapOf<Int, Int>()) } // leftIndex to rightIndex

    val leftColors = remember { listOf(Color.Red, Color.Blue, Color.Yellow) }
    // Randomize/scramble right terminal placements
    var rightColors by remember { mutableStateOf(listOf(Color.Yellow, Color.Red, Color.Blue)) }

    LaunchedEffect(level) {
        selectedLeftIndex = null
        wireConnections = emptyMap()
        rightColors = leftColors.shuffled()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080B10))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (lang == "TR") "⚡ ELEKTRİK PANOSU REHABİLİTASYONU" else "⚡ ELECTRICAL PANEL REHAB",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFC107)
            )

            Text(
                text = if (lang == "TR") 
                    "Kabloları eşleştirmek için soldan bir kabloya basın, ardından sağdaki aynı renkte olan terminale basarak devreyi tamamlayın!"
                    else "Connect matching colors: tap a wire on the left, then tap its matching terminal on the right!",
                fontSize = 11.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            // Terminals Area Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(0xFF11141B), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF1E283C), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Terminals
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    leftColors.forEachIndexed { idx, color ->
                        val isConnected = wireConnections.containsKey(idx)
                        val isSelected = selectedLeftIndex == idx

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isConnected) Color.Gray else Color.White,
                                    shape = CircleShape
                                )
                                .clickable(enabled = !isConnected) { selectedLeftIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isConnected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "OK", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                // Visual Connective Wires Canvas Drawing
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp)
                ) {
                    val h = size.height
                    val spacing = h / 3f

                    // Draw already connected circuits
                    wireConnections.forEach { (leftIdx, rightIdx) ->
                        drawLine(
                            color = leftColors[leftIdx],
                            start = Offset(10f, leftIdx * spacing + spacing / 2f),
                            end = Offset(size.width - 10f, rightIdx * spacing + spacing / 2f),
                            strokeWidth = 6f
                        )
                    }
                }

                // Right Terminals
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    rightColors.forEachIndexed { idx, color ->
                        val isRightConnected = wireConnections.containsValue(idx)

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White, CircleShape)
                                .clickable(enabled = !isRightConnected && selectedLeftIndex != null) {
                                    val leftIdx = selectedLeftIndex
                                    if (leftIdx != null) {
                                        if (leftColors[leftIdx] == color) {
                                            // Correct wire matched!
                                            wireConnections = wireConnections + (leftIdx to idx)
                                            selectedLeftIndex = null

                                            if (wireConnections.size == 3) {
                                                // Paneli bitirdi!
                                                scoreCash += 25.0
                                                onEarn(25.0)
                                                level++
                                            }
                                        } else {
                                            // Misconnected, shock penalty or reset selection
                                            selectedLeftIndex = null
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRightConnected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "OK", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            Text(
                text = "Tamamlanan Pano: ${level - 1} | Toplam Kazanç: $${scoreCash}",
                color = Color(0xFF00E676),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { level++ }, // Skip/scramble panel
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283C))
                ) {
                    Text(text = if (lang == "TR") "Sıfırla" else "Scramble")
                }

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                ) {
                    Text(text = if (lang == "TR") "Parayı Al & Ayrıl" else "Collect & Exit", color = Color.Black)
                }
            }
        }
    }
}


// =================== INTERACTIVE GAME 3: KAZI (MINER CLICKER) ===================

@Composable
fun MinerGame(
    lang: String,
    onEarn: (Double) -> Unit,
    onClose: () -> Unit
) {
    var rockHealth by remember { mutableFloatStateOf(100f) }
    var rocksCleared by remember { mutableIntStateOf(0) }
    var totalEarned by remember { mutableDoubleStateOf(0.0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C12))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (lang == "TR") "⛏️ TAŞ OCAĞINDA BALYOZ ÇALIŞMASI" else "⛏️ ROCK QUARRY EXCAVATION",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFC107)
            )

            Text(
                text = if (lang == "TR") 
                    "Sermaye biriktirmek için aşağıdaki kayaya hızlıca art arda tıklayarak onu parçalayın!"
                    else "Rapidly tap/click on the boulder below to shatter it and uncover industrial coal!",
                fontSize = 11.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            // Giant Rock shape drawn via Canvas
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1D222B))
                    .border(2.dp, Color.Gray, RoundedCornerShape(20.dp))
                    .clickable {
                        rockHealth -= 8f
                        if (rockHealth <= 0f) {
                            rockHealth = 100f
                            rocksCleared++
                            totalEarned += 15.0
                            onEarn(15.0)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Drawing irregular granite stone geometry
                    drawPath(
                        path = Path().apply {
                            moveTo(w * 0.2f, h * 0.15f)
                            lineTo(w * 0.75f, h * 0.1f)
                            lineTo(w * 0.9f, h * 0.5f)
                            lineTo(w * 0.8f, h * 0.85f)
                            lineTo(w * 0.3f, h * 0.9f)
                            lineTo(w * 0.1f, h * 0.6f)
                            close()
                        },
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF5D5E63), Color(0xFF2C2D32)),
                            center = Offset(w * 0.4f, h * 0.4f),
                            radius = w * 0.6f
                        )
                    )

                    // Draw cracked veins on rock proportional to damage
                    if (rockHealth < 80f) {
                        drawLine(Color.Black, Offset(w * 0.3f, h * 0.3f), Offset(w * 0.5f, h * 0.45f), strokeWidth = 3f)
                    }
                    if (rockHealth < 50f) {
                        drawLine(Color.Black, Offset(w * 0.5f, h * 0.45f), Offset(w * 0.6f, h * 0.7f), strokeWidth = 3f)
                        drawLine(Color.Black, Offset(w * 0.5f, h * 0.45f), Offset(w * 0.8f, h * 0.35f), strokeWidth = 3f)
                    }
                }
                Text(text = "💥", fontSize = 44.sp, modifier = Modifier.align(Alignment.Center))
            }

            // Rock Health Bar
            Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = if (lang == "TR") "Kaya Mukavemeti" else "Stone Integrity", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "${rockHealth.toInt()}%", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { rockHealth / 100f },
                    color = Color(0xFFFF5252),
                    trackColor = Color(0xFF1E283C),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            Text(
                text = "Parçalanan Kaya: $rocksCleared | Toplam Kazanç: $$totalEarned",
                color = Color(0xFF00E676),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(44.dp)
            ) {
                Text(text = if (lang == "TR") "Parayı Al & İşten Çık" else "Collect & Return", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// =================== INTERACTIVE GAME 4: 3D DRIVING SIMULATOR ===================

@Composable
fun DrivingSimulator(
    activeCarId: String,
    lang: String,
    onEarn: (Double) -> Unit,
    onClose: () -> Unit
) {
    var scoreCash by remember { mutableDoubleStateOf(0.0) }
    var carX by remember { mutableFloatStateOf(0f) } // -100f to 100f (Left to Right coordinate)
    var isGameOver by remember { mutableStateOf(false) }

    // Driving mechanics coordinates (road side pillars, obstacles, dollars)
    var obstacleY by remember { mutableFloatStateOf(-50f) } // -50f (horizon) to 500f (screen bottom)
    var obstacleX by remember { mutableFloatStateOf(0f) }   // Offset from center
    var coinY by remember { mutableFloatStateOf(-10f) }
    var coinX by remember { mutableFloatStateOf(-30f) }

    // Steer controls flags
    var steerLeft by remember { mutableStateOf(false) }
    var steerRight by remember { mutableStateOf(false) }

    // Speed factors depending on the purchased car tier
    val speedFactor = when (activeCarId) {
        "rust_bucket" -> 1.0
        "tofas_sahin" -> 1.5
        "bmw_m3" -> 2.2
        "tesla_model_s" -> 3.0
        "ferrari" -> 4.5
        else -> 1.0
    }

    // Interactive driving game loop
    LaunchedEffect(isGameOver) {
        if (isGameOver) return@LaunchedEffect
        while (!isGameOver) {
            delay(33) // ~30 FPS loop

            // Steer input response
            if (steerLeft) {
                carX = (carX - 5.5f).coerceIn(-120f, 120f)
            }
            if (steerRight) {
                carX = (carX + 5.5f).coerceIn(-120f, 120f)
            }

            // Scroll obstacle towards screen bottom
            obstacleY += (8f * speedFactor).toFloat()
            if (obstacleY > 380f) {
                // Reset obstacle randomly on horizon
                obstacleY = -50f
                obstacleX = Random.nextInt(-90, 90).toFloat()
            }

            // Scroll collectible dollar bill
            coinY += (7f * speedFactor).toFloat()
            if (coinY > 380f) {
                coinY = -20f
                coinX = Random.nextInt(-100, 100).toFloat()
            }

            // Collision check with obstacle
            if (obstacleY in 240f..300f) {
                val distance = Math.abs(carX - obstacleX)
                if (distance < 34f) {
                    isGameOver = true
                }
            }

            // Collection check with floating dollar coin
            if (coinY in 240f..300f) {
                val dist = Math.abs(carX - coinX)
                if (dist < 32f) {
                    scoreCash += 20.0
                    onEarn(20.0) // Inject raw cash to player portfolio!
                    coinY = -50f // Reset coin
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04060A)),
        contentAlignment = Alignment.Center
    ) {
        if (isGameOver) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A28)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .border(2.dp, Color(0xFFFF1744), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "💥 KAZA YAPTIK!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF1744))
                    Text(
                        text = if (lang == "TR") 
                            "Arabanızla sokaktaki engellere çarptınız! Kazandığınız paralar cüzdanınıza eklendi." 
                            else "You crashed into street barricades! Earnings collected have been deposited into your portfolio.",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Text(text = "Toplam Kazanç: $${scoreCash}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                    ) {
                        Text(text = if (lang == "TR") "TAMAM" else "OK", color = Color.Black)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top HUD Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Hız Çarpanı: x${speedFactor}", color = Color.White, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1A3E21), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(text = "Sürüş Geliri: $${scoreCash}", color = Color(0xFF00E676), fontWeight = FontWeight.Black)
                    }
                }

                // Main Pseudo-3D Perspective Road Canvas!
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val w = size.width
                    val h = size.height

                    // Sky & Horizon dark neon grid
                    drawRect(Color(0xFF0C101A), size = Size(w, h * 0.35f))
                    // Horizon Line
                    drawLine(Color(0xFFFFC107), Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = 3f)

                    // Road background
                    drawRect(Color(0xFF101524), topLeft = Offset(0f, h * 0.35f), size = Size(w, h * 0.65f))

                    // Draw vanishing perspective road lines
                    val vanishingPt = Offset(w * 0.5f, h * 0.35f)
                    val bottomL = Offset(w * 0.15f, h)
                    val bottomR = Offset(w * 0.85f, h)

                    // Draw asphalt road path
                    drawPath(
                        path = Path().apply {
                            moveTo(vanishingPt.x, vanishingPt.y)
                            lineTo(bottomR.x, bottomR.y)
                            lineTo(bottomL.x, bottomL.y)
                            close()
                        },
                        color = Color(0xFF1C2237)
                    )

                    // Perspective road stripes (scrolling)
                    val tick = (System.currentTimeMillis() / 250) % 2 == 0L
                    for (i in 0..5) {
                        val py = h * 0.35f + (i * 0.12f * h)
                        val length = (i * 8f) + 10f
                        val offsetTick = if (tick) 20f else 0f
                        val finalY = (py + offsetTick).coerceIn(h * 0.35f, h)
                        drawLine(
                            color = Color.White,
                            start = Offset(w * 0.5f, finalY),
                            end = Offset(w * 0.5f, (finalY + length).coerceIn(h * 0.35f, h)),
                            strokeWidth = (i + 1).toFloat()
                        )
                    }

                    // DRAW FLOATING COIN DOLLAR
                    val coinScale = (coinY + 100f) / 400f
                    val screenCoinX = w * 0.5f + coinX * coinScale
                    val screenCoinY = h * 0.35f + coinY
                    if (screenCoinY > h * 0.35f) {
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            center = Offset(screenCoinX, screenCoinY),
                            radius = (14f * coinScale).coerceAtLeast(4f)
                        )
                        // Dollar sign symbol drawing
                        drawCircle(
                            color = Color(0xFF00E676),
                            center = Offset(screenCoinX, screenCoinY),
                            radius = (8f * coinScale).coerceAtLeast(2f)
                        )
                    }

                    // DRAW ROAD OBSTACLE BARRICADE
                    val obsScale = (obstacleY + 100f) / 400f
                    val screenObsX = w * 0.5f + obstacleX * obsScale
                    val screenObsY = h * 0.35f + obstacleY
                    if (screenObsY > h * 0.35f) {
                        drawRect(
                            color = Color(0xFFFF1744), // Danger obstacle box
                            topLeft = Offset(screenObsX - 25f * obsScale, screenObsY - 20f * obsScale),
                            size = Size(50f * obsScale, 30f * obsScale)
                        )
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(screenObsX - 20f * obsScale, screenObsY - 14f * obsScale),
                            size = Size(40f * obsScale, 6f * obsScale)
                        )
                    }

                    // DRAW PLAYER ACTIVE CAR SPRITE SILHOUETTE
                    val carScreenY = h * 0.85f
                    val carScreenX = w * 0.5f + carX
                    drawPath(
                        path = Path().apply {
                            moveTo(carScreenX - 44f, carScreenY)
                            lineTo(carScreenX - 34f, carScreenY - 28f)
                            lineTo(carScreenX + 34f, carScreenY - 28f)
                            lineTo(carScreenX + 44f, carScreenY)
                            lineTo(carScreenX + 48f, carScreenY + 24f)
                            lineTo(carScreenX - 48f, carScreenY + 24f)
                            close()
                        },
                        color = when (activeCarId) {
                            "rust_bucket" -> Color(0xFF8D6E63) // Rust brown Murat
                            "tofas_sahin" -> Color(0xFFFFFFFF) // White boxy Sahin
                            "bmw_m3" -> Color(0xFF0D47A1) // Fast Navy BMW
                            "tesla_model_s" -> Color(0xFF00E676) // Eco green Tesla
                            "ferrari" -> Color(0xFFFF1744) // Hyper Red Ferrari
                            else -> Color.DarkGray
                        }
                    )
                    // Taillights
                    drawCircle(Color.Red, center = Offset(carScreenX - 32f, carScreenY + 14f), radius = 6f)
                    drawCircle(Color.Red, center = Offset(carScreenX + 32f, carScreenY + 14f), radius = 6f)
                    // License plate
                    drawRect(Color.White, topLeft = Offset(carScreenX - 16f, carScreenY + 6f), size = Size(32f, 10f))
                    // Wheels
                    drawRect(Color.Black, topLeft = Offset(carScreenX - 46f, carScreenY + 18f), size = Size(10f, 12f))
                    drawRect(Color.Black, topLeft = Offset(carScreenX + 36f, carScreenY + 18f), size = Size(10f, 12f))
                }

                // Big high-contrast touch control paddles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1420))
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { carX = (carX - 25f).coerceIn(-120f, 120f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .padding(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283C)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = if (lang == "TR") "◀ SOL" else "◀ LEFT", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }

                    Button(
                        onClick = { carX = (carX + 25f).coerceIn(-120f, 120f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .padding(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283C)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = if (lang == "TR") "SAĞ ▶" else "RIGHT ▶", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
