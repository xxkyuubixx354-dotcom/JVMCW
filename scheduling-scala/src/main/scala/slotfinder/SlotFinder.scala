package slotfinder

import java.time.LocalDateTime
import scala.annotation.tailrec
import scheduling.{VenueInfo, TimeSlotScala}


object SlotFinder {

  /**
   * Finds the first available venue for a given capacity and start date
   * @param venues List of available venues
   * @param requiredCapacity Minimum capacity needed
   * @param earliestStart Earliest acceptable start time
   * @param duration Duration in hours
   * @return Option of (Venue, Available start time)
   */
  def findFirstAvailableSlot(
                              venues: List[VenueInfo],
                              requiredCapacity: Int,
                              earliestStart: LocalDateTime,
                              duration: Int
                            ): Option[(VenueInfo, LocalDateTime)] = {

    // Filter venues by capacity
    val suitableVenues = venues.filter(_.capacity >= requiredCapacity)

    // Find first available slot for each venue
    val availableSlots = for {
      venue <- suitableVenues
      slot <- findNextAvailableSlot(venue, earliestStart, duration)
    } yield (venue, slot)

    // Return the earliest available slot
    availableSlots.sortBy(_._2).headOption
  }

  /**
   * Finds the next available time slot for a specific venue
   */
  private def findNextAvailableSlot(
                                     venue: VenueInfo,
                                     startTime: LocalDateTime,
                                     durationHours: Int
                                   ): Option[LocalDateTime] = {

    @tailrec
    def findSlot(candidateTime: LocalDateTime, attempts: Int = 0): Option[LocalDateTime] = {
      if (attempts > 168) None // Give up after checking a week
      else {
        val endTime = candidateTime.plusHours(durationHours.toLong)
        if (isSlotAvailable(venue, candidateTime, endTime)) {
          Some(candidateTime)
        } else {
          // Try next hour
          findSlot(candidateTime.plusHours(1), attempts + 1)
        }
      }
    }

    findSlot(startTime)
  }

  /**
   * Checks if a time slot is available (no conflicts)
   */
  private def isSlotAvailable(
                               venue: VenueInfo,
                               startTime: LocalDateTime,
                               endTime: LocalDateTime
                             ): Boolean = {
    !venue.bookedSlots.exists(slot => overlaps(slot, startTime, endTime))
  }

  /**
   * Checks if two time slots overlap
   */
  private def overlaps(
                        slot: TimeSlotScala,
                        startTime: LocalDateTime,
                        endTime: LocalDateTime
                      ): Boolean = {
    !(endTime.isBefore(slot.startTime) || endTime.isEqual(slot.startTime) ||
      startTime.isAfter(slot.endTime) || startTime.isEqual(slot.endTime))
  }

  /**
   * Find all available slots within a date range
   */
  def findAllAvailableSlots(
                             venue: VenueInfo,
                             startDate: LocalDateTime,
                             endDate: LocalDateTime,
                             durationHours: Int
                           ): List[LocalDateTime] = {

    @tailrec
    def collectSlots(
                      currentTime: LocalDateTime,
                      accumulator: List[LocalDateTime]
                    ): List[LocalDateTime] = {
      if (currentTime.isAfter(endDate)) accumulator.reverse
      else {
        val slotEnd = currentTime.plusHours(durationHours.toLong)
        if (isSlotAvailable(venue, currentTime, slotEnd)) {
          collectSlots(currentTime.plusHours(1), currentTime :: accumulator)
        } else {
          collectSlots(currentTime.plusHours(1), accumulator)
        }
      }
    }

    collectSlots(startDate, List.empty)
  }
}
