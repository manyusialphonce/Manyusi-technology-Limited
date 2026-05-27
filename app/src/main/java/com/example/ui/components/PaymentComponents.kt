package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.OrderEntity

@Composable
fun PaymentProcessingDialog(
    isPlaying: Boolean,
    stageMessage: String
) {
    if (isPlaying) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("payment_processing_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .drawBehind {
                                drawArc(
                                    color = Color(0xFF2E7D32),
                                    startAngle = rotation,
                                    sweepAngle = 280f,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 6.dp.toPx(),
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "🔒 Secure Handshake",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "SECURE LEDGER CHECKOUT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stageMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun DigitalReceiptDialog(
    order: OrderEntity?,
    onDismiss: () -> Unit
) {
    if (order != null) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("receipt_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top header badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SECURE CO-OP CLEARANCE SUCCESS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    // Paper Receipt representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .drawBehind {
                                // Draw horizontal dotted separators
                                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                drawLine(
                                    color = Color.LightGray,
                                    start = Offset(0f, size.height * 0.15f),
                                    end = Offset(size.width, size.height * 0.15f),
                                    pathEffect = pathEffect,
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = Color.LightGray,
                                    start = Offset(0f, size.height * 0.82f),
                                    end = Offset(size.width, size.height * 0.82f),
                                    pathEffect = pathEffect,
                                    strokeWidth = 2f
                                )
                            }
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "AGRICONNECT LEDGER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.DarkGray,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.testTag("receipt_brand_header")
                            )
                            Text(
                                text = "BLOCKCHAIN TRANS TRANSACTION RECORD",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Details
                            ReceiptRow(label = "BILL TO:", value = order.buyerName)
                            ReceiptRow(label = "MERCHANT:", value = order.farmerName)
                            ReceiptRow(label = "OFFERING:", value = order.cropName)
                            ReceiptRow(label = "VOLUME:", value = "${order.quantityOrdered} units")
                            ReceiptRow(label = "SETTLEMENT:", value = order.paymentMethod)
                            ReceiptRow(
                                label = "SETTLED AMOUNT:",
                                value = "$${String.format("%.2f", order.totalAmount)}",
                                isBold = true,
                                valueColor = Color(0xFF2E7D32)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "LOCK STATUS: SECURED & PERSISTED",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Cryptographic Proofs
                            Text(
                                text = "RECEIPT NO: ${order.receiptNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "SHA256 REF: ${order.transactionRef}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1
                            )
                            Text(
                                text = "CLEARANCE DATE: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(order.timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dismiss_receipt_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DISMISS RECORD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End
        )
    }
}
