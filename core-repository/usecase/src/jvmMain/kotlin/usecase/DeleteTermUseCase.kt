package usecase

import repo.TermPropertyValueRepository
import repo.TermRepository

class DeleteTermUseCase(
    private val termRepository: TermRepository,
    private val termPropertyValueRepository: TermPropertyValueRepository,
) {

    suspend operator fun invoke(termId: Int) {
        val properties = termPropertyValueRepository.getAll(termId)
        for (property in properties) {
            termPropertyValueRepository.delete(property)
        }

        val term = termRepository.getById(termId)
        if (term != null) {
            termRepository.delete(term)
        }
    }
}
