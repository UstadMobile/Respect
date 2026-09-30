package world.respect.app.view.manageuser.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.defaultScreenPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.continue_button
import world.respect.shared.generated.resources.openid_login_options_description
import world.respect.shared.generated.resources.openid_site_content
import world.respect.shared.viewmodel.manageuser.login.OpenIdLoginViewModel

@Composable
fun OpenIdLoginScreen(
    viewModel: OpenIdLoginViewModel,
) {
    OpenIdLoginScreen(
        onClickContinue = viewModel::onClickContinue,
    )
}

@Composable
private fun OpenIdLoginScreen(
    onClickContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {
        Text(
            text = stringResource(Res.string.openid_site_content),
            modifier = Modifier.defaultItemPadding(),
        )
        Text(
            text = stringResource(Res.string.openid_login_options_description),
            modifier = Modifier.defaultItemPadding(),
        )
        OutlinedButton(
            onClick = onClickContinue,
            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
        ) {
            Text(text = stringResource(Res.string.continue_button))
        }
    }
}
