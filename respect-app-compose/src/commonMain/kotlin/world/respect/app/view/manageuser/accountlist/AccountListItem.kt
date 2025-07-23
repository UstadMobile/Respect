package world.respect.app.view.manageuser.accountlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import world.respect.app.components.RespectPersonAvatar
import world.respect.shared.domain.account.RespectAccount

@Composable
fun AccountListItem(
    account: RespectAccount,
    onClickAccount: ((RespectAccount) -> Unit)?,
    extras: @Composable () -> Unit = { },
) {
    ListItem(
        modifier = Modifier.clickable {
            onClickAccount?.also {
                onClickAccount(account)
            }
        },
        leadingContent = {
            RespectPersonAvatar(name = account.userSourcedId)
        },
        headlineContent = {
            Text(account.userSourcedId)
        },
        supportingContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = account.userSourcedId,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = account.serverUrls.xapi.toString(),
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                extras()
            }
        }
    )
}
