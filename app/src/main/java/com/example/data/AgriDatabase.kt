package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgriDao {
    // Crop queries
    @Query("SELECT * FROM crops ORDER BY dateAdded DESC")
    fun getAllCrops(): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE farmerName = :farmerName ORDER BY dateAdded DESC")
    fun getCropsByFarmer(farmerName: String): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE id = :cropId")
    suspend fun getCropById(cropId: Int): CropEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity): Long

    @Update
    suspend fun updateCrop(crop: CropEntity)

    @Delete
    suspend fun deleteCrop(crop: CropEntity)

    @Query("UPDATE crops SET quantity = :quantity WHERE id = :cropId")
    suspend fun updateCropQuantity(cropId: Int, quantity: Double)

    // Order queries
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE farmerName = :farmerName ORDER BY timestamp DESC")
    fun getOrdersForFarmer(farmerName: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE buyerName = :buyerName ORDER BY timestamp DESC")
    fun getOrdersForBuyer(buyerName: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET orderStatus = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)
}

@Database(entities = [CropEntity::class, OrderEntity::class], version = 1, exportSchema = false)
abstract class AgriDatabase : RoomDatabase() {
    abstract fun agriDao(): AgriDao
}
