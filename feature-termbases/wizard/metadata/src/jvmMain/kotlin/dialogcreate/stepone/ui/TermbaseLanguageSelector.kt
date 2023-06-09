package dialogcreate.stepone.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.LanguageModel
import common.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TermbaseLanguageSelector(
    modifier: Modifier = Modifier,
    availableLanguages: List<LanguageModel> = emptyList(),
    currentAvailableLanguage: LanguageModel? = null,
    selectedLanguages: List<LanguageModel> = emptyList(),
    currentSelectedLanguage: LanguageModel? = null,
    onAvailableLanguageClicked: (LanguageModel) -> Unit,
    onSelectedLanguageClicked: (LanguageModel) -> Unit,
    onArrowLeft: () -> Unit,
    onArrowRight: () -> Unit,
) {
    Row(
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
                .fillMaxHeight()
                .background(color = Color.White, shape = RoundedCornerShape(4.dp)),
        ) {
            items(availableLanguages) {
                LanguageItem(it, it == currentAvailableLanguage) {
                    onAvailableLanguageClicked(it)
                }
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = Spacing.xxs),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.size(24.dp).padding(2.dp).onClick { onArrowRight() },
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
            )
            Icon(
                modifier = Modifier.size(24.dp).padding(2.dp).onClick { onArrowLeft() },
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White,
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        LazyColumn(
            modifier = Modifier.weight(1f)
                .fillMaxHeight()
                .background(color = Color.White, shape = RoundedCornerShape(4.dp)),
        ) {
            items(selectedLanguages) {
                LanguageItem(it, it == currentSelectedLanguage) {
                    onSelectedLanguageClicked(it)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LanguageItem(
    it: LanguageModel,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .background(color = if (selected) Color.Blue else Color.Transparent)
            .padding(vertical = Spacing.xxs, horizontal = Spacing.xs)
            .fillMaxWidth()
            .onClick {
                onClick()
            },
        text = it.name,
        style = MaterialTheme.typography.body2,
        color = if (selected) Color.White else Color.Black,
    )
}
