package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val farmerName: String,
    val cropName: String,
    val category: String, // "Vegetables", "Fruits", "Grains", "Tubers", "Legumes", "Other"
    val quantity: Double, // Live stock available
    val unit: String, // "kg", "bags", "crates", "tons"
    val pricePerUnit: Double, // Price in USD
    val description: String,
    val location: String,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val buyerName: String,
    val farmerName: String,
    val cropId: Int,
    val cropName: String,
    val quantityOrdered: Double,
    val totalAmount: Double,
    val paymentMethod: String, // "Mobile Money", "Credit Card", "Bank Transfer"
    val orderStatus: String, // "Pending Payment", "Processing", "Shipped", "Delivered"
    val receiptNumber: String, // Agri-XXXXX mock unique ID
    val transactionRef: String, // mock pay transaction hash
    val timestamp: Long = System.currentTimeMillis()
)
