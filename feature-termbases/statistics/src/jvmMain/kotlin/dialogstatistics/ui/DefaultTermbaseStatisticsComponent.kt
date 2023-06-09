package dialogstatistics.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.TermbaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import localized
import repo.*
import repository.*
import kotlin.coroutines.CoroutineContext

internal class DefaultTermbaseStatisticsComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val entryRepository: EntryRepository,
    private val languageRepository: LanguageRepository,
    private val languageNameRepository: LanguageNameRepository,
    private val flagsRepository: FlagsRepository,
    private val termRepository: TermRepository,
) : TermbaseStatisticsComponent, ComponentContext by componentContext {

    private val items = MutableStateFlow<List<TermbaseStatisticsItem>>(emptyList())
    private val loading = MutableStateFlow(false)
    private lateinit var viewModelScope: CoroutineScope

    override lateinit var uiState: StateFlow<TermbaseStatisticsUiState>

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(items, loading) { items, loading ->
                    TermbaseStatisticsUiState(
                        items = items,
                        loading = loading,
                    )
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = TermbaseStatisticsUiState(),
                )
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun load(termbase: TermbaseModel) {
        val termbaseId = termbase.id
        loading.value = true
        viewModelScope.launch(dispatcherProvider.io) {
            val languages = languageRepository.getAll(termbaseId).map {
                val name = languageNameRepository.getName(it.code)
                val flag = flagsRepository.getFlag(it.code)
                it.copy(
                    name = "$flag $name",
                )
            }.sortedBy { it.code }
            items.update {
                buildList {
                    this += TermbaseStatisticsItem.Header("dialog_statistics_section_general".localized())
                    this += TermbaseStatisticsItem.TextRow(
                        title = "dialog_statistics_item_total_languages".localized(),
                        value = languages.count().toString(),
                    )
                    val totalEntryCount = entryRepository.countAll(termbaseId)
                    this += TermbaseStatisticsItem.TextRow(
                        title = "dialog_statistics_item_total_entries".localized(),
                        value = totalEntryCount.toString(),
                    )
                    val totalTermCount = termRepository.countAll(termbaseId)
                    this += TermbaseStatisticsItem.TextRow(
                        title = "dialog_statistics_item_total_terms".localized(),
                        value = totalTermCount.toString(),
                    )

                    this += TermbaseStatisticsItem.Divider
                    this += TermbaseStatisticsItem.Header(title = "dialog_statistics_section_languages".localized())
                    for (language in languages) {
                        this += TermbaseStatisticsItem.LanguageHeader(language.name)

                        val langCode = language.code
                        val termCount = termRepository.countByLanguage(code = langCode, termbaseId = termbaseId)
                        this += TermbaseStatisticsItem.TextRow(
                            title = "dialog_statistics_item_language_terms".localized(),
                            value = termCount.toString(),
                        )

                        val completeCount = entryRepository.countComplete(code = langCode, termbaseId = termbaseId)
                        this += TermbaseStatisticsItem.BarChartRow(
                            title = "dialog_statistics_item_language_completion".localized(),
                            value = completeCount.toFloat() / totalEntryCount,
                        )
                    }
                }
            }
            loading.value = false
        }
    }
}
