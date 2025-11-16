import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.notes.MainNotesView

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MainNotesView()
    }
}