package world.respect.server

import io.ktor.server.config.ApplicationConfig

data class SchoolConfig(
    val registration: RegistrationConfig
) {
    data class RegistrationConfig(
        val enabled: Boolean,
        val mode: RegistrationMode,
        val topLevelDomain: String = ""
    ) {
        enum class RegistrationMode {
            DISABLED,
            SUBDOMAIN,
            ANY_URL
        }
    }

    companion object {
        fun fromConfig(config: ApplicationConfig): SchoolConfig {
            val registrationEnabled =
                config.propertyOrNull("ktor.school.registration.enabled")?.getString()?.toBoolean()
                    ?: false
            val modeString =
                config.propertyOrNull("ktor.school.registration.mode")?.getString() ?: "disabled"
            val topLevelDomain =
                config.propertyOrNull("ktor.school.top-level-domain")?.getString() ?: ""

            val mode = when (modeString.lowercase()) {
                "subdomain" -> RegistrationConfig.RegistrationMode.SUBDOMAIN
                "any-url" -> RegistrationConfig.RegistrationMode.ANY_URL
                else -> RegistrationConfig.RegistrationMode.DISABLED
            }

            return SchoolConfig(
                registration = RegistrationConfig(
                    enabled = registrationEnabled && mode != RegistrationConfig.RegistrationMode.DISABLED,
                    mode = mode,
                    topLevelDomain = topLevelDomain
                )
            )
        }
    }
}