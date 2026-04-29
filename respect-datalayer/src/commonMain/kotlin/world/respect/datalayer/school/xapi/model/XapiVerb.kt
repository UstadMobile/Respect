package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.Serializable


const val VERB_COMPLETED = "http://adlnet.gov/expapi/verbs/completed"

const val VERB_PROGRESSED = "http://adlnet.gov/expapi/verbs/progressed"

const val VERB_CREATED = "http://adlnet.gov/expapi/verbs/create"

const val VERB_UPDATED = "http://adlnet.gov/expapi/verbs/update"

const val VERB_DELETED = "http://adlnet.gov/expapi/verbs/deleted"

@Serializable
data class XapiVerb(
    val id: String,

    val display: Map<String, String>? = null,
)
