package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun FarmerDashboard(viewModel: AgriViewModel) {
    val farmerName by viewModel.farmerName.collectAsState()
    val isEditProfileOpen = remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Live Inventory", "Fulfillment Center")

    Column(modifier = Modifier.fillMaxSize()) {
        // Co-op header card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FARMER CO-OPERATIVE PORTAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            letterSpacing = 1.1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = farmerName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { isEditProfileOpen.value = true },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                CircleShape
                            )
                            .testTag("edit_farmer_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Cooperative Profile",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
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
                    modifier = Modifier.testTag("farmer_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content Switcher
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (selectedTab) {
                0 -> FarmerInventoryTab(viewModel = viewModel)
                1 -> FarmerOrdersTab(viewModel = viewModel)
            }
        }
    }

    if (isEditProfileOpen.value) {
        EditFarmerProfileDialog(
            currentName = farmerName,
            onDismiss = { isEditProfileOpen.value = false },
            onSave = { updatedName ->
                viewModel.updateProfile(farmer = updatedName, buyer = "Jane Doe")
                isEditProfileOpen.value = false
            }
        )
    }
}

@Composable
fun FarmerInventoryTab(viewModel: AgriViewModel) {
    val myCrops by viewModel.farmerCrops.collectAsState()
    var isAddSheetOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (myCrops.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = "No Crops Listed",
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Live Inventory Active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap the '+' floating button below to list a freshly harvested crop to the real-time marketplace.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(myCrops, key = { it.id }) { crop ->
                    FarmerCropCard(crop = crop, viewModel = viewModel)
                }
            }
        }

        FloatingActionButton(
            onClick = { isAddSheetOpen = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_listing_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Crop Listing")
        }
    }

    if (isAddSheetOpen) {
        AddListingModal(
            onDismiss = { isAddSheetOpen = false },
            onAdd = { name, category, qty, unit, price, desc, loc ->
                viewModel.addCropListing(name, category, qty, unit, price, desc, loc)
                isAddSheetOpen = false
            }
        )
    }
}

