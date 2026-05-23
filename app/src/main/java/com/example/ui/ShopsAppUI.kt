package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Renders the main App container holding all screen transitions nicely nested inside a Scaffold with edge-to-edge support.
@Composable
fun ShopsAppUI(viewModel: AppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartProducts.collectAsStateWithLifecycle()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StickyNavbar(
                viewModel = viewModel,
                onNavigateToCart = { viewModel.navigateTo(Screen.Cart) },
                onNavigateToProfile = { viewModel.navigateTo(Screen.Profile) },
                onNavigateToAdmin = { viewModel.navigateTo(Screen.AdminDashboard) }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentScreen = currentScreen,
                hasUser = user != null,
                onTabSelected = { targetScreen ->
                    viewModel.navigateTo(targetScreen)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) togetherWith
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.Home -> HomeScreen(
                        viewModel = viewModel,
                        onProductClick = { pId -> viewModel.navigateTo(Screen.ProductDetail(pId)) }
                    )
                    is Screen.Categories -> CategoriesScreen(
                        viewModel = viewModel
                    )
                    is Screen.Products -> ProductsListScreen(
                        viewModel = viewModel,
                        onProductClick = { pId -> viewModel.navigateTo(Screen.ProductDetail(pId)) }
                    )
                    is Screen.ProductDetail -> ProductDetailScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                    is Screen.FlashSale -> FlashSaleScreen(
                        viewModel = viewModel,
                        onProductClick = { pId -> viewModel.navigateTo(Screen.ProductDetail(pId)) }
                    )
                    is Screen.Services -> ServicesScreen()
                    is Screen.Assistant -> AssistantScreen(viewModel = viewModel)
                    is Screen.Cart -> CartScreen(
                        viewModel = viewModel,
                        onCheckoutSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("SR Shops: Order processed successfully! Thank you for your purchase.")
                            }
                        }
                    )
                    is Screen.Profile -> ProfileScreen(
                        viewModel = viewModel,
                        onAuthSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Welcome back to SR Shops!")
                            }
                        }
                    )
                    is Screen.AdminDashboard -> AdminDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToProduct = { pId -> viewModel.navigateTo(Screen.ProductDetail(pId)) }
                    )
                }
            }
        }
    }
}

// STICKY NAVBAR COMPONENT
@Composable
fun StickyNavbar(
    viewModel: AppViewModel,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val cartList by viewModel.cartProducts.collectAsStateWithLifecycle()
    val totalCartCount = cartList.sumOf { it.second.selectQuantity }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF08090A)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Logo & Text Group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.Home) }
                ) {
                    // Blue to Purple Gradient Icon Badge matching our exact brand colors
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF2563EB), Color(0xFF9333EA))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SR",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SR",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Shops",
                                color = Color(0xFFE2E8F0),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        Text(
                            text = "AI-Powered Marketplace",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Quick Actions Actions Group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Admin Toggle (if logged in, quick entry)
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.testTag("admin_navbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AdminPanelSettings,
                            contentDescription = "Admin Area",
                            tint = if (user?.email?.contains("owner") == true || user?.email?.contains("vip") == true) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.6f)
                        )
                    }

                    // AI Assistant
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Assistant) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = "AI Assistant",
                            tint = Color(0xFF9333EA) // Brand Purple Highlight
                        )
                    }

                    // Cart With Live Badge Counter
                    Box {
                        IconButton(
                            onClick = onNavigateToCart,
                            modifier = Modifier.testTag("cart_navbar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ShoppingCart,
                                contentDescription = "Shopping Cart",
                                tint = Color.White
                            )
                        }
                        if (totalCartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)) // Brand blue highlight
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = totalCartCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Profile / Account
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.testTag("account_navbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "User Account",
                            tint = if (user != null) Color(0xFF60A5FA) else Color.White
                        )
                    }
                }
            }
            HorizontalDivider(color = Color(0x0DFFFFFF), thickness = 1.dp) // Subtle elegant border line (border-white/5)
        }
    }
}

