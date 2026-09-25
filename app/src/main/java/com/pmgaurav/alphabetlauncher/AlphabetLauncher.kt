package com.pmgaurav.alphabetlauncher

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlphabetLauncher() {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var currentTime by remember {
        mutableStateOf(Date())
    }
    var selectedLetter by remember{
        mutableStateOf('A')
    }
    var isDragging by remember {
        mutableStateOf(false)
    }
    var showSelectedApps by remember {
        mutableStateOf(false)
    }
    var selectedSpecial by remember {
        mutableStateOf<Char?>(null)
    }
    LaunchedEffect(Unit) {
        while (true){
            currentTime = Date()
            delay(1000)
        }
    }

    val applications = remember {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager
            .queryIntentActivities(intent,0)
        resolveInfos
            .groupBy {
                it.activityInfo.packageName
            }
            .map {(_, activities) ->
                val resolveInfo = activities.first()
                AppInfo(
                    name = resolveInfo
                        .loadLabel(packageManager)
                        .toString(),
                    packageName = resolveInfo
                        .activityInfo
                        .packageName,
                    className = resolveInfo
                        .activityInfo
                        .name,
                    icon = resolveInfo.loadIcon(packageManager)
                )
            }
            .sortedWith(
                compareBy<AppInfo> {
                    if (it.name.trim().firstOrNull()?.isLetter() == true) {
                        0
                    } else {
                        1
                    }
                }.thenBy {
                    it.name.lowercase()
                }
            )
    }
    val favouriteApps = applications.take(7)

    val appsByLetter = applications.groupBy { app ->
        val firstCharecter = app.name
            .trim()
            .firstOrNull()
        if (firstCharecter?.isLetter() == true){
            firstCharecter.uppercaseChar()
        } else {
            '#'
        }
    }
    val selectedApps = appsByLetter[selectedLetter].orEmpty()
    val displayedApps =
        if (showSelectedApps) {
            selectedApps
        } else {
            favouriteApps
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        start = 32.dp,
                        top = 80.dp,
                        end = 20.dp,
                    )
            ) {
                if (!isDragging){
                    Text(
                        text = SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(currentTime),
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = SimpleDateFormat(
                            "EEE, dd MMM",
                            Locale.getDefault()
                        ).format(currentTime),
                        color = Color.LightGray,
                        fontSize = 16.sp
                    )
                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )
                }
                if (isDragging) {

                    Text(
                        text = selectedLetter.toString(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }
                if (displayedApps.isEmpty()){
                    Text(
                        text = " No Apps",
                        color = Color.LightGray,
                        fontSize = 16.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            vertical = 8.dp
                        )
                    ) {
                        items(
                            items = displayedApps,
                            key = {app ->
                                app.packageName
                            }
                        ){ app ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable{
                                        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
                                            component = ComponentName(
                                                app.packageName,
                                                app.className
                                            )
                                            addCategory(Intent.CATEGORY_LAUNCHER)
                                        }
                                        context.startActivity(launchIntent)
                                    }
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 10.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val bitmap = app.icon
                                    .toBitmap()
                                    .asImageBitmap()
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = app.name,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(
                                    modifier = Modifier.width(14.dp)
                                )
                                Text(
                                    text = app.name,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(42.dp)
                    .padding(vertical = 20.dp)
            ) {

                CurvedAlphabet(
                    selectedLetter = selectedLetter,
                    selectedSpecial = selectedSpecial,
                    onLetterSelected = { letter ->
                        selectedLetter = letter
                        showSelectedApps = true
                    },
                    onSpecialSelected = { special ->
                        selectedSpecial = special
                    },
                    onDragStateChanged = { dragging ->
                        isDragging = dragging
                        showSelectedApps = dragging

                        if (!dragging) {
                            selectedLetter = ' '
                            selectedSpecial = null
                        }
                    }
                )
            }
        }
    }
}