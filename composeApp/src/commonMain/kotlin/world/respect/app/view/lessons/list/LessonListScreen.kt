package world.respect.app.view.lessons.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ustadmobile.libuicompose.theme.black
import com.ustadmobile.libuicompose.theme.white
import org.jetbrains.compose.resources.stringResource
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.clazz
import respect.composeapp.generated.resources.duration
import world.respect.app.app.LessonDetail
import world.respect.app.appstate.getTitle
import world.respect.app.appstate.toDisplayString
import world.respect.app.viewmodel.lessons.list.LessonListViewModel

@Composable
fun LessonListScreen(
    navController: NavHostController,
    viewModel: LessonListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        uiState.lessonFilter.firstOrNull()?.let { facet ->
            var expanded by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .border(1.dp, black, shape = RoundedCornerShape(6.dp))
                        .clip(CircleShape)
                        .clickable { expanded = true }
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = uiState.selectedFilterTitle ?: (facet.metadata?.title ?: ""))
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            modifier = Modifier.padding(6.dp),
                            contentDescription = null
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = facet.metadata?.title ?: "",
                                fontSize = 14.sp
                            )
                        },
                        onClick = {},
                        enabled = false
                    )

                    facet.links.forEach { link ->
                        DropdownMenuItem(
                            text = { Text(link.title ?: "") },
                            onClick = {
                                viewModel.onFilterSelected(link.title ?: "")
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(uiState.publications) { publication ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(LessonDetail)
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(white)
                                .border(1.dp, black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                modifier = Modifier.padding(6.dp),
                                contentDescription = null
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(text = publication.metadata.title.getTitle(), fontSize = 16.sp)
                            Text(
                                text = stringResource(Res.string.clazz),
                                fontSize = 12.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = publication.metadata.subject
                                    ?.joinToString(", ") { it.toDisplayString() }
                                    ?: "No subjects", fontSize = 12.sp)
                                Text(
                                    text = "${stringResource(Res.string.duration)}- ${publication.metadata.duration}",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Filled.ArrowCircleDown,
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                }

            }
        }
    }

}