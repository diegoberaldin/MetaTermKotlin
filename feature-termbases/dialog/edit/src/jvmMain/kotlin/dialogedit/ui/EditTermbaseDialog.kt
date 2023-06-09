package dialogedit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import data.TermbaseModel
import kotlinx.coroutines.launch
import localized
import common.ui.components.CustomTabBar
import dialogcreate.stepone.ui.CreateTermbaseWizardStepOne
import dialogcreate.stepthree.ui.CreateTermbaseWizardStepThree
import dialogcreate.steptwo.ui.CreateTermbaseWizardStepTwo
import common.ui.theme.MetaTermTheme
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing
import dialogcreate.stepone.ui.CreateTermbaseWizardStepOneComponent
import dialogcreate.stepthree.ui.CreateTermbaseWizardStepThreeComponent
import dialogcreate.steptwo.ui.CreateTermbaseWizardStepTwoComponent

@Composable
fun EditTermbaseDialog(
    component: EditTermbaseComponent,
    initialTermbase: TermbaseModel,
    onClose: () -> Unit,
) {
    val content by component.content.subscribeAsState()

    MetaTermTheme {
        Window(
            title = "dialog_title_edit_termbase".localized(),
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            resizable = false,
            onCloseRequest = {
                onClose()
            },
        ) {
            LaunchedEffect(initialTermbase) {
                component.setTermbase(initialTermbase)
            }

            val uiState by component.uiState.collectAsState()

            Box(
                modifier = Modifier.size(800.dp, 600.dp)
                    .background(MaterialTheme.colors.background),
            ) {
                Column {
                    CustomTabBar(
                        modifier = Modifier.fillMaxWidth(),
                        tabs = listOf(
                            "dialog_edit_termbase_tab_1".localized(),
                            "dialog_edit_termbase_tab_2".localized(),
                            "dialog_edit_termbase_tab_3".localized(),
                        ),
                        current = uiState.step,
                        onTabSelected = {
                            component.changeStep(it)
                        },
                    )

                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().background(SelectedBackground),
                    ) {
                        when (content.child?.configuration) {
                            EditTermbaseComponent.ContentConfig.Step1 -> {
                                val childComponent = content.child?.instance as CreateTermbaseWizardStepOneComponent
                                LaunchedEffect(initialTermbase) {
                                    childComponent.loadInitial(initialTermbase)

                                    launch {
                                        childComponent.done.collect { (termbase, languages) ->
                                            val name = termbase.name
                                            val description = termbase.description
                                            component.submitStep1(
                                                name = name,
                                                description = description,
                                                selectedLanguages = languages,
                                            )
                                            onClose()
                                        }
                                    }
                                }
                                CreateTermbaseWizardStepOne(
                                    component = childComponent,
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                )

                                Spacer(modifier = Modifier.height(Spacing.xs))
                                ButtonArea(
                                    onSubmit = {
                                        childComponent.submit()
                                    },
                                    onClose = onClose,
                                )
                            }

                            EditTermbaseComponent.ContentConfig.Step2 -> {
                                val childComponent =
                                    content.child?.instance as CreateTermbaseWizardStepTwoComponent
                                LaunchedEffect(initialTermbase) {
                                    childComponent.loadInitial(initialTermbase)

                                    launch {
                                        childComponent.done.collect { properties ->
                                            component.submitStep2(properties)
                                            onClose()
                                        }
                                    }
                                }
                                CreateTermbaseWizardStepTwo(
                                    component = childComponent,
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                )

                                Spacer(modifier = Modifier.height(Spacing.xs))
                                ButtonArea(
                                    onSubmit = {
                                        childComponent.submit()
                                    },
                                    onClose = onClose,
                                )
                            }

                            EditTermbaseComponent.ContentConfig.Step3 -> {
                                val childComponent =
                                    content.child?.instance as CreateTermbaseWizardStepThreeComponent
                                LaunchedEffect(initialTermbase) {
                                    childComponent.loadInitial(initialTermbase)

                                    launch {
                                        childComponent.done.collect { result ->
                                            component.submitStep3(result)
                                            onClose()
                                        }
                                    }
                                }
                                CreateTermbaseWizardStepThree(
                                    component = childComponent,
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                )

                                Spacer(modifier = Modifier.height(Spacing.xs))
                                ButtonArea(
                                    onSubmit = {
                                        childComponent.submit()
                                    },
                                    onClose = onClose,
                                )
                            }

                            else -> Unit
                        }
                    }
                }

                if (uiState.loading) {
                    Surface(
                        modifier = Modifier.matchParentSize(),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonArea(
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(Spacing.s),
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
                text = "button_cancel".localized(),
                style = MaterialTheme.typography.button,
            )
        }
        Button(
            modifier = Modifier.heightIn(max = 25.dp),
            contentPadding = PaddingValues(0.dp),
            onClick = {
                onSubmit()
            },
        ) {
            Text(
                text = "button_ok".localized(),
                style = MaterialTheme.typography.button,
            )
        }
    }
}
