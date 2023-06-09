package dialogedit.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import common.coroutines.CoroutineDispatcherProvider
import data.InputDescriptorModel
import data.LanguageModel
import data.PropertyModel
import data.TermbaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import common.notification.NotificationCenter
import common.utils.getByInjection
import dialogcreate.stepone.ui.CreateTermbaseWizardStepOneComponent
import dialogcreate.stepthree.ui.CreateTermbaseWizardStepThreeComponent
import dialogcreate.steptwo.ui.CreateTermbaseWizardStepTwoComponent
import repo.InputDescriptorRepository
import repo.LanguageRepository
import repo.PropertyRepository
import repo.TermbaseRepository
import usecase.DeleteTermbaseLanguageUseCase
import kotlin.coroutines.CoroutineContext

internal class DefaultEditTermbaseComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val languageRepository: LanguageRepository,
    private val propertyRepository: PropertyRepository,
    private val inputDescriptorRepository: InputDescriptorRepository,
    private val deleteTermbaseLanguage: DeleteTermbaseLanguageUseCase,
    private val notificationCenter: NotificationCenter,
) : EditTermbaseComponent, ComponentContext by componentContext {

    private var termbase: TermbaseModel? = null
    private val step = MutableStateFlow(0)
    private val loading = MutableStateFlow(false)
    private val done = MutableStateFlow(false)
    private lateinit var viewModelScope: CoroutineScope
    private val contentNavigation = SlotNavigation<EditTermbaseComponent.ContentConfig>()

    override lateinit var uiState: StateFlow<EditTermbaseUiState>
    override val content: Value<ChildSlot<EditTermbaseComponent.ContentConfig, *>> = childSlot(
        source = contentNavigation,
        key = "EditTermbaseContentSlot",
        childFactory = { config, context ->
            when (config) {
                EditTermbaseComponent.ContentConfig.Step1 -> getByInjection<CreateTermbaseWizardStepOneComponent>(
                    context,
                    coroutineContext
                )

                EditTermbaseComponent.ContentConfig.Step2 -> getByInjection<CreateTermbaseWizardStepTwoComponent>(
                    context,
                    coroutineContext
                )

                EditTermbaseComponent.ContentConfig.Step3 -> getByInjection<CreateTermbaseWizardStepThreeComponent>(
                    context,
                    coroutineContext
                )
            }
        }
    )

    init {
        with(lifecycle) {
            doOnCreate {
                viewModelScope = CoroutineScope(coroutineContext + SupervisorJob())
                uiState = combine(
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

                contentNavigation.activate(EditTermbaseComponent.ContentConfig.Step1)
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun reset() {
        termbase = null
        step.value = 0
        viewModelScope.launch(dispatcherProvider.main) {
            contentNavigation.activate(EditTermbaseComponent.ContentConfig.Step1)
        }
        loading.value = false
        done.value = false
    }

    override fun setTermbase(value: TermbaseModel) {
        termbase = value
    }

    override fun changeStep(index: Int) {
        step.value = index
        viewModelScope.launch(dispatcherProvider.main) {
            when (index) {
                1 -> contentNavigation.activate(EditTermbaseComponent.ContentConfig.Step2)
                2 -> contentNavigation.activate(EditTermbaseComponent.ContentConfig.Step3)
                else -> contentNavigation.activate(EditTermbaseComponent.ContentConfig.Step1)
            }
        }
    }

    override fun submitStep1(name: String, description: String, selectedLanguages: List<LanguageModel>) {
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

    override fun submitStep2(properties: List<PropertyModel>) {
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
            val propertiesToDelete = propertyRepository.getAll(termbaseId = current.id)
                .filter { it.id !in properties.map { p -> p.id } && it.id !in newlyCreatedIds }
            for (property in propertiesToDelete) {
                propertyRepository.delete(property)
            }
        }
    }

    override fun submitStep3(descriptors: List<InputDescriptorModel>) {
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
            val descriptorsToDelete = inputDescriptorRepository.getAll(termbaseId = current.id)
                .filter { it.id !in descriptors.map { p -> p.id } && it.id !in newlyCreatedIds }
            for (descriptor in descriptorsToDelete) {
                inputDescriptorRepository.delete(descriptor)
            }
        }
    }
}
