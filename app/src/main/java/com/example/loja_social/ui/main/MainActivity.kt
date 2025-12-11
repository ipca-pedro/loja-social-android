
package com.example.loja_social.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.loja_social.SessionManager
import com.example.loja_social.api.RetrofitHelper
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.repository.*
import com.example.loja_social.ui.beneficiarios.*
import com.example.loja_social.ui.dashboard.DashboardScreen
import com.example.loja_social.ui.dashboard.DashboardViewModelFactory
import com.example.loja_social.ui.entregas.*
import com.example.loja_social.ui.login.LoginActivity
import com.example.loja_social.ui.stock.*
import com.example.loja_social.ui.theme.LojaSocialTheme
import com.example.loja_social.ui.beneficiario.BeneficiarioMainViewModel
import com.example.loja_social.ui.beneficiario.BeneficiarioMainViewModelFactory
import android.util.Log

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Entregas : Screen("entregas?filter={filter}", "Entregas", Icons.Default.List) {
        fun createRoute(filter: String? = null) = if (filter != null) "entregas?filter=$filter" else "entregas"
    }
    object Beneficiarios : Screen("beneficiarios", "Beneficiários", Icons.Default.Person)
    object Stock : Screen("stock?filter={filter}", "Stock", Icons.Default.ShoppingCart) {
        fun createRoute(filter: String? = null) = if (filter != null) "stock?filter=$filter" else "stock"
    }
    object Logout : Screen("logout", "Logout", Icons.Default.ExitToApp)

    // Rotas sem ícone na barra
    object BeneficiarioDetail : Screen("beneficiarioDetail?beneficiarioId={beneficiarioId}&title={title}", "Detalhes", Icons.Default.Person) {
        fun createRoute(beneficiarioId: String?, title: String) = "beneficiarioDetail?beneficiarioId=$beneficiarioId&title=$title"
    }
    object AddStock : Screen("addStock", "Adicionar Stock", Icons.Default.Add)
    object StockDetail : Screen("stockDetail/{produtoId}/{produtoNome}", "Detalhes do Stock", Icons.Default.ShoppingCart) {
        fun createRoute(produtoId: Int, produtoNome: String) = "stockDetail/$produtoId/$produtoNome"
    }
    object AgendarEntrega : Screen("agendarEntrega", "Agendar Entrega", Icons.Default.Add)
}

