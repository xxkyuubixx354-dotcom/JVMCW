package domain

import java.time.LocalDateTime
import persistence.JsonDataPersistence

class EventManager {
    private val events: MutableList<Event> = mutableListOf()
    private val venues: MutableList<Venue> = mutableListOf()
    private val persistence = JsonDataPersistence()

    init {
        loadData()
    }

    fun createEvent(event: Event): Result<Unit> {
        if (!isVenueAvailable(event.venue.id, event.startDateTime, event.endDateTime)) {
            return Result.failure(IllegalArgumentException("Venue is not available at this time"))
        }

        return try {
            events.add(event)
            saveData()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isVenueAvailable(
        venueId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): Boolean {
        return events
            .filter { it.venue.id == venueId }
            .none { existing ->
                start < existing.endDateTime && end > existing.startDateTime
            }
    }


    fun getAllEvents(): List<Event> = events.toList()

    fun getEventById(id: String): Event? = events.find { it.id == id }

    fun deleteEvent(eventId: String): Boolean {
        return events.removeIf { it.id == eventId }
    }

    // Venue Management
    fun addVenue(venue: Venue) {
        venues.add(venue)
    }

    fun getAllVenues(): List<Venue> = venues.toList()


    // Participant Management
    fun registerParticipantToEvent(eventId: String, participant: Participant): RegistrationResult {
        val event = getEventById(eventId)
            ?: return RegistrationResult.Failure("Event not found")
        return event.registerParticipant(participant)
    }

    fun getEventParticipants(eventId: String): List<Participant> {
        return getEventById(eventId)?.getParticipants() ?: emptyList()
    }

    fun removeParticipantFromEvent(eventId: String, participantIndex: Int): Boolean {
        val event = getEventById(eventId) ?: return false
        val removed = event.removeParticipantAt(participantIndex)
        if (removed) {
            saveData()
        }
        return removed
    }



    fun saveData(): Result<Unit> {
        val venueResult = persistence.saveVenues(venues)
        val eventResult = persistence.saveEvents(events)

        return if (venueResult.isSuccess && eventResult.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to save data"))
        }
    }

    fun removeVenue(venueId: String): Boolean {
        val venue = venues.find { it.id == venueId } ?: return false
        return venues.remove(venue)
    }

    private fun loadData() {
        persistence.loadVenues().onSuccess { loadedVenues ->
            venues.clear()
            venues.addAll(loadedVenues)
        }

        persistence.loadEvents().onSuccess { loadedEvents ->
            events.clear()
            events.addAll(loadedEvents)
        }
    }
}
