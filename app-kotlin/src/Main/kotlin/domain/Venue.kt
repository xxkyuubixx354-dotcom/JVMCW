package domain

import java.time.LocalDateTime

data class Venue(
    val id: String,
    val name: String,
    val capacity: Int,
    val location: String,
    val facilities: List<String> = emptyList(),
    val availableTimeSlots: MutableList<TimeSlot> = mutableListOf()
)

data class TimeSlot(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isBooked: Boolean = false
)

