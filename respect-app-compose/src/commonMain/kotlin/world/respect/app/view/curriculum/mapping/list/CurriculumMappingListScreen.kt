package world.respect.app.view.curriculum.mapping.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.map
import world.respect.shared.generated.resources.more_options
import world.respect.shared.generated.resources.no_textbooks_available
import world.respect.shared.generated.resources.textbooks
import world.respect.shared.viewmodel.curriculum.mapping.list.CurriculumMappingListUiState
import world.respect.shared.viewmodel.curriculum.mapping.list.CurriculumMappingListViewModel
import world.respect.shared.viewmodel.curriculum.mapping.model.CurriculumMapping

@Composable
fun CurriculumMappingListScreen(
    uiState: CurriculumMappingListUiState = CurriculumMappingListUiState(),
    onClickMapping: (CurriculumMapping) -> Unit = {},
    onClickMoreOptions: (CurriculumMapping) -> Unit = {},
    onClickMap: () -> Unit = {},
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

            if (uiState.mappings.isEmpty()) {
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
                    items(
                        items = uiState.mappings,
                        key = { mapping -> mapping.uid }
                    ) { mapping ->
                        MappingCard(
                            mapping = mapping,
                            onClickMapping = onClickMapping,
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
private fun MappingCard(
    mapping: CurriculumMapping,
    onClickMapping: (CurriculumMapping) -> Unit,
    onClickMoreOptions: (CurriculumMapping) -> Unit
) {
    Card(
        onClick = { onClickMapping(mapping) },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Book,
                    contentDescription = stringResource(Res.string.textbooks),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = mapping.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onClickMoreOptions(mapping) },
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mapping.title.split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2)
                            .joinToString(""),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CurriculumMappingListScreenForViewModel(
    viewModel: CurriculumMappingListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CurriculumMappingListScreen(
        uiState = uiState,
        onClickMapping = viewModel::onClickMapping,
        onClickMoreOptions = viewModel::onClickMoreOptions,
        onClickMap = viewModel::onClickMap
    )
}