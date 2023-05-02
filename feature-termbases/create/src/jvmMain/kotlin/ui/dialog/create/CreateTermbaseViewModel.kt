package ui.dialog.create

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import coroutines.CoroutineDispatcherProvider
import data.InputDescriptorModel
import data.LanguageModel
import data.PropertyModel
import data.TermbaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import notification.NotificationCenter
import repository.InputDescriptorRepository
import repository.LanguageRepository
import repository.PropertyRepository
import repository.TermbaseRepository
import kotlin.math.max

class CreateTermbaseViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val languageRepository: LanguageRepository,
    private val propertyRepository: PropertyRepository,
    private val inputDescriptorRepository: InputDescriptorRepository,
    private val notificationCenter: NotificationCenter,
) : InstanceKeeper.Instance {

    companion object {
        const val STEPS = 3
    }

    private val step = MutableStateFlow(0)
    private val loading = MutableStateFlow(false)
    private val done = MutableStateFlow(false)

    private var termbase: TermbaseModel? = null
    private var languages: List<LanguageModel> = emptyList()
    private var properties: List<PropertyModel> = emptyList()
    private var inputDescriptors: List<InputDescriptorModel> = emptyList()
    private var openNewlyCreated: Boolean = false
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = combine(
        step,
        loading,
        done,
    ) { step, loading, done ->
        CreateTermbaseUiState(
            step = step,
            loading = loading,
            done = done,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateTermbaseUiState(),
    )

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun reset() {
        step.value = 0
        loading.value = false
        done.value = false
        termbase = null
        languages = emptyList()
        properties = emptyList()
        inputDescriptors = emptyList()
        openNewlyCreated = false
    }

    fun setOpenNewlyCreated(value: Boolean) {
        openNewlyCreated = value
    }

    fun setTermbase(termbase: TermbaseModel) {
        this.termbase = termbase
    }

    fun setSelectedLanguages(value: List<LanguageModel>) {
        languages = value
    }

    fun setProperties(value: List<PropertyModel>) {
        properties = value
    }

    fun getProperties() = properties

    fun getLanguages() = languages

    fun getInputModelDescriptors() = inputDescriptors

    fun setInputModelDescriptors(value: List<InputDescriptorModel>) {
        inputDescriptors = value
    }

    fun previous() {
        step.updateAndGet {
            max(0, it - 1)
        }
    }

    fun next() {
        step.updateAndGet {
            it + 1
        }
    }

    fun submit() {
        val termbase = termbase ?: return
        loading.value = true
        viewModelScope.launch(dispatcherProvider.io) {
            val termbaseId = termbaseRepository.create(termbase)

            // create languages
            for (language in languages) {
                val lang = language.copy(termbaseId = termbaseId)
                languageRepository.create(lang)
            }

            // create definition model
            for (property in properties) {
                val prop = property.copy(termbaseId = termbaseId)
                propertyRepository.create(prop)
            }

            // create input model
            for (descriptor in inputDescriptors) {
                val desc = descriptor.copy(termbaseId = termbaseId)
                inputDescriptorRepository.create(desc)
            }

            // open new termbase if needed
            if (openNewlyCreated) {
                notificationCenter.send(NotificationCenter.Event.OpenTermbase(termbaseId))
            }

            loading.value = false
            done.value = true
        }
    }
}
