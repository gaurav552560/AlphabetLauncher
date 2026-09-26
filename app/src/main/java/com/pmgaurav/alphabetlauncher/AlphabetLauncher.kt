package com.pmgaurav.alphabetlauncher

import android.content.ComponentName
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlphabetLauncher() {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val favouriteStore = remember {
        FavouriteStore(context)
    }
    val favouritePackages by favouriteStore
        .favouritePackages
        .collectAsState(initial = emptySet())

    val coroutineScope = rememberCoroutineScope()
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
    var isSearchOpen by remember {
        mutableStateOf(false)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    val focusRequester = remember {
        FocusRequester()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    BackHandler(enabled = isSearchOpen) {
        isSearchOpen = false
        searchQuery = ""
        keyboardController?.hide()
    }
    LaunchedEffect(Unit) {
        while (true){
            currentTime = Date()
            delay(1000)
        }
    }
    LaunchedEffect(isSearchOpen) {
        if (isSearchOpen) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    fun loadApplications(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager
            .queryIntentActivities(intent, 0)

        return resolveInfos
            .groupBy {
                it.activityInfo.packageName
            }
            .map { (_, activities) ->
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
                    if (it.name
                            .trim()
                            .firstOrNull()
                            ?.isLetter() == true
                    ) {
                        0
                    } else {
                        1
                    }
                }.thenBy {
                    it.name.lowercase()
                }
            )
    }
    var applications by remember {
        mutableStateOf(
            loadApplications()
        )
    }
    DisposableEffect(Unit) {

        val receiver = object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                applications = loadApplications()
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }

        context.registerReceiver(
            receiver,
            filter
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    val favouriteApps =
        if( favouritePackages.isEmpty()){
            applications.take(7)
        } else {
            applications
                .filter { app ->
                    app.packageName in favouritePackages
                }
                .take(7)
        }

//    val appsByLetter = applications.groupBy { app ->
//        val firstCharecter = app.name
//            .trim()
//            .firstOrNull()
//        if (firstCharecter?.isLetter() == true){
//            firstCharecter.uppercaseChar()
//        } else {
//            '#'
//        }
//    }
    val appsByLetter = applications.groupBy { app ->
        letterFromName(app.name)
    }
    val selectedApps = appsByLetter[selectedLetter].orEmpty()
    val searchResults = if (searchQuery.isBlank()) {
        applications
    } else {
        applications.filter {
            it.name.contains(
                searchQuery,
                ignoreCase = true
            )
        }
    }
    val displayedApps =
        if (showSelectedApps){
            selectedApps
        } else {
            favouriteApps
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        if (isSearchOpen) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 32.dp
                    )
            ) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = "Search apps",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                        )
                    },
                    leadingIcon = {
                        Text(
                            text = "⌕",
                            fontSize = 25.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.7f
                            )
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "×",
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.7f
                                ),
                                modifier = Modifier.clickable {
                                    searchQuery = ""
                                }
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor =
                            MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor =
                            MaterialTheme.colorScheme.surface,
                        focusedBorderColor =
                            MaterialTheme.colorScheme.onBackground
                                .copy(alpha = 0.35f),
                        unfocusedBorderColor =
                            MaterialTheme.colorScheme.onBackground
                                .copy(alpha = 0.20f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    )
                )

                if (searchQuery.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            vertical = 8.dp
                        )
                    ) {

                        items(
                            items = searchResults,
                            key = { app ->
                                app.packageName
                            }
                        ) { app ->

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
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 17.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

        } else {

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
                        .pointerInput(isSearchOpen) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->

                                    if (
                                        dragAmount < -20f &&
                                        !isSearchOpen &&
                                        !isDragging
                                    ) {
                                        isSearchOpen = true
                                        change.consume()
                                    }
                                }
                            )
                        }
                ) {
                    if (!isDragging) {
                        Text(
                            text = SimpleDateFormat(
                                "HH:mm",
                                Locale.getDefault()
                            ).format(currentTime),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = SimpleDateFormat(
                                "EEE, dd MMM",
                                Locale.getDefault()
                            ).format(currentTime),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                        Spacer(
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    if (isDragging) {

                        Text(
                            text = selectedLetter.toString(),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )
                    }
                    if (displayedApps.isEmpty()) {
                        Text(
                            text = " No Apps",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
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
                                key = { app ->
                                    app.packageName
                                }
                            ) { app ->

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .combinedClickable(
                                            onClick = {
                                                val launchIntent = Intent(Intent.ACTION_MAIN).apply {
                                                    component = ComponentName(
                                                        app.packageName,
                                                        app.className
                                                    )
                                                    addCategory(Intent.CATEGORY_LAUNCHER)
                                                }

                                                context.startActivity(launchIntent)
                                            },
                                            onLongClick = {
                                                coroutineScope.launch {

                                                    if (app.packageName in favouritePackages) {
                                                        favouriteStore.removeFavourite(
                                                            app.packageName
                                                        )
                                                    } else if (favouritePackages.size < 7) {
                                                        favouriteStore.addFavourite(
                                                            app.packageName
                                                        )
                                                    }
                                                }
                                            }
                                        )
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
                                        color = MaterialTheme.colorScheme.onBackground,
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
                        .padding(
                            top = 120.dp,
                            bottom = 10.dp
                        )
                ) {

                    CurvedAlphabet(
                        selectedLetter = selectedLetter,
                        selectedSpecial = selectedSpecial,
                        availableLetters = applications
                            .mapNotNull { app ->
                                app.name
                                    .trim()
                                    .firstOrNull()
                                    ?.uppercaseChar()
                                    ?.takeIf { it in 'A'..'Z' }
                            }
                            .toSet(),
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
}