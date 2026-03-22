package world.respect.datalayer.school.xapi.model

import kotlinx.serialization.Serializable


const val VERB_COMPLETED = "http://adlnet.gov/expapi/verbs/completed"

const val VERB_PROGRESSED = "http://adlnet.gov/expapi/verbs/progressed"

@Serializable
data class XapiVerb(
    val id: String,

    val display: Map<String, String>? = null,
)
