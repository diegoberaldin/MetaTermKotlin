package usecase

import data.EntryModel
import data.PropertyLevel
import data.PropertyValueModel
import data.TermModel
import data.TermbaseModel
import kotlinx.coroutines.CompletableDeferred
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import repository.EntryRepository
import repository.LanguageRepository
import repository.PropertyRepository
import repository.TermPropertyValueRepository
import repository.TermRepository
import usecase.dto.TermWithDefinition
import java.io.File
import javax.xml.parsers.SAXParserFactory

class ImportTbxUseCase(
    private val entryRepository: EntryRepository,
    private val termRepository: TermRepository,
    private val languageRepository: LanguageRepository,
    private val propertyRepository: PropertyRepository,
    private val termPropertyValueRepository: TermPropertyValueRepository,
) {

    suspend operator fun invoke(termbase: TermbaseModel, source: File) {
        val entryRegistry = getEntryRegistry(source)

        for (key in entryRegistry.keys) {
            val entry = EntryModel(termbaseId = termbase.id)
            val entryId = entryRepository.create(entry)
            val definitionPropertyId =
                propertyRepository.getAll(termbase.id)
                    .firstOrNull { it.name.equals("definition", ignoreCase = true) && it.level == PropertyLevel.TERM }
                    ?.id
            for (lang in entryRegistry[key]?.keys ?: emptyList()) {
                val allowedCodes = languageRepository.getAll(termbase.id).map { it.code }
                if (lang !in allowedCodes) {
                    continue
                }

                for (termWithDef in entryRegistry[key]?.get(lang) ?: emptyList()) {
                    val term = termWithDef.term.copy(entryId = entryId)
                    val definition = termWithDef.definition
                    val termId = termRepository.create(term)
                    if (definitionPropertyId != null && definition != null) {
                        val property = PropertyValueModel(propertyId = definitionPropertyId, value = definition)
                        termPropertyValueRepository.create(model = property, termId = termId)
                    }
                }
            }
        }
    }

    private suspend fun getEntryRegistry(source: File): Map<String, Map<String, List<TermWithDefinition>>> {
        val deferred = CompletableDeferred<Map<String, Map<String, List<TermWithDefinition>>>>()
        try {
            val factory = SAXParserFactory.newInstance()
            val parser = factory.newSAXParser()
            val reader = parser.xmlReader
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            reader.contentHandler = object : DefaultHandler() {
                private val innerMap = mutableMapOf<String, Map<String, List<TermWithDefinition>>>()
                private var currentEntryId: String? = null
                private var currentTerms: Map<String, List<TermWithDefinition>>? = null
                private var currentLang: String? = null
                private var currentLemma: String? = null
                private var currentDefinition: String? = null
                private var inTerm: Boolean = false
                private var inDefinition: Boolean = false

                private fun saveTerm() {
                    val lang = currentLang ?: return
                    val lemma = currentLemma ?: return
                    val map = currentTerms?.toMutableMap() ?: mutableMapOf()
                    val currentList = map[lang] ?: emptyList()
                    val term = TermModel(
                        lemma = lemma,
                        lang = lang,
                    )
                    map[lang] = currentList + TermWithDefinition(
                        term = term,
                        definition = currentDefinition?.takeIf { it.isNotEmpty() })
                    currentTerms = map
                }

                override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                    super.startElement(uri, localName, qName, attributes)
                    when (qName) {
                        "termEntry" -> {
                            currentEntryId = attributes?.getValue("id") ?: ""
                            currentTerms = mapOf()
                        }

                        "langSet" -> {
                            currentLang = attributes?.getValue("xml:lang") ?: ""
                        }

                        "term" -> {
                            inTerm = true
                        }

                        "descrip" -> {
                            if ((attributes?.getValue("type") ?: "") == "definition") {
                                inDefinition = true
                            }
                        }
                    }
                }

                override fun characters(ch: CharArray?, start: Int, length: Int) {
                    super.characters(ch, start, length)
                    when {
                        inDefinition -> {
                            val value = ch?.let { String(it) }?.trim()?.substringBefore("\n") ?: ""
                            if (value.isNotEmpty()) {
                                currentDefinition = value
                            }
                        }

                        inTerm -> {
                            val value = ch?.let { String(it) }?.trim()?.substringBefore("\n") ?: ""
                            if (value.isNotEmpty()) {
                                currentLemma = value
                            }
                        }
                    }
                }

                override fun endElement(uri: String?, localName: String?, qName: String?) {
                    super.endElement(uri, localName, qName)
                    val entryId = currentEntryId
                    when (qName) {
                        "termEntry" -> {
                            if (!entryId.isNullOrEmpty()) {
                                innerMap[entryId] = currentTerms ?: mapOf()
                            }
                            currentEntryId = null
                            currentTerms = null
                        }

                        "langSet" -> {
                            currentLang = null
                        }

                        "term" -> {
                            inTerm = false
                            saveTerm()
                        }

                        "descrip" -> {
                            if (inDefinition) {
                                inDefinition = false
                            }
                        }
                    }
                }

                override fun endDocument() {
                    super.endDocument()
                    deferred.complete(innerMap)
                }
            }
            reader.parse(InputSource(source.inputStream()))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return deferred.await()
    }
}
