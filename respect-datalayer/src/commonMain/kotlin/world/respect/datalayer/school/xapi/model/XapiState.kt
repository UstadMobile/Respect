package world.respect.datalayer.school.xapi.model

class XapiState(
    val stateId: String?,
    val agent: XapiActor?,
    val activityId: String?,
    val content: HashMap<String, Any>?,
    val registration: String?
)
