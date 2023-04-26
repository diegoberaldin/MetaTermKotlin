package ui.dialog.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import data.TermbaseModel
import localized
import moe.tlaster.precompose.PreComposeWindow
import ui.theme.MetaTermTheme
import ui.theme.Spacing

@Composable
fun ManageTermbasesDialog(
    viewModel: ManageTermbasesViewModel,
    onNew: () -> Unit,
    onClose: () -> Unit,
    onEdit: (TermbaseModel) -> Unit,
) {
    MetaTermTheme {
        PreComposeWindow(
            title = "dialog_title_manage_termbases".localized(),
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            resizable = false,
            onCloseRequest = {
                onClose()
            },
        ) {
            val uiState by viewModel.uiState.collectAsState()

            Box(
                modifier = Modifier
                    .size(width = 600.dp, height = 400.dp)
                    .background(MaterialTheme.colors.background),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                ) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "manage_termbase_intro".localized(),
                        style = MaterialTheme.typography.caption,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(Spacing.s))
                    Row(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(2.dp),
                                )
                                .padding(Dp.Hairline),
                        ) {
                            items(uiState.termbases) {
                                TermbaseCell(
                                    name = it.name,
                                    selected = it == uiState.selectedTermbase,
                                    onSelected = {
                                        viewModel.selectTermbase(it)
                                    },
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(0.25f).padding(start = Spacing.s, top = Spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            Button(
                                modifier = Modifier.heightIn(max = 25.dp).fillMaxWidth(),
                                contentPadding = PaddingValues(0.dp),
                                onClick = {
                                    onNew()
                                },
                            ) {
                                Text(text = "menu_termbase_new".localized(), style = MaterialTheme.typography.button)
                            }
                            Button(
                                modifier = Modifier.heightIn(max = 25.dp).fillMaxWidth(),
                                contentPadding = PaddingValues(0.dp),
                                enabled = uiState.selectedTermbase != null,
                                onClick = {
                                    uiState.selectedTermbase?.also {
                                        onEdit(it)
                                    }
                                },
                            ) {
                                Text(text = "menu_termbase_edit".localized(), style = MaterialTheme.typography.button)
                            }
                            Button(
                                modifier = Modifier.heightIn(max = 25.dp).fillMaxWidth(),
                                contentPadding = PaddingValues(0.dp),
                                enabled = uiState.selectedTermbase != null,
                                onClick = {
                                    viewModel.openCurrentTermbase()
                                    onClose()
                                },
                            ) {
                                Text(text = "button_open".localized(), style = MaterialTheme.typography.button)
                            }
                            Button(
                                modifier = Modifier.heightIn(max = 25.dp).fillMaxWidth(),
                                contentPadding = PaddingValues(0.dp),
                                enabled = uiState.selectedTermbase != null,
                                onClick = {
                                    viewModel.deleteCurrentTermbase()
                                },
                            ) {
                                Text(text = "button_delete".localized(), style = MaterialTheme.typography.button)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.heightIn(max = 25.dp),
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                onClose()
                            },
                        ) {
                            Text(text = "button_close".localized(), style = MaterialTheme.typography.button)
                        }
                    }
                }
            }
        }
    }
}
