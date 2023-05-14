package ui.dialog.create

import androidx.compose.animation.*
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
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.launch
import localized
import org.koin.java.KoinJavaComponent.inject
import ui.dialog.create.stepone.CreateTermbaseWizardStepOne
import ui.dialog.create.stepone.CreateTermbaseWizardStepOneViewModel
import ui.dialog.create.stepthree.CreateTermbaseWizardStepThree
import ui.dialog.create.stepthree.CreateTermbaseWizardStepThreeViewModel
import ui.dialog.create.steptwo.CreateTermbaseWizardStepTwo
import ui.dialog.create.steptwo.CreateTermbaseWizardStepTwoViewModel
import ui.theme.MetaTermTheme
import ui.theme.Spacing
import utils.AppBusiness

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CreateTermbaseWizardDialog(
    openNewlyCreated: Boolean = false,
    onClose: () -> Unit,
) {
    val viewModel: CreateTermbaseViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: CreateTermbaseViewModel by inject(CreateTermbaseViewModel::class.java)
        res
    }
    val stepOneViewModel: CreateTermbaseWizardStepOneViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: CreateTermbaseWizardStepOneViewModel by inject(CreateTermbaseWizardStepOneViewModel::class.java)
        res
    }
    val stepTwoViewModel: CreateTermbaseWizardStepTwoViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: CreateTermbaseWizardStepTwoViewModel by inject(CreateTermbaseWizardStepTwoViewModel::class.java)
        res
    }
    val stepThreeViewModel: CreateTermbaseWizardStepThreeViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: CreateTermbaseWizardStepThreeViewModel by inject(CreateTermbaseWizardStepThreeViewModel::class.java)
        res
    }

    MetaTermTheme {
        Window(
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
                            )

                            1 -> {
                                CreateTermbaseWizardStepTwo(
                                    modifier = Modifier.fillMaxSize(),
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
