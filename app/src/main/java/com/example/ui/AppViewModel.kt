package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiHelper
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface Screen {
    object Home : Screen
    object Categories : Screen
    object Products : Screen
    object FlashSale : Screen
    object Services : Screen
    object Assistant : Screen
    object Cart : Screen
    object Profile : Screen
    object AdminDashboard : Screen
    data class ProductDetail(val productId: String) : Screen
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = ShopRepository(db)

    // Current screen navigation routing state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Screen navigation stack to support back triggers
    private val navigationHistory = mutableListOf<Screen>()

    // Selected product for product detail screens
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Authentication session state properties
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Global active product registry (Room stream)
    val productsList: StateFlow<List<ProductEntity>> = repository.getAllProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active search & filter parameters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Dynamic cart item flow combined with actual product entities
    val cartProducts: StateFlow<List<Pair<ProductEntity, CartItemEntity>>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else {
                repository.getUserCartFlow(user.email).combine(productsList) { cartItems, products ->
                    cartItems.mapNotNull { cartItem ->
                        val matchedProd = products.find { it.id == cartItem.productId }
                        if (matchedProd != null) Pair(matchedProd, cartItem) else null
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wishlist mapped items Flow
    val wishlistProducts: StateFlow<List<ProductEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else {
                repository.getUserWishlistFlow(user.email).combine(productsList) { wishes, prods ->
                    wishes.mapNotNull { wish -> prods.find { it.id == wish.productId } }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Order tracking metrics
    val userOrdersList: StateFlow<List<OrderEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getUserOrdersFlow(user.email)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin state Flow elements
    val allOrdersAdmin: StateFlow<List<OrderEntity>> = repository.getAllOrdersAdminFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminAnalytics = MutableStateFlow(
        SalesStats(0.0, 0, 0, emptyMap(), emptyList())
    )
    val adminAnalytics: StateFlow<SalesStats> = _adminAnalytics.asStateFlow()

    // AI Shopping Assistant chat history state
    private val _chatHistory = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "assistant" to "Hello! Welcome to **SR Shops**, your premium AI-powered digital marketplace. How can I assist you with your luxury shopping or custom developer services today?"
        )
    )
    val chatHistory: StateFlow<List<Pair<String, String>>> = _chatHistory.asStateFlow()

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    // Product reviews for the selected product detail sheet
    val selectedProductReviews: StateFlow<List<ReviewEntity>> = _selectedProduct
        .flatMapLatest { prod ->
            if (prod == null) flowOf(emptyList())
            else repository.getProductReviewsFlow(prod.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.populateInitialCatalogIfEmpty()
            refreshAdminStats()
            
            // Auto sign-in a demo VIP customer during physical run or simulator load
            demoLogin()
        }
    }

    // Auto load starter customer profile
    private suspend fun demoLogin() {
        val email = "vip.shopper@srshops.com"
        val existing = repository.getUserProfile(email)
        if (existing == null) {
            repository.registerUser("VIP Premium Shopper", email, "secret123")
            val user = repository.getUserProfile(email)
            if (user != null) {
                _currentUser.value = user.copy(address = "99 Luxe Boulevard, Sector 4, Silicon Delta")
                repository.updateUserProfile(_currentUser.value!!)
            }
        } else {
            _currentUser.value = existing
        }
    }

    // Navigation trigger methods
    fun navigateTo(screen: Screen) {
        navigationHistory.add(_currentScreen.value)
        _currentScreen.value = screen
        
        if (screen is Screen.ProductDetail) {
            viewModelScope.launch {
                _selectedProduct.value = repository.getProductById(screen.productId)
            }
        }
    }

    fun navigateBack() {
        if (navigationHistory.isNotEmpty()) {
            val prev = navigationHistory.removeAt(navigationHistory.size - 1)
            _currentScreen.value = prev
        } else {
            _currentScreen.value = Screen.Home
        }
    }

    // Authentication procedures
    fun register(name: String, email: String, pass: String, onSuccess: () -> Unit) {
        _authError.value = null
        if (name.isEmpty() || email.isEmpty() || pass.length < 4) {
            _authError.value = "Please complete credentials with a strong password (minimum 4 characters)"
            return
        }
        viewModelScope.launch {
            val registered = repository.registerUser(name, email, pass)
            if (registered) {
                val profile = repository.loginUser(email, pass)
                _currentUser.value = profile
                onSuccess()
            } else {
                _authError.value = "Email is already registered on SR Shops marketplace."
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        _authError.value = null
        viewModelScope.launch {
            val user = repository.loginUser(email, pass)
            if (user != null) {
                _currentUser.value = user
                onSuccess()
            } else {
                _authError.value = "Invalid email or secure password combination. Try again."
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.Home
    }

    fun updateProfileAddress(phone: String, addr: String) {
        val active = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = active.copy(phone = phone, address = addr)
            _currentUser.value = updated
            repository.updateUserProfile(updated)
        }
    }

    // Product search handlers
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    // Cart Interactions
    fun addProductToCart(productId: String, quantity: Int = 1) {
        val user = _currentUser.value
        if (user == null) {
            // Send back to log screen to secure sessions
            _currentScreen.value = Screen.Profile
            return
        }
        viewModelScope.launch {
            repository.addToCart(productId, quantity, user.email)
        }
    }

    fun incrementCartQty(itemId: Int, currentQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(itemId, currentQty + 1)
        }
    }

    fun decrementCartQty(itemId: Int, currentQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(itemId, currentQty - 1)
        }
    }

    fun removeCartItem(itemId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(itemId)
        }
    }

    // Wishlist toggles
    fun toggleWishlist(productId: String) {
        val user = _currentUser.value
        if (user == null) {
            _currentScreen.value = Screen.Profile
            return
        }
        viewModelScope.launch {
            repository.toggleWishlist(productId, user.email)
        }
    }

    fun getWishlistStatusFlow(productId: String): Flow<Boolean> {
        val user = _currentUser.value ?: return flowOf(false)
        return repository.isProductWishedFlow(productId, user.email)
    }

    // Checkout execution
    fun checkoutCart(shippingAddress: String, totalCost: Double, onComplete: () -> Unit) {
        val user = _currentUser.value ?: return
        val cartList = cartProducts.value
        if (cartList.isEmpty()) return

        viewModelScope.launch {
            val itemsToCheckout = cartList.map { Pair(it.first, it.second.selectQuantity) }
            val completed = repository.placeOrder(
                email = user.email,
                cartItems = itemsToCheckout,
                address = shippingAddress,
                totalPrice = totalCost
            )
            if (completed) {
                refreshAdminStats()
                onComplete()
            }
        }
    }

    // Reviews submissions
    fun postProductReview(productId: String, rating: Float, comment: String) {
        val activeUser = _currentUser.value ?: return
        val authorName = activeUser.username
        viewModelScope.launch {
            repository.submitReview(productId, authorName, rating, comment)
            // Reload product details to update live ranking stars
            _selectedProduct.value = repository.getProductById(productId)
        }
    }

    // Admin Dashboard procedures
    fun addAdminProduct(title: String, price: Double, category: String, desc: String, imageUrl: String, stock: Int) {
        val id = "p_user_${System.currentTimeMillis()}"
        val finalUrl = if (imageUrl.trim().isEmpty()) {
            "https://images.unsplash.com/photo-1542496658-e33a6d0d50f6?w=500" // default premium gadget image
        } else {
            imageUrl
        }
        viewModelScope.launch {
            val newProduct = ProductEntity(
                id = id,
                title = title,
                price = price,
                originalPrice = price * 1.25,
                category = category,
                description = desc,
                imageUrl = finalUrl,
                stock = stock,
                rating = 5.0f,
                reviewCount = 0
            )
            repository.insertAdminProduct(newProduct)
            refreshAdminStats()
        }
    }

    fun deleteAdminProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteAdminProduct(productId)
            refreshAdminStats()
        }
    }

    fun editOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun refreshAdminStats() {
        viewModelScope.launch {
            _adminAnalytics.value = repository.computeSalesStats()
        }
    }

    // AI Chat Bot Integration call through REST API
    fun sendAssistantMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return
        
        val activeHistory = _chatHistory.value.toMutableList()
        activeHistory.add("user" to messageText)
        _chatHistory.value = activeHistory
        _isAiTyping.value = true

        viewModelScope.launch {
            val responseText = GeminiHelper.getShoppingAssistantResponse(messageText, activeHistory.dropLast(1))
            withContext(Dispatchers.Main) {
                val updatedHistory = _chatHistory.value.toMutableList()
                updatedHistory.add("assistant" to responseText)
                _chatHistory.value = updatedHistory
                _isAiTyping.value = false
            }
        }
    }

    fun clearChatHistory() {
        _chatHistory.value = listOf(
            "assistant" to "Hello! Welcome to **SR Shops**, your premium AI-powered digital marketplace. How can I assist you with your luxury shopping or custom developer services today?"
        )
    }

    fun handlePresetPrompt(promptText: String) {
        sendAssistantMessage(promptText)
    }
}
