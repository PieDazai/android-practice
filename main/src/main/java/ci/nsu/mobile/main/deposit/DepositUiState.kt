package ci.nsu.mobile.main.deposit

data class DepositUiState(

    val initialAmount: String = "",

    val period: String = "",

    val monthly: String = "",

    val finalAmount: Double = 0.0,

    val interest: Double = 0.0,

    val error: String? = null
)