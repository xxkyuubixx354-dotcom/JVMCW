package ui

import domain.*
import persistence.JsonDataPersistence
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.geometry.Insets
import javafx.collections.FXCollections
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventCreationView(
    private val eventManager: EventManager,
    private val onEventsChanged: () -> Unit
) {

    private lateinit var eventListView: ListView<String>
    private val persistence = JsonDataPersistence("data")

    fun createView(): BorderPane {
        val root = BorderPane()
        root.padding = Insets(20.0)

        val form = createEventForm()
        eventListView = ListView<String>()
        updateEventList()

        root.left = form
        root.center = VBox(10.0).apply {
            padding = Insets(0.0, 0.0, 0.0, 20.0)
            children.addAll(
                Label("Scheduled Events").apply {
                    style = "-fx-font-size: 18px; -fx-font-weight: bold;"
                },
                eventListView
            )
            prefWidth = 500.0
        }

        return root
    }

    private fun createEventForm(): VBox {
        val form = VBox(10.0)
        form.padding = Insets(10.0)
        form.prefWidth = 450.0

        val titleField = TextField().apply { promptText = "Event Title" }
        val descriptionArea = TextArea().apply {
            promptText = "Description"
            prefRowCount = 3
        }

        val categoryComboBox = ComboBox<EventCategory>().apply {
            items = FXCollections.observableArrayList(EventCategory.values().toList())
            value = EventCategory.GENERAL
        }

        val venueComboBox = ComboBox<Venue>().apply {
            items = FXCollections.observableArrayList(eventManager.getAllVenues())
        }

        val startDateField = TextField().apply {
            promptText = "Start Date/Time (dd-MM-yyyy HH:mm)"
        }
        val endDateField = TextField().apply {
            promptText = "End Date/Time (dd-MM-yyyy HH:mm)"
        }

        val organizerField = TextField().apply { promptText = "Organizer Name" }
        val capacityField = TextField().apply { promptText = "Max Capacity (optional)" }

        val createButton = Button("Create Event")
        val statusLabel = Label()

        createButton.setOnAction {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                val selectedVenue = venueComboBox.value
                    ?: throw IllegalArgumentException("Please select a venue")

                val event = Event(
                    title = titleField.text,
                    description = descriptionArea.text,
                    startDateTime = LocalDateTime.parse(startDateField.text, formatter),
                    endDateTime = LocalDateTime.parse(endDateField.text, formatter),
                    venue = selectedVenue,
                    maxCapacity = capacityField.text.toIntOrNull() ?: selectedVenue.capacity,
                    organizer = organizerField.text,
                    category = categoryComboBox.value
                )

                val result = eventManager.createEvent(event)

                if (result.isSuccess) {
                    val saveResult = persistence.saveEvents(eventManager.getAllEvents())
                    if (saveResult.isFailure) {
                        throw saveResult.exceptionOrNull() ?: RuntimeException("Unknown save error")
                    }

                    statusLabel.text = "Event created and saved successfully!"
                    statusLabel.style = "-fx-text-fill: green;"

                    clearForm(
                        titleField,
                        descriptionArea,
                        startDateField,
                        endDateField,
                        organizerField,
                        capacityField
                    )

                    onEventsChanged()   // refresh ParticipantRegistrationView
                    updateEventList()   // refresh Scheduled Events list
                } else {
                    statusLabel.text = "Error: ${result.exceptionOrNull()?.message}"
                    statusLabel.style = "-fx-text-fill: red;"
                }
            } catch (e: Exception) {
                statusLabel.text = "Error: ${e.message}"
                statusLabel.style = "-fx-text-fill: red;"
            }
        }

        form.children.addAll(
            Label("Create New Event").apply { style = "-fx-font-size: 18px; -fx-font-weight: bold;" },
            Label("Title:"), titleField,
            Label("Description:"), descriptionArea,
            Label("Category:"), categoryComboBox,
            Label("Venue:"), venueComboBox,
            Label("Start Date/Time:"), startDateField,
            Label("End Date/Time:"), endDateField,
            Label("Organizer:"), organizerField,
            Label("Max Capacity:"), capacityField,
            createButton,
            statusLabel
        )

        return form
    }

    private fun clearForm(vararg fields: TextInputControl) {
        fields.forEach { it.clear() }
    }

    private fun updateEventList() {
        if (!::eventListView.isInitialized) return
        val events = eventManager.getAllEvents()
        val items = events.map { "${it.title} - ${it.startDateTime} @ ${it.venue.name}" }
        eventListView.items = FXCollections.observableArrayList(items)
    }
}