val bottomNavItems = listOf(Screen.Dashboard, Screen.Entregas, Screen.Beneficiarios, Screen.Stock, Screen.Logout)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(applicationContext)
        val token = sessionManager.fetchAuthToken()
        val role = sessionManager.fetchUserRole()
        
        Log.d("MainActivity", "Token: ${token?.take(20)}..., Role: $role")
        Log.d("MainActivity", "isAdmin: ${sessionManager.isAdmin()}, isBeneficiario: ${sessionManager.isBeneficiario()}")
        
        // Verificar se o utilizador está autenticado
        if (token == null) {
            Log.d("MainActivity", "Sem token, redirecionando para LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            LojaSocialTheme {
                // Verificar role do utilizador e mostrar interface apropriada
                Log.d("MainActivity", "Verificando role para navegação...")
                val userRole = sessionManager.fetchUserRole()
                Log.d("MainActivity", "Role atual: '$userRole'")
                Log.d("MainActivity", "isAdmin(): ${sessionManager.isAdmin()}")
                Log.d("MainActivity", "isBeneficiario(): ${sessionManager.isBeneficiario()}")
                
                when (userRole) {
                    "admin" -> {
                        Log.d("MainActivity", "Utilizador é admin, mostrando MainAppScreen")
                        MainAppScreen()
                    }
                    "beneficiario" -> {
                        Log.d("MainActivity", "Utilizador é beneficiário, mostrando BeneficiarioAppScreen")
                        BeneficiarioAppScreen()
                    }
                    null -> {
                        Log.w("MainActivity", "Role é null, assumindo admin por defeito")
                        MainAppScreen() // Assumir admin se role for null
                    }
                    else -> {
                        Log.w("MainActivity", "Role desconhecido: '$userRole', assumindo admin")
                        MainAppScreen() // Assumir admin para roles desconhecidos
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
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (screen.route == Screen.Logout.route) {
                                    showLogoutDialog = true
                                } else {
                                    val route = when (screen) {
                                        Screen.Entregas -> Screen.Entregas.createRoute()
                                        Screen.Stock -> Screen.Stock.createRoute()
                                        else -> screen.route
                                    }
                                    navController.navigate(route) { 
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
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
        
        // Garantir que Retrofit está inicializado
        LaunchedEffect(Unit) {
            if (!RetrofitInstance.isInitialized()) {
                RetrofitHelper.ensureInitialized(context.applicationContext)
            }
        }
        
        val apiService = try {
            RetrofitInstance.api
        } catch (e: UninitializedPropertyAccessException) {
            RetrofitHelper.ensureInitialized(context.applicationContext)
            RetrofitInstance.api
        }
        
        NavHost(navController = navController, startDestination = Screen.Dashboard.route, modifier = modifier) {
            composable(Screen.Dashboard.route) {
                val viewModel: com.example.loja_social.ui.dashboard.DashboardViewModel = viewModel(
                    factory = DashboardViewModelFactory(DashboardRepository(apiService))
                )
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAlerts = { navController.navigate(Screen.Stock.createRoute("alerts")) },
                    onNavigateToEntregas = { navController.navigate(Screen.Entregas.createRoute("today")) }
                )
            }
            
            composable(
                route = Screen.Entregas.route,
                arguments = listOf(
                    navArgument("filter") { nullable = true; type = NavType.StringType }
                )
            ) { backStackEntry ->
                val filter = backStackEntry.arguments?.getString("filter")
                val viewModel: EntregasViewModel = viewModel(
                    factory = EntregasViewModelFactory(EntregaRepository(apiService))
                )
                
                // Apply filter if coming from dashboard
                LaunchedEffect(filter) {
                    if (filter == "today") {
                        viewModel.filterByToday()
                    }
                }
                
                EntregasScreen(
                    viewModel = viewModel,
                    onAgendarClick = { navController.navigate(Screen.AgendarEntrega.route) }
                )
            }
            
            composable(Screen.AgendarEntrega.route) {
                val viewModel: AgendarEntregaViewModel = viewModel(
                    factory = AgendarEntregaViewModelFactory(
                        AgendarEntregaRepository(apiService),
                        StockRepository(apiService)
                    )
                )
                val sessionManager = SessionManager(context)
                AgendarEntregaScreen(
                    viewModel = viewModel,
                    onScheduleClick = { dataAgendamento ->
                        val colaboradorId = sessionManager.fetchColaboradorId()
                        if (colaboradorId != null) {
                            viewModel.agendarEntrega(colaboradorId, dataAgendamento)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Fluxo de Beneficiários
            composable(Screen.Beneficiarios.route) {
                val viewModel: BeneficiariosViewModel = viewModel(
                    factory = BeneficiariosViewModelFactory(BeneficiarioRepository(apiService))
                )
                BeneficiariosScreen(viewModel) { beneficiarioId ->
                    val title = if(beneficiarioId == null) "Novo Beneficiário" else "Editar Beneficiário"
                    navController.navigate(Screen.BeneficiarioDetail.createRoute(beneficiarioId, title))
                }
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
                val viewModel: BeneficiarioDetailViewModel = viewModel(
                    factory = BeneficiarioDetailViewModelFactory(
                        BeneficiarioRepository(apiService),
                        beneficiarioId
                    )
                )
                BeneficiarioDetailScreen(
                    viewModel = viewModel,
                    title = title,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Fluxo de Stock
            composable(
                route = Screen.Stock.route,
                arguments = listOf(
                    navArgument("filter") { nullable = true; type = NavType.StringType }
                )
            ) { backStackEntry ->
                val filter = backStackEntry.arguments?.getString("filter")
                val viewModel: StockListViewModel = viewModel(
                    factory = StockListViewModelFactory(StockRepository(apiService))
                )
                
                // Apply filter if coming from dashboard
                LaunchedEffect(filter) {
                    if (filter == "alerts") {
                        viewModel.setFilterType("validade_proxima")
                    }
                }
                
                StockListScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { navController.navigate(Screen.StockDetail.createRoute(it.produtoId, it.produto)) },
                    onNavigateToAdd = { navController.navigate(Screen.AddStock.route) }
                )
            }
            
            composable(Screen.AddStock.route) {
                val viewModel: StockViewModel = viewModel(
                    factory = StockViewModelFactory(StockRepository(apiService))
                )
                StockScreen(viewModel)
            }
            
            composable(
                route = Screen.StockDetail.route,
                arguments = listOf(
                    navArgument("produtoId") { type = NavType.IntType },
                    navArgument("produtoNome") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val produtoId = backStackEntry.arguments?.getInt("produtoId") ?: 0
                val viewModel: StockDetailViewModel = viewModel(
                    factory = StockDetailViewModelFactory(
                        StockRepository(apiService),
                        produtoId
                    )
                )
                StockDetailScreen(viewModel)
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
            dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } }
        )
    }

    @Composable
    fun BeneficiarioAppScreen() {
        val context = LocalContext.current
        
        // Garantir que Retrofit está inicializado
        LaunchedEffect(Unit) {
            if (!RetrofitInstance.isInitialized()) {
                RetrofitHelper.ensureInitialized(context.applicationContext)
            }
        }
        
        val apiService = try {
            RetrofitInstance.api
        } catch (e: UninitializedPropertyAccessException) {
            RetrofitHelper.ensureInitialized(context.applicationContext)
            RetrofitInstance.api
        }
        
        val viewModel: BeneficiarioMainViewModel = viewModel(
            factory = BeneficiarioMainViewModelFactory(apiService)
        )
        
        com.example.loja_social.ui.beneficiario.BeneficiarioMainScreen(
            viewModel = viewModel,
            onLogoutClick = { performLogout() }
        )
    }
    
    private fun performLogout() {
        SessionManager(applicationContext).clearAuthToken()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}