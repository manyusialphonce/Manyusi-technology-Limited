package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CropEntity
import com.example.data.OrderEntity
import com.example.ui.AgriViewModel
import androidx.compose.ui.window.Dialog

@Composable
fun BuyerDashboard(viewModel: AgriViewModel) {
    val buyerName by viewModel.buyerName.collectAsState()
    val isEditProfileOpen = remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Browse Marketplace", "My Purchases")

    Column(modifier = Modifier.fillMaxSize()) {
        // Buyer profile card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BUYER ACCOUNT PROFILE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buyerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { isEditProfileOpen.value = true },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .testTag("edit_buyer_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Buyer Profile",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.testTag("buyer_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch screens based on tab selection
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (selectedTab) {
                0 -> MarketplaceTab(viewModel = viewModel)
                1 -> BuyerPurchasesTab(viewModel = viewModel)
            }
        }
    }

    if (isEditProfileOpen.value) {
        EditBuyerProfileDialog(
            currentName = buyerName,
            onDismiss = { isEditProfileOpen.value = false },
            onSave = { updatedName ->
                viewModel.updateProfile(farmer = "Sunset Valley Co-op", buyer = updatedName)
                isEditProfileOpen.value = false
            }
        )
    }
}

@Composable
fun MarketplaceTab(viewModel: AgriViewModel) {
    val crops by viewModel.allCrops.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var activeBuyCrop by remember { mutableStateOf<CropEntity?>(null) }

    // Categories list for scrollable chips
    val categories = listOf("All", "Vegetables", "Fruits", "Grains", "Tubers", "Legumes", "Other")

    // Filter results reactively
    val filteredCrops = remember(crops, searchQuery, selectedCategory) {
        crops.filter { crop ->
            val matchesCategory = selectedCategory == "All" || crop.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = crop.cropName.contains(searchQuery, ignoreCase = true) ||
                    crop.location.contains(searchQuery, ignoreCase = true) ||
                    crop.farmerName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch && crop.quantity > 0.0 // Only show active instock listings
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            label = { Text("Search crops, location, or farms...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("marketplace_search_input"),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategory(cat) },
                    label = { Text(cat, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("chip_$cat")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredCrops.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = "No Results",
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Matching Harvest Crops",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Try adjusting your cooperative keyword or switching the category query.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredCrops, key = { it.id }) { crop ->
                    BuyerCropCard(crop = crop, onBuyClick = { activeBuyCrop = crop })
                }
            }
        }
    }

    if (activeBuyCrop != null) {
        BuyCropSheet(
            crop = activeBuyCrop!!,
            onDismiss = { activeBuyCrop = null },
            onConfirmPurchase = { buyQty, paymentMethod, cardNo, pName ->
                viewModel.checkoutAndPay(activeBuyCrop!!, buyQty, paymentMethod, cardNo, pName)
                activeBuyCrop = null
            }
        )
    }
}

@Composable
fun BuyerCropCard(crop: CropEntity, onBuyClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crop_card_${crop.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = crop.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = crop.cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }

                // stock badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (crop.quantity < 20) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${crop.quantity.toInt()} ${crop.unit} Left",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (crop.quantity < 20) Color(0xFFE65100) else Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = crop.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Farmer Profile Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = "Farm store",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = crop.farmerName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = crop.location, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CO-OP INDEX PRICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "$${crop.pricePerUnit} / ${crop.unit}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2E7D32)
                    )
                }

                Button(
                    onClick = onBuyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("buy_button_${crop.id}")
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BUY NOW", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BuyerPurchasesTab(viewModel: AgriViewModel) {
    val orders by viewModel.buyerOrders.collectAsState()
    var selectedOrderForReceipt by remember { mutableStateOf<OrderEntity?>(null) }

    if (orders.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "No receipts",
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Purchases Logged",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Secure transactional clearances will generate blockchain receipt proofs. Explore the marketplace tab to place orders.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("buyer_order_card_${order.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SECURE REFERENCE: ${order.receiptNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = order.cropName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (order.orderStatus) {
                                            "Processing" -> Color(0xFFFFF3E0)
                                            "Shipped" -> Color(0xFFE3F2FD)
                                            "Delivered" -> Color(0xFFE8F5E9)
                                            else -> Color.LightGray
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = order.orderStatus.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (order.orderStatus) {
                                        "Processing" -> Color(0xFFE65100)
                                        "Shipped" -> Color(0xFF0D47A1)
                                        "Delivered" -> Color(0xFF1B5E20)
                                        else -> Color.DarkGray
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(text = "VOLUME PURCHASED", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(text = "${order.quantityOrdered} units", fontWeight = FontWeight.Bold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "TOTAL AMOUNT PAID", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    text = "$${String.format("%.2f", order.totalAmount)}",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { selectedOrderForReceipt = order },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("view_receipt_button_${order.id}")
                        ) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VIEW CO-OP RECEIPT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (selectedOrderForReceipt != null) {
        com.example.ui.components.DigitalReceiptDialog(
            order = selectedOrderForReceipt,
            onDismiss = { selectedOrderForReceipt = null }
        )
    }
}

@Composable
fun EditBuyerProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameState by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Buyer Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Specify your registered buyer name or institution details for checkout records:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it },
                    label = { Text("Buyer Registered Name") },
                    modifier = Modifier.fillMaxWidth().testTag("buyer_profile_name_input"),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nameState) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.testTag("save_buyer_profile_button")
            ) {
                Text("SAVE NAME", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun BuyCropSheet(
    crop: CropEntity,
    onDismiss: () -> Unit,
    onConfirmPurchase: (buyQty: Double, paymentMethod: String, cardNo: String, pName: String) -> Unit
) {
    var buyQtyString by remember { mutableStateOf("1") }
    var selectedPayMethod by remember { mutableStateOf("Mobile Money") }
    val payMethods = listOf("Mobile Money", "Credit Card", "Co-op Bank Transfer")

    var identifierInput by remember { mutableStateOf("")}
    var secondaryInput by remember { mutableStateOf("")} // e.g., pin / cvv / branch

    var inputError by remember { mutableStateOf<String?>(null) }

    val computedTotal = remember(buyQtyString, crop.pricePerUnit) {
        val qty = buyQtyString.toDoubleOrNull() ?: 0.0
        qty * crop.pricePerUnit
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("buy_crop_dialog")
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "SECURE CO-OP CHECKOUT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "You are purchasing directly from ${crop.farmerName}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = crop.cropName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(
                            text = "$${crop.pricePerUnit}/${crop.unit}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Text(
                        text = "Available stock: ${crop.quantity.toInt()} ${crop.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                item {
                    OutlinedTextField(
                        value = buyQtyString,
                        onValueChange = {
                            buyQtyString = it
                            inputError = null
                        },
                        label = { Text("Volume to Buy (${crop.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("buy_quantity_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    Text(text = "Choose Settlement Channel", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(payMethods) { method ->
                            val isChosen = method == selectedPayMethod
                            ElevatedCard(
                                onClick = {
                                    selectedPayMethod = method
                                    identifierInput = ""
                                    secondaryInput = ""
                                    inputError = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (isChosen) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("pay_method_$method")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (method) {
                                            "Mobile Money" -> Icons.Default.PhoneAndroid
                                            "Credit Card" -> Icons.Default.CreditCard
                                            else -> Icons.Default.AccountBalance
                                        },
                                        contentDescription = null,
                                        tint = if (isChosen) MaterialTheme.colorScheme.secondary else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = method,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChosen) MaterialTheme.colorScheme.secondary else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Context-Specific Secure Input fields
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (selectedPayMethod) {
                            "Mobile Money" -> {
                                OutlinedTextField(
                                    value = identifierInput,
                                    onValueChange = { identifierInput = it },
                                    label = { Text("Mobile phone (+255 XXX XX...)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    placeholder = { Text("+255 712345678") },
                                    modifier = Modifier.fillMaxWidth().testTag("payment_phone_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    leadingIcon = { Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null) }
                                )
                                OutlinedTextField(
                                    value = secondaryInput,
                                    onValueChange = { secondaryInput = it },
                                    label = { Text("MTN / M-Pesa Quick Release PIN") },
                                    placeholder = { Text("XXXX") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.fillMaxWidth().testTag("payment_pin_input"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                            "Credit Card" -> {
                                OutlinedTextField(
                                    value = identifierInput,
                                    onValueChange = { identifierInput = it },
                                    label = { Text("Card Number (Visa/Mastercard)") },
                                    placeholder = { Text("4000 1234 5678 9010") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("payment_card_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    leadingIcon = { Icon(imageVector = Icons.Default.CreditCard, contentDescription = null) }
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = secondaryInput,
                                        onValueChange = { secondaryInput = it },
                                        label = { Text("CVV") },
                                        placeholder = { Text("123") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).testTag("payment_cvv_input"),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                            "Co-op Bank Transfer" -> {
                                OutlinedTextField(
                                    value = identifierInput,
                                    onValueChange = { identifierInput = it },
                                    label = { Text("Agrarian Co-operative Acct No") },
                                    placeholder = { Text("COOP-992388-X") },
                                    modifier = Modifier.fillMaxWidth().testTag("payment_coop_acct_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    leadingIcon = { Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null) }
                                )
                            }
                        }
                    }
                }

                item {
                    // Itemized summary box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Subtotal:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("$${String.format("%.2f", computedTotal)}", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Co-op Clearance Stamp:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("$1.50", fontWeight = FontWeight.Bold)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TOTAL SETTLED DUE:", fontWeight = FontWeight.ExtraBold)
                            Text(
                                "$${String.format("%.2f", computedTotal + 1.50)}",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32),
                                fontSize = 16.sp,
                                modifier = Modifier.testTag("receipt_total_amount_text")
                            )
                        }
                    }
                }

                if (inputError != null) {
                    item {
                        Text(
                            text = inputError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("CANCEL")
                        }

                        Button(
                            onClick = {
                                val buyQtyVal = buyQtyString.toDoubleOrNull()
                                when {
                                    buyQtyVal == null || buyQtyVal <= 0.0 -> {
                                        inputError = "Invalid quantity specified."
                                    }
                                    buyQtyVal > crop.quantity -> {
                                        inputError = "Quantity exceeds crop's available stock of ${crop.quantity.toInt()}."
                                    }
                                    identifierInput.isBlank() -> {
                                        inputError = "Please specify a billing identifier."
                                    }
                                    else -> {
                                        onConfirmPurchase(buyQtyVal, selectedPayMethod, identifierInput, "Jane Doe")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1.5f).testTag("authorise_payment_button")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AUTHORISE PAY", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
