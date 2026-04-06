package world.respect.app.view.person.inviteperson

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.images.RespectImage
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invite_how_title
import world.respect.shared.viewmodel.person.inviteperson.InvitePersonUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteSliderBottomSheet(
    uiState: InvitePersonUiState,
    onPageChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.currentSliderPage
    ) { uiState.sliderPages.size }

    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }

    if (uiState.showSlider) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val item = uiState.sliderPages[page]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(Res.string.invite_how_title),
                            textAlign = TextAlign.Center
                        )
                        Text(item.title)
                        Spacer(Modifier.size(12.dp))
                        Text(
                            item.description,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(uiState.sliderPages.size) { index ->
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
    val onboardingImage: RespectImage,
    val title: String,
    val description: String
)