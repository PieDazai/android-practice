package ci.nsu.mobile.main.ui.data

class Repository(private val dao: DepositDao) {

    val allDeposits = dao.getAll()

    suspend fun insert(deposit: DepositCalculation) {
        dao.insert(deposit)
    }
}