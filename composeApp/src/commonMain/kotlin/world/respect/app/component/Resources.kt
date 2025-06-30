package world.respect.app.component

import org.jetbrains.compose.resources.StringResource

interface Resources {
    suspend fun getString(resource: StringResource): String
}