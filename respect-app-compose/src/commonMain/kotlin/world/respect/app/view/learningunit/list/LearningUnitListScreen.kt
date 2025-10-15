@file:Suppress("UNCHECKED_CAST")

package world.respect.app.view.learningunit.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.classes
import world.respect.shared.generated.resources.duration
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.RespectListSortHeader
import world.respect.app.components.defaultItemPadding
import world.respect.shared.viewmodel.app.appstate.getTitle
import world.respect.shared.viewmodel.learningunit.detail.LearningUnitDetailViewModel.Companion.IMAGE
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListUiState
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel
import world.respect.shared.util.SortOrderOption
import world.respect.lib.opds.model.OpdsPublication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.viewmodel.learningunit.list.LearningUnitListViewModel.Companion.ICON

@Composable
fun LearningUnitListScreen(
    viewModel: LearningUnitListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LearningUnitListScreen(
        uiState = uiState,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onClickPublication = { viewModel.onClickPublication(it) },
        onClickNavigation = { viewModel.onClickNavigation(it) }
    )
}

@Composable
fun LearningUnitListScreen(
    uiState: LearningUnitListUiState,
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onClickPublication: (OpdsPublication) -> Unit,
    onClickNavigation: (ReadiumLink) -> Unit

) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        RespectListSortHeader(
            modifier = Modifier.defaultItemPadding(),
            activeSortOrderOption = uiState.activeSortOrderOption,
            sortOptions = uiState.sortOptions,
            enabled = uiState.fieldsEnabled,
            onClickSortOption = onSortOrderChanged,
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            itemsIndexed(
                items = uiState.navigation,
                key = { index, navigation ->
                    navigation.href
                }
            ) { index, navigation ->
                NavigationListItem(
                    navigation,
                    onClickNavigation = {
                        onClickNavigation(navigation)
                    }
                )
            }

            itemsIndexed(
                items = uiState.publications,
                key = { index, publications ->
                    publications.metadata.identifier.toString()
                }
            ) { index, publication ->
                PublicationListItem(
                    publication,
                    onClickPublication = {
                        onClickPublication(publication)
                    }
                )
            }

            uiState.group.forEach { group ->
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = group.metadata.title,
                            )
                        }
                    )
                }

                itemsIndexed(
                    items = group.navigation ?: emptyList(),
                    key = { index, navigation ->
                        navigation.href
                    }
                ) { index, navigation ->
                    NavigationListItem(
                        navigation,
                        onClickNavigation = {
                            onClickNavigation(navigation)
                        }
                    )
                }
                itemsIndexed(
                    items = group.publications ?: emptyList(),
                    key = { index, publication ->
                        publication.metadata.identifier.toString()
                    }
                ) { index, publication ->
                    PublicationListItem(
                        publication,
                        onClickPublication = {
                            onClickPublication(publication)
                        }
                    )
                }

            }
        }
    }

}

@Composable
fun NavigationListItem(
    navigation: ReadiumLink,
    onClickNavigation: (ReadiumLink) -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clickable {
                onClickNavigation(navigation)
            },

        leadingContent = {

            val iconUrl = navigation.alternate?.find {
                it.rel?.contains(ICON) == true
            }?.href

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                iconUrl.also { icon ->
                    RespectAsyncImage(
                        uri = icon,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                    )
                }
            }
        },

        headlineContent = {
            Text(
                text = navigation.title.toString()
            )
        },

        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(Res.string.classes),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    navigation.language
                        ?.let { language ->
                            Text(
                                text = language.joinToString(", ")
                            )
                        }

                    navigation.duration
                        ?.let { duration ->
                            Text(
                                text = "${stringResource(Res.string.duration)} - $duration"
                            )
                        }
                }
            }
        },

        trailingContent = {
            Icon(
                imageVector = Icons.Filled.ArrowCircleDown,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    )
}

@Composable
fun PublicationListItem(
    publication: OpdsPublication, onClickPublication: (OpdsPublication) -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clickable {
                onClickPublication(publication)
            },

        leadingContent = {
            val iconUrl = publication.images?.find {
                it.type?.contains(IMAGE) == true
            }?.href

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                iconUrl.also { icon ->
                    RespectAsyncImage(
                        uri = icon,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                    )
                }
            }
        },

        headlineContent = {
            Text(
                text = publication.metadata.title.getTitle()
            )
        },

        supportingContent = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(Res.string.classes),
                )
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    publication.metadata.language
                        ?.let { language ->
                            Text(
                                text = language.joinToString(", ")
                            )
                        }

                    publication.metadata.duration
                        ?.let { duration ->
                            Text(text = "${stringResource(Res.string.duration)} - $duration")
                        }
                }
            }
        },

        trailingContent = {
            Icon(
                imageVector = Icons.Filled.ArrowCircleDown,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    )
}