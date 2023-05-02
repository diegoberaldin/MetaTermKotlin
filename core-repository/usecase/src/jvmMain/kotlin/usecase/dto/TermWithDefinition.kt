package usecase.dto

import data.TermModel

internal data class TermWithDefinition(val term: TermModel, val definition: String? = null)