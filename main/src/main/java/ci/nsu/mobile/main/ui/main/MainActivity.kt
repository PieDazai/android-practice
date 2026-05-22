package ci.nsu.mobile.main.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ci.nsu.mobile.main.data.GroupDto
import ci.nsu.mobile.main.data.LoginUiState
import ci.nsu.mobile.main.data.LoginViewModel
import ci.nsu.mobile.main.data.RegisterUiState
import ci.nsu.mobile.main.data.RegisterViewModel
import ci.nsu.mobile.main.data.UserDto
import ci.nsu.mobile.main.data.UsersUiState
import ci.nsu.mobile.main.data.UsersViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ci.nsu.mobile.main.data.AuthRepository
import ci.nsu.mobile.main.data.TokenManager
import ci.nsu.mobile.main.deposit.BottomScreen
import ci.nsu.mobile.main.deposit.DepositViewModel
import ci.nsu.mobile.main.deposit.ServiceLocator

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRepository = AuthRepository(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {

    val context = LocalContext.current

    // Service Locator создаётся ОДИН раз
    val serviceLocator = remember {
        ServiceLocator(context)
    }

    var currentScreen by remember {
        mutableStateOf<Screen>(Screen.Login)
    }

    var restartKey by remember { mutableStateOf(0) }

    val loginViewModel: LoginViewModel = viewModel(
        key = "login_$restartKey"
    ) {
        LoginViewModel(serviceLocator.authRepository)
    }

    val registerViewModel: RegisterViewModel = viewModel(
        key = "register_$restartKey"
    ) {
        RegisterViewModel(serviceLocator.authRepository)
    }

    val usersViewModel: UsersViewModel = viewModel(
        key = "users_$restartKey"
    ) {
        UsersViewModel(serviceLocator.authRepository)
    }

    val depositViewModel: DepositViewModel = viewModel(
        key = "deposit_$restartKey"
    ) {
        DepositViewModel(
            serviceLocator.depositRepository,
            serviceLocator.tokenManager
        )
    }

    when (currentScreen) {

        Screen.Login -> {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    currentScreen = Screen.Main
                },
                onNavigateToRegister = {
                    currentScreen = Screen.Register
                }
            )
        }

        Screen.Register -> {
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    currentScreen = Screen.Login
                },
                onBackToLogin = {
                    currentScreen = Screen.Login
                }
            )
        }

        Screen.Main -> {
            MainScreen(
                usersViewModel = usersViewModel,
                depositViewModel = depositViewModel,
                onLogout = {
                    serviceLocator.tokenManager.clearToken()
                    restartKey++
                    currentScreen = Screen.Login
                }
            )
        }

        Screen.Users -> {
            UsersScreen(
                viewModel = usersViewModel
            )
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Users : Screen()
    object Main : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Вход в систему",
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(32.dp))

                TextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text("Логин") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = uiState !is LoginUiState.Loading
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.login(login, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading && login.isNotBlank() && password.isNotBlank()
                ) {
                    if (uiState is LoginUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Войти")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onNavigateToRegister
                ) {
                    Text("Нет аккаунта? Зарегистрироваться")
                }

                if (uiState is LoginUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (uiState as LoginUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf(0) }
    var selectedGroupName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var groups by remember { mutableStateOf(listOf<GroupDto>()) }
    var expanded by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.GroupsLoaded) {
            groups = (uiState as RegisterUiState.GroupsLoaded).groups
            if (groups.isNotEmpty() && selectedGroupId == 0) {
                selectedGroupId = groups[0].id
                selectedGroupName = groups[0].name
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Регистрация",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Фамилия") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = middleName,
            onValueChange = { middleName = it },
            label = { Text("Отчество") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = birthDate,
            onValueChange = { birthDate = it },
            label = { Text("Дата рождения (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            enabled = uiState !is RegisterUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Пол") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (groups.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedGroupName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Группа") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = { expanded = !expanded }
                        ) {
                            Icon(
                                if (expanded)
                                    Icons.Default.ArrowDropDown
                                else
                                    Icons.Default.ArrowDropDown,
                                contentDescription = if (expanded) "Свернуть" else "Развернуть"
                            )
                        }
                    },
                    enabled = uiState !is RegisterUiState.Loading
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                selectedGroupId = group.id
                                selectedGroupName = group.name
                                expanded = false
                            }
                        )
                    }
                }
            }
        } else if (uiState is RegisterUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = uiState !is RegisterUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = uiState !is RegisterUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Телефон") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = uiState !is RegisterUiState.Loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.register(
                    firstName, lastName, middleName, birthDate,
                    gender, selectedGroupId, login, password, email, phoneNumber
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading &&
                    firstName.isNotBlank() && lastName.isNotBlank() &&
                    login.isNotBlank() && password.isNotBlank() &&
                    email.isNotBlank() && selectedGroupId != 0
        ) {
            if (uiState is RegisterUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Зарегистрироваться")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Назад к входу")
        }

        if (uiState is RegisterUiState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (uiState as RegisterUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: UsersViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UsersUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UsersUiState.Success -> {
                    if (state.users.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Список пользователей пуст",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadUsers() }) {
                                Text("Обновить")
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.users) { user ->
                                UserCard(user = user)
                            }
                        }
                    }
                }
                is UsersUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadUsers() }) {
                            Text("Повторить")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun UserCard(user: UserDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = user.login,
                fontSize = 18.sp,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Email: ${user.email}",
                fontSize = 14.sp
            )
            Text(
                text = "Телефон: ${user.phoneNumber}",
                fontSize = 14.sp
            )
            user.person?.let { person ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Имя: ${person.lastName} ${person.firstName} ${person.middleName}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Дата рождения: ${person.birthDate}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Пол: ${person.gender}",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    usersViewModel: UsersViewModel,
    depositViewModel: DepositViewModel,
    onLogout: () -> Unit
) {

    var currentTab by remember {
        mutableStateOf<BottomScreen>(
            BottomScreen.Users
        )
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Расчёт вкладов")
                },

                actions = {

                    TextButton(
                        onClick = {
                            onLogout()
                        }
                    ) {

                        Text("Выйти")
                    }
                }
            )
        },

        bottomBar = {

            NavigationBar {

                NavigationBarItem(
                    selected = currentTab is BottomScreen.Users,
                    onClick = {
                        currentTab = BottomScreen.Users
                    },
                    icon = {},
                    label = {
                        Text("Пользователи")
                    }
                )

                NavigationBarItem(
                    selected = currentTab is BottomScreen.Deposits,
                    onClick = {
                        currentTab = BottomScreen.Deposits
                    },
                    icon = {},
                    label = {
                        Text("Мои расчёты")
                    }
                )

                NavigationBarItem(
                    selected = currentTab is BottomScreen.NewDeposit,
                    onClick = {
                        currentTab = BottomScreen.NewDeposit
                    },
                    icon = {},
                    label = {
                        Text("Новый расчёт")
                    }
                )
            }
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when (currentTab) {

                BottomScreen.Users -> {

                    UsersScreen(
                        viewModel = usersViewModel
                    )
                }

                BottomScreen.Deposits -> {

                    DepositHistoryScreen(
                        viewModel = depositViewModel
                    )
                }

                BottomScreen.NewDeposit -> {

                    NewDepositScreen(
                        viewModel = depositViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun DepositHistoryScreen(
    viewModel: DepositViewModel
) {

    val list by viewModel.history.collectAsState()

    if (list.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text("История расчётов пуста")
        }

    } else {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(list) { deposit ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text("Сумма: ${deposit.finalAmount}")

                        Text("Проценты: ${deposit.interestEarned}")

                        Text("Срок: ${deposit.periodMonths}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.delete(deposit)
                            }
                        ) {

                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewDepositScreen(
    viewModel: DepositViewModel
) {

    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TextField(
            value = state.initialAmount,
            onValueChange = viewModel::updateInitial,
            label = {
                Text("Начальная сумма")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.period,
            onValueChange = viewModel::updatePeriod,
            label = {
                Text("Срок")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.monthly,
            onValueChange = viewModel::updateMonthly,
            label = {
                Text("Пополнение")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.calculate()
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Рассчитать")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Итоговая сумма: ${state.finalAmount}")

        Text("Проценты: ${state.interest}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.save()
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Сохранить")
        }
    }
}