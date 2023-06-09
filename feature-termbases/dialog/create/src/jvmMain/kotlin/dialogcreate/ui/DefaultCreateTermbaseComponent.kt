package dialogcreate.ui

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
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

internal class DefaultCreateTermbaseComponent(
    componentContext: ComponentContext,
    coroutineContext: CoroutineContext,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val termbaseRepository: TermbaseRepository,
    private val languageRepository: LanguageRepository,
    private val propertyRepository: PropertyRepository,
    private val inputDescriptorRepository: InputDescriptorRepository,
    private val notificationCenter: NotificationCenter,
) : CreateTermbaseComponent, ComponentContext by componentContext {

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
    private lateinit var viewModelScope: CoroutineScope
    private val contentNavigation = SlotNavigation<CreateTermbaseComponent.ContentConfig>()

    override lateinit var uiState: StateFlow<CreateTermbaseUiState>
    override val content: Value<ChildSlot<CreateTermbaseComponent.ContentConfig, *>> = childSlot(
        source = contentNavigation,
        key = "CreateTermbaseContentSlot",
        childFactory = { config, context ->
            when (config) {
                CreateTermbaseComponent.ContentConfig.Step1 -> getByInjection<CreateTermbaseWizardStepOneComponent>(
                    context,
                    coroutineContext
                )

                CreateTermbaseComponent.ContentConfig.Step2 -> getByInjection<CreateTermbaseWizardStepTwoComponent>(
                    context,
                    coroutineContext
                )

                CreateTermbaseComponent.ContentConfig.Step3 -> getByInjection<CreateTermbaseWizardStepThreeComponent>(
                    context,
                    coroutineContext
                )

                else -> Unit
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
                contentNavigation.activate(CreateTermbaseComponent.ContentConfig.Step1)
            }
            doOnDestroy {
                viewModelScope.cancel()
            }
        }
    }

    override fun reset() {
        step.value = 0
        loading.value = false
        done.value = false
        termbase = null
        languages = emptyList()
        properties = emptyList()
        inputDescriptors = emptyList()
        openNewlyCreated = false
    }

    override fun setOpenNewlyCreated(value: Boolean) {
        openNewlyCreated = value
    }

    override fun setTermbase(termbase: TermbaseModel) {
        this.termbase = termbase
    }

    override fun setSelectedLanguages(value: List<LanguageModel>) {
        languages = value
    }

    override fun setProperties(value: List<PropertyModel>) {
        properties = value
    }

    override fun getProperties() = properties

    override fun getLanguages() = languages

    override fun getInputModelDescriptors() = inputDescriptors

    override fun setInputModelDescriptors(value: List<InputDescriptorModel>) {
        inputDescriptors = value
    }

    override fun previous() {
        step.updateAndGet {
            max(0, it - 1)
        }
    }

    override fun next() {
        val newStep = step.value +1
        step.value = newStep
        viewModelScope.launch(dispatcherProvider.main) {
            when (newStep) {
                1 -> contentNavigation.activate(CreateTermbaseComponent.ContentConfig.Step2)
                2 -> contentNavigation.activate(CreateTermbaseComponent.ContentConfig.Step3)
                0 -> contentNavigation.activate(CreateTermbaseComponent.ContentConfig.Step1)
            }
        }
    }

    override fun submit() {
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
