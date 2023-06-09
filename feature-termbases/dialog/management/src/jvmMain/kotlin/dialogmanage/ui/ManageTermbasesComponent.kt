package dialogmanage.ui

import data.TermbaseModel
import kotlinx.coroutines.flow.StateFlow

interface ManageTermbasesComponent {
    val uiState: StateFlow<ManageTermbasesUiState>
    fun selectTermbase(termbase: TermbaseModel)
    fun openCurrentTermbase()
    fun deleteCurrentTermbase()
}