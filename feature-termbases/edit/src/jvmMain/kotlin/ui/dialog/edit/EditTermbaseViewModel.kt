package ui.dialog.edit

import data.InputDescriptorModel
import data.LanguageModel
import data.PropertyModel
import data.TermbaseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import notification.NotificationCenter
import repository.InputDescriptorRepository
import repository.LanguageRepository
import repository.PropertyRepository
import repository.TermbaseRepository
import usecase.DeleteTermbaseLanguageUseCase
import coroutines.CoroutineDispatcherProvider

class EditTermbaseViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val languageRepository: LanguageRepository,
    private val propertyRepository: PropertyRepository,
    private val inputDescriptorRepository: InputDescriptorRepository,
    private val deleteTermbaseLanguage: DeleteTermbaseLanguageUseCase,
    private val notificationCenter: NotificationCenter,
) : ViewModel() {

    private var termbase: TermbaseModel? = null
    private val step = MutableStateFlow(0)
    private val loading = MutableStateFlow(false)
    private val done = MutableStateFlow(false)

    val uiState = combine(
        step,
        loading,
        done,
    ) { step, loading, done ->
        EditTermbaseUiState(
            step = step,
            loading = loading,
            done = done,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EditTermbaseUiState(),
    )

    fun reset() {
        termbase = null
        step.value = 0
        loading.value = false
        done.value = false
    }

    fun setTermbase(value: TermbaseModel) {
        termbase = value
    }

    fun changeStep(index: Int) {
        step.value = index
    }

    fun submitStep1(name: String, description: String, selectedLanguages: List<LanguageModel>) {
        val current = termbase ?: return
        loading.value = true
        viewModelScope.launch(dispatcherProvider.io) {
            val termbase = current.copy(name = name, description = description)
            termbaseRepository.update(termbase)

            val oldTermbaseLanguages = languageRepository.getAll(termbase.id)
            val oldLanguageCodes = oldTermbaseLanguages.map { it.code }
            val languagesToDelete = oldLanguageCodes.filter { it !in selectedLanguages.map { l -> l.code } }
            val languagesToAdd = selectedLanguages.map { it.code }.filter { it !in oldLanguageCodes }
            for (code in languagesToAdd) {
                val language = LanguageModel(code = code, termbaseId = termbase.id)
                languageRepository.create(language)
            }
            for (code in languagesToDelete) {
                // removes all properties
                deleteTermbaseLanguage(code, termbase.id)
            }

            if (languagesToAdd.isNotEmpty() || languagesToDelete.isNotEmpty()) {
                notificationCenter.send(NotificationCenter.Event.CurrentLanguagesEdited)
            }

            loading.value = false
            done.value = true
        }
    }

    fun submitStep2(properties: List<PropertyModel>) {
        val current = termbase ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            val newlyCreatedIds = mutableListOf<Int>()
            // upsert properties
            for (property in properties) {
                val toSave = property.copy(termbaseId = current.id)
                val existing = propertyRepository.getById(property.id) != null
                if (existing) {
                    propertyRepository.update(toSave)
                } else {
                    val id = propertyRepository.create(toSave)
                    newlyCreatedIds += id
                }
            }

            // removes unneeded properties
            val propertiesToDelete =
                propertyRepository.getAll(termbaseId = current.id)
                    .filter { it.id !in properties.map { p -> p.id } && it.id !in newlyCreatedIds }
            for (property in propertiesToDelete) {
                propertyRepository.delete(property)
            }
        }
    }

    fun submitStep3(descriptors: List<InputDescriptorModel>) {
        val current = termbase ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            val newlyCreatedIds = mutableListOf<Int>()
            for (descriptor in descriptors) {
                val toSave = descriptor.copy(termbaseId = current.id)
                val existing = inputDescriptorRepository.getById(descriptor.id) != null
                if (existing) {
                    inputDescriptorRepository.update(toSave)
                } else {
                    val id = inputDescriptorRepository.create(toSave)
                    newlyCreatedIds += id
                }
            }

            // removes unneeded properties
            val descriptorsToDelete =
                inputDescriptorRepository.getAll(termbaseId = current.id)
                    .filter { it.id !in descriptors.map { p -> p.id } && it.id !in newlyCreatedIds }
            for (descriptor in descriptorsToDelete) {
                inputDescriptorRepository.delete(descriptor)
            }
        }
    }
}
