package world.respect.datalayer.db.school.xapi.entities

/**
 * As per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#interaction-activities
 *
 * An Activity can contain properties: choices, scale, source, target, steps.
 */
enum class ActivityInteractionEntityPropEnum(
    val flag: Int,
) {
    CHOICES(1),
    SCALE(2),
    SOURCE(3),
    TARGET(4),
    STEPS(5);

    companion object {

        fun fromFlag(flag: Int): ActivityInteractionEntityPropEnum {
            return entries.firstOrNull { it.flag == flag } ?: CHOICES
        }

    }
}
