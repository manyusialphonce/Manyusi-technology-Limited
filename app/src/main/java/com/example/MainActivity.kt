package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AgriViewModel
import com.example.ui.components.PaymentProcessingDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AgriViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val userRole by viewModel.userRole.collectAsState()
                val toastMessage by viewModel.toastMessage.collectAsState()
                val isProcessingPayment by viewModel.isProcessingPayment.collectAsState()
                val paymentStageMessage by viewModel.paymentStageMessage.collectAsState()
                val paymentSuccessReceipt by viewModel.paymentSuccessReceipt.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Bottom Tab selection state
                var selectedBottomTab by remember { mutableIntStateOf(0) }

                // Pop toasts to Snackbar
                LaunchedEffect(toastMessage) {
                    toastMessage?.let {
                        snackbarHostState.showSnackbar(
                            message = it,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearToast()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Agriculture,
                                        contentDescription = "AgriConnect Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AgriConnect",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        letterSpacing = (-0.5).sp
                                    )
                                }
                            },
                            actions = {
                                // Double-role sandbox switch
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    TextButton(
                                        onClick = { viewModel.setRole("BUYER") },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (userRole == "BUYER") MaterialTheme.colorScheme.secondary else Color.Transparent,
                                            contentColor = if (userRole == "BUYER") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("role_toggle_buyer")
                                    ) {
                                        Text("Buyer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    TextButton(
                                        onClick = { viewModel.setRole("FARMER") },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (userRole == "FARMER") MaterialTheme.colorScheme.primary else Color.Transparent,
                                            contentColor = if (userRole == "FARMER") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("role_toggle_farmer")
                                    ) {
                                        Text("Farmer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedBottomTab == 0,
                                onClick = { selectedBottomTab = 0 },
                                label = {
                                    Text(
                                        text = if (userRole == "BUYER") "Market" else "Inventory",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (userRole == "BUYER") Icons.Rounded.Storefront else Icons.Rounded.Agriculture,
                                        contentDescription = "Main Market Tab"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (userRole == "BUYER") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    selectedTextColor = if (userRole == "BUYER") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("bottom_tab_market")
                            )

                            NavigationBarItem(
                                selected = selectedBottomTab == 1,
                                onClick = { selectedBottomTab = 1 },
                                label = {
                                    Text(
                                        text = if (userRole == "BUYER") "Purchases" else "Fulfillments",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (userRole == "BUYER") Icons.Rounded.ReceiptLong else Icons.Rounded.LocalShipping,
                                        contentDescription = "Orders Tab"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (userRole == "BUYER") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    selectedTextColor = if (userRole == "BUYER") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("bottom_tab_orders")
                            )

                            NavigationBarItem(
                                selected = selectedBottomTab == 2,
                                onClick = { selectedBottomTab = 2 },
                                label = { Text("Benchmarks", fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.TrendingUp,
                                        contentDescription = "Benchmarks Tab"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (userRole == "BUYER") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    selectedTextColor = if (userRole == "BUYER") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("bottom_tab_benchmarks")
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AnimatedContent(
                            targetState = Pair(userRole, selectedBottomTab),
                            transitionSpec = {
                                slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut()
                            },
                            label = "screen_navigation_content"
                        ) { (role, tab) ->
                            when {
                                tab == 2 -> {
                                    AnalyticsDashboard()
                                }
                                role == "BUYER" -> {
                                    when (tab) {
                                        0 -> MarketplaceTab(viewModel = viewModel)
                                        1 -> BuyerPurchasesTab(viewModel = viewModel)
                                    }
                                }
                                role == "FARMER" -> {
                                    when (tab) {
                                        0 -> FarmerInventoryTab(viewModel = viewModel)
                                        1 -> FarmerOrdersTab(viewModel = viewModel)
                                    }
                                }
                            }
                        }

                        // Simulated Secure Payment Dialog clears
                        PaymentProcessingDialog(
                            isPlaying = isProcessingPayment,
                            stageMessage = paymentStageMessage
                        )

                        // Blockchain printed receipt popup
                        if (paymentSuccessReceipt != null) {
                            com.example.ui.components.DigitalReceiptDialog(
                                order = paymentSuccessReceipt,
                                onDismiss = { viewModel.clearReceipt() }
                            )
                        }
                    }
                }
            }
        }
    }
}
