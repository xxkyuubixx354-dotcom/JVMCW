package ui

import domain.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.geometry.Insets
import javafx.collections.FXCollections
import java.util.UUID
import persistence.JsonDataPersistence

class VenueManagementView(private val eventManager: EventManager) {
    private lateinit var venueListView: ListView<String>  // <- DECLARE

    public fun createView(): BorderPane {
        val root = BorderPane()
        root.padding = Insets(20.0)

        val form = createVenueForm()

        venueListView = ListView<String>()
        updateVenueList()      // <- NOW WORKS (line 76)

        root.left = form
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

                // Save to JSON persistence
                val persistence = JsonDataPersistence("data")  // Adjust path as needed
                val result = persistence.saveVenues(eventManager.getAllVenues())
                if (result.isSuccess) {
                    statusLabel.text = "Venue added and saved successfully!"
                    statusLabel.style = "-fx-text-fill: green;"
                } else {
                    statusLabel.text = "Venue added but save failed: ${result.exceptionOrNull()?.message}"
                    statusLabel.style = "-fx-text-fill: orange;"
                }

                // Clear fields
                nameField.clear()
                capacityField.clear()
                locationField.clear()
                facilitiesField.clear()
                updateVenueList()  // Refresh list
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
        val venueStrings = venues.map { "${it.name} - Capacity ${it.capacity} - ${it.location}" }
        venueListView.items = FXCollections.observableArrayList(venueStrings)
    }
}
