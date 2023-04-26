package ui.dialog.edit

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
import data.TermbaseModel
import kotlinx.coroutines.launch
import localized
import moe.tlaster.precompose.PreComposeWindow
import ui.dialog.create.stepone.CreateTermbaseWizardStepOne
import ui.dialog.create.stepone.CreateTermbaseWizardStepOneViewModel
import ui.dialog.create.stepthree.CreateTermbaseWizardStepThree
import ui.dialog.create.stepthree.CreateTermbaseWizardStepThreeViewModel
import ui.dialog.create.steptwo.CreateTermbaseWizardStepTwo
import ui.dialog.create.steptwo.CreateTermbaseWizardStepTwoViewModel
import ui.components.CustomTabBar
import ui.theme.MetaTermTheme
import ui.theme.SelectedBackground
import ui.theme.Spacing

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EditTermbaseDialog(
    initialTermbase: TermbaseModel,
    viewModel: EditTermbaseViewModel,
    stepOneViewModel: CreateTermbaseWizardStepOneViewModel,
    stepTwoViewModel: CreateTermbaseWizardStepTwoViewModel,
    stepThreeViewModel: CreateTermbaseWizardStepThreeViewModel,
    onClose: () -> Unit,
) {
    MetaTermTheme {
        PreComposeWindow(
            title = "dialog_title_edit_termbase".localized(),
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            resizable = false,
            onCloseRequest = {
                onClose()
            },
        ) {
            LaunchedEffect(initialTermbase) {
                viewModel.setTermbase(initialTermbase)
                stepOneViewModel.loadInitial(initialTermbase)
                stepTwoViewModel.loadInitial(initialTermbase)
                stepThreeViewModel.loadInitial(initialTermbase)

                launch {
                    stepOneViewModel.done.collect { (termbase, languages) ->
                        val name = termbase.name
                        val description = termbase.description
                        viewModel.submitStep1(
                            name = name,
                            description = description,
                            selectedLanguages = languages,
                        )
                        onClose()
                    }
                }

                launch {
                    stepTwoViewModel.done.collect { properties ->
                        viewModel.submitStep2(properties)
                        onClose()
                    }
                }

                launch {
                    stepThreeViewModel.done.collect { result ->
                        viewModel.submitStep3(result)
                        onClose()
                    }
                }
            }

            val uiState by viewModel.uiState.collectAsState()

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
                            viewModel.changeStep(it)
                        },
                    )
                    AnimatedContent(
                        modifier = Modifier.weight(1f).fillMaxWidth().background(SelectedBackground),
                        targetState = uiState.step,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        },
                    ) {
                        Column {
                            when (uiState.step) {
                                0 -> {
                                    CreateTermbaseWizardStepOne(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        viewModel = stepOneViewModel,
                                    )

                                    Spacer(modifier = Modifier.height(Spacing.xs))
                                    ButtonArea(
                                        onSubmit = {
                                            stepOneViewModel.submit()
                                        },
                                        onClose = onClose,
                                    )
                                }

                                1 -> {
                                    CreateTermbaseWizardStepTwo(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        viewModel = stepTwoViewModel,
                                    )

                                    Spacer(modifier = Modifier.height(Spacing.xs))
                                    ButtonArea(
                                        onSubmit = {
                                            stepTwoViewModel.submit()
                                        },
                                        onClose = onClose,
                                    )
                                }

                                else -> {
                                    CreateTermbaseWizardStepThree(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        viewModel = stepThreeViewModel,
                                    )

                                    Spacer(modifier = Modifier.height(Spacing.xs))
                                    ButtonArea(
                                        onSubmit = {
                                            stepThreeViewModel.submit()
                                        },
                                        onClose = onClose,
                                    )
                                }
                            }
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
