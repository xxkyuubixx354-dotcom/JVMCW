package scheduling

import java.time.LocalDateTime
import scala.annotation.tailrec

case class EventRequest(
                         id: String,
                         title: String,
                         duration: Int, // in hours
                         preferredStartTime: Option[LocalDateTime] = None,
                         requiredCapacity: Int
                       )

case class ScheduledEvent(
                           eventRequest: EventRequest,
                           venue: VenueInfo,
                           startTime: LocalDateTime,
                           endTime: LocalDateTime
                         )

case class Schedule(
                     scheduledEvents: List[ScheduledEvent],
                     unscheduledEvents: List[EventRequest]
                   )

object EventScheduler {

  /**
   * Creates a conflict-free schedule for a list of events and venues
   * Uses a greedy algorithm with backtracking
   */
  def createSchedule(
                      events: List[EventRequest],
                      venues: List[VenueInfo],
                      defaultStartTime: LocalDateTime
                    ): Schedule = {

    // Sort events by priority (preferred time, then capacity requirement)
    val sortedEvents = events.sortBy(e =>
      (e.preferredStartTime.isEmpty, -e.requiredCapacity)
    )

    scheduleEvents(sortedEvents, venues, List.empty, defaultStartTime)
  }

  /**
   * Recursively schedules events
   */
  
  private def scheduleEvents(
                              remainingEvents: List[EventRequest],
                              venues: List[VenueInfo],
                              scheduled: List[ScheduledEvent],
                              currentTime: LocalDateTime
                            ): Schedule = {

    remainingEvents match {
      case Nil =>
        Schedule(scheduled, List.empty)

      case event :: rest =>
        findSlotForEvent(event, venues, scheduled, currentTime) match {
          case Some(scheduledEvent) =>
            // Successfully scheduled, continue with updated venue info
            val updatedVenues = updateVenueBookings(venues, scheduledEvent)
            scheduleEvents(rest, updatedVenues, scheduledEvent :: scheduled, currentTime)

          case None =>
            // Could not schedule this event, add to unscheduled
            val finalSchedule = scheduleEvents(rest, venues, scheduled, currentTime)
            Schedule(finalSchedule.scheduledEvents, event :: finalSchedule.unscheduledEvents)
        }
    }
  }

  /**
   * Finds a suitable slot for an event
   */
  private def findSlotForEvent(
                                event: EventRequest,
                                venues: List[VenueInfo],
                                alreadyScheduled: List[ScheduledEvent],
                                defaultTime: LocalDateTime
                              ): Option[ScheduledEvent] = {

    val startTime = event.preferredStartTime.getOrElse(defaultTime)
    val suitableVenues = venues.filter(_.capacity >= event.requiredCapacity)

    // Try to find available slot in each venue
    suitableVenues.flatMap { venue =>
      findAvailableSlotInVenue(venue, event, startTime, alreadyScheduled)
    }.sortBy(_.startTime).headOption
  }

  /**
   * Find available slot in a specific venue
   */
  private def findAvailableSlotInVenue(
                                        venue: VenueInfo,
                                        event: EventRequest,
                                        preferredStart: LocalDateTime,
                                        scheduled: List[ScheduledEvent]
                                      ): Option[ScheduledEvent] = {

    @tailrec
    def trySlot(candidateTime: LocalDateTime, attempts: Int): Option[ScheduledEvent] = {
      if (attempts > 336) None // Give up after 2 weeks
      else {
        val endTime = candidateTime.plusHours(event.duration.toLong)
        if (isSlotFree(venue, candidateTime, endTime, scheduled)) {
          Some(ScheduledEvent(event, venue, candidateTime, endTime))
        } else {
          trySlot(candidateTime.plusHours(1), attempts + 1)
        }
      }
    }

    trySlot(preferredStart, 0)
  }

  /**
   * Checks if a slot is free from conflicts
   */
  private def isSlotFree(
                          venue: VenueInfo,
                          startTime: LocalDateTime,
                          endTime: LocalDateTime,
                          scheduled: List[ScheduledEvent]
                        ): Boolean = {
    // Check against already booked slots
    val noExistingConflicts = !venue.bookedSlots.exists(slot =>
      overlaps(slot.startTime, slot.endTime, startTime, endTime)
    )

    // Check against newly scheduled events in same venue
    val noNewConflicts = !scheduled.exists { se =>
      se.venue.id == venue.id &&
        overlaps(se.startTime, se.endTime, startTime, endTime)
    }

    noExistingConflicts && noNewConflicts
  }

  /**
   * Check for time overlap
   */
  private def overlaps(
                        start1: LocalDateTime,
                        end1: LocalDateTime,
                        start2: LocalDateTime,
                        end2: LocalDateTime
                      ): Boolean = {
    !(end2.isBefore(start1) || end2.isEqual(start1) ||
      start2.isAfter(end1) || start2.isEqual(end1))
  }

  /**
   * Updates venue with new booking
   */
  private def updateVenueBookings(
                                   venues: List[VenueInfo],
                                   scheduled: ScheduledEvent
                                 ): List[VenueInfo] = {
    venues.map { venue =>
      if (venue.id == scheduled.venue.id) {
        val newSlot = TimeSlotScala(scheduled.startTime, scheduled.endTime)
        venue.copy(bookedSlots = newSlot :: venue.bookedSlots)
      } else {
        venue
      }
    }
  }

  /**
   * Validates a complete schedule for conflicts
   */
  def validateSchedule(schedule: Schedule): Boolean = {
    val events = schedule.scheduledEvents

    // Check for any overlapping events in the same venue
    events.combinations(2).forall { case List(e1, e2) =>
      e1.venue.id != e2.venue.id ||
        !overlaps(e1.startTime, e1.endTime, e2.startTime, e2.endTime)
    }
  }

  /**
   * Generate schedule statistics
   */
  def getScheduleStats(schedule: Schedule): Map[String, Int] = {
    Map(
      "totalEvents" -> (schedule.scheduledEvents.length + schedule.unscheduledEvents.length),
      "scheduled" -> schedule.scheduledEvents.length,
      "unscheduled" -> schedule.unscheduledEvents.length,
      "venuesUsed" -> schedule.scheduledEvents.map(_.venue.id).distinct.length
    )
  }
}
