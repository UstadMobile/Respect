package world.respect.app.view.manageuser.sharefeedback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackUiState
import world.respect.shared.viewmodel.manageuser.sharefeedback.ShareFeedbackViewModel
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Whatsapp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPhoneNumberTextField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.defaultScreenPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.domain.feedback.FeedbackCategory
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.email_respect
import world.respect.shared.generated.resources.public_forum
import world.respect.shared.generated.resources.quick_contact
import world.respect.shared.generated.resources.whatsapp_respect
import world.respect.shared.generated.resources.category
import world.respect.shared.generated.resources.describe_feedback_placeholder
import world.respect.shared.generated.resources.email
import world.respect.shared.generated.resources.phone_number
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.share_feedback
import world.respect.shared.generated.resources.submit_feedback
import world.respect.shared.generated.resources.your_feedback
import world.respect.shared.generated.resources.want_response_from_team
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText

@Composable
fun ShareFeedbackScreen(
    viewModel: ShareFeedbackViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    ShareFeedbackScreen(
        uiState = uiState,
        onClickWhatsApp = viewModel::onClickWhatsApp,
        onClickEmail = viewModel::onClickEmail,
        onClickPublicForum = viewModel::onClickPublicForum,
        onCategorySelected = viewModel::onCategorySelected,
        onFeedbackDescriptionChanged = viewModel::onFeedbackDescriptionChanged,
        onClickCheckBox = viewModel::onClickCheckBox,
        onClickSubmit = viewModel::onClickSubmit,
        onNationalNumberSetChanged = viewModel::onNationalPhoneNumSetChanged,
        onPhoneChanged = viewModel::onPhoneChanged,
        onEmailChanged = viewModel::onEmailChanged,
    )
}

@Composable
fun ShareFeedbackScreen(
    uiState: ShareFeedbackUiState,
    onClickWhatsApp: () -> Unit,
    onClickEmail: () -> Unit,
    onClickPublicForum: () -> Unit,
    onCategorySelected: (FeedbackCategory) -> Unit,
    onFeedbackDescriptionChanged: (String) -> Unit,
    onClickSubmit: () -> Unit,
    onClickCheckBox: () -> Unit,
    onNationalNumberSetChanged: (Boolean) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .defaultScreenPadding()
    ) {

        item {
            Text(
                modifier = Modifier.defaultItemPadding(),
                text = stringResource(Res.string.quick_contact)
            )
        }

        item {
            FeedbackItem(
                title = stringResource(Res.string.whatsapp_respect),
                leadingIcon = Icons.Outlined.Whatsapp,
                onClick = onClickWhatsApp
            )
        }

        item {
            FeedbackItem(
                title = stringResource(Res.string.email_respect),
                leadingIcon = Icons.Outlined.Email,
                onClick = onClickEmail
            )
        }

        item {
            FeedbackItem(
                title = stringResource(Res.string.public_forum),
                leadingIcon = Icons.Outlined.People,
                onClick = onClickPublicForum,
            )
        }

        item {
            HorizontalDivider(
                modifier = Modifier.defaultScreenPadding()
            )
        }

        item {
            Text(
                modifier = Modifier.defaultItemPadding(),
                text = stringResource(Res.string.share_feedback)
            )
        }
        item {
            CategoryDropdown(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        item {
            FeedbackDescription(
                text = uiState.feedbackDescription,
                onValueChange = onFeedbackDescriptionChanged,
                error = uiState.feedbackDescriptionError
            )
        }
        item {
            Row(
                modifier = Modifier
                    .clickable {
                        onClickCheckBox()
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = uiState.isCheckBoxSelected,
                    onCheckedChange = {
                        onClickCheckBox()
                    },
                )

                Text(
                    text = stringResource(Res.string.want_response_from_team),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (uiState.isCheckBoxSelected) {
            item {
                ContactFields(
                    uiState,
                    onPhoneChange = onPhoneChanged,
                    onEmailChange = onEmailChanged,
                    onNationalNumberSetChanged = onNationalNumberSetChanged
                )
            }
        }

        item {
            Button(
                onClick = {
                    onClickSubmit()
                },
                modifier = Modifier.fillMaxWidth()
                    .defaultItemPadding()
            ) {
                Text(stringResource(Res.string.submit_feedback))
            }
        }
    }
}

@Composable
private fun FeedbackItem(
    title: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = title)
        },
        leadingContent = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<FeedbackCategory>,
    selectedCategory: FeedbackCategory,
    onCategorySelected: (FeedbackCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultItemPadding()
    ) {

        Text(
            text = stringResource(Res.string.category),
            modifier = Modifier.padding(bottom = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = stringResource(selectedCategory.resource),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(stringResource(category.resource)) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FeedbackDescription(
    text: String,
    onValueChange: (String) -> Unit,
    error: UiText?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultItemPadding()
    ) {
        Text(
            text = stringResource(Res.string.your_feedback),
            modifier = Modifier.padding(bottom = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = {
                Text(text = stringResource(Res.string.describe_feedback_placeholder))
            },
            isError = error != null,
            shape = RoundedCornerShape(8.dp),
            supportingText = {
                Text(uiTextStringResource(error ?: Res.string.required.asUiText()))
            }
        )
    }
}

@Composable
fun ContactFields(
    uiState: ShareFeedbackUiState,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNationalNumberSetChanged: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth())
    {

        RespectPhoneNumberTextField(
            value = uiState.phoneNumber,
            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            label = { Text(stringResource(Res.string.phone_number)) },
            onValueChange = onPhoneChange,
            onNationalNumberSetChanged = onNationalNumberSetChanged,
            isError = uiState.phoneNumberError != null,
            supportingText = uiState.phoneNumberError?.let {
                { Text(uiTextStringResource(it)) }
            },
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            value = uiState.email,
            label = { Text(stringResource(Res.string.email)) },
            singleLine = true,
            onValueChange = onEmailChange,
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let {
                { Text(uiTextStringResource(it)) }
            },
        )
    }
}
