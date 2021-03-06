package com.andriawan.boilanegg.ui.pages.timer

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.andriawan.boilanegg.R
import com.andriawan.boilanegg.ui.components.PlaybackButton
import com.andriawan.boilanegg.ui.components.TimerWithProgress
import com.andriawan.boilanegg.ui.theme.BoilAnEggTheme
import com.andriawan.boilanegg.utils.NotificationUtil
import com.andriawan.boilanegg.utils.StatePlayerBroadcast
import com.andriawan.boilanegg.utils.StatePlayerReceiver
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

        // Register broadcast receiver
        val receiver = object : StatePlayerBroadcast() {
            override fun onReceive(context: Context?, intent: Intent?) {
                super.onReceive(context, intent)
                Timber.d("Go here ${intent?.getStringExtra(StatePlayerReceiver.EXTRAS_STATUS)}")
                if (intent?.hasExtra(StatePlayerReceiver.EXTRAS_STATUS) == true) {
                    when (intent.getStringExtra(StatePlayerReceiver.EXTRAS_STATUS)) {
                        StatePlayerReceiver.STATUS_PAUSE -> {
                            viewModel.onEvent(TimerStateUIEvent.PauseTimer)
                        }

                        StatePlayerReceiver.STATUS_RESUME -> {
                            viewModel.onEvent(TimerStateUIEvent.StartTimer)
                        }

                        StatePlayerReceiver.STATUS_STOP -> {
                            viewModel.onEvent(TimerStateUIEvent.StopTimer)
                        }
                    }
                }
            }
        }

        IntentFilter(StatePlayerBroadcast.ACTION_NAME).also {
            context.registerReceiver(receiver, it)
        }

        // When the effect leaves, remove observer
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
            context.unregisterReceiver(receiver)
        }
    }

    MainPage(state = state, viewModel = viewModel)
}

@Composable
fun MainPage(
    state: TimerState,
    viewModel: TimerViewModel
) {
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

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun TimerPageDarkModePreview() {
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
                eggLevelID = 2
            )
        }
    }
}
