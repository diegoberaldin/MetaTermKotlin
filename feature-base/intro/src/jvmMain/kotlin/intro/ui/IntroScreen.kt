package intro.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Token
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import localized
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(
    component: IntroComponent,
) {
    SideEffect {
        component.load()
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(Spacing.m)
            .padding(horizontal = Spacing.xxl),
    ) {
        Spacer(Modifier.height(Spacing.m))

        Text(
            text = "app_intro_title".localized(),
            style = MaterialTheme.typography.h3,
            color = Color.White
        )
        Spacer(Modifier.height(Spacing.m))

        val uiState by component.uiState.collectAsState()
        if (uiState.termbases.isEmpty()) {
            Text(
                text = "app_intro_empty".localized(),
                style = MaterialTheme.typography.h5.copy(lineHeight = 32.sp),
                color = Color.White
            )
        } else {
            Text(
                text = "app_intro".localized(),
                style = MaterialTheme.typography.h5,
                color = Color.White
            )
            Spacer(Modifier.height(Spacing.l))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                items(uiState.termbases) { termbase ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = SelectedBackground, shape = RoundedCornerShape(4.dp))
                            .padding(Spacing.m).onClick {
                                component.openTermbase(termbase)
                            },
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Token,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = termbase.name,
                            style = MaterialTheme.typography.body1,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}