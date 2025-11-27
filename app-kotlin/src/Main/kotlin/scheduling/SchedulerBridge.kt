package scheduling

import scheduling.EventScheduler
import scheduling.EventRequest
import scheduling.Schedule
import domain.Event
import domain.Venue
import java.time.LocalDateTime
import scheduling.VenueInfo

object SchedulerBridge {

    fun scheduleEvents(
        events: List<Event>,
        venues: List<Venue>,
        defaultStartTime: LocalDateTime
    ): ScheduleResult {

        // Convert to Scala types
        val scalaEvents = events.map { event ->
            EventRequest(
                event.id,
                event.title,
                java.time.Duration.between(event.startDateTime, event.endDateTime).toHours().toInt(),
                scala.Option.apply(event.startDateTime),
                event.maxCapacity
            )
        }
        val scalaVenues = venues.map { venue ->
            scheduling.VenueInfo(
                venue.id,
                venue.name,
                venue.capacity,
                venue.location,
                convertToScalaList(venue.availableTimeSlots.map {
                    scheduling.TimeSlotScala(it.startTime, it.endTime)
                })
            )
        }

        val schedule = EventScheduler.createSchedule(
            convertToScalaList(scalaEvents),
            convertToScalaList(scalaVenues),
            defaultStartTime
        )

        return ScheduleResult(
            scheduled = convertFromScalaList(schedule.scheduledEvents()).map {
                ScheduledEventKotlin(
                    it.eventRequest().id(),
                    it.venue().name(),
                    it.startTime(),
                    it.endTime()
                )
            },
            unscheduled = convertFromScalaList(schedule.unscheduledEvents()).map { it.id() }
        )
    }

    private fun <T> convertToScalaList(list: List<T>): scala.collection.immutable.List<T> {
        return scala.jdk.CollectionConverters.ListHasAsScala(list).asScala().toList()
    }

    private fun <T> convertFromScalaList(scalaList: scala.collection.immutable.List<T>): List<T> {
        return scala.jdk.CollectionConverters.SeqHasAsJava(scalaList).asJava().toList()
    }
}

data class ScheduledEventKotlin(
    val eventId: String,
    val venueName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

data class ScheduleResult(
    val scheduled: List<ScheduledEventKotlin>,
    val unscheduled: List<String>
)
