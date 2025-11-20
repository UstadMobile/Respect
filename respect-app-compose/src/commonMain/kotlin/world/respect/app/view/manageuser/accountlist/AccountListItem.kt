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
import world.respect.app.components.CountryFlag
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.flagSizeMedium
import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.RespectAccountAndPerson
import world.respect.shared.util.ext.fullName


@Composable
fun AccountListItem(
    account: RespectAccountAndPerson,
    onClickAccount: ((RespectAccount) -> Unit)?,
    extras: @Composable () -> Unit = { },
){
   ListItem(
        modifier = Modifier.clickable {
            onClickAccount?.also {
                onClickAccount(account.account)
            }
        },
        leadingContent = {
            RespectPersonAvatar(name = account.person.fullName())
        },
        headlineContent = {
            Text(account.person.fullName())
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
                        text = account.person.username ?: "",
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = account.account.school.self.toString(),
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    CountryFlag(
                        schoolUrl = account.account.school.self.toString(),
                        modifier = Modifier.flagSizeMedium()
                    )
                }
                extras()
            }
        }
    )
}
