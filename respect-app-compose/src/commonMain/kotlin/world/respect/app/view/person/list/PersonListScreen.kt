package world.respect.app.view.person.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import world.respect.app.components.RespectPersonAvatar
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
    val pager = remember(uiState.persons) {
        Pager(
            config = PagingConfig(20, 50, false),
            pagingSourceFactory = uiState.persons,
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            count = lazyPagingItems.itemCount,
        ) { index ->
            val person = lazyPagingItems[index]

            ListItem(
                modifier = Modifier.clickable {
                    //onClickItem()
                },
                leadingContent = {
                    RespectPersonAvatar(person?.item?.fullName() ?: "")
                },
                headlineContent = {
                    Text(person?.item?.fullName() ?: "")
                }
            )
        }
    }
}