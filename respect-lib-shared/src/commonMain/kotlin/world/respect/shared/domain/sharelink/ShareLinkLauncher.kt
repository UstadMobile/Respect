package world.respect.shared.domain.sharelink

interface ShareLinkLauncher {
    suspend fun launch(body: String)

    companion object {
        const val MIME_TYPE = "text/plain"
    }

}
