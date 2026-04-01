package world.respect.datalayer.db.school.xapi.entities


enum class ActivityLangMapEntryPropEnum(
    val flag: Int,
) {

    NAME(1),
    DESCRIPTION(2),


    CHOICES_INTERACTIONS(3),

    SCALE_INTERACTIONS(4),

    SOURCE_INTERACTIONS(5),

    TARGET_INTERACTIONS(6),

    STEPS_INTERACTIONS(7);

    companion object {

        fun fromFlag(flag: Int): ActivityLangMapEntryPropEnum {
            return entries.firstOrNull { it.flag == flag } ?: NAME
        }

    }

}