package com.example.data

import kotlinx.coroutines.flow.Flow

class AgriRepository(private val agriDao: AgriDao) {

    // Crop flow & suspending operations
    val allCrops: Flow<List<CropEntity>> = agriDao.getAllCrops()

    fun getCropsByFarmer(farmerName: String): Flow<List<CropEntity>> = 
        agriDao.getCropsByFarmer(farmerName)

    suspend fun getCropById(cropId: Int): CropEntity? = 
        agriDao.getCropById(cropId)

    suspend fun insertCrop(crop: CropEntity): Long = 
        agriDao.insertCrop(crop)

    suspend fun updateCrop(crop: CropEntity) = 
        agriDao.updateCrop(crop)

    suspend fun deleteCrop(crop: CropEntity) = 
        agriDao.deleteCrop(crop)

    suspend fun updateCropQuantity(cropId: Int, quantity: Double) = 
        agriDao.updateCropQuantity(cropId, quantity)

    // Order flow & suspending operations
    val allOrders: Flow<List<OrderEntity>> = agriDao.getAllOrders()

    fun getOrdersForFarmer(farmerName: String): Flow<List<OrderEntity>> = 
        agriDao.getOrdersForFarmer(farmerName)

    fun getOrdersForBuyer(buyerName: String): Flow<List<OrderEntity>> = 
        agriDao.getOrdersForBuyer(buyerName)

    suspend fun insertOrder(order: OrderEntity): Long = 
        agriDao.insertOrder(order)

    suspend fun updateOrderStatus(orderId: Int, status: String) = 
        agriDao.updateOrderStatus(orderId, status)
}
