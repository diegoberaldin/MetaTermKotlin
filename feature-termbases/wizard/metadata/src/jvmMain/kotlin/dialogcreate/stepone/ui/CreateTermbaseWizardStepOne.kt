package dialogcreate.stepone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import localized
import common.ui.components.CustomDialog
import common.ui.components.CustomTextField
import common.ui.theme.Spacing

@Composable
fun CreateTermbaseWizardStepOne(
    component: CreateTermbaseWizardStepOneComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsState()
    val errorState by component.errorUiState.collectAsState()
    val languageState by component.languagesUiState.collectAsState()

    Box(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Spacer(modifier = Modifier.height(Spacing.s))
            CustomTextField(
                modifier = Modifier.height(44.dp),
                label = "create_termbase_step1_field_name".localized(),
                value = uiState.name,
                singleLine = true,
                onValueChange = {
                    component.setName(it)
                },
            )
            if (errorState.nameError.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(horizontal = Spacing.xs),
                    text = errorState.nameError,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.s))
            CustomTextField(
                label = "create_termbase_step1_field_description".localized(),
                modifier = Modifier.height(100.dp),
                value = uiState.description,
                onValueChange = {
                    component.setDescription(it)
                },
            )
            Spacer(modifier = Modifier.height(Spacing.s))
            Text(
                text = "create_termbase_step1_field_languages".localized(),
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            TermbaseLanguageSelector(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                availableLanguages = languageState.availableLanguages,
                currentAvailableLanguage = languageState.currentAvailableLanguage,
                selectedLanguages = languageState.selectedLanguages,
                currentSelectedLanguage = languageState.currentSelectedLanguage,
                onArrowLeft = {
                    component.onArrowLeft()
                },
                onArrowRight = {
                    component.onArrowRight()
                },
                onAvailableLanguageClicked = {
                    component.onAvailableClick(it)
                },
                onSelectedLanguageClicked = {
                    component.onSelectedClick(it)
                },
            )
            if (errorState.languagesError.isNotEmpty()) {
                Text(
                    text = errorState.languagesError,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                )
            }
        }
    }

    if (errorState.genericError.isNotEmpty()) {
        CustomDialog(
            title = "dialog_title_error".localized(),
            message = errorState.genericError,
            closeButtonText = "button_close".localized(),
            onClose = {
                component.clearErrors()
            },
        )
    }
}
