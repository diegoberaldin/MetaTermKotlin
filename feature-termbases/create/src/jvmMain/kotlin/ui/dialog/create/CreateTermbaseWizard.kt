package ui.dialog.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch
import localized
import moe.tlaster.precompose.PreComposeWindow
import ui.dialog.create.stepone.CreateTermbaseWizardStepOne
import ui.dialog.create.stepone.CreateTermbaseWizardStepOneViewModel
import ui.dialog.create.stepthree.CreateTermbaseWizardStepThree
import ui.dialog.create.stepthree.CreateTermbaseWizardStepThreeViewModel
import ui.dialog.create.steptwo.CreateTermbaseWizardStepTwo
import ui.dialog.create.steptwo.CreateTermbaseWizardStepTwoViewModel
import ui.theme.MetaTermTheme
import ui.theme.Spacing

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CreateTermbaseWizardDialog(
    openNewlyCreated: Boolean = false,
    viewModel: CreateTermbaseViewModel,
    stepOneViewModel: CreateTermbaseWizardStepOneViewModel,
    stepTwoViewModel: CreateTermbaseWizardStepTwoViewModel,
    stepThreeViewModel: CreateTermbaseWizardStepThreeViewModel,
    onClose: () -> Unit,
) {
    MetaTermTheme {
        PreComposeWindow(
            title = "dialog_title_create_termbase".localized(),
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            resizable = false,
            onCloseRequest = {
                onClose()
            },
        ) {
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(openNewlyCreated) {
                viewModel.setOpenNewlyCreated(openNewlyCreated)
            }

            LaunchedEffect(uiState.done) {
                if (uiState.done) {
                    onClose()
                }
            }

            LaunchedEffect(stepOneViewModel) {
                launch {
                    stepOneViewModel.done.collect { result ->
                        viewModel.setTermbase(result.first)
                        viewModel.setSelectedLanguages(result.second)
                        viewModel.next()
                    }
                }
            }
            LaunchedEffect(stepTwoViewModel) {
                launch {
                    stepTwoViewModel.done.collect { result ->
                        viewModel.setProperties(result)
                        viewModel.next()
                    }
                }
            }
            LaunchedEffect(stepThreeViewModel) {
                launch {
                    stepThreeViewModel.done.collect { result ->
                        viewModel.setInputModelDescriptors(result)
                        viewModel.submit()
                    }
                }
            }

            Box(
                modifier = Modifier.size(800.dp, 600.dp)
                    .background(MaterialTheme.colors.background),
            ) {
                Column {
                    AnimatedContent(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        targetState = uiState.step,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        },
                    ) {
                        when (uiState.step) {
                            0 -> CreateTermbaseWizardStepOne(
                                modifier = Modifier.fillMaxSize(),
                                viewModel = stepOneViewModel,
                            )

                            1 -> {
                                CreateTermbaseWizardStepTwo(
                                    modifier = Modifier.fillMaxSize(),
                                    viewModel = stepTwoViewModel,
                                )
                            }

                            else -> {
                                LaunchedEffect(uiState.step) {
                                    val properties = viewModel.getProperties()
                                    val languages = viewModel.getLanguages()
                                    val descriptors = viewModel.getInputModelDescriptors()
                                    stepThreeViewModel.loadItems(
                                        properties = properties,
                                        languages = languages,
                                        oldInputModel = descriptors,
                                    )
                                }
                                CreateTermbaseWizardStepThree(
                                    modifier = Modifier.fillMaxSize(),
                                    viewModel = stepThreeViewModel,
                                )
                            }
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
                                    viewModel.previous()
                                },
                            ) {
                                Text(text = "button_prev".localized(), style = MaterialTheme.typography.button)
                            }
                        }
                        if (uiState.step < CreateTermbaseViewModel.STEPS - 1) {
                            Button(
                                modifier = Modifier.heightIn(max = 25.dp),
                                contentPadding = PaddingValues(0.dp),
                                onClick = {
                                    when (uiState.step) {
                                        0 -> stepOneViewModel.submit()
                                        1 -> stepTwoViewModel.submit()
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
                                    stepThreeViewModel.submit()
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
