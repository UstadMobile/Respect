package world.respect.app.view.curriculum.appbycurriculum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.*
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import world.respect.shared.viewmodel.curriculum.list.CurriculumListViewModel
import world.respect.shared.viewmodel.curriculum.list.CurriculumListUiState
import world.respect.shared.viewmodel.curriculum.list.TabConstants
import world.respect.shared.domain.curriculum.models.Curriculum


@Composable
fun AppsByCurriculumScreen(
    viewModel: CurriculumListViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppsByCurriculumScreenContent(
        uiState = uiState,
        onTabSelected = viewModel::onTabSelected,
        onAddCurriculumClick = viewModel::onAddCurriculumClick,
        onProfileClick = viewModel::onProfileClick,
        onBottomNavClick = viewModel::onBottomNavClick,
        onBackClick = viewModel::onBackClick,
        onCurriculumClick = viewModel::onCurriculumClick,
        onRefresh = viewModel::onRefresh
    )
}

@Composable
private fun AppsByCurriculumScreenContent(
    uiState: CurriculumListUiState,
    onTabSelected: (Int) -> Unit,
    onAddCurriculumClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBottomNavClick: (Any) -> Unit,
    onBackClick: () -> Unit,
    onCurriculumClick: (Curriculum) -> Unit,
    onRefresh: () -> Unit
) {
    var currentTab by remember { mutableIntStateOf(uiState.selectedTab) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null
                    )
                }
                Text(
                    text = stringResource(Res.string.apps),
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            }
        }

        TabRow(
            selectedTabIndex = currentTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = currentTab == TabConstants.FOR_YOU,
                onClick = {
                    currentTab = TabConstants.FOR_YOU
                    onTabSelected(TabConstants.FOR_YOU)
                },
                text = { Text(stringResource(Res.string.for_you)) }
            )
            Tab(
                selected = currentTab == TabConstants.BY_CURRICULUM,
                onClick = {
                    currentTab = TabConstants.BY_CURRICULUM
                    onTabSelected(TabConstants.BY_CURRICULUM)
                },
                text = { Text(stringResource(Res.string.by_curriculum)) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when (currentTab) {
                TabConstants.FOR_YOU -> {
                    Text(stringResource(Res.string.for_you))
                }

                TabConstants.BY_CURRICULUM -> {
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else if (uiState.curricula.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(Res.string.no_curricula_yet),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(Res.string.add_curriculum),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.curricula) { curriculum ->
                                CurriculumItem(
                                    curriculum = curriculum,
                                    onClick = { onCurriculumClick(curriculum) }
                                )
                            }
                        }
                    }
                }
            }
        }

        uiState.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(error),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (currentTab == TabConstants.BY_CURRICULUM) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onAddCurriculumClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(Res.string.curriculum))
                    }
                }
            }
        }
    }
}

@Composable
private fun CurriculumItem(
    curriculum: Curriculum,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = curriculum.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (curriculum.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = curriculum.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}