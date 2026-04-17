package ci.nsu.mobile.main.ui

enum class Screen {
    MAIN, STEP1, STEP2, RESULT, HISTORY
}

data class UiState(
    val screen: Screen = Screen.MAIN,

    val initialAmount: String = "",
    val period: String = "",
    val monthly: String = "",

    val finalAmount: Double = 0.0,
    val interest: Double = 0.0,

    val error: String? = null
)