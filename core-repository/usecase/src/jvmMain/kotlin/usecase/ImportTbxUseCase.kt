package usecase

import data.EntryModel
import data.TermModel
import data.TermbaseModel
import kotlinx.coroutines.CompletableDeferred
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import repository.EntryRepository
import repository.LanguageRepository
import repository.TermRepository
import java.io.File
import javax.xml.parsers.SAXParserFactory

class ImportTbxUseCase(
    private val entryRepository: EntryRepository,
    private val termRepository: TermRepository,
    private val languageRepository: LanguageRepository,
) {

    suspend operator fun invoke(termbase: TermbaseModel, source: File) {
        val entryRegistry = getEntryRegistry(source)

        for (key in entryRegistry.keys) {
            val entry = EntryModel(termbaseId = termbase.id)
            val entryId = entryRepository.create(entry)
            for (lang in entryRegistry[key]?.keys ?: emptyList()) {
                val allowedCodes = languageRepository.getAll(termbase.id).map { it.code }
                if (lang !in allowedCodes) {
                    continue
                }

                for (lemma in entryRegistry[key]?.get(lang) ?: emptyList()) {
                    val term = TermModel(entryId = entryId, lang = lang, lemma = lemma)
                    termRepository.create(term)
                }
            }
        }
    }

    private suspend fun getEntryRegistry(source: File): Map<String, Map<String, List<String>>> {
        val deferred = CompletableDeferred<Map<String, Map<String, List<String>>>>()
        try {
            val factory = SAXParserFactory.newInstance()
            val parser = factory.newSAXParser()
            val reader = parser.xmlReader
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            reader.contentHandler = object : DefaultHandler() {
                private val innerMap = mutableMapOf<String, Map<String, List<String>>>()
                private var currentEntryId: String? = null
                private var currentTerms: Map<String, List<String>>? = null
                private var currentLang: String? = null
                private var inTerm: Boolean = false

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
                    }
                }

                override fun characters(ch: CharArray?, start: Int, length: Int) {
                    super.characters(ch, start, length)
                    if (inTerm) {
                        val lang = currentLang
                        if (!lang.isNullOrEmpty()) {
                            val lemma = ch?.let { String(it) }?.trim()?.substringBefore("\n") ?: ""
                            if (lemma.isNotEmpty()) {
                                val map = currentTerms?.toMutableMap() ?: mutableMapOf()
                                val currentList = map[lang] ?: emptyList()
                                map[lang] = currentList + lemma
                                currentTerms = map
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
