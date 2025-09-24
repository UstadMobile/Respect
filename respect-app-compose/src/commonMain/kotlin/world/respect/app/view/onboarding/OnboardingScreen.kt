package world.respect.app.view.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.images.RespectImage
import world.respect.images.respectImagePainter
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.get_started
import world.respect.shared.generated.resources.headline_consent_content
import world.respect.shared.generated.resources.onboardingDescription2
import world.respect.shared.generated.resources.onboardingDescription3
import world.respect.shared.generated.resources.onboardingDescription4
import world.respect.shared.generated.resources.onboardingTitle1
import world.respect.shared.generated.resources.onboardingTitle2
import world.respect.shared.generated.resources.onboardingTitle3
import world.respect.shared.generated.resources.onboardingTitle4
import world.respect.shared.generated.resources.supporting_consent_content
import world.respect.shared.viewmodel.onboarding.OnboardingUiState
import world.respect.shared.viewmodel.onboarding.OnboardingViewModel


data class OnboardingItem(
    val onboardingImage: RespectImage,
    val onboardingTitle: String,
    val onboardingDescription: String
)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    OnboardingScreen(
        uiState = uiState,
        onClickGetStartedButton = viewModel::onClickGetStartedButton,
        onConsentChanged = viewModel::onConsentChanged,
        onSnackBarShown = viewModel::clearSnackBar
    )
}

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onClickGetStartedButton: () -> Unit,
    onConsentChanged: (Boolean) -> Unit,
    onSnackBarShown: () -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    uiState.snackBarMessage?.let { message ->
        LaunchedEffect(message) {
            snackBarHostState.showSnackbar(message)
            onSnackBarShown()
        }
    }

    val onboardingItem = listOf(
        OnboardingItem(
            onboardingImage = RespectImage.DIGITAL_LIBRARY,
            onboardingTitle = stringResource(Res.string.onboardingTitle1),
            onboardingDescription = stringResource(Res.string.onboardingDescription2)
        ),
        OnboardingItem(
            onboardingImage = RespectImage.WORKS_OFFLINE,
            onboardingTitle = stringResource(Res.string.onboardingTitle2),
            onboardingDescription = stringResource(Res.string.onboardingDescription2)
        ),
        OnboardingItem(
            onboardingImage = RespectImage.DATA_REPORTING,
            onboardingTitle = stringResource(Res.string.onboardingTitle3),
            onboardingDescription = stringResource(Res.string.onboardingDescription3)
        ),
        OnboardingItem(
            onboardingImage = RespectImage.ASSIGNMENTS,
            onboardingTitle = stringResource(Res.string.onboardingTitle4),
            onboardingDescription = stringResource(Res.string.onboardingDescription4)
        )
    )

    val pagerState = rememberPagerState(pageCount = { onboardingItem.size })

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val item = onboardingItem[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = respectImagePainter(item.onboardingImage),
                        contentDescription = null,
                        modifier = Modifier.size(280.dp).padding(32.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = item.onboardingTitle,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = item.onboardingDescription,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                        ConsentCheckbox(
                            checked = uiState.consentGiven,
                            onCheckedChange = onConsentChanged,
                        )
                    }
                    Spacer(Modifier.height(4.dp))

                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(onboardingItem.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (selected) 12.dp else 8.dp)
                    ) {
                        val dotColor = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.LightGray
                        }
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawCircle(
                                color = dotColor,
                                radius = size.minDimension / 2f
                            )
                        }
                    }
                }
            }
            Button(
                modifier = Modifier.padding(bottom = 8.dp),
                onClick = {
                    onClickGetStartedButton()
                }
            ) {
                Text(stringResource(Res.string.get_started))
            }
        }
    }
}

@Composable
fun CheckBoxConsent(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.2f))
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ConsentCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.2f)),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        leadingContent = {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }
        },
        headlineContent = {
            Text(
                text = stringResource(Res.string.headline_consent_content),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        supportingContent = {
            Text(
                text = stringResource(Res.string.supporting_consent_content),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    )
}







