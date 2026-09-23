package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    suspend fun getAllUsersDirect(): List<UserAccount>

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserAccount>)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Delete
    suspend fun deleteUser(user: UserAccount)

    @Query("DELETE FROM user_accounts")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserCount(): Int
}
