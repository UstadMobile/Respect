package world.respect.datalayer.db.school.xapi.ext

import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntity

internal val XapiStatementEntity.hasResult: Boolean
    get() = resultCompletion != null ||
            resultSuccess != null ||
            resultResponse != null ||
            resultDuration != null ||
            resultExtensions != null ||
            hasResultScore

internal val XapiStatementEntity.hasResultScore: Boolean
    get() = resultScoreScaled != null ||
            resultScoreRaw != null  ||
            resultScoreMin != null ||
            resultScoreMax != null

internal val XapiStatementEntity.hasContext: Boolean
    get() = contextRegistrationHi != 0L ||
            contextRegistrationLo != 0L ||
            contextInstructorActorUid != 0L

