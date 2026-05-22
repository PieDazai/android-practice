package ci.nsu.mobile.main.deposit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ci.nsu.mobile.main.data.TokenManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DepositViewModel(
    private val repository: DepositRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(DepositUiState())

    val state: StateFlow<DepositUiState> = _state

    val history = repository
        .getUserDeposits(tokenManager.userId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )

    fun updateInitial(v: String) {
        _state.update { it.copy(initialAmount = v) }
    }

    fun updatePeriod(v: String) {
        _state.update { it.copy(period = v) }
    }

    fun updateMonthly(v: String) {
        _state.update { it.copy(monthly = v) }
    }

    fun getRate(period: Int): Double {

        return when {

            period < 6 -> 0.15

            period < 12 -> 0.10

            else -> 0.05
        }
    }

    fun calculate() {

        val s = _state.value

        val amount = s.initialAmount.toDoubleOrNull()

        val months = s.period.toIntOrNull()

        if (amount == null || months == null) {

            _state.update {
                it.copy(error = "Ошибка ввода")
            }

            return
        }

        val monthly = s.monthly.toDoubleOrNull() ?: 0.0

        val rate = getRate(months)

        var total: Double = amount

        repeat(months) {

            total += monthly

            total += total * rate / 12
        }

        val interest =
            total - amount - monthly * months

        _state.update {

            it.copy(
                finalAmount = total,
                interest = interest,
                error = null
            )
        }
    }

    fun save() {

        val s = _state.value

        val deposit = DepositCalculation(

            userId = tokenManager.userId,

            initialAmount = s.initialAmount.toDouble(),

            periodMonths = s.period.toInt(),

            interestRate = getRate(s.period.toInt()),

            monthlyTopUp = s.monthly.toDoubleOrNull(),

            finalAmount = s.finalAmount,

            interestEarned = s.interest,

            calculationDate = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.insert(deposit)
        }
    }

    fun delete(deposit: DepositCalculation) {

        viewModelScope.launch {
            repository.delete(deposit)
        }
    }
}