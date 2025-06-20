package world.respect.app.model.appsdetail

import world.respect.datasource.opds.model.OpdsPublication


data class Images(
    val imageUrl: String
)
data class AppsDetailModel(
    val imageName: String,
    val appName: String,
    val appDescription: String,
    val publications: List<OpdsPublication>,
    val images: List<Images>
)

