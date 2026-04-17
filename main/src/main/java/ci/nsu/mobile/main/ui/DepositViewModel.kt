package ci.nsu.mobile.main.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ci.nsu.mobile.main.ui.data.AppDatabase
import ci.nsu.mobile.main.ui.data.DepositCalculation
import ci.nsu.mobile.main.ui.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class DepositViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: Repository

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    val history: StateFlow<List<DepositCalculation>>

    init {
        val dao = AppDatabase.getDatabase(application).depositDao()
        repo = Repository(dao)

        history = repo.allDeposits.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )
    }

    fun navigate(screen: Screen) {
        _state.update { it.copy(screen = screen) }
    }

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

    fun calculateResult() {
        val s = _state.value

        val amount = s.initialAmount.toDoubleOrNull()
        val months = s.period.toIntOrNull()

        if (amount == null || months == null) {
            _state.update { it.copy(error = "Ошибка ввода (пустые поля)") }
            return
        }

        if (amount <= 0 || months <= 0) {
            _state.update { it.copy(error = "Значения должны быть больше 0") }
            return
        }

        val monthly = s.monthly.toDoubleOrNull() ?: 0.0

        if (monthly < 0) {
            _state.update { it.copy(error = "Пополнение не может быть отрицательным") }
            return
        }

        val rate = getRate(months)

        var total: Double = amount

        repeat(months) {
            total += monthly
            total += total * rate / 12
        }

        val interest = total - amount - monthly * months

        _state.update {
            it.copy(
                finalAmount = total,
                interest = interest,
                screen = Screen.RESULT
            )
        }
    }

    fun save() {
        val s = _state.value

        val amount = s.initialAmount.toDoubleOrNull()
        val period = s.period.toIntOrNull()

        if (amount == null || period == null) {
            _state.update { it.copy(error = "Нельзя сохранить пустые данные") }
            return
        }

        val rate = getRate(period)

        val entity = DepositCalculation(
            initialAmount = amount,
            periodMonths = period,
            interestRate = rate,
            monthlyTopUp = s.monthly.toDoubleOrNull(),
            finalAmount = s.finalAmount,
            interestEarned = s.interest,
            calculationDate = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repo.insert(entity)
        }
    }
}