package ui

import domain.EventManager
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class EventPlannerApp : Application() {

    private val eventManager = EventManager()

    private val participantRegistrationView = ParticipantRegistrationView(eventManager)
    private val eventCreationView = EventCreationView(eventManager) {
        participantRegistrationView.refreshEvents()
    }
    private val venueManagementView = VenueManagementView(eventManager)

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Event Planning Manager"

        val tabPane = TabPane()

        val venueTab = Tab("Venues", venueManagementView.createView())
        val eventTab = Tab("Events", eventCreationView.createView())
        val participantTab = Tab("Participants", participantRegistrationView.createView())
        val schedulerTab = Tab("Scheduler", SchedulerView(eventManager).createView())

        venueTab.isClosable = false
        eventTab.isClosable = false
        participantTab.isClosable = false
        schedulerTab.isClosable = false

        tabPane.tabs.addAll(schedulerTab, venueTab, eventTab, participantTab)


        eventTab.setOnSelectionChanged {
            if (eventTab.isSelected) {
                eventCreationView.refreshVenues()
            }
        }

        val root = BorderPane().apply {
            padding = Insets(0.0)
            top = createMenuBar(eventManager)
            center = tabPane
        }

        val scene = Scene(root, 1000.0, 700.0)
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun createMenuBar(eventManager: EventManager): MenuBar {
        val menuBar = MenuBar()

        val fileMenu = Menu("File")
        val saveItem = MenuItem("Save All Data")
        val loadItem = MenuItem("Reload Data")
        val exitItem = MenuItem("Exit")

        saveItem.setOnAction {
            eventManager.saveData().onSuccess {
                Alert(Alert.AlertType.INFORMATION, "Data saved successfully!").show()
            }.onFailure {
                Alert(Alert.AlertType.ERROR, "Failed to save data: ${it.message}").show()
            }
        }


        loadItem.isDisable = true

        exitItem.setOnAction {
            javafx.application.Platform.exit()
        }

        fileMenu.items.addAll(saveItem, loadItem, exitItem)
        menuBar.menus.add(fileMenu)

        return menuBar
    }
}
