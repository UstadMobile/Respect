package world.respect.app.view.curriculum.mapping.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import world.respect.datalayer.db.curriculum.entities.TextbookMapping
import world.respect.shared.viewmodel.curriculum.mapping.list.CurriculumMappingListUiState
import world.respect.shared.viewmodel.curriculum.mapping.list.CurriculumMappingListViewModel


@Composable
fun CurriculumMappingListScreen(
    uiState: CurriculumMappingListUiState = CurriculumMappingListUiState(),
    onClickTextbook: (TextbookMapping) -> Unit = { },
    onClickMoreOptions: (TextbookMapping) -> Unit = { },
    onClickMap: () -> Unit = { },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Textbooks",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.textbooks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No textbooks available",
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
            .testTag("textbook_card_${textbook.uid}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = textbook.title ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onClickMoreOptions(textbook) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More options"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (textbook.coverImageUrl != null) {
                    AsyncImage(
                        model = textbook.coverImageUrl,
                        contentDescription = "Book cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = textbook.title?.take(2)?.uppercase() ?: "??",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
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