// THE STYLISH BOTTOM NAVIGATION BAR WITH PILL ACTIVE SYSTEM
@Composable
fun BottomNavBar(
    currentScreen: Screen,
    hasUser: Boolean,
    onTabSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF08090A),
        tonalElevation = 0.dp,
        modifier = Modifier.border(width = 0.5.dp, color = Color(0x0DFFFFFF), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(
            selected = currentScreen is Screen.Home,
            onClick = { onTabSelected(Screen.Home) },
            icon = { Icon(Icons.Rounded.Home, "Home") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB), // Active icon (blue-600)
                selectedTextColor = Color(0xFF2563EB), // Active label text
                unselectedIconColor = Color(0xFF64748B), // Inactive icon (slate-500)
                unselectedTextColor = Color(0xFF64748B), // Inactive label text
                indicatorColor = Color.Transparent // Clean borderless selection indicator
            )
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Categories,
            onClick = { onTabSelected(Screen.Categories) },
            icon = { Icon(Icons.Rounded.GridView, "Categories") },
            label = { Text("Store", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB),
                selectedTextColor = Color(0xFF2563EB),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Products,
            onClick = { onTabSelected(Screen.Products) },
            icon = { Icon(Icons.Rounded.ShoppingBag, "Store") },
            label = { Text("Catalog", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB),
                selectedTextColor = Color(0xFF2563EB),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentScreen is Screen.FlashSale,
            onClick = { onTabSelected(Screen.FlashSale) },
            icon = { Icon(Icons.Rounded.LocalFireDepartment, "Flash") },
            label = { Text("Sales", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB),
                selectedTextColor = Color(0xFF2563EB),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Services,
            onClick = { onTabSelected(Screen.Services) },
            icon = { Icon(Icons.Rounded.Build, "Services") },
            label = { Text("Account", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB),
                selectedTextColor = Color(0xFF2563EB),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = Color.Transparent
            )
        )
    }
}

// 1. HOME SCREEN
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onProductClick: (String) -> Unit
) {
    val searchVal by viewModel.searchQuery.collectAsStateWithLifecycle()
    val prodList by viewModel.productsList.collectAsStateWithLifecycle()
    val featured = remember(prodList) { prodList.filter { !it.isFlashSale }.take(6) }
    val recommended = remember(prodList) { prodList.distinctBy { it.category }.take(4) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Section & Search
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF08090A), Color(0xFF1E1B4B).copy(alpha = 0.35f))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SR SHOPS",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Smart Shopping Experience Powered By Modern Technology",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                    )

                    // Embedded Search bar + Direct Ask AI Trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x0FFFFFFF))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search icon",
                            tint = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchVal,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Ask AI to find products...", color = Color(0xFF64748B), fontSize = 13.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                viewModel.navigateTo(Screen.Products)
                            })
                        )
                        // Radiant Smart assistant trigger
                        Text(
                            text = "ASK AI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF2563EB), Color(0xFF9333EA))
                                    )
                                )
                                .clickable {
                                    if (searchVal.trim().isNotEmpty()) {
                                        viewModel.sendAssistantMessage(searchVal)
                                    }
                                    viewModel.navigateTo(Screen.Assistant)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Luxury Button Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.Products) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF08090A)),
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Text("Shop Now", fontWeight = FontWeight.ExtraBold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.FlashSale) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color(0x1BFFFFFF)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Text("Explore Deals", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Animated Promo Slider
        item {
            AnimatedPromoSlider()
        }

        // Shop Categories grid/chips links
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Featured categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "See All",
                        color = Color(0xFF8B5CF6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.Categories) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    val quickcats = listOf("All", "Electronics", "Gadgets", "Fashion", "Smart Devices", "Digital Products")
                    items(quickcats) { cat ->
                        ElegantFilterChip(
                            selected = false,
                            onClick = {
                                viewModel.setCategoryFilter(cat)
                                viewModel.navigateTo(Screen.Products)
                            },
                            label = cat
                        )
                    }
                }
            }
        }

        // Dynamic Smart AI Recommendation Stream (Custom Layout)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.08f), Color(0xFF3B82F6).copy(alpha = 0.08f))
                        )
                    )
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Smart AI Icon",
                        tint = Color(0xFFCA8A04),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Smart recommendations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Personalized dynamically based on current technology trends.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommended) { prod ->
                        SmartProductRecommendationCard(
                            product = prod,
                            onProductClick = { onProductClick(prod.id) }
                        )
                    }
                }
            }
        }

        // Featured catalog List
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Featured Products",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Render products directly inside LazyColumn
                featured.chunked(2).forEach { rowList ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowList.forEach { prod ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProductMiniCard(
                                    product = prod,
                                    onProductClick = { onProductClick(prod.id) }
                                )
                            }
                        }
                        if (rowList.size == 1) {
                            Box(modifier = Modifier.weight(1f)) // filler space
                        }
                    }
                }
            }
        }
    }
}

// AMANDED HERO BANNER PROMO ROTATOR WITH SOFT TRANSITIONS
@Composable
fun AnimatedPromoSlider() {
    var activeProgressIndex by remember { mutableStateOf(0) }
    val promotions = listOf(
        Pair("SR Quantum MagSafe Dock", "Exclusive launch: 25% OFF with Qi2 wireless charging!"),
        Pair("Midnight Tailored Trench Coat", "Artisan cashmere visual styling for ultimate premium vibes."),
        Pair("SaaS Figma Design System", "Prebuilt auto-layouts & tokens configured under light/dark modes.")
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            activeProgressIndex = (activeProgressIndex + 1) % promotions.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0x66312E81), Color(0x33581C87)) // Indigo-900/40 to Purple-900/20 in Tailwind
                )
            )
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp)) // border-white/10
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1.3f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "AI EXCLUSIVE",
                    color = Color(0xFF60A5FA), // blue-400
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = promotions[activeProgressIndex].first,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = promotions[activeProgressIndex].second,
                    color = Color(0xFF94A3B8), // slate-400
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Navigation Dots Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    promotions.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (idx == activeProgressIndex) Color.White else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .padding(start = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Explore Deals",
                        color = Color(0xFF08090A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// RECOMMENDED CARD
@Composable
fun SmartProductRecommendationCard(
    product: ProductEntity,
    onProductClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onProductClick)
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp)), // border-white/10 glass feel
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp)), // rounded-2xl
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$${product.price}",
                color = Color(0xFF60A5FA), // blue-400 accent highlight
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// PREMIUM MULTI-FEATURE PRODUCT CHIP CARD
@Composable
fun ProductMiniCard(
    product: ProductEntity,
    onProductClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick)
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp)), // border-white/10 glass feel
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp),
                    contentScale = ContentScale.Crop
                )
                // Flash Sale Percentage discount Badge
                if (product.isFlashSale && product.flashSaleDiscount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEF4444)) // modern flash red
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "-${product.flashSaleDiscount}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.category.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B), // slate-500
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating star",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = product.rating.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.isFlashSale) {
                            Text(
                                text = "$${product.originalPrice}",
                                style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough),
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = "$${product.price}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    // Beautiful White Nav Arrow Button matching design spec button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = "Navigate detail",
                            tint = Color(0xFF08090A),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

