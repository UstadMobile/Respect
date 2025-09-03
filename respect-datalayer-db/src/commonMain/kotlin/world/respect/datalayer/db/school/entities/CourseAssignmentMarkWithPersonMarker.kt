package world.respect.datalayer.db.school.entities

import androidx.room.Embedded
import world.respect.datalayer.school.model.Person

class CourseAssignmentMarkWithPersonMarker: CourseAssignmentMark() {

    var isGroup: Boolean = false

    @Embedded
    var marker: Person? = null

}