package org.getscol.gscol.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.NavController
import org.getscol.gscol.core.data.session.Session
import org.koin.compose.koinInject
import kotlin.reflect.KClass

/*
   ---------
   In future we will adopt nav3 if nav3 is available for cmp/kmp
   BTW IT WORKS :)
   ---------
*/


class Navigator(
    private val navController: NavController,
    val session: Session,
    private val onAuthRequired: (String) -> Unit = {}
) {

    private val authRoute = mutableListOf<Route>()
    private val startDestinationRoute = Route.HomeRoute

    private val protectedRoutes: Map<KClass<out Route>, String> = mapOf(
        Route.Profile::class to "Please login to access your profile",
        Route.ApplicationList::class to "Please login to view applications",
        Route.FavouriteRoute::class to "Login to view favorite courses",
        Route.Consultant::class to "Please login to contact consultants",
    )

    private fun guardedNavigate(route: KClass<out Route>, navigationBlock: () -> Unit) {
        val message = protectedRoutes[route]
        if (message != null && !session.isUserLoggedIn.value) {
            onAuthRequired(message)
        } else {
            navigationBlock()
        }
    }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        val route = destination.route as? Route ?: return
        guardedNavigate(route::class) {
            navController.navigate(destination.route) {
                popUpTo(Route.HomeRoute) {
                    saveState = true
                    inclusive = false
                }
                // Avoid multiple copies
                launchSingleTop = true

                // Restore state when reselection
                restoreState = true

            }
        }
    }

    // From now try to use this method if possible
    fun navigateTo(route: Route, popUpToStartDestinationRoute: Boolean = false) {
        guardedNavigate(route::class) {
            navController.navigate(route) {
                if (popUpToStartDestinationRoute) {
                    popUpTo(startDestinationRoute) { inclusive = false }
                    // Avoid multiple copies
                    launchSingleTop = true

                    // Restore state when reselection
                    restoreState = true
                }
            }
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    /** Push a route onto the back stack (e.g. Search from Home). */
    fun navigateToRoute(route: Route) {
        navController.navigate(route)
    }

    // will be removed soon
    fun navigateToAuthScreen(loginRoute: Route) {
        authRoute.add(loginRoute)
        navController.navigate(loginRoute)
    }

    // will be removed soon
    fun navigateAuthScreenBack(route: Route){
        authRoute.remove(route)
        navController.popBackStack()
    }

    fun navigateTo(action: NavigationAction) {
        when (action) {
            NavigationAction.NavigateToLogInScreen -> navigateToAuthScreen(Route.Login)
        }
    }
}

@Composable
fun rememberNavigator(
    navController: NavController,
    onAuthRequired: (String) -> Unit = {}
): Navigator {
    val session = koinInject<Session>()
    val updatedOnAuthRequired by rememberUpdatedState(onAuthRequired)
    return remember(navController, session) {
        Navigator(navController, session, onAuthRequired = { msg ->
            updatedOnAuthRequired(msg)
        })
    }
}