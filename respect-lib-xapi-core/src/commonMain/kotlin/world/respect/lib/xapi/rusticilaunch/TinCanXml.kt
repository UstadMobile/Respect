package world.respect.lib.xapi.rusticilaunch

/*
PENDING UPDATE to Kotlin 2.4.0
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

const val TINCAN_XML_NAMESPACE = "http://projecttincan.com/tincan.xsd"

@Serializable
@XmlSerialName("tincan", TINCAN_XML_NAMESPACE, "")
data class TinCanXml(
    val activities: TinCanActivities
)

@Serializable
@XmlSerialName("activities", TINCAN_XML_NAMESPACE, "")
data class TinCanActivities(
    val activity: List<TinCanActivity>
)

@Serializable
@XmlSerialName("activity", TINCAN_XML_NAMESPACE, "")
data class TinCanActivity(
    @XmlElement(false)
    val id: String,
    @XmlElement(false)
    val type: String? = null,
    val name: String? = null,
    val description: TinCanLangText? = null,
    val launch: TinCanLangText? = null,
)

@Serializable
data class TinCanLangText(
    @XmlElement(false)
    val lang: String,
    @XmlValue
    val value: String,
)
 */