package ui

import domain.EventManager
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage
import javafx.geometry.Insets

class EventPlannerApp : Application() {
    val eventManager = EventManager()

    val participantRegistrationView = ParticipantRegistrationView(eventManager)

    val eventCreationView = EventCreationView(eventManager) {
        participantRegistrationView.refreshEvents()
    }

    val venueManagementView = VenueManagementView(eventManager)

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Event Planning Manager"

        val tabPane = TabPane()

        val venueTab = Tab("Venues", venueManagementView.createView())
        val eventTab = Tab("Events", eventCreationView.createView())
        val participantTab = Tab("Participants", participantRegistrationView.createView())
        val schedulerTab = Tab("Scheduler", SchedulerView(eventManager).createView())
        schedulerTab.isClosable = false

        tabPane.tabs.add(schedulerTab)

        venueTab.isClosable = false
        eventTab.isClosable = false
        participantTab.isClosable = false

        tabPane.tabs.addAll(venueTab, eventTab, participantTab)

        val scene = Scene(tabPane, 1000.0, 700.0)
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

    exitItem.setOnAction {
        javafx.application.Platform.exit()
    }

    fileMenu.items.addAll(saveItem, loadItem, exitItem)
    menuBar.menus.add(fileMenu)

    return menuBar
  }
}


