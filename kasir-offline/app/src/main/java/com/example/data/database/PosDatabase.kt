package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CategoryDao
import com.example.data.dao.CustomerDao
import com.example.data.dao.PriceHistoryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.ShopSettingsDao
import com.example.data.dao.StockMovementDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserAccountDao
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.PriceHistory
import com.example.data.model.Product
import com.example.data.model.ShopSettings
import com.example.data.model.StockMovement
import com.example.data.model.TransactionEntity
import com.example.data.model.UserAccount

@Database(
    entities = [
        Product::class,
        Category::class,
        StockMovement::class,
        PriceHistory::class,
        TransactionEntity::class,
        Customer::class,
        UserAccount::class,
        ShopSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun customerDao(): CustomerDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun shopSettingsDao(): ShopSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: PosDatabase? = null

        fun getDatabase(context: Context): PosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PosDatabase::class.java,
                    "pos_kasir_offline.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
