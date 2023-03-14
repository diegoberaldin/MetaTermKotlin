import kotlinx.coroutines.flow.*
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class TermsViewModel : ViewModel() {

    private val terms = MutableStateFlow(listOf<String>())
    private val selectedTerm = MutableStateFlow<String?>(null)

    val uiState = combine(terms, selectedTerm) { terms, selected ->
        TermsUiState(
            terms = terms,
            selectedTerm = selected
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = TermsUiState())

    init {
        for (i in 1..100) {
            terms.update {
                it + "Term $i"
            }
        }
    }

    fun setSelected(value: String?) {
        selectedTerm.update { value }
    }
}

data class TermsUiState(
    val terms: List<String> = listOf(),
    val selectedTerm: String? = null,
)