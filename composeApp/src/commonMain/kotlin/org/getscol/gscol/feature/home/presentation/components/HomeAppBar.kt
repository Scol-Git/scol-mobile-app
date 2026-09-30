package org.getscol.gscol.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.getscol.gscol.feature.home.presentation.HomeAction
import org.getscol.gscol.navigation.Navigator
import org.getscol.gscol.navigation.Route
import org.getscol.gscol.navigation.TopLevelDestination
import org.getscol.gscol.theme.appColors
import org.jetbrains.compose.resources.painterResource
import scol.composeapp.generated.resources.Res
import scol.composeapp.generated.resources.route
import scol.composeapp.generated.resources.scol_text_logo_2

@Composable
fun HomeAppBar(
    navigator: Navigator,
    action: (HomeAction) -> Unit,
    isUserFillupAcademicForm: Boolean,
    isUserLogin: Boolean
) {
    val colors = appColors()
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(color = colors.customSecondaryContainer)
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.scol_text_logo_2),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.width(50.dp).padding(bottom = 2.dp)
                )
                HomeSearchBar(
                    modifier = Modifier.weight(1f),
                    onClick = { navigator.navigateToRoute(Route.Search) }
                )

                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = colors.customPrimary,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                ) {
                    IconButton(onClick = { navigator.navigateTo(Route.FavouriteRoute) }) {
                        AsyncImage(
                            model = Res.getUri("files/ic_fav.svg"),
                            contentDescription = "Document fav",
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(Color(0xFF0D171B)),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = colors.customPrimary,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                ) {
                    IconButton(onClick = { navigator.navigateToTopLevel(TopLevelDestination.APPLICATION) }) {
                        Icon(
                            painterResource(Res.drawable.route),
                            contentDescription = "Trace"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            EligibilityButton(navigator, isUserFillupAcademicForm, isUserLogin)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
