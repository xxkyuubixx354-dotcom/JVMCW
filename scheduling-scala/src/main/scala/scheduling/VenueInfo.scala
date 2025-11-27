package scheduling

case class VenueInfo(
                      id: String,
                      name: String,
                      capacity: Int,
                      location: String,
                      bookedSlots: List[TimeSlotScala]
                    )
