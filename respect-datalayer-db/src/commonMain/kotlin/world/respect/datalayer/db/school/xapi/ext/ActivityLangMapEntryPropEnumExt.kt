package world.respect.datalayer.db.school.xapi.ext

import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntityPropEnum
import world.respect.datalayer.db.school.xapi.entities.ActivityLangMapEntryPropEnum

/**
 *
 */
val ActivityLangMapEntryPropEnum.isActivityProp: Boolean
    get() = this == ActivityLangMapEntryPropEnum.NAME ||
            this == ActivityLangMapEntryPropEnum.DESCRIPTION


val ActivityLangMapEntryPropEnum.interactionProp: ActivityInteractionEntityPropEnum?
    get() = when(this) {
        ActivityLangMapEntryPropEnum.CHOICES_INTERACTIONS -> ActivityInteractionEntityPropEnum.CHOICES
        ActivityLangMapEntryPropEnum.SCALE_INTERACTIONS -> ActivityInteractionEntityPropEnum.SCALE
        ActivityLangMapEntryPropEnum.SOURCE_INTERACTIONS -> ActivityInteractionEntityPropEnum.SOURCE
        ActivityLangMapEntryPropEnum.TARGET_INTERACTIONS -> ActivityInteractionEntityPropEnum.TARGET
        ActivityLangMapEntryPropEnum.STEPS_INTERACTIONS -> ActivityInteractionEntityPropEnum.STEPS
        else -> null
    }


