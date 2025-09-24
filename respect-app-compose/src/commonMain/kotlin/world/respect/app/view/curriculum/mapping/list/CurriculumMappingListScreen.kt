package world.respect.app.view.curriculum.mapping.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import world.respect.datalayer.db.curriculum.entities.TextbookMapping
import world.respect.shared.viewmodel.curriculum.mapping.list.CurriculumMappingListUiState
import world.respect.shared.viewmodel.curriculum.mapping.list.CurriculumMappingListViewModel
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_book_cover
import world.respect.shared.generated.resources.textbooks
import world.respect.shared.generated.resources.no_textbooks_available
import world.respect.shared.generated.resources.map
import world.respect.shared.generated.resources.more_options

@Composable
fun CurriculumMappingListScreen(
    uiState: CurriculumMappingListUiState = CurriculumMappingListUiState(),
    onClickTextbook: (TextbookMapping) -> Unit = { },
    onClickMoreOptions: (TextbookMapping) -> Unit = { },
    onClickMap: () -> Unit = { },
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.textbooks),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.textbooks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.no_textbooks_available),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.textbooks) { textbook ->
                        TextbookCard(
                            textbook = textbook,
                            onClickTextbook = onClickTextbook,
                            onClickMoreOptions = onClickMoreOptions
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onClickMap,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("map_fab")
        ) {
            Text(
                text = stringResource(Res.string.map),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextbookCard(
    textbook: TextbookMapping,
    onClickTextbook: (TextbookMapping) -> Unit,
    onClickMoreOptions: (TextbookMapping) -> Unit
) {
    Card(
        onClick = { onClickTextbook(textbook) },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .testTag("textbook_card_${textbook.uid}"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )  {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Book,
                    contentDescription = stringResource(Res.string.textbooks),
                    modifier = Modifier
                        .size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = textbook.title ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                   modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onClickMoreOptions(textbook) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(Res.string.more_options)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (textbook.coverImageUrl != null) {
                    AsyncImage(
                        model = textbook.coverImageUrl,
                        contentDescription = stringResource(Res.string.add_book_cover),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = textbook.title?.split(" ")
                                ?.mapNotNull { it.firstOrNull()?.uppercase() }
                                ?.take(2)
                                ?.joinToString("") ?: "??",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurriculumMappingListScreenForViewModel(
    viewModel: CurriculumMappingListViewModel
) {
    val uiState: CurriculumMappingListUiState by viewModel.uiState.collectAsState()

    CurriculumMappingListScreen(
        uiState = uiState,
        onClickTextbook = viewModel::onClickTextbook,
        onClickMoreOptions = viewModel::onClickMoreOptions,
        onClickMap = viewModel::onClickMap
    )
}