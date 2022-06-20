package com.andriawan.boilanegg.ui.pages.timer

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.andriawan.boilanegg.R
import com.andriawan.boilanegg.ui.theme.BoilAnEggTheme
import com.andriawan.boilanegg.utils.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun TimerPage(
    navController: NavHostController,
    scope: CoroutineScope,
    snackBarState: SnackbarHostState,
    eggLevelID: Int,
    viewModel: TimerViewModel = viewModel(),
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val state = viewModel.state
    val context = LocalContext.current
    val notificationUtil = NotificationUtil(context)

    LaunchedEffect(key1 = null) {
        Timber.d("Get Egg Level")
        if (eggLevelID == -1) {
            scope.launch {
                snackBarState.showSnackbar(
                    message = "ID is not valid",
                    duration = SnackbarDuration.Short
                )
            }
            navController.navigateUp()
        }
        viewModel.getEggLevel(eggLevelID, notificationUtil)
    }

    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_DESTROY) {
                viewModel.onEvent(TimerStateUIEvent.StopTimer)
            }
        }

        // Add observer to lifecycle
        lifeCycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves, remove observer
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.eggLevel?.name ?: "Test",
                style = MaterialTheme.typography.h1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(80.dp))

            Image(
                painter = painterResource(id = state.eggLevel?.icon ?: R.drawable.ic_soft),
                contentDescription = "Image Icon"
            )

            Spacer(modifier = Modifier.height(80.dp))

            TimerWithProgress(
                showTimer = state.showTime,
                progress = state.percentageProgress
            )
        }

        PlaybackButton(
            isStarted = state.isStarted,
            isFinished = state.isFinished,
            isPlaying = state.isPlaying,
            onPauseTimer = {
                viewModel.onEvent(TimerStateUIEvent.PauseTimer)
            },
            onStartTimer = {
                viewModel.onEvent(TimerStateUIEvent.StartTimer)
            },
            onStopTimer = {
                viewModel.onEvent(TimerStateUIEvent.StopTimer)
            }
        )
    }
}

@Composable
fun TimerWithProgress(
    showTimer: String,
    progress: Float = 0F
) {
    Column {
        Text(
            text = showTimer,
            style = MaterialTheme.typography.h3,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(24.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PlaybackButton(
    isStarted: Boolean,
    isFinished: Boolean,
    isPlaying: Boolean,
    onPauseTimer: () -> Unit,
    onStartTimer: () -> Unit,
    onStopTimer: () -> Unit
) {
    Row {
        if (isStarted && !isFinished) {
            Button(
                modifier = Modifier.weight(1F),
                onClick = {
                    if (isPlaying) {
                        onPauseTimer.invoke()
                    } else {
                        onStartTimer.invoke()
                    }
                },
            ) {
                Text(
                    text = if (isPlaying) {
                        stringResource(id = R.string.button_pause_title)
                    } else {
                        stringResource(id = R.string.button_resume_title)
                    },
                    style = MaterialTheme.typography.button
                )
            }

            Spacer(modifier = Modifier.size(20.dp))
        }

        Button(
            modifier = Modifier.weight(1F),
            onClick = {
                if (isStarted)
                    onStopTimer.invoke()
                else
                    onStartTimer.invoke()
            }
        ) {
            Text(
                text = if (!isStarted) {
                    stringResource(id = R.string.button_play_title)
                } else {
                    stringResource(id = R.string.button_stop_title)
                }
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun TimerPagePreview() {
    BoilAnEggTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            TimerPage(
                navController = rememberNavController(),
                scope = rememberCoroutineScope(),
                snackBarState = SnackbarHostState(),
                eggLevelID = 1
            )
        }
    }
}

@Preview
@Composable
fun PlaybackButtonPreview() {
    BoilAnEggTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(24.dp)
        ) {
            PlaybackButton(
                isStarted = true,
                isFinished = false,
                isPlaying = true,
                onPauseTimer = { },
                onStartTimer = { },
                onStopTimer = { }
            )
        }
    }
}