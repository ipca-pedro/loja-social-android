package com.example.loja_social.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.loja_social.SessionManager
import com.example.loja_social.api.RetrofitHelper
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.repository.*
import com.example.loja_social.ui.beneficiario.BeneficiarioMainViewModel
import com.example.loja_social.ui.beneficiario.BeneficiarioMainViewModelFactory
import com.example.loja_social.ui.beneficiarios.*
import com.example.loja_social.ui.dashboard.DashboardScreen
import com.example.loja_social.ui.dashboard.DashboardViewModel
import com.example.loja_social.ui.dashboard.DashboardViewModelFactory
import com.example.loja_social.ui.entregas.*
import com.example.loja_social.ui.login.LoginActivity
import com.example.loja_social.ui.stock.*
import com.example.loja_social.ui.theme.LojaSocialTheme
import com.example.loja_social.ui.campanhas.CampanhasScreen
import com.example.loja_social.ui.campanhas.CampanhasViewModel
import com.example.loja_social.ui.campanhas.CampanhasViewModelFactory
import com.example.loja_social.repository.CampanhaRepository
import android.util.Log

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Entregas : Screen("entregas?filter={filter}", "Entregas", Icons.AutoMirrored.Filled.List) {
        fun createRoute(filter: String? = null) = if (filter != null) "entregas?filter=$filter" else "entregas"
    }
    object Beneficiarios : Screen("beneficiarios", "Beneficiários", Icons.Default.Person)
    object Stock : Screen("stock?filter={filter}", "Stock", Icons.Default.ShoppingCart) {
        fun createRoute(filter: String? = null) = if (filter != null) "stock?filter=$filter" else "stock"
    }

    object Logout : Screen("logout", "Logout", Icons.AutoMirrored.Filled.ExitToApp)

    // Rotas sem ícone na barra
    object BeneficiarioDetail : Screen("beneficiarioDetail?beneficiarioId={beneficiarioId}&title={title}", "Detalhes", Icons.Default.Person) {
        fun createRoute(beneficiarioId: String?, title: String) = "beneficiarioDetail?beneficiarioId=$beneficiarioId&title=$title"
    }
    object AddStock : Screen("addStock", "Adicionar Stock", Icons.Default.Add)
    object StockDetail : Screen("stockDetail/{produtoId}", "Detalhes do Stock", Icons.Default.ShoppingCart) {
        fun createRoute(produtoId: Int) = "stockDetail/$produtoId"
    }
    object Campanhas : Screen("campanhas", "Campanhas", Icons.Default.Event)
    object AgendarEntrega : Screen("agendarEntrega?deliveryId={deliveryId}", "Agendar Entrega", Icons.Default.Add) {
        fun createRoute(deliveryId: String? = null) = if (deliveryId != null) "agendarEntrega?deliveryId=$deliveryId" else "agendarEntrega"
    }
    object EntregaDetail : Screen("entregaDetail/{entregaId}/{estado}", "Detalhes da Entrega", Icons.AutoMirrored.Filled.List) {
        fun createRoute(entregaId: String, estado: String) = "entregaDetail/$entregaId/$estado"
    }
    object Relatorios : Screen("relatorios", "Relatórios", Icons.Default.Description)
    object Notificacoes : Screen("notificacoes", "Notificações", Icons.Default.Notifications)
}

