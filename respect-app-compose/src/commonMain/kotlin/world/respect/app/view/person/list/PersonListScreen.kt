package world.respect.app.view.person.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.shared.util.ext.fullName
import world.respect.shared.viewmodel.person.list.PersonListUiState
import world.respect.shared.viewmodel.person.list.PersonListViewModel

@Composable
fun PersonListScreen(
    viewModel: PersonListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    PersonListScreen(
        uiState = uiState,
        onClickItem = viewModel::onClickItem
    )
}

@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    onClickItem: (PersonListDetails) -> Unit,
) {
    val pager = respectRememberPager(uiState.persons)

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.guid ?: index.toString() },
            contentType = { PersonDataSource.ENDPOINT_NAME },
        ) { person ->
            ListItem(
                modifier = Modifier.clickable {
                    person?.also(onClickItem)
                },
                leadingContent = {
                    RespectPersonAvatar(person?.fullName() ?: "")
                },
                headlineContent = {
                    Text(person?.fullName() ?: "")
                }
            )
        }
    }
}
