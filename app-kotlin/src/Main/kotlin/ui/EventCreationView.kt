package ui

import domain.*
import persistence.JsonDataPersistence
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.geometry.Insets
import javafx.collections.FXCollections
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventCreationView(
    private val eventManager: EventManager,
    private val onEventsChanged: () -> Unit
) {

    private lateinit var eventListView: ListView<String>
    private lateinit var venueComboBox: ComboBox<Venue>
    private val persistence = JsonDataPersistence("data")

    fun createView(): BorderPane {
        val root = BorderPane()
        root.padding = Insets(20.0)

        val form = createEventForm()
        eventListView = ListView()
        updateEventList()

        val contextMenu = ContextMenu()
        val detailsItem = MenuItem("View Details")
        val deleteItem = MenuItem("Delete Event")
        contextMenu.items.addAll(detailsItem, deleteItem)
        eventListView.contextMenu = contextMenu

        detailsItem.setOnAction {
            val index = eventListView.selectionModel.selectedIndex
            if (index < 0) return@setOnAction

            val events = eventManager.getAllEvents()
            val event = events.getOrNull(index) ?: return@setOnAction

            val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val startText = event.startDateTime.toLocalDate().format(displayFormatter)
            val endText = event.endDateTime.toLocalDate().format(displayFormatter)

            val info = """
                Title: ${event.title}
                Description: ${event.description}
                Venue: ${event.venue.name}
                Start: $startText
                End: $endText
                Organizer: ${event.organizer}
                Capacity: ${event.maxCapacity}
                Registered: ${event.getParticipants().size}
            """.trimIndent()

            Alert(Alert.AlertType.INFORMATION).apply {
                title = "Event Details"
                headerText = event.title
                contentText = info
                showAndWait()
            }
        }

        deleteItem.setOnAction {
            val index = eventListView.selectionModel.selectedIndex
            if (index < 0) return@setOnAction

            val events = eventManager.getAllEvents()
            val event = events.getOrNull(index) ?: return@setOnAction

            val confirm = Alert(Alert.AlertType.CONFIRMATION).apply {
                title = "Delete Event"
                headerText = "Delete '${event.title}'?"
                contentText = "This will remove the event and its registrations."
            }.showAndWait()

            if (confirm.isPresent && confirm.get().buttonData.isDefaultButton) {
                if (eventManager.deleteEvent(event.id)) {
                    eventManager.saveData()
                    updateEventList()
                    onEventsChanged()
                }
            }
        }

        val formScroll = ScrollPane(form).apply {
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        }

        root.left = formScroll
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

        venueComboBox = ComboBox<Venue>().apply {
            items = FXCollections.observableArrayList(eventManager.getAllVenues())
            converter = object : javafx.util.StringConverter<Venue>() {
                override fun toString(venue: Venue?): String = venue?.name ?: ""
                override fun fromString(string: String?): Venue? = null
            }
        }

        val startDateField = TextField().apply {
            promptText = "Start Date (dd/MM/yyyy)"
        }
        val endDateField = TextField().apply {
            promptText = "End Date (dd/MM/yyyy)"
        }

        val organizerField = TextField().apply { promptText = "Organizer Name" }
        val capacityField = TextField().apply { promptText = "Max Capacity (optional)" }

        val createButton = Button("Create Event")
        val statusLabel = Label()

        createButton.setOnAction {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val selectedVenue = venueComboBox.value
                    ?: throw IllegalArgumentException("Please select a venue")

                val startDate = LocalDate.parse(startDateField.text, formatter)
                val endDate = LocalDate.parse(endDateField.text, formatter)

                val event = Event(
                    title = titleField.text,
                    description = descriptionArea.text,
                    startDateTime = startDate.atStartOfDay(),
                    endDateTime = endDate.atStartOfDay(),
                    venue = selectedVenue,
                    maxCapacity = capacityField.text.toIntOrNull() ?: selectedVenue.capacity,
                    organizer = organizerField.text
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

                    onEventsChanged()
                    updateEventList()
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
            Label("Venue:"), venueComboBox,
            Label("Start Date:"), startDateField,
            Label("End Date:"), endDateField,
            Label("Organizer:"), organizerField,
            Label("Max Capacity:"), capacityField,
            createButton,
            statusLabel
        )

        return form
    }

    fun refreshVenues() {
        if (!::venueComboBox.isInitialized) return
        venueComboBox.items = FXCollections.observableArrayList(eventManager.getAllVenues())
    }

    private fun clearForm(vararg fields: TextInputControl) {
        fields.forEach { it.clear() }
    }

    private fun updateEventList() {
        if (!::eventListView.isInitialized) return
        val events = eventManager.getAllEvents()
        val items = events.map { it.title }
        eventListView.items = FXCollections.observableArrayList(items)
    }
}
