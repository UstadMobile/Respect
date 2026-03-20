package world.respect.server

import io.ktor.server.config.ApplicationConfig

data class SchoolConfig(
    val registration: RegistrationConfig
) {
    data class RegistrationConfig(
        val mode: RegistrationMode,
        val topLevelDomain: String?,
    ) {
        enum class RegistrationMode(val value : String) {
            DISABLED("disabled"),
            SUBDOMAIN("subdomain"),
            ANY_URL("any-url");

            companion object {

                fun fromValue(value: String) : RegistrationMode {
                    return entries.first { it.value == value }
                }
            }

        }

        val enabled: Boolean
            get() = mode != RegistrationMode.DISABLED
    }

    companion object {
        fun fromConfig(config: ApplicationConfig): SchoolConfig {
            val modeString = config.propertyOrNull("" +
                    "ktor.school.registration.mode"
            )?.getString() ?: RegistrationConfig.RegistrationMode.DISABLED.value

            val topLevelDomain = config.propertyOrNull(
                "ktor.school.registration.top-level-domain"
            )?.getString()

            val mode = RegistrationConfig.RegistrationMode.fromValue(modeString)

            if(mode == RegistrationConfig.RegistrationMode.SUBDOMAIN && topLevelDomain == null)
                throw IllegalStateException("Subdomain registration mode requires setting top level domain")

            return SchoolConfig(
                registration = RegistrationConfig(
                    mode = mode,
                    topLevelDomain = topLevelDomain
                )
            )
        }
    }
}