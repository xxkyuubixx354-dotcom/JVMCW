package scheduling

import slotfinder.SlotFinder
import scheduling.`VenueInfo$`
import scheduling.VenueInfo
import scheduling.TimeSlotScala
import domain.Venue
import java.time.LocalDateTime
import scala.collection.immutable.`$colon$colon`
import scala.collection.immutable.`Nil$`
import scala.jdk.CollectionConverters

object SlotFinderBridge {

    fun findAvailableSlot(
        venues: List<Venue>,
        requiredCapacity: Int,
        earliestStart: LocalDateTime,
        durationHours: Int
    ): Pair<Venue, LocalDateTime>? {

        // Convert Kotlin venues to Scala venues
        val scalaVenues = venues.map { venue ->
            VenueInfo(
                venue.id,
                venue.name,
                venue.capacity,
                venue.location,
                convertToScalaList(venue.availableTimeSlots.map {
                    TimeSlotScala(it.startTime, it.endTime)
                })
            )
        }

        val result = SlotFinder.findFirstAvailableSlot(
            convertToScalaList(scalaVenues),
            requiredCapacity,
            earliestStart,
            durationHours
        )

        return if (result.isDefined) {
            val tuple = result.get()
            val scalaVenue = tuple._1      // First element
            val time = tuple._2            // Second element
            val originalVenue = venues.find { it.id == scalaVenue.id() }
            originalVenue?.let { it to time }
        } else {
            null
        }
    }

        private fun <T> convertToScalaList(list: List<T>): scala.collection.immutable.List<T> {
        return scala.jdk.CollectionConverters.ListHasAsScala(list).asScala().toList()
    }
}
