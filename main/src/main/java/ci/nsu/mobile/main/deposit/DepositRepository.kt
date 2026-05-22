package ci.nsu.mobile.main.deposit

class DepositRepository(
    private val dao: DepositDao
) {

    suspend fun insert(deposit: DepositCalculation) {
        dao.insert(deposit)
    }

    fun getUserDeposits(userId: Long) =
        dao.getUserDeposits(userId)

    suspend fun delete(deposit: DepositCalculation) {
        dao.delete(deposit)
    }
}