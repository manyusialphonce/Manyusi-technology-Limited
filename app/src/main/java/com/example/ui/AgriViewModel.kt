package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AgriDatabase
import com.example.data.AgriRepository
import com.example.data.CropEntity
import com.example.data.OrderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class AgriViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AgriDatabase::class.java,
        "agriconnect_db"
    ).build()

    private val repository = AgriRepository(db.agriDao())

    // Roles and User Details
    private val _userRole = MutableStateFlow("BUYER") // "BUYER" or "FARMER"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _farmerName = MutableStateFlow("Sunset Valley Co-op")
    val farmerName: StateFlow<String> = _farmerName.asStateFlow()

    private val _buyerName = MutableStateFlow("Jane Doe")
    val buyerName: StateFlow<String> = _buyerName.asStateFlow()

    fun setRole(role: String) {
        _userRole.value = role
    }

    fun updateProfile(farmer: String, buyer: String) {
        _farmerName.value = farmer
        _buyerName.value = buyer
    }

    // Active Live Crops (all available in the market)
    val allCrops: StateFlow<List<CropEntity>> = repository.allCrops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive Farmers listings
    val farmerCrops: StateFlow<List<CropEntity>> = _farmerName
        .flatMapLatest { name -> repository.getCropsByFarmer(name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive orders
    val farmerOrders: StateFlow<List<OrderEntity>> = _farmerName
        .flatMapLatest { name -> repository.getOrdersForFarmer(name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buyerOrders: StateFlow<List<OrderEntity>> = _buyerName
        .flatMapLatest { name -> repository.getOrdersForBuyer(name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter/Search parameters for UI
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    // Interactive Checkout & Payment Processing States
    private val _isProcessingPayment = MutableStateFlow(false)
    val isProcessingPayment = _isProcessingPayment.asStateFlow()

    private val _paymentStageMessage = MutableStateFlow("")
    val paymentStageMessage = _paymentStageMessage.asStateFlow()

    private val _paymentSuccessReceipt = MutableStateFlow<OrderEntity?>(null)
    val paymentSuccessReceipt = _paymentSuccessReceipt.asStateFlow()

    fun clearReceipt() {
        _paymentSuccessReceipt.value = null
    }

    // UI Toast-like temporary messaging state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    init {
        // Seed some starter crops if database is empty
        viewModelScope.launch {
            repository.allCrops.collect { list ->
                if (list.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        val seeds = listOf(
            CropEntity(
                farmerName = "Mbeya Farms Group",
                cropName = "Organic Avocados",
                category = "Fruits",
                quantity = 450.0,
                unit = "crates",
                pricePerUnit = 12.50,
                description = "Grade-A Hass Avocados, freshly harvested from high-yield orchards.",
                location = "Mbeya, Tanzania"
            ),
            CropEntity(
                farmerName = "Sunset Valley Co-op",
                cropName = "Highland Red Onions",
                category = "Vegetables",
                quantity = 1200.0,
                unit = "kg",
                pricePerUnit = 0.85,
                description = "Pungent, sweet highland red onions with extended shelf life.",
                location = "Morogoro, Tanzania"
            ),
            CropEntity(
                farmerName = "Sunset Valley Co-op",
                cropName = "Golden Wheat Grains",
                category = "Grains",
                quantity = 50.0,
                unit = "bags",
                pricePerUnit = 32.00,
                description = "Premium double-sieved golden wheat grains, gluten-rich, pesticide-free.",
                location = "Iringa, Tanzania"
            ),
            CropEntity(
                farmerName = "Rungwe Fruit Farms",
                cropName = "Cavendish Bananas",
                category = "Fruits",
                quantity = 150.0,
                unit = "crates",
                pricePerUnit = 6.00,
                description = "Unblemished yellow Cavendish bananas, loaded with potassium, natural flavor.",
                location = "Rungwe, Tanzania"
            ),
            CropEntity(
                farmerName = "Highland Agriculture Union",
                cropName = "Irish White Potatoes",
                category = "Tubers",
                quantity = 850.0,
                unit = "bags",
                pricePerUnit = 11.20,
                description = "High-starch Irish white potatoes, freshly dug up and pre-cleaned.",
                location = "Njombe, Tanzania"
            )
        )
        for (seed in seeds) {
            repository.insertCrop(seed)
        }
    }

    // Farmer Operations
    fun addCropListing(name: String, category: String, qty: Double, unit: String, price: Double, desc: String, loc: String) {
        viewModelScope.launch {
            if (name.isBlank() || qty <= 0.0 || price <= 0.0) {
                _toastMessage.value = "Invalid listing details. Please enter all fields."
                return@launch
            }
            val newCrop = CropEntity(
                farmerName = _farmerName.value,
                cropName = name,
                category = category,
                quantity = qty,
                unit = unit,
                pricePerUnit = price,
                description = desc,
                location = loc
            )
            repository.insertCrop(newCrop)
            _toastMessage.value = "New crop inventory listing added: $name ($qty $unit)."
        }
    }

    fun updateCropListing(crop: CropEntity) {
        viewModelScope.launch {
            repository.updateCrop(crop)
            _toastMessage.value = "Stock updated for ${crop.cropName}."
        }
    }

    fun deleteCropListing(crop: CropEntity) {
        viewModelScope.launch {
            repository.deleteCrop(crop)
            _toastMessage.value = "Listing for ${crop.cropName} removed."
        }
    }

    fun updateFulfillmentStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            _toastMessage.value = "Order status updated to $newStatus."
        }
    }

    // Buyer checkout & secure payment simulation with live inventory decrease
    fun checkoutAndPay(crop: CropEntity, buyQty: Double, paymentMethod: String, cardNumber: String = "", mmPhone: String = "") {
        viewModelScope.launch {
            if (buyQty <= 0.0) {
                _toastMessage.value = "Please select a valid quantity to buy."
                return@launch
            }
            if (buyQty > crop.quantity) {
                _toastMessage.value = "Insufficient inventory available. Available: ${crop.quantity} ${crop.unit}."
                return@launch
            }

            _isProcessingPayment.value = true
            
            // Payment Stage simulation loop
            val stages = listOf(
                "Establishing secure SSL socket handshake...",
                "Contacting payment routing engine...",
                "Authorizing account balance clearance...",
                "Deducting live inventory ledger...",
                "Locking agricultural contract records..."
            )

            for (stage in stages) {
                _paymentStageMessage.value = stage
                kotlinx.coroutines.delay(800)
            }

            // Perform Database decrement and insertion
            val remainingQuantity = crop.quantity - buyQty
            repository.updateCropQuantity(crop.id, remainingQuantity)

            // Generate receipt details
            val receiptNo = "Agri-" + Random.nextInt(10000, 99999)
            val txRef = "txn_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() % 1000

            val computedTotal = buyQty * crop.pricePerUnit

            val newOrder = OrderEntity(
                buyerName = _buyerName.value,
                farmerName = crop.farmerName,
                cropId = crop.id,
                cropName = crop.cropName,
                quantityOrdered = buyQty,
                totalAmount = computedTotal,
                paymentMethod = paymentMethod,
                orderStatus = "Processing", // Initially Processing
                receiptNumber = receiptNo,
                transactionRef = txRef
            )

            repository.insertOrder(newOrder)

            _isProcessingPayment.value = false
            _paymentStageMessage.value = ""
            _paymentSuccessReceipt.value = newOrder
            _toastMessage.value = "Secure clearance success! Receipt generated."
        }
    }
}
