package dialogstatistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import data.TermbaseModel
import localized
import common.ui.components.CustomProgressIndicator
import common.ui.theme.MetaTermTheme
import common.ui.theme.Spacing

@Composable
fun TermbaseStatisticsDialog(
    component: TermbaseStatisticsComponent,
    termbase: TermbaseModel,
    onClose: () -> Unit,
) {
    MetaTermTheme {
        Window(
            title = "dialog_title_termbase_statistics".localized(),
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            resizable = false,
            onCloseRequest = {
                onClose()
            },
        ) {
            LaunchedEffect(termbase) {
                component.load(termbase)
            }

            val uiState by component.uiState.collectAsState()

            Column(
                modifier = Modifier.size(600.dp, 400.dp)
                    .background(MaterialTheme.colors.background),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(Spacing.m).weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    items(uiState.items) { item ->
                        when (item) {
                            is TermbaseStatisticsItem.Divider -> {
                                Divider(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.s))
                            }

                            is TermbaseStatisticsItem.Header -> {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onBackground,
                                )
                            }

                            is TermbaseStatisticsItem.LanguageHeader -> {
                                Text(
                                    modifier = Modifier.padding(top = Spacing.s),
                                    text = item.name,
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onBackground,
                                )
                            }

                            is TermbaseStatisticsItem.TextRow -> {
                                Row {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onBackground,
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = item.value,
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onBackground,
                                    )
                                }
                            }

                            is TermbaseStatisticsItem.BarChartRow -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onBackground,
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.s))
                                    CustomProgressIndicator(
                                        modifier = Modifier.weight(1f).height(20.dp),
                                        progress = item.value,
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(Spacing.s),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        modifier = Modifier.heightIn(max = 25.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            onClose()
                        },
                    ) {
                        Text(
                            text = "button_close".localized(),
                            style = MaterialTheme.typography.button,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
