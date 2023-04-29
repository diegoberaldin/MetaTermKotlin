package ui.dialog.create.steptwo

import coroutines.CoroutineDispatcherProvider
import data.PicklistValueModel
import data.PropertyLevel
import data.PropertyModel
import data.PropertyType
import data.TermbaseModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import repository.PropertyRepository
import kotlin.math.max

class CreateTermbaseWizardStepTwoViewModel(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val propertyRepository: PropertyRepository,
) : ViewModel() {

    private val items = MutableStateFlow<List<CreateTermbaseStepTwoItem>>(emptyList())
    private val selectedProperty = MutableStateFlow<PropertyModel?>(null)

    private val currentPropertyName = MutableStateFlow("")
    private val currentPropertyLevel = MutableStateFlow(PropertyLevel.ENTRY)
    private val currentPropertyType = MutableStateFlow(PropertyType.TEXT)
    private val currentPicklistValues = MutableStateFlow<List<String>>(emptyList())

    private val _done = MutableSharedFlow<List<PropertyModel>>()
    val done = _done.asSharedFlow()

    val uiState = combine(
        items,
        selectedProperty,
    ) { items, selectedProperty ->
        CreateTermbaseStepTwoUiState(
            items = items,
            selectedProperty = selectedProperty,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateTermbaseStepTwoUiState(),
    )

    val currentPropertyState = combine(
        currentPropertyName,
        currentPropertyLevel,
        currentPropertyType,
        currentPicklistValues,
    ) { name, level, type, picklistValues ->
        CreteTermbaseStepTwoEditPropertyState(
            name = name,
            level = level,
            type = type,
            picklistValues = picklistValues,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreteTermbaseStepTwoEditPropertyState(),
    )

    fun reset() {
        items.value = emptyList()
        selectedProperty.value = null
        currentPropertyName.value = ""
        currentPropertyLevel.value = PropertyLevel.ENTRY
        currentPropertyType.value = PropertyType.TEXT
        currentPicklistValues.value = emptyList()
    }

    fun loadInitial(termbase: TermbaseModel) {
        viewModelScope.launch(dispatcherProvider.io) {
            val data = propertyRepository.getAll(termbase.id)
            items.update {
                buildList {
                    this += CreateTermbaseStepTwoItem.SectionHeader(PropertyLevel.ENTRY)
                    addAll(
                        data.filter { it.level == PropertyLevel.ENTRY }.sortedBy { it.id }
                            .map { CreateTermbaseStepTwoItem.Property(property = it) },
                    )

                    this += CreateTermbaseStepTwoItem.SectionHeader(PropertyLevel.LANGUAGE)
                    addAll(
                        data.filter { it.level == PropertyLevel.LANGUAGE }.sortedBy { it.id }
                            .map { CreateTermbaseStepTwoItem.Property(property = it) },
                    )

                    this += CreateTermbaseStepTwoItem.SectionHeader(PropertyLevel.TERM)
                    addAll(
                        data.filter { it.level == PropertyLevel.TERM }.sortedBy { it.id }
                            .map { CreateTermbaseStepTwoItem.Property(property = it) },
                    )
                }
            }
        }
    }

    fun selectProperty(value: PropertyModel) {
        selectedProperty.updateAndGet {
            if (it == value) {
                currentPropertyName.value = ""
                currentPropertyLevel.value = PropertyLevel.ENTRY
                currentPropertyType.value = PropertyType.TEXT
                currentPicklistValues.value = emptyList()

                null
            } else {
                currentPropertyName.value = value.name
                currentPropertyLevel.value = value.level
                currentPropertyType.value = value.type
                currentPicklistValues.value = value.picklistValues.map { e -> e.value }

                value
            }
        }
    }

    fun addProperty(level: PropertyLevel) {
        items.updateAndGet { oldList ->
            val maxId = oldList.fold(0) { acc, current ->
                max(
                    acc,
                    (current as? CreateTermbaseStepTwoItem.Property)?.property?.id ?: 0,
                )
            }
            val newProperty = PropertyModel(id = maxId + 1, level = level)
            selectProperty(newProperty)

            val newList = mutableListOf<CreateTermbaseStepTwoItem>()
            newList.addAll(oldList)
            var insertIndex = 0
            for (i in oldList.indices) {
                val item = oldList[i]
                if (item is CreateTermbaseStepTwoItem.SectionHeader && item.level == PropertyLevel.LANGUAGE && level == PropertyLevel.ENTRY) {
                    break
                }
                if (item is CreateTermbaseStepTwoItem.SectionHeader && item.level == PropertyLevel.TERM && level == PropertyLevel.LANGUAGE) {
                    break
                }
                insertIndex = i + 1
            }

            newList.add(insertIndex, CreateTermbaseStepTwoItem.Property(property = newProperty))
            if (insertIndex == 0) {
                // header always added when at the beginning
                newList.add(0, CreateTermbaseStepTwoItem.SectionHeader(level = level))
            }
            val prevIndex = (insertIndex - 1).coerceAtLeast(0)
            when (val e = newList[prevIndex]) {
                is CreateTermbaseStepTwoItem.SectionHeader -> {
                    if (e.level != level) {
                        newList.add(prevIndex + 1, CreateTermbaseStepTwoItem.SectionHeader(level = level))
                    }
                }

                is CreateTermbaseStepTwoItem.Property -> {
                    if (e.property.level != level) {
                        newList.add(prevIndex + 1, CreateTermbaseStepTwoItem.SectionHeader(level = level))
                    }
                }
            }

            newList
        }
    }

    fun removeProperty() {
        val property = selectedProperty.value ?: return
        items.updateAndGet {
            it.filterNot { e -> e is CreateTermbaseStepTwoItem.Property && e.property.id == property.id }
        }
    }

    fun setCurrentPropertyName(value: String) {
        currentPropertyName.value = value
        items.updateAndGet {
            val newList = mutableListOf<CreateTermbaseStepTwoItem>()
            for (item in it) {
                newList += if (item is CreateTermbaseStepTwoItem.Property && item.property.id == selectedProperty.value?.id) {
                    item.copy(property = item.property.copy(name = value))
                } else {
                    item
                }
            }
            newList
        }
    }

    fun setCurrentPropertyLevel(value: PropertyLevel) {
        currentPropertyLevel.value = value
        items.updateAndGet {
            val newList = mutableListOf<CreateTermbaseStepTwoItem>()
            for (item in it) {
                newList += if (item is CreateTermbaseStepTwoItem.Property && item.property.id == selectedProperty.value?.id) {
                    item.copy(property = item.property.copy(level = value))
                } else {
                    item
                }
            }
            newList
        }
    }

    fun setCurrentPropertyType(value: PropertyType) {
        currentPropertyType.value = value
        items.updateAndGet {
            val newList = mutableListOf<CreateTermbaseStepTwoItem>()
            for (item in it) {
                newList += if (item is CreateTermbaseStepTwoItem.Property && item.property.id == selectedProperty.value?.id) {
                    item.copy(property = item.property.copy(type = value))
                } else {
                    item
                }
            }
            newList
        }
    }

    fun addPicklistValue(value: String) {
        val temporaryId = selectedProperty.value?.id ?: return
        currentPicklistValues.updateAndGet {
            val res = it + value
            items.updateAndGet { oldItems ->
                val newList = mutableListOf<CreateTermbaseStepTwoItem>()
                for (item in oldItems) {
                    newList += when (item) {
                        !is CreateTermbaseStepTwoItem.Property -> item
                        else -> {
                            when (item.property.id) {
                                temporaryId -> {
                                    item.copy(
                                        property = item.property.copy(
                                            picklistValues = item.property.picklistValues + PicklistValueModel(
                                                propertyId = temporaryId,
                                                value = value,
                                            ),
                                        ),
                                    )
                                }

                                else -> {
                                    item
                                }
                            }
                        }
                    }
                }
                newList
            }
            res
        }
    }

    fun removePicklistValue(index: Int) {
        val temporaryId = selectedProperty.value?.id ?: return
        currentPicklistValues.updateAndGet {
            val res = it.filterIndexed { i, _ -> i != index }
            items.updateAndGet { oldItems ->
                val newList = mutableListOf<CreateTermbaseStepTwoItem>()
                for (item in oldItems) {
                    newList += when (item) {
                        !is CreateTermbaseStepTwoItem.Property -> item
                        else -> {
                            when (item.property.id) {
                                temporaryId -> {
                                    item.copy(item.property.copy(picklistValues = item.property.picklistValues.filterIndexed { i, _ -> i != index }))
                                }

                                else -> item
                            }
                        }
                    }
                }
                newList
            }
            res
        }
    }

    fun submit() {
        viewModelScope.launch(dispatcherProvider.io) {
            _done.emit(items.value.mapNotNull { (it as? CreateTermbaseStepTwoItem.Property)?.property })
        }
    }
}
