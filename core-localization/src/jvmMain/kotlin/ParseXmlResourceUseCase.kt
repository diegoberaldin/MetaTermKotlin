import java.io.InputStream

internal interface ParseXmlResourceUseCase {
    operator fun invoke(inputStream: InputStream): List<LocalizableString>
}