package ui

import domain.EventManager
import scheduling.SchedulerBridge
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.geometry.Insets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SchedulerView(private val eventManager: EventManager) {

    fun createView(): BorderPane {
        val root = BorderPane()
        root.padding = Insets(20.0)

        val controlPanel = VBox(15.0)
        controlPanel.padding = Insets(10.0)

        val scheduleButton = Button("Generate Schedule")
        val resultArea = TextArea().apply {
            isEditable = false
            prefRowCount = 20
        }

        scheduleButton.setOnAction {
            try {
                val events = eventManager.getAllEvents()
                val venues = eventManager.getAllVenues()
                val startTime = LocalDateTime.now()

                val result = SchedulerBridge.scheduleEvents(events, venues, startTime)

                val output = buildString {
                    val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

                    appendLine("=== SCHEDULE RESULTS ===\n")
                    appendLine("Scheduled Events (${result.scheduled.size}):")
                    result.scheduled.forEach { scheduled ->
                        val startText = scheduled.startTime.format(displayFormatter)
                        val endText = scheduled.endTime.format(displayFormatter)

                        val title = eventManager.getEventById(scheduled.eventId)?.title
                            ?: scheduled.eventId

                        appendLine("  • $title")
                        appendLine("    Venue: ${scheduled.venueName}")
                        appendLine("    Dates: $startText to $endText\n")
                    }

                    if (result.unscheduled.isNotEmpty()) {
                        appendLine("\nUnscheduled Events (${result.unscheduled.size}):")
                        result.unscheduled.forEach { eventId ->
                            val title = eventManager.getEventById(eventId)?.title ?: eventId
                            appendLine("  • $title")
                        }
                    }
                }

                resultArea.text = output

            } catch (e: Exception) {
                resultArea.text = "Error generating schedule: ${e.message}"
            }
        }

        controlPanel.children.addAll(
            Label("Event Scheduler").apply { style = "-fx-font-size: 20px; -fx-font-weight: bold;" },
            Label("Generate a conflict-free schedule for all events"),
            scheduleButton,
            Separator(),
            Label("Schedule Results:"),
            resultArea
        )

        root.center = controlPanel
        return root
    }
}