// 2. CATEGORIES OVERVIEW SCREEN
@Composable
fun CategoriesScreen(viewModel: AppViewModel) {
    val categories = listOf(
        Pair("Electronics", "Smart accessories, premium audio systems, visual workstation monitors."),
        Pair("Gadgets", "Advanced fitness watch hardware, aerospace tracking, micro-sensor components."),
        Pair("Fashion", "Classy tailoring woolen trenchcoats, smart designer apparel configurations."),
        Pair("Smart Devices", "Cinematic drone units, smart spatial hubs, advanced audio telemetry devices."),
        Pair("Mobile Accessories", "Secure MagSafe magnetic charging dock stands, fast micro cords."),
        Pair("Gaming", "Ultra low latency esports pointer hardware, ultra tactile keyboards, mouse pads."),
        Pair("Home & Living", "Waterless cold-air diffusers made of natural elements, ambient ceramics."),
        Pair("Digital Products", "Pre-designed UI Kits, elite Figma components libraries, responsive templates.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Browse Departments",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Explore categorized luxury collections and SaaS digital templates.",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable {
                            viewModel.setCategoryFilter(item.first)
                            viewModel.navigateTo(Screen.Products)
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = when (item.first) {
                                "Electronics" -> Icons.Rounded.Headphones
                                "Gadgets" -> Icons.Rounded.Watch
                                "Fashion" -> Icons.Rounded.Checkroom
                                "Smart Devices" -> Icons.Rounded.SettingsPower
                                "Mobile Accessories" -> Icons.Rounded.BatteryChargingFull
                                "Gaming" -> Icons.Rounded.Gamepad
                                "Home & Living" -> Icons.Rounded.HomeMini
                                else -> Icons.Rounded.DataArray
                            },
                            contentDescription = item.first,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = item.first,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.second,
                                fontSize = 9.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. PRODUCTS LIST SCREEN
@Composable
fun ProductsListScreen(
    viewModel: AppViewModel,
    onProductClick: (String) -> Unit
) {
    val searchVal by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val listRaw by viewModel.productsList.collectAsStateWithLifecycle()

    var sortBy by remember { mutableStateOf("Default") }
    val filteredList = remember(listRaw, searchVal, selectedCat, sortBy) {
        var res = listRaw.filter {
            (selectedCat == "All" || it.category == selectedCat) &&
            (it.title.contains(searchVal, ignoreCase = true) || it.category.contains(searchVal, ignoreCase = true) || it.description.contains(searchVal, ignoreCase = true))
        }
        res = when (sortBy) {
            "Price Low-High" -> res.sortedBy { it.price }
            "Price High-Low" -> res.sortedByDescending { it.price }
            "Top Rated" -> res.sortedByDescending { it.rating }
            else -> res
        }
        res
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Header Selection Panel
        Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Info block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SR Digital Store",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredList.size} items matching",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Inline horizontal Chip selectors
                val cats = listOf("All", "Electronics", "Gadgets", "Fashion", "Smart Devices", "Digital Products")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cats) { c ->
                        ElegantFilterChip(
                            selected = (c == selectedCat),
                            onClick = { viewModel.setCategoryFilter(c) },
                            label = c
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Sort controllers row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sort By:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    listOf("Default", "Price Low-High", "Price High-Low", "Top Rated").forEach { s ->
                        AssistChip(
                            onClick = { sortBy = s },
                            label = { Text(s, fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (sortBy == s) Color(0xFFFDE047).copy(alpha = 0.2f) else Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        // Display results Grid list
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.SearchOff, "Empty List", modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No products match your parameters.", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("Try resetting filters or search coordinates.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.setCategoryFilter("All"); viewModel.setSearchQuery("") }) {
                        Text("Reset Search")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { prod ->
                    ProductMiniCard(
                        product = prod,
                        onProductClick = { onProductClick(prod.id) }
                    )
                }
            }
        }
    }
}

// 4. PRODUCT DETAIL SCREEN
@Composable
fun ProductDetailScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val prod by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val reviews by viewModel.selectedProductReviews.collectAsStateWithLifecycle()

    var reviewRatingInput by remember { mutableStateOf(5f) }
    var reviewTextInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    if (prod == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val activeProduct = prod!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        // Sticky/Immersive header image
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = activeProduct.imageUrl,
                    contentDescription = activeProduct.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
                // Flow Header Controls Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.ArrowBack, "Back icon", tint = Color.White)
                    }

                    // Wishlist Heart Indicator Button
                    val isFav by viewModel.getWishlistStatusFlow(activeProduct.id).collectAsState(false)
                    IconButton(
                        onClick = { viewModel.toggleWishlist(activeProduct.id) },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Quick Toggle Favorite",
                            tint = if (isFav) Color.Red else Color.White
                        )
                    }
                }
            }
        }

        // High fidelity info block
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Category Label with badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = activeProduct.category.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF8B5CF6)
                    )
                    // Stock warnings
                    val stockToken = activeProduct.stock
                    val stockPrompt = if (stockToken <= 0) "OUT OF STOCK" else if (stockToken <= 5) "HOT! ONLY $stockToken LEFT" else "IN STOCK"
                    Text(
                        text = stockPrompt,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        color = if (stockToken == 0) Color.Red else if (stockToken <= 5) Color(0xFFF97316) else Color(0xFF22C55E)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Title
                Text(
                    text = activeProduct.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Price Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${activeProduct.price}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (activeProduct.isFlashSale) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$${activeProduct.originalPrice}",
                            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough),
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Red)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${activeProduct.flashSaleDiscount}% Off Sales",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Star Ratings aggregate
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, "Ratings Average", tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", activeProduct.rating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${activeProduct.reviewCount} customer reviews)",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Core description text
                Text(
                    text = "Product Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeProduct.description,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ADD TO CART TRIGGER ACTION
                Button(
                    onClick = { viewModel.addProductToCart(activeProduct.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("add_to_cart_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeProduct.stock > 0) MaterialTheme.colorScheme.primary else Color.Gray
                    ),
                    enabled = activeProduct.stock > 0
                ) {
                    Icon(Icons.Rounded.ShoppingCart, "Cart Icon add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeProduct.stock > 0) "Add To Shopping Cart" else "Sold Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Live Reviews feed list section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = "Customer Feedbacks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (reviews.isEmpty()) {
                    Text(
                        text = "No reviews logged yet. Be the first to express opinion!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    reviews.forEach { rev ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rev.reviewerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Row {
                                    Icon(Icons.Filled.Star, "Review rating star", tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                                    Text(text = rev.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rev.comment,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // SUBMIT REVIEW SECTION
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Leave your feedback",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating stars setup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rating: ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        (1..5).forEach { rate ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rate scale",
                                tint = if (rate <= reviewRatingInput) Color(0xFFFBBF24) else Color.LightGray,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { reviewRatingInput = rate.toFloat() }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = reviewTextInput,
                        onValueChange = { reviewTextInput = it },
                        placeholder = { Text("Quality is superb! Fast packaging services...", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (reviewTextInput.trim().isNotEmpty()) {
                                viewModel.postProductReview(activeProduct.id, reviewRatingInput, reviewTextInput)
                                reviewTextInput = ""
                                reviewTextInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        modifier = Modifier
                            .align(Alignment.End)
                            .height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Post Review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 5. FLASH SALE SCREEN
@Composable
fun FlashSaleScreen(
    viewModel: AppViewModel,
    onProductClick: (String) -> Unit
) {
    val prods by viewModel.productsList.collectAsStateWithLifecycle()
    val saleList = remember(prods) { prods.filter { it.isFlashSale } }

    var ticksCount by remember { mutableStateOf(10800L) } // 3 hours in seconds
    LaunchedEffect(Unit) {
        while (ticksCount > 0) {
            delay(1000)
            ticksCount--
        }
    }

    val hr = ticksCount / 3600
    val min = (ticksCount % 3600) / 60
    val sec = ticksCount % 60
    val clockOutput = String.format("%02d:%02d:%02d", hr, min, sec)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Red glowing countdown block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFEF4444), Color(0xFF7F1D1D))
                    )
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FLASH SALE COUNTDOWN",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                // Glass timer visual
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Alarm, "Clock icon alert", tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = clockOutput,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Limited luxury stock. Auto-ends when timer hits zero.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Sale elements list
        if (saleList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hot deal collections at this hour. Check back soon!")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(saleList) { deal ->
                    ProductMiniCard(
                        product = deal,
                        onProductClick = { onProductClick(deal.id) }
                    )
                }
            }
        }
    }
}

// 6. SERVICES EXPO COMPONENT
@Composable
fun ServicesScreen() {
    val solutionsList = listOf(
        Pair("Website Development", "Creating high-performing custom e-commerce web clients matching React and Next.js protocols."),
        Pair("Branding Design", "Tailoring bespoke high-end brand books, luxury typography scales, logos, guidelines, and visual resources."),
        Pair("UI/UX Design", "Crafting intuitive system hierarchies, component diagrams, wireframes, and interactive mockups inside Figma."),
        Pair("Hosting & Deployment", "Configuring rapid SSL cloud hosting instances, CDNs setups, caching optimization, and server configurations."),
        Pair("Business Solutions", "Deploying payment processors nodes, inventory tracking tools, analytics setups, and CRM dashboards."),
        Pair("E-commerce Setup", "Setting up absolute turn-key Shopify configurations, Daraz vendor setups, or custom database stores.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "E-Commerce Creative Services",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "SR Shops offers premium creative & technical enterprise integrations.",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(solutionsList) { skill ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color icon setup
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (skill.first) {
                                    "Website Development" -> Icons.Rounded.Code
                                    "Branding Design" -> Icons.Rounded.Brush
                                    "UI/UX Design" -> Icons.Rounded.DesignServices
                                    "Hosting & Deployment" -> Icons.Rounded.CloudUpload
                                    "Business Solutions" -> Icons.Rounded.BarChart
                                    else -> Icons.Rounded.ShoppingBag
                                },
                                contentDescription = "Skill icon decoration",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = skill.first,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = skill.second,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. AI ASSISTANT CHAT SCREEN (INTEGRATING GEMINI API)
@Composable
fun AssistantScreen(viewModel: AppViewModel) {
    val chatStream by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isTyperActive by viewModel.isAiTyping.collectAsStateWithLifecycle()
    var inputQueryText by remember { mutableStateOf("") }
    val listScrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to latest bubble whenever chat log expands
    LaunchedEffect(chatStream.size) {
        if (chatStream.isNotEmpty()) {
            listScrollState.animateScrollToItem(chatStream.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Assistant Bio Header
        Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, "Bot face logo representation", tint = Color(0xFFCA8A04))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("SR Smart Concierge", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (isTyperActive) "Thinking & compiling ideas..." else "AI Companion. Online",
                            fontSize = 10.sp,
                            color = if (isTyperActive) Color(0xFFFBBF24) else Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Clear button chatlogs
                Text(
                    text = "Clear History",
                    color = Color.Red.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.clearChatHistory() }
                        .padding(6.dp)
                )
            }
        }

        // Bubbles lazy area
        LazyColumn(
            state = listScrollState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(chatStream) { turn ->
                val isSelf = (turn.first == "user")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isSelf) 16.dp else 0.dp,
                                    bottomEnd = if (isSelf) 0.dp else 16.dp
                                )
                            )
                            .background(
                                if (isSelf) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surface
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = turn.second,
                            color = if (isSelf) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Custom typewriter dot progress indicator
            if (isTyperActive) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("SR AI is compiling details...", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Preset bubbles suggestions
        val suggestions = listOf(
            "What gadgets are on Sale?",
            "Drone specifications info",
            "SaaS dynamic code services"
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color(0xFFA78BFA),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(30.dp))
                        .clickable { viewModel.handlePresetPrompt(label) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Direct bottom text input field control row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputQueryText,
                onValueChange = { inputQueryText = it },
                placeholder = { Text("Ask about size recommendations, tech stock, support services...", fontSize = 12.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputQueryText.trim().isNotEmpty()) {
                        viewModel.sendAssistantMessage(inputQueryText)
                        inputQueryText = ""
                    }
                }),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (inputQueryText.trim().isNotEmpty()) {
                        viewModel.sendAssistantMessage(inputQueryText)
                        inputQueryText = ""
                    }
                },
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Rounded.Send, "Send button query", modifier = Modifier.size(18.dp))
            }
        }
    }
}

// 8. CART & CHECKOUT DIALOG SCREEN
@Composable
fun CartScreen(
    viewModel: AppViewModel,
    onCheckoutSuccess: () -> Unit
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val cartList by viewModel.cartProducts.collectAsStateWithLifecycle()

    var shippingAddressText by remember { mutableStateOf("") }
    var showingAddressConfig by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            shippingAddressText = user?.address ?: ""
        }
    }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                Icon(Icons.Rounded.Lock, "Lock secure info", modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Secure Sessions Required", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Please register or login to manage your e-commerce cart lists.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.navigateTo(Screen.Profile) }) {
                    Text("Proceed to Login Area")
                }
            }
        }
        return
    }

    val subtotal = cartList.sumOf { it.first.price * it.second.selectQuantity }
    val tax = subtotal * 0.08 // 8 percent VAT
    val shipping = if (subtotal > 200.0) 0.0 else 15.0 // Free shipping promotions
    val totalCost = subtotal + tax + shipping

    if (cartList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.RemoveShoppingCart, "Clear item lists", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Your e-commerce basket is completely empty", fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Select items with dynamic chips from store.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.navigateTo(Screen.Products) }) {
                    Text("Visit Storefront Catalog")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Core items list lazy column
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Text(
                    text = "Your Luxury Bin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(cartList) { (prod, itemMeta) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = prod.imageUrl,
                            contentDescription = prod.title,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prod.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$${prod.price}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFA78BFA)
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            // Interactive counter
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "-",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray.copy(alpha = 0.15f))
                                        .clickable { viewModel.decrementCartQty(itemMeta.id, itemMeta.selectQuantity) }
                                        .padding(horizontal = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = itemMeta.selectQuantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "+",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray.copy(alpha = 0.15f))
                                        .clickable { viewModel.incrementCartQty(itemMeta.id, itemMeta.selectQuantity) }
                                        .padding(horizontal = 4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Remove bin button
                        IconButton(
                            onClick = { viewModel.removeCartItem(itemMeta.id) }
                        ) {
                            Icon(Icons.Rounded.Delete, "Remove target item icon", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Summary invoices segment
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Text("Purchase Coordinates Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 11.sp, color = Color.Gray)
                        Text(String.format("$%.2f", subtotal), fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Customs/Tax (8% VAT)", fontSize = 11.sp, color = Color.Gray)
                        Text(String.format("$%.2f", tax), fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Shipping Protection Fee", fontSize = 11.sp, color = Color.Gray)
                        Text(if (shipping == 0.0) "FREE" else String.format("$%.2f", shipping), fontSize = 11.sp, color = if (shipping == 0.0) Color.Green else MaterialTheme.colorScheme.onSurface)
                    }
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total Cost", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(String.format("$%.2f", totalCost), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFFA78BFA))
                    }
                }
            }

            // Shipping Location segment
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shipping Address Destination", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "Edit Log",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6),
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { showingAddressConfig = !showingAddressConfig }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (showingAddressConfig) {
                        OutlinedTextField(
                            value = shippingAddressText,
                            onValueChange = { shippingAddressText = it },
                            placeholder = { Text("65 Platinum Plaza, Delta tech complex...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                    } else {
                        Text(
                            text = if (shippingAddressText.trim().isEmpty()) "No shipping destination logged yet. Click edit log above." else shippingAddressText,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Action buy bar stick
        Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        val finalAddr = if (shippingAddressText.trim().isEmpty()) "Default physical delivery terminal SR Shops" else shippingAddressText
                        viewModel.checkoutCart(finalAddr, totalCost) {
                            onCheckoutSuccess()
                            viewModel.navigateTo(Screen.Profile) // send to tracking orders
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("checkout_order_button")
                ) {
                    Icon(Icons.Rounded.Lock, "Secure payments representation lock")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Secure Checkout ($${String.format("%.2f", totalCost)})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// 9. PROFILE & LOGIN COMPONENT
@Composable
fun ProfileScreen(
    viewModel: AppViewModel,
    onAuthSuccess: () -> Unit
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val orders by viewModel.userOrdersList.collectAsStateWithLifecycle()
    val authErr by viewModel.authError.collectAsStateWithLifecycle()

    var regName by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isRegisterStateToggle by remember { mutableStateOf(false) }

    var editingDetailsState by remember { mutableStateOf(false) }
    var editablePhoneInput by remember { mutableStateOf("") }
    var editableAddressInput by remember { mutableStateOf("") }

    if (user == null) {
        // Authenticating form displays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegisterStateToggle) "CREATE SR PROFILE" else "SECURE CUSTOMER LOGIN",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isRegisterStateToggle) "Register now to access global premium digital products." else "Enter coordinates to fetch sessions keys.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    if (authErr != null) {
                        Text(
                            text = authErr!!,
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Red.copy(alpha = 0.1f))
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (isRegisterStateToggle) {
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Your Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_field"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Secure Email ID") },
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Registry Password Key") },
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (isRegisterStateToggle) {
                                viewModel.register(regName, emailInput, passwordInput) {
                                    onAuthSuccess()
                                }
                            } else {
                                viewModel.login(emailInput, passwordInput) {
                                    onAuthSuccess()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Text(
                            text = if (isRegisterStateToggle) "Establish Profile Account" else "Authenticate Keys",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isRegisterStateToggle) "Ready registered shopper? Login here." else "New client? Create customized account here.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA),
                        modifier = Modifier
                            .clickable { isRegisterStateToggle = !isRegisterStateToggle }
                            .padding(8.dp)
                    )
                }
            }
        }
        return
    }

    // Active customer profile displays
    val activeShopper = user!!

    LaunchedEffect(activeShopper) {
        editablePhoneInput = activeShopper.phone
        editableAddressInput = activeShopper.address
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Shopper Avatar & Details card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Color initials avatar image
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeShopper.username.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeShopper.username,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = activeShopper.email,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Rounded.ExitToApp, "Signout sessions", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    if (editingDetailsState) {
                        OutlinedTextField(
                            value = editablePhoneInput,
                            onValueChange = { editablePhoneInput = it },
                            label = { Text("Physical Contact Mobile") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editableAddressInput,
                            onValueChange = { editableAddressInput = it },
                            label = { Text("Saved Shipping Terminal Location") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    viewModel.updateProfileAddress(editablePhoneInput, editableAddressInput)
                                    editingDetailsState = false
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                Text("Save Profile Logs")
                            }
                            OutlinedButton(onClick = { editingDetailsState = false }) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        Column {
                            Text("Mobile Contact: ${if (activeShopper.phone.isEmpty()) "Not logged" else activeShopper.phone}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Delivery Destination Terminal: ${if (activeShopper.address.isEmpty()) "Not logged" else activeShopper.address}", fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { editingDetailsState = true },
                                modifier = Modifier.height(34.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                Text("Update Registry coordinates", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Dynamic Order history layout tracking milestones
        item {
            Text(
                text = "Tracking Orders History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.HistoryToggleOff, "No orders logged", modifier = Modifier.size(36.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No orders placed yet.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(orders) { ord ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        val markerColor = when (ord.status) {
                            "Delivered" -> Color(0xFF22C55E)
                            "Shipped" -> Color(0xFF3B82F6)
                            else -> Color(0xFFF97316)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ORDER ID: ${ord.orderId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFA78BFA)
                            )
                            Text(
                                text = ord.status.uppercase(),
                                color = markerColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ord.itemsSummary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Cost: $${String.format("%.2f", ord.totalPrice)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            // Simple visual progress bar milestone
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(
                                            when (ord.status) {
                                                "Delivered" -> 1.0f
                                                "Shipped" -> 0.6f
                                                else -> 0.3f
                                            }
                                        )
                                        .fillMaxHeight()
                                        .background(markerColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 10. SAAS ADMIN DASHBOARD SCREEN
@Composable
fun AdminDashboardScreen(
    viewModel: AppViewModel,
    onNavigateToProduct: (String) -> Unit
) {
    val stats by viewModel.adminAnalytics.collectAsStateWithLifecycle()
    val allActiveProducts by viewModel.productsList.collectAsStateWithLifecycle()
    val allPlacedOrders by viewModel.allOrdersAdmin.collectAsStateWithLifecycle()

    var activeAdminTabSelection by remember { mutableStateOf("Analytics") }

    // Forms controllers
    var formTitle by remember { mutableStateOf("") }
    var formCategory by remember { mutableStateOf("Electronics") }
    var formPrice by remember { mutableStateOf("") }
    var formStock by remember { mutableStateOf("") }
    var formDesc by remember { mutableStateOf("") }
    var formImageUrl by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // Enforce immediate stats calculations checks refresh
    LaunchedEffect(Unit) {
        viewModel.refreshAdminStats()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SR Shops Admin SaaS", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onBackground)
                Icon(Icons.Rounded.DeveloperMode, "SaaS administration markers representation icon", tint = Color(0xFFFBBF24))
            }
        }

        // Horizontal toggle selection buttons tab logs
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Analytics", "Products", "Orders").forEach { item ->
                    Button(
                        onClick = { activeAdminTabSelection = item },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeAdminTabSelection == item) Color(0xFF6366F1) else MaterialTheme.colorScheme.surface,
                            contentColor = if (activeAdminTabSelection == item) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(item, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        when (activeAdminTabSelection) {
            "Analytics" -> {
                // Renders the Sales overview metrics grid cards
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AnalyticsMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Revenue Cashflow",
                                value = String.format("$%.2f", stats.totalRevenue),
                                trendLabel = "+12.4% MoM",
                                isGain = true,
                                iconColor = Color(0xFF22C55E)
                            )
                            AnalyticsMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Orders Total",
                                value = stats.totalOrdersCount.toString(),
                                trendLabel = "+8.2% MoM",
                                isGain = true,
                                iconColor = Color(0xFF3B82F6)
                            )
                        }
                        AnalyticsMetricCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Total Physical/SaaS Units Handled",
                            value = stats.totalUnitsSold.toString(),
                            trendLabel = "Inventory optimized",
                            isGain = true,
                            iconColor = Color(0xFF8B5CF6)
                        )
                    }
                }

                // LINE CHART CONTAINER
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Revenue Over Time (Historical Coordinates)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Line analytical chart drawing
                        AdminSalesTrendsLineChart(salesList = stats.historicSales)
                    }
                }

                // SHARE PROGRESS BAR CHART
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Text("Department Sales Distribution Percentage", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        stats.departmentShares.forEach { (cat, share) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(String.format("%.1f%%", share), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (share.toFloat() / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = when (cat) {
                                        "Electronics" -> Color(0xFF6366F1)
                                        "Gadgets" -> Color(0xFFEAB308)
                                        "Fashion" -> Color(0xFFEC4899)
                                        else -> Color(0xFF10B981)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            "Products" -> {
                // Renders the Product additions panel & inline delete elements
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Text("Add New Product Catalog", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = formTitle,
                            onValueChange = { formTitle = it },
                            placeholder = { Text("Product Title Designation", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("admin_field_title")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Category switcher select
                        val catsOpt = listOf("Electronics", "Gadgets", "Fashion", "Smart Devices", "Mobile Accessories", "Gaming", "Home & Living", "Digital Products")
                        var expandDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Category: $formCategory (Click to select)",
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { expandDropdown = !expandDropdown }
                                    .padding(12.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            DropdownMenu(
                                expanded = expandDropdown,
                                onDismissRequest = { expandDropdown = false }
                            ) {
                                catsOpt.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category, fontSize = 11.sp) },
                                        onClick = {
                                            formCategory = category
                                            expandDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = formPrice,
                                onValueChange = { formPrice = it },
                                placeholder = { Text("Price ($)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).testTag("admin_field_price"),
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = formStock,
                                onValueChange = { formStock = it },
                                placeholder = { Text("Stock Qty", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).testTag("admin_field_stock"),
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = formDesc,
                            onValueChange = { formDesc = it },
                            placeholder = { Text("Description textual overview details...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("admin_field_desc")
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = formImageUrl,
                            onValueChange = { formImageUrl = it },
                            placeholder = { Text("Unsplash image URL address link", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("admin_field_url")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val parsedPrice = formPrice.toDoubleOrNull() ?: 0.0
                                val parsedStock = formStock.toIntOrNull() ?: 10
                                if (formTitle.trim().isNotEmpty() && parsedPrice > 0.0) {
                                    viewModel.addAdminProduct(
                                        title = formTitle,
                                        price = parsedPrice,
                                        category = formCategory,
                                        desc = formDesc,
                                        imageUrl = formImageUrl,
                                        stock = parsedStock
                                    )
                                    // reset forms
                                    formTitle = ""
                                    formPrice = ""
                                    formStock = ""
                                    formDesc = ""
                                    formImageUrl = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("admin_add_product_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Insert into Dynamic Database Catalog", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Inventory listing list with delete buttons
                item {
                    Text("Current Live Catalog (${allActiveProducts.size} items)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                }

                items(allActiveProducts) { prod ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            AsyncImage(
                                model = prod.imageUrl,
                                contentDescription = prod.title,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = prod.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("Price: $${prod.price} | Stock: ${prod.stock} left", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = { viewModel.deleteAdminProduct(prod.id) }) {
                            Icon(Icons.Rounded.Delete, "Delete catalog asset", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            "Orders" -> {
                // Renders standard CRM list allowing admins to change order states
                if (allPlacedOrders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("No customers orders logged yet on dynamic database.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                        }
                    }
                } else {
                    items(allPlacedOrders) { ord ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("ORDER: ${ord.orderId}", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("User: ${ord.userEmail}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(
                                        text = ord.status.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = when (ord.status) {
                                            "Delivered" -> Color(0xFF22C55E)
                                            "Shipped" -> Color(0xFF3B82F6)
                                            else -> Color(0xFFF97316)
                                        }
                                    )
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(text = ord.itemsSummary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // CRM Order transition levers
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Progress Tracker Status:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Button(
                                        onClick = { viewModel.editOrderStatus(ord.orderId, "Shipped") },
                                        modifier = Modifier.height(28.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Ship", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.editOrderStatus(ord.orderId, "Delivered") },
                                        modifier = Modifier.height(28.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Deliver", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// COMPACT ANALYTIQUE CARD METRIQUE
@Composable
fun AnalyticsMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    trendLabel: String,
    isGain: Boolean,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(iconColor)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = trendLabel,
                fontSize = 9.sp,
                color = if (isGain) Color(0xFF22C55E) else Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// EXQUISITE LINE VISUAL TREND ANALYTIQUE CHART (CANVAS DRAWN IN COMPOSE)
@Composable
fun AdminSalesTrendsLineChart(salesList: List<Double>) {
    if (salesList.isEmpty()) return
    
    val maxElement = remember(salesList) { salesList.maxOrNull() ?: 1.0 }
    val minElement = remember(salesList) { salesList.minOrNull() ?: 0.0 }
    val deltaRange = remember(maxElement, minElement) { (maxElement - minElement).coerceAtLeast(1.0) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        val count = salesList.size
        val canvasWidth = size.width
        val canvasHeight = size.height

        val coordinatePoints = mutableListOf<Offset>()
        val paddingHeightPercentage = 15.dp.toPx()

        salesList.forEachIndexed { idx, valPoint ->
            val ratioWidth = idx.toFloat() / (count - 1).toFloat()
            val computedX = ratioWidth * canvasWidth

            // Invert coordinate because pixel height starts from Top-left zero
            val valueRatioHeight = (valPoint - minElement) / deltaRange
            val computedY = canvasHeight - paddingHeightPercentage - (valueRatioHeight * (canvasHeight - 2 * paddingHeightPercentage)).toFloat()
            coordinatePoints.add(Offset(computedX, computedY))
        }

        // Draw elegant gradient brush baseline fill
        val gradientFillPath = Path().apply {
            moveTo(0f, canvasHeight)
            coordinatePoints.forEach { point ->
                lineTo(point.x, point.y)
            }
            lineTo(canvasWidth, canvasHeight)
            close()
        }
        drawPath(
            path = gradientFillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.35f), Color.Transparent)
            )
        )

        // Draw the main clean core trend line
        val lineStrokePath = Path().apply {
            moveTo(coordinatePoints[0].x, coordinatePoints[0].y)
            for (i in 1 until coordinatePoints.size) {
                lineTo(coordinatePoints[i].x, coordinatePoints[i].y)
            }
        }
        drawPath(
            path = lineStrokePath,
            color = Color(0xFF6366F1),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw circles on top of each coordinate point index
        coordinatePoints.forEachIndexed { index, pos ->
            drawCircle(
                color = if (index == coordinatePoints.size - 1) Color(0xFFEAB308) else Color.White,
                radius = 4.dp.toPx(),
                center = pos
            )
            drawCircle(
                color = Color(0xFF6366F1),
                radius = 4.dp.toPx(),
                center = pos,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

// ELEGANT PREMIUM CUSTOM CAPSULE FILTER CHIP (BLENDS WITH DESIGN SPECS)
@Composable
fun ElegantFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF9333EA)) // blue-600 to purple-600
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color(0x0FFFFFFF), Color(0x0FFFFFFF)) // white/5 (0x0FFFFFFF is ~ 6% opacity)
                    )
                }
            )
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else Color(0x1AFFFFFF), // border-white/10 glass
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF94A3B8), // slate-400 unselected
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
