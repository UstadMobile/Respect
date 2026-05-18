package world.respect.shared.domain.externallink

interface OpenExternalLinkUseCase {
    
    /**
     *
     * @param url The URL to open
     * @param title Optional title to display for the content
     */
    operator fun invoke(
        url: String,
        title: String? = null,
    )
}

