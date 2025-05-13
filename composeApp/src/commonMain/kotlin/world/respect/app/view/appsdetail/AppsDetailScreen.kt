package world.respect.app.view.appsdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import respect.composeapp.generated.resources.Res
import respect.composeapp.generated.resources.try_it
import respect.composeapp.generated.resources.add_app
import respect.composeapp.generated.resources.lessons


import world.respect.app.viewmodel.appsdetail.AppsDetailScreenViewModel

@Composable
fun AppsDetailScreen(
    viewModel: AppsDetailScreenViewModel,
    param: String,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray)
            ) {
                Text(
                    uiState.appsDetailData?.imageName ?: "",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    uiState.appsDetailData?.appName ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    uiState.appsDetailData?.appDescription ?: "", fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { /* Options */ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { /* Try it */ }, modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.try_it))
            }

            OutlinedButton(onClick = { /* Add app */ }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(Res.string.add_app))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Featured Images
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lessons Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.lessons), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Go to all lessons */ }) {
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }

        uiState.appsDetailData?.let { appData ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(appData.lessons) { lesson ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(lesson.title, fontSize = 12.sp)
                    }
                }
            }
        }

    }
}