val bottomNavItems = listOf(Screen.Dashboard, Screen.Entregas, Screen.Beneficiarios, Screen.Stock, Screen.Campanhas)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(applicationContext)
        val token = sessionManager.fetchAuthToken()
        val role = sessionManager.fetchUserRole()
        
        if (token == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            LojaSocialTheme {
                when (role) {
                    "admin" -> MainAppScreen()
                    "beneficiario" -> BeneficiarioAppScreen()
                    else -> {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    @Composable
    fun MainAppScreen() {
        val navController = rememberNavController()
        var showLogoutDialog by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { 
                                Text(
                                    screen.label,
                                    softWrap = false,
                                    fontSize = 10.sp
                                )
                            },
                            selected = currentRoute?.startsWith(screen.route.substringBefore("?")) ?: false,
                            onClick = {
                                navController.navigate(screen.route.substringBefore("?")) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            },
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { 
                        AsyncImage(
                            model = "https://lojasocial.duckdns.org/logo-navbar.png",
                            contentDescription = "Loja Social Logo",
                            modifier = Modifier.height(46.dp),
                            contentScale = ContentScale.Fit
                        )
                    },
                    actions = {
                        val notificationRepository = remember { com.example.loja_social.repository.NotificationRepository(com.example.loja_social.api.RetrofitInstance.api) }
                        var unreadCount by remember { mutableStateOf(0) }
                        
                        LaunchedEffect(Unit) {
                            while(true) {
                                 try {
                                     if (com.example.loja_social.api.RetrofitInstance.isInitialized()) {
                                         val response = notificationRepository.getNotificacoes()
                                         if (response.success && response.data != null) {
                                             unreadCount = response.data.count { !it.lida }
                                         }
                                     }
                                 } catch (e: Exception) {
                                     // ignore
                                 }
                                 kotlinx.coroutines.delay(15000) // Poll every 15s
                            }
                        }

                        IconButton(onClick = { navController.navigate(Screen.Relatorios.route) }) {
                            Icon(Icons.Default.Description, contentDescription = "Relatórios")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Notificacoes.route) }) {
                             if (unreadCount > 0) {
                                BadgedBox(badge = { Badge { Text("$unreadCount") } }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notificações")
                                }
                            } else {
                                Icon(Icons.Default.Notifications, contentDescription = "Notificações")
                            }
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                        }
                    }
                )
            }
        ) { innerPadding ->
            AppNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
        }

        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = { 
                    performLogout()
                    showLogoutDialog = false
                },
                onDismiss = { showLogoutDialog = false }
            )
        }
    }

    @Composable
    fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        
        RetrofitHelper.ensureInitialized(context.applicationContext)
        val apiService = RetrofitInstance.api
        
        NavHost(navController = navController, startDestination = Screen.Dashboard.route, modifier = modifier) {
            composable(Screen.Dashboard.route) { backStackEntry ->
                val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("should_refresh_dashboard")
                val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(DashboardRepository(apiService), StockRepository(apiService)))

                LaunchedEffect(shouldRefresh) {
                    if (shouldRefresh == true) {
                        viewModel.fetchDashboardData()
                        backStackEntry.savedStateHandle.remove<Boolean>("should_refresh_dashboard")
                    }
                }

                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAlerts = { alerta ->
                        val productId = viewModel.getProductIdFromAlert(alerta)
                        if (productId != null) {
                            navController.navigate(Screen.StockDetail.createRoute(productId))
                        } else {
                            navController.navigate(Screen.Stock.createRoute("alerts"))
                        }
                    },
                    onNavigateToEntregaDetail = { entregaId, estado ->
                        navController.navigate(Screen.EntregaDetail.createRoute(entregaId, estado))
                    }
                )
            }
            
            composable(
                route = Screen.Entregas.route,
                arguments = listOf(navArgument("filter") { nullable = true; type = NavType.StringType })
            ) { backStackEntry ->
                val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("should_refresh_entregas")
                val filter = backStackEntry.arguments?.getString("filter")
                val returnTab = backStackEntry.savedStateHandle.get<String>("return_tab")
                val viewModel: EntregasViewModel = viewModel(factory = EntregasViewModelFactory(EntregaRepository(apiService)))
                
                LaunchedEffect(shouldRefresh) {
                    if (shouldRefresh == true) {
                        viewModel.fetchEntregas() 
                        backStackEntry.savedStateHandle.remove<Boolean>("should_refresh_entregas")
                    }
                }

                LaunchedEffect(returnTab, filter) {
                    if (returnTab != null) {
                        val tab = if (returnTab == "agendada") EntregaFilterType.AGENDADAS else EntregaFilterType.ENTREGUES
                        viewModel.selectTab(tab)
                        backStackEntry.savedStateHandle.remove<String>("return_tab")
                    } else if (filter == "today") {
                        viewModel.filterByToday()
                    }
                }

                EntregasScreen(
                    viewModel = viewModel,
                    onAgendarClick = { navController.navigate(Screen.AgendarEntrega.createRoute()) },
                    onNavigateBack = {
                        if (viewModel.needsDashboardRefresh()) {
                            navController.getBackStackEntry(Screen.Dashboard.route).savedStateHandle.set("should_refresh_dashboard", true)
                        }
                        navController.popBackStack()
                    },
                    onEntregaClick = { entregaId, estado ->
                        navController.navigate(Screen.EntregaDetail.createRoute(entregaId, estado))
                    }
                )
            }

            composable(
                route = Screen.EntregaDetail.route,
                arguments = listOf(
                    navArgument("entregaId") { type = NavType.StringType },
                    navArgument("estado") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val entregaId = backStackEntry.arguments?.getString("entregaId")
                val estado = backStackEntry.arguments?.getString("estado")
                if (entregaId != null) {
                    val viewModel: EntregaDetailViewModel = viewModel(
                        factory = EntregaDetailViewModelFactory(entregaId, EntregaRepository(apiService))
                    )

                    var wasRefreshed by remember { mutableStateOf(false) }
                    val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("should_refresh_entregas")

                    LaunchedEffect(shouldRefresh) {
                        if (shouldRefresh == true) {
                            viewModel.fetchEntregaDetails()
                            wasRefreshed = true
                            backStackEntry.savedStateHandle.remove<Boolean>("should_refresh_entregas")
                        }
                    }

                    EntregaDetailScreen(
                        viewModel = viewModel,
                        estado = estado ?: "",
                        onNavigateBack = { 
                            if (wasRefreshed) {
                                navController.previousBackStackEntry?.savedStateHandle?.set("should_refresh_entregas", true)
                            }
                            navController.popBackStack() 
                        },
                        onEditClick = {
                             navController.navigate(Screen.AgendarEntrega.createRoute(entregaId))
                        }
                    )
                }
            }
            
            composable(
                route = Screen.AgendarEntrega.route,
                arguments = listOf(navArgument("deliveryId") { nullable = true; type = NavType.StringType })
            ) { backStackEntry ->
                val deliveryId = backStackEntry.arguments?.getString("deliveryId")
                val viewModel: AgendarEntregaViewModel = viewModel(factory = AgendarEntregaViewModelFactory(AgendarEntregaRepository(apiService), StockRepository(apiService)))
                AgendarEntregaScreen(
                    viewModel = viewModel,
                    navController = navController,
                    deliveryId = deliveryId
                )
            }
            
            composable(Screen.Beneficiarios.route) { backStackEntry ->
                val viewModel: BeneficiariosViewModel = viewModel(factory = BeneficiariosViewModelFactory(BeneficiarioRepository(apiService)))
                val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("should_refresh_beneficiarios") ?: false
                
                BeneficiariosScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { beneficiarioId ->
                        val title = if(beneficiarioId == null) "Novo Beneficiário" else "Editar Beneficiário"
                        navController.navigate(Screen.BeneficiarioDetail.createRoute(beneficiarioId, title))
                    },
                    shouldRefresh = shouldRefresh,
                    onRefreshDone = { backStackEntry.savedStateHandle.set("should_refresh_beneficiarios", false) }
                )
            }
            
            composable(
                route = Screen.BeneficiarioDetail.route,
                arguments = listOf(
                    navArgument("beneficiarioId") { nullable = true; type = NavType.StringType },
                    navArgument("title") { defaultValue = "Detalhes"; type = NavType.StringType }
                )
            ) { backStackEntry ->
                val beneficiarioId = backStackEntry.arguments?.getString("beneficiarioId")
                val title = backStackEntry.arguments?.getString("title") ?: "Detalhes"
                val viewModel: BeneficiarioDetailViewModel = viewModel(factory = BeneficiarioDetailViewModelFactory(BeneficiarioRepository(apiService), beneficiarioId))
                
                BeneficiarioDetailScreen(
                    viewModel = viewModel,
                    title = title,
                    onNavigateBack = { 
                        if (viewModel.uiState.value.beneficiaryDataChanged) {
                            navController.previousBackStackEntry?.savedStateHandle?.set("should_refresh_beneficiarios", true)
                        }
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.Stock.route,
                arguments = listOf(navArgument("filter") { nullable = true; type = NavType.StringType })
            ) { backStackEntry ->
                val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("should_refresh_stock")
                val filter = backStackEntry.arguments?.getString("filter")
                val viewModel: StockListViewModel = viewModel(factory = StockListViewModelFactory(StockRepository(apiService)))
                
                LaunchedEffect(shouldRefresh) {
                    if (shouldRefresh == true) {
                        viewModel.fetchStock()
                        backStackEntry.savedStateHandle.remove<Boolean>("should_refresh_stock")
                    }
                }

                LaunchedEffect(filter) {
                    if (filter == "alerts") viewModel.setFilterType("validade_proxima")
                }
                
                StockListScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { navController.navigate(Screen.StockDetail.createRoute(it.produtoId)) },
                    onNavigateToAdd = { navController.navigate(Screen.AddStock.route) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.AddStock.route) {
                val viewModel: StockViewModel = viewModel(factory = StockViewModelFactory(StockRepository(apiService)))
                StockScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (viewModel.uiState.value.stockDataChanged) {
                            navController.getBackStackEntry(Screen.Dashboard.route).savedStateHandle.set("should_refresh_dashboard", true)
                            navController.getBackStackEntry(Screen.Stock.route).savedStateHandle.set("should_refresh_stock", true)
                        }
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.StockDetail.route,
                arguments = listOf(
                    navArgument("produtoId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val produtoId = backStackEntry.arguments?.getInt("produtoId") ?: 0
                val viewModel: StockDetailViewModel = viewModel(factory = StockDetailViewModelFactory(StockRepository(apiService), produtoId))
                StockDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { 
                        if (viewModel.uiState.value.stockDataChanged) {
                            navController.getBackStackEntry(Screen.Dashboard.route).savedStateHandle.set("should_refresh_dashboard", true)
                            navController.getBackStackEntry(Screen.Stock.route).savedStateHandle.set("should_refresh_stock", true)
                            viewModel.onRefreshHandled() // Resetar a flag
                        }
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Campanhas.route) {
                val repository = CampanhaRepository(apiService)
                val viewModel: CampanhasViewModel = viewModel(
                    factory = CampanhasViewModelFactory(repository)
                )
                CampanhasScreen(viewModel = viewModel)
            }

            composable(Screen.Relatorios.route) {
                com.example.loja_social.ui.relatorios.RelatoriosScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Notificacoes.route) {
                 com.example.loja_social.ui.notificacoes.NotificacoesScreen(
                     onNavigateBack = { navController.popBackStack() }
                 )
            }
        }
    }
    
    @Composable
    fun LogoutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirmar Logout") },
            text = { Text("Tem certeza que deseja sair?") },
            confirmButton = { Button(onClick = onConfirm) { Text("Sair") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
        )
    }

    @Composable
    fun BeneficiarioAppScreen() {
        val navController = rememberNavController()
        val apiService = RetrofitInstance.api
        
        NavHost(navController = navController, startDestination = "beneficiarioHome") {
            composable("beneficiarioHome") {
                val viewModel: BeneficiarioMainViewModel = viewModel(factory = BeneficiarioMainViewModelFactory(apiService))
                com.example.loja_social.ui.beneficiario.BeneficiarioMainScreen(
                    viewModel = viewModel,
                    onLogoutClick = { performLogout() },
                    onNavigateToNotifications = { navController.navigate(Screen.Notificacoes.route) }
                )
            }
            
            composable(Screen.Notificacoes.route) {
                com.example.loja_social.ui.notificacoes.NotificacoesScreen(
                    onNavigateBack = { 
                        navController.popBackStack() 
                    }
                )
            }
        }
    }
    
    private fun performLogout() {
        SessionManager(applicationContext).clearAuthToken()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
