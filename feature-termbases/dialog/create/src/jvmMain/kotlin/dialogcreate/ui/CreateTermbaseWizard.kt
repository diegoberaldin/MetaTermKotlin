package dialogcreate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import common.ui.theme.MetaTermTheme
import common.ui.theme.Spacing
import dialogcreate.stepone.ui.CreateTermbaseWizardStepOne
import dialogcreate.stepone.ui.CreateTermbaseWizardStepOneComponent
import dialogcreate.stepthree.ui.CreateTermbaseWizardStepThree
import dialogcreate.stepthree.ui.CreateTermbaseWizardStepThreeComponent
import dialogcreate.steptwo.ui.CreateTermbaseWizardStepTwo
import dialogcreate.steptwo.ui.CreateTermbaseWizardStepTwoComponent
import kotlinx.coroutines.launch
import localized

@Composable
fun CreateTermbaseWizardDialog(
    component: CreateTermbaseComponent,
    openNewlyCreated: Boolean = false,
    onClose: () -> Unit,
) {
    val content by component.content.subscribeAsState()
    MetaTermTheme {
        Window(
            title = "dialog_title_create_termbase".localized(),
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            resizable = false,
            onCloseRequest = {
                onClose()
            },
        ) {
            val uiState by component.uiState.collectAsState()

            LaunchedEffect(openNewlyCreated) {
                component.setOpenNewlyCreated(openNewlyCreated)
            }

            LaunchedEffect(uiState.done) {
                if (uiState.done) {
                    onClose()
                }
            }

            Box(
                modifier = Modifier.size(800.dp, 600.dp)
                    .background(MaterialTheme.colors.background),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        when (content.child?.configuration) {
                            CreateTermbaseComponent.ContentConfig.Step1 -> {
                                val childComponent = content.child?.instance as CreateTermbaseWizardStepOneComponent
                                LaunchedEffect(childComponent) {
                                    launch {
                                        childComponent.done.collect { result ->
                                            component.setTermbase(result.first)
                                            component.setSelectedLanguages(result.second)
                                            component.next()
                                        }
                                    }
                                }
                                CreateTermbaseWizardStepOne(
                                    component = childComponent,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            CreateTermbaseComponent.ContentConfig.Step2 -> {
                                val childComponent = content.child?.instance as CreateTermbaseWizardStepTwoComponent
                                LaunchedEffect(childComponent) {
                                    launch {
                                        childComponent.done.collect { result ->
                                            component.setProperties(result)
                                            component.next()
                                        }
                                    }
                                }
                                CreateTermbaseWizardStepTwo(
                                    component = childComponent,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            CreateTermbaseComponent.ContentConfig.Step3 -> {
                                val childComponent = content.child?.instance as CreateTermbaseWizardStepThreeComponent
                                LaunchedEffect(childComponent) {
                                    launch {
                                        childComponent.done.collect { result ->
                                            component.setInputModelDescriptors(result)
                                            component.submit()
                                        }
                                    }
                                }
                                LaunchedEffect(uiState.step) {
                                    val properties = component.getProperties()
                                    val languages = component.getLanguages()
                                    val descriptors = component.getInputModelDescriptors()
                                    childComponent.loadItems(
                                        properties = properties,
                                        languages = languages,
                                        oldInputModel = descriptors,
                                    )
                                }
                                CreateTermbaseWizardStepThree(
                                    component = childComponent,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            else -> Unit
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.s))
                    Row(
                        modifier = Modifier.padding(Spacing.s),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        if (uiState.step == 0) {
                            Button(
                                modifier = Modifier.heightIn(max = 25.dp),
                                contentPadding = PaddingValues(0.dp),
                                onClick = {
                                    onClose()
                                },
                            ) {
                                Text(text = "button_cancel".localized(), style = MaterialTheme.typography.button)
                            }
                        } else {
                            Button(
                                modifier = Modifier.heightIn(max = 25.dp),
                                contentPadding = PaddingValues(0.dp),
                                onClick = {
                                    component.previous()
                                },
                            ) {
                                Text(text = "button_prev".localized(), style = MaterialTheme.typography.button)
                            }
                        }
                        if (uiState.step < DefaultCreateTermbaseComponent.STEPS - 1) {
                            Button(
                                modifier = Modifier.heightIn(max = 25.dp),
                                contentPadding = PaddingValues(0.dp),
                                onClick = {
                                    when (uiState.step) {
                                        0 -> {
                                            (content.child?.instance as? CreateTermbaseWizardStepOneComponent)?.submit()
                                        }

                                        1 -> {
                                            (content.child?.instance as? CreateTermbaseWizardStepTwoComponent)?.submit()
                                        }
                                    }
                                },
                            ) {
                                Text(text = "button_next".localized(), style = MaterialTheme.typography.button)
                            }
                        } else {
                            Button(
                                modifier = Modifier.heightIn(max = 25.dp),
                                contentPadding = PaddingValues(0.dp),
                                onClick = {
                                    (content.child?.instance as? CreateTermbaseWizardStepThreeComponent)?.submit()
                                },
                            ) {
                                Text(text = "button_ok".localized(), style = MaterialTheme.typography.button)
                            }
                        }
                    }
                }

                if (uiState.loading) {
                    Surface(
                        modifier = Modifier.matchParentSize(),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.1f)),
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
