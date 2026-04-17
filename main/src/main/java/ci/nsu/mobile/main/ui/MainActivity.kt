package ci.nsu.mobile.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import java.util.Date
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = ViewModelProvider(this)[DepositViewModel::class.java]

        setContent {
            val state by vm.state.collectAsState()

            when (state.screen) {
                Screen.MAIN -> MainScreen(vm)
                Screen.STEP1 -> Step1Screen(vm, state)
                Screen.STEP2 -> Step2Screen(vm, state)
                Screen.RESULT -> ResultScreen(vm, state)
                Screen.HISTORY -> HistoryScreen(vm)
            }
        }
    }
}

@Composable
fun MainScreen(vm: DepositViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {

        Text("Расчёт вкладов")

        Button(onClick = { vm.navigate(Screen.STEP1) }) {
            Text("Рассчитать")
        }

        Button(onClick = { vm.navigate(Screen.HISTORY) }) {
            Text("История")
        }

        Button(onClick = { exitProcess(0) }) {
            Text("Выход")
        }
    }
}

@Composable
fun Step1Screen(vm: DepositViewModel, state: UiState) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        TextField(
            value = state.initialAmount,
            onValueChange = vm::updateInitial,
            label = { Text("Стартовый взнос") }
        )

        TextField(
            value = state.period,
            onValueChange = vm::updatePeriod,
            label = { Text("Срок вклада в месяцах") }
        )

        Button(onClick = { vm.navigate(Screen.MAIN) }) {
            Text("В начало")
        }

        Button(onClick = { vm.navigate(Screen.STEP2) }) {
            Text("Далее")
        }
    }
}

@Composable
fun Step2Screen(vm: DepositViewModel, state: UiState) {


    val months = state.period.toIntOrNull() ?: 0
    val rate = vm.getRate(months)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {

        Text("Процентная ставка")

        Text("${rate * 100}%")

        TextField(
            value = state.monthly,
            onValueChange = vm::updateMonthly,
            label = { Text("Пополнение") }
        )

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Row {
            Button(onClick = { vm.navigate(Screen.STEP1) }) {
                Text("Назад")
            }

            Button(onClick = { vm.calculateResult() }) {
                Text("Рассчитать")
            }
        }
    }
}

@Composable
fun ResultScreen(vm: DepositViewModel, state: UiState) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {

        Box {
            Column(Modifier.padding(16.dp)) {

                Text("Стартовый взнос: ${state.initialAmount}")
                Text("Срок: ${state.period} мес")

                val rate = vm.getRate(state.period.toIntOrNull() ?: 0)
                Text("Ставка: ${rate * 100}%")

                Text("Пополнение: ${state.monthly.ifBlank { "0" }}")
                Text("Итоговая сумма: ${state.finalAmount}")
                Text("Начисленные проценты: ${state.interest}")
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            vm.save()
            vm.navigate(Screen.MAIN)
        }) {
            Text("Сохранить")
        }

        Button(onClick = { vm.navigate(Screen.MAIN) }) {
            Text("В начало")
        }
    }
}

@Composable
fun HistoryScreen(vm: DepositViewModel) {

    val list by vm.history.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {

        Button(onClick = { vm.navigate(Screen.MAIN) }) {
            Text("Назад")
        }

        Spacer(modifier = Modifier.height(8.dp))

        list.forEach {
            Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Сумма: ${it.finalAmount}")
                    Text("Дата: ${Date(it.calculationDate)}")
                }
            }
        }
    }
}