package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable


const val VERB_COMPLETED = "http://adlnet.gov/expapi/verbs/completed"

const val VERB_PROGRESSED = "http://adlnet.gov/expapi/verbs/progressed"

const val VERB_ASSIGN = "http://adlnet.gov/expapi/verbs/assign"

@Serializable
data class XapiVerb(
    val id: String,
    val display: Map<String, String>? = null,
) {

    companion object {
        const val ID_VOIDED = "http://adlnet.gov/expapi/verbs/voided"

        // As per https://github.com/AICC/CMI-5_Spec_Current/blob/quartz/cmi5_spec.md#933-completed
        const val ID_COMPLETED = "http://adlnet.gov/expapi/verbs/completed"

        const val ID_SAVED = "http://activitystrea.ms/schema/1.0/saved"

    }

}
