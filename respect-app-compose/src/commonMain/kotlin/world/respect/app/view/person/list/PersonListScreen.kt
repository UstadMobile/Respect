package world.respect.app.view.person.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_person
import world.respect.shared.generated.resources.copy_invite_code
import world.respect.shared.generated.resources.invite_person
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
        onClickItem = viewModel::onClickItem,
        onClickAddPerson = viewModel::onClickAdd,
        onClickInviteCode = viewModel::onClickInviteCode,
        onClickInvitePerson = viewModel::onClickInvitePerson,
    )
}

@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    onClickItem: (PersonListDetails) -> Unit,
    onClickAddPerson: () -> Unit,
    onClickInviteCode: () -> Unit,
    onClickInvitePerson: () -> Unit,
) {
    val pager = respectRememberPager(uiState.persons)

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if(uiState.showAddPersonItem) {
            item("add_person") {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickAddPerson()
                    },
                    headlineContent = {
                        Text(stringResource(Res.string.add_person))
                    },
                    leadingContent = {
                        Icon(Icons.Default.Add,
                            modifier = Modifier.size(40.dp).padding(8.dp),
                            contentDescription = null)
                    }
                )
            }
        }
        if(uiState.showInviteButton) {
            item("invite_person") {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickInvitePerson()
                    },
                    headlineContent = {
                        Text(stringResource(Res.string.invite_person))
                    },
                    leadingContent = {
                        Icon(Icons.Default.Share,
                            modifier = Modifier.size(40.dp).padding(8.dp),
                            contentDescription = null)
                    }
                )
            }
        }

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
