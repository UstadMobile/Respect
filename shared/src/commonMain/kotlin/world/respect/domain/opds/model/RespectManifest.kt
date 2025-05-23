package world.respect.domain.opds.model

import kotlinx.serialization.Serializable

/**
 * Represents the RESPECT manifest as described in the RESPECT Launcher App Integration Guide.
 * This manifest provides information about the app for the RESPECT launcher.
 */
@Serializable
data class RespectManifest(
    val name: Map<String, String>,     // Required: language map of the app's name
    val license: String,               // Required: the license ID for the app itself
    val learningUnits: String,         // Required: link to an OPDS 2.0 catalog of Learning Units
    val defaultLaunchUri: String,      // Required: URL that the RESPECT launcher app will use
    val android: AndroidDetails? = null // Optional: Android-specific details
) {
    companion object {
        const val DEFAULT_MANIFEST_PATH = ".well-known/respect-app.json"
    }
}

/**
 * Contains Android-specific details for the RESPECT manifest.
 */
@Serializable
data class AndroidDetails(
    val packageId: String,             // Required: package id of the app on Android
    val stores: List<String> = emptyList() // Optional: List of app store URLs
)