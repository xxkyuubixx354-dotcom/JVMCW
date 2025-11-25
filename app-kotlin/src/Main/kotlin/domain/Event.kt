package domain

import java.time.LocalDateTime
import java.util.UUID

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val venue: Venue,
    val maxCapacity: Int = venue.capacity,
    val organizer: String,
    val category: EventCategory = EventCategory.GENERAL,
    val status: EventStatus = EventStatus.PLANNED
) {
    private val registeredParticipants: MutableList<Participant> = mutableListOf()

    val currentCapacity: Int
        get() = registeredParticipants.size

    val availableSpots: Int
        get() = maxCapacity - currentCapacity

    fun registerParticipant(participant: Participant): RegistrationResult {
        return when {
            currentCapacity >= maxCapacity ->
                RegistrationResult.Failure("Event is at full capacity")
            registeredParticipants.any { it.id == participant.id } ->
                RegistrationResult.Failure("Participant already registered")
            else -> {
                registeredParticipants.add(participant)
                RegistrationResult.Success(participant)
            }
        }
    }

    fun unregisterParticipant(participantId: String): Boolean {
        return registeredParticipants.removeIf { it.id == participantId }
    }

    fun getParticipants(): List<Participant> = registeredParticipants.toList()

    fun isFull(): Boolean = currentCapacity >= maxCapacity
}

enum class EventCategory {
    WORKSHOP, CONFERENCE, FESTIVAL, SEMINAR, GENERAL
}

enum class EventStatus {
    PLANNED, ONGOING, COMPLETED, CANCELLED
}

sealed class RegistrationResult {
    data class Success(val participant: Participant) : RegistrationResult()
    data class Failure(val reason: String) : RegistrationResult()
}
