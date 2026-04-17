package ci.nsu.mobile.main.ui.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositDao {

    @Insert
    suspend fun insert(deposit: DepositCalculation)

    @Query("SELECT * FROM deposit_calculations")
    fun getAll(): Flow<List<DepositCalculation>>
}