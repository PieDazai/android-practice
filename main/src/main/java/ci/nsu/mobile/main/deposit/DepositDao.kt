package ci.nsu.mobile.main.deposit

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositDao {

    @Insert
    suspend fun insert(deposit: DepositCalculation)

    @Query("""
        SELECT * FROM deposit_calculations
        WHERE userId = :userId
        ORDER BY calculationDate DESC
    """)
    fun getUserDeposits(userId: Long): Flow<List<DepositCalculation>>

    @Delete
    suspend fun delete(deposit: DepositCalculation)
}