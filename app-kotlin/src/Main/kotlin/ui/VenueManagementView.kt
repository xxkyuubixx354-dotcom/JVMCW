package ui

import domain.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.geometry.Insets
import javafx.collections.FXCollections
import java.util.UUID
import persistence.JsonDataPersistence

class VenueManagementView(private val eventManager: EventManager) {

    private lateinit var venueListView: ListView<String>

    fun createView(): BorderPane {
        val root = BorderPane()
        root.padding = Insets(20.0)

        val form = createVenueForm()

        venueListView = ListView()
        updateVenueList()

        val contextMenu = ContextMenu()
        val deleteItem = MenuItem("Delete Venue")
        contextMenu.items.add(deleteItem)
        venueListView.contextMenu = contextMenu

        deleteItem.setOnAction {
            val selectedIndex = venueListView.selectionModel.selectedIndex
            if (selectedIndex < 0) return@setOnAction

            val venues = eventManager.getAllVenues()
            val venue = venues.getOrNull(selectedIndex) ?: return@setOnAction

            if (eventManager.removeVenue(venue.id)) {
                val persistence = JsonDataPersistence("data")
                persistence.saveVenues(eventManager.getAllVenues())
                updateVenueList()
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
                Label("Existing Venues").apply {
                    style = "-fx-font-size: 18px; -fx-font-weight: bold;"
                },
                venueListView
            )
        }
        return root
    }

    private fun createVenueForm(): VBox {
        val form = VBox(10.0)
        form.padding = Insets(10.0)
        form.prefWidth = 400.0

        val nameField = TextField().apply { promptText = "Venue Name" }
        val capacityField = TextField().apply { promptText = "Capacity" }
        val locationField = TextField().apply { promptText = "Location" }
        val facilitiesField = TextField().apply { promptText = "Facilities (comma-separated)" }

        val addButton = Button("Add Venue")
        val statusLabel = Label()

        addButton.setOnAction {
            try {
                val venue = Venue(
                    id = UUID.randomUUID().toString(),
                    name = nameField.text,
                    capacity = capacityField.text.toInt(),
                    location = locationField.text,
                    facilities = facilitiesField.text.split(",").map { it.trim() }
                )
                eventManager.addVenue(venue)

                val persistence = JsonDataPersistence("data")
                val result = persistence.saveVenues(eventManager.getAllVenues())
                if (result.isSuccess) {
                    statusLabel.text = "Venue added and saved successfully!"
                    statusLabel.style = "-fx-text-fill: green;"
                } else {
                    statusLabel.text =
                        "Venue added but save failed: ${result.exceptionOrNull()?.message}"
                    statusLabel.style = "-fx-text-fill: orange;"
                }

                nameField.clear()
                capacityField.clear()
                locationField.clear()
                facilitiesField.clear()
                updateVenueList()
            } catch (e: Exception) {
                statusLabel.text = "Error: ${e.message}"
                statusLabel.style = "-fx-text-fill: red;"
            }
        }

        form.children.addAll(
            Label("Add New Venue").apply { style = "-fx-font-size: 18px; -fx-font-weight: bold;" },
            Label("Name:"), nameField,
            Label("Capacity:"), capacityField,
            Label("Location:"), locationField,
            Label("Facilities:"), facilitiesField,
            addButton,
            statusLabel
        )

        return form
    }

    private fun updateVenueList() {
        val venues = eventManager.getAllVenues()
        val venueStrings = venues.map { venue ->
            "${venue.name} - Capacity: ${venue.capacity}"
        }
        venueListView.items = FXCollections.observableArrayList(venueStrings)
    }

}
