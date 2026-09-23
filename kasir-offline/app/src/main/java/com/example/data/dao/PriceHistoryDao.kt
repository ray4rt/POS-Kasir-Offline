package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PriceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Query("SELECT * FROM price_history ORDER BY timestamp DESC")
    fun getAllPriceHistory(): Flow<List<PriceHistory>>

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp DESC")
    fun getPriceHistoryForProduct(productId: Long): Flow<List<PriceHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(historyList: List<PriceHistory>)

    @Query("DELETE FROM price_history")
    suspend fun deleteAll()
}
