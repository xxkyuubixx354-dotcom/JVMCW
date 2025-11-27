package ui

import domain.*
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.geometry.Insets
import javafx.collections.FXCollections

class ParticipantRegistrationView(private val eventManager: EventManager) {

    private lateinit var eventComboBox: ComboBox<Event>


    fun createView(): BorderPane {
        val root = BorderPane()
        root.padding = Insets(20.0)

        val form = createRegistrationForm()
        root.center = form

        return root
    }

    private fun createRegistrationForm(): VBox {
        val form = VBox(15.0)
        form.padding = Insets(10.0)
        form.maxWidth = 600.0

        // Event selection
        eventComboBox = ComboBox<Event>().apply {
            items = FXCollections.observableArrayList(eventManager.getAllEvents())
            converter = object : javafx.util.StringConverter<Event>() {
                override fun toString(event: Event?): String {
                    return event?.let { "${it.title} - ${it.startDateTime}" } ?: ""
                }
                override fun fromString(string: String?): Event? = null
            }
        }

        val capacityLabel = Label()

        eventComboBox.setOnAction {
            val selectedEvent = eventComboBox.value
            if (selectedEvent != null) {
                capacityLabel.text = "Available Spots: ${selectedEvent.availableSpots} / ${selectedEvent.maxCapacity}"
            }
        }


        // Participant fields
        val firstNameField = TextField().apply { promptText = "First Name" }
        val lastNameField = TextField().apply { promptText = "Last Name" }
        val emailField = TextField().apply { promptText = "Email" }
        val phoneField = TextField().apply { promptText = "Phone Number" }
        val organizationField = TextField().apply { promptText = "Organization (optional)" }
        val dietaryField = TextField().apply { promptText = "Dietary Restrictions (comma-separated)" }
        val accessibilityArea = TextArea().apply {
            promptText = "Accessibility Needs"
            prefRowCount = 2
        }

        val registerButton = Button("Register Participant")
        val statusLabel = Label()

        // Participant list
        val participantListView = ListView<String>()

        registerButton.setOnAction {
            try {
                val selectedEvent = eventComboBox.value
                    ?: throw IllegalArgumentException("Please select an event")

                val participant = Participant(
                    firstName = firstNameField.text,
                    lastName = lastNameField.text,
                    email = emailField.text,
                    phoneNumber = phoneField.text,
                    organization = organizationField.text,
                    dietaryRestrictions = dietaryField.text.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    accessibilityNeeds = accessibilityArea.text
                )

                if (!participant.isValidEmail()) {
                    throw IllegalArgumentException("Invalid email format")
                }

                when (val result = eventManager.registerParticipantToEvent(selectedEvent.id, participant)) {
                    is RegistrationResult.Success -> {
                        statusLabel.text = "Participant registered successfully!"
                        statusLabel.style = "-fx-text-fill: green;"
                        updateParticipantList(participantListView, selectedEvent.id)
                        clearParticipantFields(firstNameField, lastNameField, emailField, phoneField,
                            organizationField, dietaryField, accessibilityArea)

                        // Update capacity label
                        capacityLabel.text = "Available Spots: ${selectedEvent.availableSpots} / ${selectedEvent.maxCapacity}"
                    }
                    is RegistrationResult.Failure -> {
                        statusLabel.text = "Registration failed: ${result.reason}"
                        statusLabel.style = "-fx-text-fill: red;"
                    }
                }

            } catch (e: Exception) {
                statusLabel.text = "Error: ${e.message}"
                statusLabel.style = "-fx-text-fill: red;"
            }
        }

        eventComboBox.setOnAction {
            val selectedEvent = eventComboBox.value
            if (selectedEvent != null) {
                updateParticipantList(participantListView, selectedEvent.id)
            }
        }

        form.children.addAll(
            Label("Participant Registration").apply { style = "-fx-font-size: 20px; -fx-font-weight: bold;" },
            Separator(),
            Label("Select Event:"), eventComboBox,
            capacityLabel,
            Separator(),
            Label("Participant Information").apply { style = "-fx-font-weight: bold;" },
            Label("First Name:"), firstNameField,
            Label("Last Name:"), lastNameField,
            Label("Email:"), emailField,
            Label("Phone:"), phoneField,
            Label("Organization:"), organizationField,
            Label("Dietary Restrictions:"), dietaryField,
            Label("Accessibility Needs:"), accessibilityArea,
            registerButton,
            statusLabel,
            Separator(),
            Label("Registered Participants").apply { style = "-fx-font-weight: bold;" },
            participantListView
        )

        return form
    }

    fun refreshEvents() {
        eventComboBox.items = FXCollections.observableArrayList(eventManager.getAllEvents())
    }

    private fun updateParticipantList(listView: ListView<String>, eventId: String) {
        val participants = eventManager.getEventParticipants(eventId)
        val participantStrings = participants.map {
            "${it.fullName} - ${it.email} - ${it.organization}"
        }
        listView.items = FXCollections.observableArrayList(participantStrings)
    }

    private fun clearParticipantFields(vararg fields: TextInputControl) {
        fields.forEach { it.clear() }
    }
}
