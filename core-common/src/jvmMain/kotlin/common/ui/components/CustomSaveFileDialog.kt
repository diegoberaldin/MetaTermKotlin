package common.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Window
import java.io.FilenameFilter

@Composable
fun CustomSaveFileDialog(
    title: String = "",
    initialFileName: String = "",
    nameFilter: (String) -> Boolean = { true },
    parent: Frame? = null,
    onCloseRequest: (result: String?) -> Unit,
) {
    fun createDialog(): Window {
        return object : FileDialog(parent, title, SAVE) {

            init {
                file = initialFileName
            }

            override fun getFilenameFilter(): FilenameFilter {
                return FilenameFilter { _, name ->
                    nameFilter(name)
                }
            }

            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    val result = file?.let { directory + file }
                    onCloseRequest(result)
                }
            }
        }
    }

    AwtWindow(
        create = {
            createDialog()
        },
        dispose = {
            it.dispose()
        },
    )
}
