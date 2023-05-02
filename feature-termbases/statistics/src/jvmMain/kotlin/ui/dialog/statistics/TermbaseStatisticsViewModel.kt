package ui.dialog.statistics

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import data.TermbaseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import localized
import repository.EntryRepository
import repository.FlagsRepository
import repository.LanguageNameRepository
import repository.LanguageRepository
import repository.TermRepository
import coroutines.CoroutineDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class TermbaseStatisticsViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val entryRepository: EntryRepository,
    private val languageRepository: LanguageRepository,
    private val languageNameRepository: LanguageNameRepository,
    private val flagsRepository: FlagsRepository,
    private val termRepository: TermRepository,
) : InstanceKeeper.Instance {

    private val items = MutableStateFlow<List<TermbaseStatisticsItem>>(emptyList())
    private val loading = MutableStateFlow(false)
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(items, loading) { items, loading ->
        TermbaseStatisticsUiState(
            items = items,
            loading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TermbaseStatisticsUiState(),
    )

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun load(termbase: TermbaseModel) {
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
