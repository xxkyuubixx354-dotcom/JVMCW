package domain

import java.time.LocalDateTime

data class Venue(
    val id: String,
    val name: String,
    val capacity: Int,
    val location: String,
    val facilities: List<String> = emptyList(),
    val availableTimeSlots: MutableList<TimeSlot> = mutableListOf()
) {
    fun isAvailable(startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        return availableTimeSlots.none { slot ->
            slot.overlaps(startTime, endTime)
        }
    }

    fun bookSlot(startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        if (!isAvailable(startTime, endTime)) return false
        availableTimeSlots.add(TimeSlot(startTime, endTime, true))
        return true
    }
}

data class TimeSlot(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isBooked: Boolean = false
) {
    fun overlaps(start: LocalDateTime, end: LocalDateTime): Boolean {
        return !(end <= startTime || start >= endTime)
    }
}
