package world.respect.datalayer.db.school.xapi.ext

import world.respect.datalayer.db.school.xapi.entities.ActivityInteractionEntityPropEnum
import world.respect.datalayer.db.school.xapi.entities.ActivityLangMapEntryPropEnum

val ActivityInteractionEntityPropEnum.langMapPropEnum: ActivityLangMapEntryPropEnum
    get() = when(this) {
        ActivityInteractionEntityPropEnum.CHOICES -> ActivityLangMapEntryPropEnum.CHOICES_INTERACTIONS
        ActivityInteractionEntityPropEnum.SCALE -> ActivityLangMapEntryPropEnum.SCALE_INTERACTIONS
        ActivityInteractionEntityPropEnum.SOURCE -> ActivityLangMapEntryPropEnum.SOURCE_INTERACTIONS
        ActivityInteractionEntityPropEnum.TARGET -> ActivityLangMapEntryPropEnum.TARGET_INTERACTIONS
        ActivityInteractionEntityPropEnum.STEPS -> ActivityLangMapEntryPropEnum.STEPS_INTERACTIONS
    }
