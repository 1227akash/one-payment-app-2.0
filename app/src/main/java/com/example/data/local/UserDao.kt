package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.UserProfile
import com.example.model.UserSession
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Query("SELECT * FROM user_sessions ORDER BY isCurrentDevice DESC, lastActiveTimestamp DESC")
    fun getSessionsFlow(): Flow<List<UserSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<UserSession>)

    @Query("DELETE FROM user_sessions WHERE isCurrentDevice = 0")
    suspend fun logoutAllOtherDevices()

    @Query("DELETE FROM user_sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()
}
