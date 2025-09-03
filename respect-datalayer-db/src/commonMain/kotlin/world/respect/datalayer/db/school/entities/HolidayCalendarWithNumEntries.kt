package world.respect.datalayer.db.school.entities

import kotlinx.serialization.Serializable

@Serializable
class HolidayCalendarWithNumEntries : HolidayCalendar() {

    var numEntries: Int = 0
}
