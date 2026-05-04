package world.respect.app.view.manageuser.accountlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectLongVersionInfoItem
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.app.view.clazz.detail.ClassPendingPersonListItem
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Person
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_account
import world.respect.shared.generated.resources.collapse_pending_invites
import world.respect.shared.generated.resources.developed_by
import world.respect.shared.generated.resources.enter_code_label
import world.respect.shared.generated.resources.expand_pending_invites
import world.respect.shared.generated.resources.family_members
import world.respect.shared.generated.resources.license_text
import world.respect.shared.generated.resources.logout
import world.respect.shared.generated.resources.pending_requests
import world.respect.shared.generated.resources.profile
import world.respect.shared.generated.resources.respect_is_open_source
import world.respect.shared.generated.resources.student
import world.respect.shared.generated.resources.supported_by_spix_foundation
import world.respect.shared.generated.resources.use_another_account
import world.respect.shared.viewmodel.manageuser.accountlist.AccountListUiState
import world.respect.shared.viewmodel.manageuser.accountlist.AccountListViewModel

@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    AccountListScreen(
        uiState = uiState,
        onClickAccount = viewModel::onClickAccount,
        onClickAddAccount = viewModel::onClickAddAccount,
        onClickLogout = viewModel::onClickLogout,
        onClickFamilyPerson = viewModel::onClickFamilyPerson,
        onClickProfile = viewModel::onClickProfile,
        onClickEnterInviteCode = viewModel::onClickEnterInviteCode,
        onTogglePendingSection = viewModel::onTogglePendingSection,
        )
}

@Composable
fun AccountListScreen(
    uiState: AccountListUiState,
    onClickAccount: (RespectAccount) -> Unit,
    onClickFamilyPerson: (Person) -> Unit,
    onClickAddAccount: () -> Unit,
    onClickEnterInviteCode: () -> Unit,
    onClickLogout: () -> Unit,
    onTogglePendingSection: () -> Unit,
    onClickProfile: () -> Unit,
) {

    val familyPersons = uiState.selectedAccount?.relatedPersons ?: emptyList()


    LazyColumn(modifier = Modifier.fillMaxSize()) {
        uiState.selectedAccount?.also { activeAccount ->
            item("selected_account") {
                AccountListItem(
                    account = activeAccount,
                    onClickAccount = null,
                    extras = {
                        Row {
                            if(uiState.showSelectedAccountProfileButton) {
                                OutlinedButton(
                                    onClick = onClickProfile,
                                ) {
                                    Text(stringResource(Res.string.profile))
                                }

                                Spacer(Modifier.width(16.dp))
                            }

                            OutlinedButton(onClick = onClickLogout) {
                                Text(stringResource(Res.string.logout))
                            }
                        }
                    }
                )
            }

            item("enter_invite_code") {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickEnterInviteCode()
                    },
                    headlineContent = {
                        Text(stringResource(Res.string.enter_code_label))
                    },
                    leadingContent = {
                        Icon(Icons.Default.Code, contentDescription = "")
                    }
                )
            }
        }
        if (uiState.pendingEnrolmentPerson.isNotEmpty()) {
            item("pending_header") {
                ListItem(
                    modifier = Modifier
                        .clickable { onTogglePendingSection() },
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.pending_requests)

                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription =
                                if (uiState.isPendingExpanded) {
                                    stringResource(
                                        Res.string.collapse_pending_invites
                                    )
                                } else {
                                    stringResource(
                                        Res.string.expand_pending_invites
                                    )
                                },
                            modifier = Modifier.size(24.dp)
                                .rotate(
                                    if (uiState.isPendingExpanded) 0f else -90f
                                ),
                        )
                    }
                )
            }
        }
        if (uiState.isPendingExpanded) {
            items(
                items = uiState.pendingEnrolmentPerson,
                key = { it.person.guid.hashCode() }
            ) { item ->
                PendingPersonEnrollmentItem(
                    personWithEnrollment = item
                )
            }
        }
        if (!familyPersons.isEmpty()) {
            item("family_member_header") {
                Text(
                    modifier = Modifier.defaultItemPadding(),
                    text = stringResource(Res.string.family_members)
                )
            }

            items(
                items = familyPersons,
                key = { it.guid }
            ) { account ->
                ListItem(
                    modifier = Modifier.clickable(
                        enabled = uiState.familyMembersClickEnabled,
                    ) {
                        onClickFamilyPerson(account)
                    },
                    leadingContent = {
                        RespectPersonAvatar(name = account.fullName())
                    },
                    headlineContent = {
                        Text(account.fullName())
                    }
                )
            }
        }

        item("divider1") {
            HorizontalDivider()
        }

        items(
            items = uiState.accounts,
            key = { it.session.account.userGuid }
        ) { account ->
            AccountListItem(
                account = account,
                onClickAccount = onClickAccount,
            )
        }

        item("divider2") {
            HorizontalDivider()
        }

        item("add_account") {
            val text = if (uiState.isSelectAccountMode) Res.string.use_another_account else Res.string.add_account
            ListItem(
                modifier = Modifier.clickable {
                    onClickAddAccount()
                },
                headlineContent = {
                    Text(stringResource(text))
                },
                leadingContent = {
                    Icon(Icons.Default.Add, contentDescription = "")
                }
            )
        }
        item("divider3") {
            HorizontalDivider()
        }

        item("version_info") {
            RespectLongVersionInfoItem()
        }

        item("copyright_info") {
            HorizontalDivider()
            ListItem(
                headlineContent = {
                    Text(stringResource(Res.string.developed_by))
                },
                supportingContent = {
                    Column {
                        Text(stringResource(Res.string.supported_by_spix_foundation))
                        Text(stringResource(Res.string.respect_is_open_source))
                        Text(stringResource(Res.string.license_text))
                    }
                }
            )
        }
    }
}
