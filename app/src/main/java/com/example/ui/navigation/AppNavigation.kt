package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MainViewModel
import com.example.ui.screens.active.ActiveJobScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.screens.availability.AvailabilityScreen
import com.example.ui.screens.bank.BankDetailsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.earnings.EarningsScreen
import com.example.ui.screens.jobs.JobDetailScreen
import com.example.ui.screens.jobs.JobsScreen
import com.example.ui.screens.kyc.KycScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.servicearea.ServiceAreaScreen
import com.example.ui.screens.support.NewTicketScreen
import com.example.ui.screens.support.SupportScreen
import com.example.ui.theme.BrandAmberAccent
import com.example.ui.theme.BrandBluePrimary

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")

    // Main tabs
    object Dashboard : Screen("dashboard")
    object Jobs : Screen("jobs")
    object Earnings : Screen("earnings")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")

    // Sub-screens
    object ActiveJob : Screen("active_job/{jobId}") {
        fun createRoute(jobId: String) = "active_job/$jobId"
    }
    object JobDetail : Screen("job_detail/{jobId}") {
        fun createRoute(jobId: String) = "job_detail/$jobId"
    }
    object Kyc : Screen("kyc")
    object Availability : Screen("availability")
    object ServiceAreas : Screen("service_areas")
    object BankDetails : Screen("bank_details")
    object Support : Screen("support")
    object NewTicket : Screen("new_ticket")
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadCount = notifications.count { !it.isRead }

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("Home", Screen.Dashboard.route, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Jobs", Screen.Jobs.route, Icons.Filled.Work, Icons.Outlined.WorkOutline),
        BottomNavItem("Earnings", Screen.Earnings.route, Icons.Filled.Payments, Icons.Outlined.Payments),
        BottomNavItem("Alerts", Screen.Notifications.route, Icons.Filled.Notifications, Icons.Outlined.Notifications, badgeCount = unreadCount),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Jobs.route,
        Screen.Earnings.route,
        Screen.Notifications.route,
        Screen.Profile.route
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                if (item.badgeCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = BrandAmberAccent) {
                                                Text("${item.badgeCount}", color = Color.Black)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                }
                            },
                            label = { Text(item.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandBluePrimary,
                                selectedTextColor = BrandBluePrimary,
                                indicatorColor = BrandBluePrimary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${item.label.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { fullName, phone, email, city, category ->
                        navController.navigate(Screen.Onboarding.route)
                    },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onOnboardingComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToActiveJob = { jobId -> navController.navigate(Screen.ActiveJob.createRoute(jobId)) },
                    onNavigateToJobDetail = { jobId -> navController.navigate(Screen.JobDetail.createRoute(jobId)) },
                    onNavigateToEarnings = { navController.navigate(Screen.Earnings.route) },
                    onNavigateToKyc = { navController.navigate(Screen.Kyc.route) },
                    onNavigateToAvailability = { navController.navigate(Screen.Availability.route) },
                    onNavigateToServiceAreas = { navController.navigate(Screen.ServiceAreas.route) },
                    onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
                )
            }

            composable(Screen.Jobs.route) {
                JobsScreen(
                    viewModel = viewModel,
                    onNavigateToActiveJob = { jobId -> navController.navigate(Screen.ActiveJob.createRoute(jobId)) },
                    onNavigateToJobDetail = { jobId -> navController.navigate(Screen.JobDetail.createRoute(jobId)) }
                )
            }

            composable(Screen.Earnings.route) {
                EarningsScreen(
                    viewModel = viewModel,
                    onNavigateToBankDetails = { navController.navigate(Screen.BankDetails.route) }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToKyc = { navController.navigate(Screen.Kyc.route) },
                    onNavigateToAvailability = { navController.navigate(Screen.Availability.route) },
                    onNavigateToServiceArea = { navController.navigate(Screen.ServiceAreas.route) },
                    onNavigateToBank = { navController.navigate(Screen.BankDetails.route) },
                    onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                    onLogout = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.ActiveJob.route,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                ActiveJobScreen(
                    jobId = jobId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToSupport = { navController.navigate(Screen.Support.route) }
                )
            }

            composable(
                route = Screen.JobDetail.route,
                arguments = listOf(navArgument("jobId") { type = NavType.StringType })
            ) { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                JobDetailScreen(
                    jobId = jobId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToActiveJob = { jId -> navController.navigate(Screen.ActiveJob.createRoute(jId)) }
                )
            }

            composable(Screen.Kyc.route) {
                KycScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Availability.route) {
                AvailabilityScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ServiceAreas.route) {
                ServiceAreaScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BankDetails.route) {
                BankDetailsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Support.route) {
                SupportScreen(
                    viewModel = viewModel,
                    onNavigateToNewTicket = { navController.navigate(Screen.NewTicket.route) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NewTicket.route) {
                NewTicketScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
