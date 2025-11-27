import javafx.application.Application
import javafx.stage.Stage
import ui.EventPlannerApp

class Main : Application() {
    override fun start(primaryStage: Stage) {
        val app = EventPlannerApp()
        app.start(primaryStage)
    }
}

fun main() {
    Application.launch(Main::class.java)
}