@Composable
fun FarmerCropCard(crop: CropEntity, viewModel: AgriViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("farmer_crop_card_${crop.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = crop.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ID: ${crop.id}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = crop.cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { viewModel.deleteCropListing(crop) },
                    modifier = Modifier.testTag("delete_crop_button_${crop.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inventory Counter Meter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "LIVE INVENTORY STATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${crop.quantity} ${crop.unit}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (crop.quantity < 20.0) Color.Red else Color(0xFF2E7D32)
                        )
                        if (crop.quantity < 20.0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LOW STOCK",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Rapid Stock Counter Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (crop.quantity > 0) {
                                viewModel.updateCropListing(crop.copy(quantity = crop.quantity - 1))
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                            .testTag("decrement_stock_button_${crop.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease Stock",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = {
                            viewModel.updateCropListing(crop.copy(quantity = crop.quantity + 1))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .testTag("increment_stock_button_${crop.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase Stock",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "UNIT PRICE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = "$${crop.pricePerUnit}/${crop.unit}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "LOCATION", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = crop.location, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun FarmerOrdersTab(viewModel: AgriViewModel) {
    val orders by viewModel.farmerOrders.collectAsState()

    if (orders.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "No Orders",
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Orders Received",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "When buyers purchase your crops in the marketplace, co-op trans clearances appear here for fulfillment tracking.",
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
            contentPadding = PaddingValues(bottom = 50.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                FarmerOrderCard(order = order, onStatusChange = { newStatus ->
                    viewModel.updateFulfillmentStatus(order.id, newStatus)
                })
            }
        }
    }
}

@Composable
fun FarmerOrderCard(order: OrderEntity, onStatusChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("farmer_order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ORDER ID: ${order.receiptNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = order.cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
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
                        color = when (order.orderStatus) {
                            "Processing" -> Color(0xFFE65100)
                            "Shipped" -> Color(0xFF0D47A1)
                            "Delivered" -> Color(0xFF1B5E20)
                            else -> Color.DarkGray
                        },
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "BUYER PROFILE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = order.buyerName, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "VOLUME BOUGHT", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = "${order.quantityOrdered} units", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "CLEARANCE SETTLEMENT", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = order.paymentMethod, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "TOTAL PRICE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = "$${String.format("%.2f", order.totalAmount)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Triggers for logistics state
            if (order.orderStatus != "Delivered") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (order.orderStatus == "Processing") {
                        Button(
                            onClick = { onStatusChange("Shipped") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("ship_button_${order.id}")
                        ) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = "Ship Unit")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SHIP UNIT", fontWeight = FontWeight.Bold)
                        }
                    } else if (order.orderStatus == "Shipped") {
                        Button(
                            onClick = { onStatusChange("Delivered") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("deliver_button_${order.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm Delivery")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("MARK DELIVERED", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditFarmerProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameState by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Co-operative Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Specify your registered agricultural cooperative or farm union identity name:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = nameState,
                    onValueChange = { nameState = it },
                    label = { Text("Co-operative Name") },
                    modifier = Modifier.fillMaxWidth().testTag("farmer_profile_name_input"),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nameState) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_farmer_profile_button")
            ) {
                Text("SAVE PROFILE", fontWeight = FontWeight.Bold)
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
fun AddListingModal(
    onDismiss: () -> Unit,
    onAdd: (name: String, category: String, qty: Double, unit: String, price: Double, desc: String, loc: String) -> Unit
) {
    var cropName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pricePerUnit by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedCategory by remember { mutableStateOf("Vegetables") }
    val categories = listOf("Vegetables", "Fruits", "Grains", "Tubers", "Legumes", "Other")
    var isCatDropdownExpanded by remember { mutableStateOf(false) }

    var selectedUnit by remember { mutableStateOf("kg") }
    val units = listOf("kg", "crates", "bags", "tons")
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_listing_dialog")
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "NEW MARKET OFFERING",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Broadcast your live harvest stock on the real-time marketplace.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text("Crop/Item Name") },
                        modifier = Modifier.fillMaxWidth().testTag("crop_name_field"),
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = { Icon(imageVector = Icons.Default.Agriculture, contentDescription = null) }
                    )
                }

                item {
                    Column {
                        Text(text = "Category (Press to Select)", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { isCatDropdownExpanded = true }
                                .padding(14.dp)
                                .testTag("category_selector_clickable")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(selectedCategory, fontWeight = FontWeight.Bold)
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = isCatDropdownExpanded,
                            onDismissRequest = { isCatDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        isCatDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Stock Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("quantity_field"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Unit", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .clickable { isUnitDropdownExpanded = true }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(selectedUnit, fontWeight = FontWeight.Bold)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = isUnitDropdownExpanded,
                                onDismissRequest = { isUnitDropdownExpanded = false }
                            ) {
                                units.forEach { un ->
                                    DropdownMenuItem(
                                        text = { Text(un) },
                                        onClick = {
                                            selectedUnit = un
                                            isUnitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = pricePerUnit,
                        onValueChange = { pricePerUnit = it },
                        label = { Text("Price per Unit (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("price_field"),
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = { Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null) }
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (City/Region)") },
                        modifier = Modifier.fillMaxWidth().testTag("location_field"),
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = { Icon(imageVector = Icons.Default.Place, contentDescription = null) }
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Harvest Notes") },
                        modifier = Modifier.fillMaxWidth().testTag("description_field"),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 3
                    )
                }

                if (validationError != null) {
                    item {
                        Text(
                            text = validationError!!,
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
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ABORT", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val qtyVal = quantity.toDoubleOrNull()
                                val priceVal = pricePerUnit.toDoubleOrNull()
                                when {
                                    cropName.isBlank() -> validationError = "Crop name is required."
                                    qtyVal == null || qtyVal <= 0.0 -> validationError = "Valid quantity is required."
                                    priceVal == null || priceVal <= 0.0 -> validationError = "Valid price is required."
                                    location.isBlank() -> validationError = "Harvest storage location is required."
                                    else -> {
                                        onAdd(cropName, selectedCategory, qtyVal, selectedUnit, priceVal, description, location)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).testTag("save_listing_button")
                        ) {
                            Text("PUBLISH", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
