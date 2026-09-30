package org.getscol.gscol

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import org.getscol.gscol.bottombar.ScolBottomBar
import org.getscol.gscol.core.presentation.components.LoginPromptBottomSheet
import org.getscol.gscol.navigation.NavigationAction
import org.getscol.gscol.navigation.Route
import org.getscol.gscol.navigation.ScolNavHost
import org.getscol.gscol.navigation.TopLevelDestination
import org.getscol.gscol.navigation.rememberNavigator
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun ScolApp(
    currentLogoutEvent: NavigationAction? = null,
    onLogoutHandler: () -> Unit = {}
) {
    val navController = rememberNavController()

    var authMessage by remember { mutableStateOf<String?>(null) }
    val navigator = rememberNavigator(navController) { msg ->
        authMessage = msg
    }

    authMessage?.let { message ->
        LoginPromptBottomSheet(
            message = message,
            onDismiss = { authMessage = null },
            onLoginClick = {
                authMessage = null
                navigator.navigateTo(Route.Login)
            }
        )
    }

    // Get the current showing screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    //Determine if bottom bar should show
    val topLevelRoutes = TopLevelDestination.entries.map { it.route::class.qualifiedName }
    val shouldShowBottomBar = topLevelRoutes.contains(
        currentRoute?.substringBefore("?")
    )

    LaunchedEffect(currentLogoutEvent){
        currentLogoutEvent?.let { navigator.navigateTo(Route.HomeRoute, true) }
        onLogoutHandler()
    }

    val isOnline by NetworkStatus.isAvailable.debounce(1000L.milliseconds).collectAsState(initial = true)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar) {
                    ScolBottomBar(
                        destinations = TopLevelDestination.entries,
                        currentRoute = currentRoute,
                        onNavigateDestination = { destinations ->
                            navigator.navigateToTopLevel(destinations)
                        },
                    )
                }
            }
        ) { paddingValues ->
            ScolNavHost(
                navController = navController,
                navigator = navigator,
                modifier = Modifier.padding(paddingValues)
            )
        }


        AnimatedVisibility(
            visible = !isOnline,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) { NoInternetScreen() }
    }

}