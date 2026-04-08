package world.respect.app.view.person.inviteperson

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_invite_code
import world.respect.shared.generated.resources.invite_how_title
import world.respect.shared.generated.resources.invite_step_confirm_desc
import world.respect.shared.generated.resources.invite_step_enter_code_desc
import world.respect.shared.generated.resources.invite_step_enter_code_title
import world.respect.shared.generated.resources.invite_step_open_app_desc
import world.respect.shared.generated.resources.invite_step_open_app_title
import world.respect.shared.generated.resources.invite_step_review_title
import world.respect.shared.generated.resources.invite_step_search_school_desc
import world.respect.shared.generated.resources.invite_step_search_school_title
import world.respect.shared.generated.resources.invite_step_share_desc
import world.respect.shared.generated.resources.invite_step_share_title
import world.respect.shared.generated.resources.open_respect_app
import world.respect.shared.generated.resources.review_and_complete_setup
import world.respect.shared.generated.resources.search_school
import world.respect.shared.generated.resources.share_invite_code
import world.respect.shared.viewmodel.person.inviteperson.InvitePersonUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteSliderBottomSheet(
    uiState: InvitePersonUiState,
    onPageChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sliderPages = listOf(
        InviteSliderPageUi(
            sliderImage = Res.drawable.share_invite_code,
            title = stringResource(Res.string.invite_step_share_title),
            description = stringResource(
                Res.string.invite_step_share_desc,
                uiState.inviteCode?:""
            )
        ),
        InviteSliderPageUi(
            sliderImage = Res.drawable.open_respect_app,
            title = stringResource(Res.string.invite_step_open_app_title),
            description = stringResource(Res.string.invite_step_open_app_desc)
        ),
        InviteSliderPageUi(
            sliderImage = Res.drawable.search_school,
            title = stringResource(Res.string.invite_step_search_school_title),
            description = stringResource(
                Res.string.invite_step_search_school_desc,
                uiState.schoolName?:""
            )
        ),
        InviteSliderPageUi(
            sliderImage = Res.drawable.review_and_complete_setup,
            title = stringResource(Res.string.invite_step_review_title),
            description = stringResource(Res.string.invite_step_confirm_desc)
        ),
        InviteSliderPageUi(
            sliderImage = Res.drawable.enter_invite_code,
            title = stringResource(Res.string.invite_step_enter_code_title),
            description = stringResource(
                Res.string.invite_step_enter_code_desc
            )
        )
    )
    val pagerState = rememberPagerState(
        initialPage = uiState.currentSliderPage
    ) { sliderPages.size }

    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }

    if (uiState.showSlider) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val item = sliderPages[page]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.invite_how_title),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold
                        )
                        Image(
                            painter = painterResource(item.sliderImage),
                            contentDescription = null,
                            modifier = Modifier.size(200.dp).padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        ListItem(
                            headlineContent = {
                                Text(item.title)
                            },
                            supportingContent = {
                                Text(item.description)
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(sliderPages.size) { index ->
                        val selected = uiState.currentSliderPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (selected) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) Color.Black else Color.LightGray
                                )
                        )
                    }
                }
            }
        }
    }
}

data class InviteSliderPageUi(
    val sliderImage: DrawableResource,
    val title: String,
    val description: String
)