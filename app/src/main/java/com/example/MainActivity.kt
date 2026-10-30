package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.WorkoutPlan
import com.example.ui.components.ProfileSheet
import com.example.ui.screens.ActiveWorkoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProgressHistoryScreen
import com.example.ui.screens.WorkoutBuilderScreen
import com.example.ui.screens.WorkoutSummaryScreen
import com.example.ui.theme.FitCasaTheme
import com.example.ui.viewmodel.WorkoutViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("Inicio", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    BUILDER("Rutinas", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "nav_builder"),
    HISTORY("Progreso", Icons.Filled.History, Icons.Outlined.History, "nav_history")
}

enum class ScreenState {
    TABS,
    ACTIVE_WORKOUT,
    SUMMARY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FitCasaTheme {
                FitCasaApp()
            }
        }
    }
}

@Composable
fun FitCasaApp(
    viewModel: WorkoutViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(ScreenState.TABS) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var showProfileSheet by remember { mutableStateOf(false) }

    val activeState by viewModel.activeWorkoutState.collectAsStateWithLifecycle()
    val history by viewModel.workoutHistory.collectAsStateWithLifecycle()
    val totalWorkouts by viewModel.totalWorkouts.collectAsStateWithLifecycle()
    val totalMinutes by viewModel.totalMinutes.collectAsStateWithLifecycle()
    val totalCalories by viewModel.totalCalories.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakDays.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceCoachState.collectAsStateWithLifecycle()
    val currentVoiceMessage by viewModel.currentVoiceMessage.collectAsStateWithLifecycle()

    val selectedBodyPart by viewModel.selectedBodyPart.collectAsStateWithLifecycle()
    val selectedIntensity by viewModel.selectedIntensity.collectAsStateWithLifecycle()
    val selectedDurationMinutes by viewModel.selectedDurationMinutes.collectAsStateWithLifecycle()
    val configuredPlan by viewModel.configuredPlan.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    // Automatically navigate to summary when workout is completed
    LaunchedEffect(activeState.isCompleted, currentScreen) {
        if (activeState.isCompleted && currentScreen == ScreenState.ACTIVE_WORKOUT) {
            currentScreen = ScreenState.SUMMARY
        }
    }

    // Handle back button for secondary tabs
    if (currentScreen == ScreenState.TABS && selectedTab != MainTab.HOME) {
        BackHandler {
            selectedTab = MainTab.HOME
        }
    }

    // Back from the summary returns to the history tab (the session is already saved)
    if (currentScreen == ScreenState.SUMMARY) {
        BackHandler {
            viewModel.cancelWorkout()
            currentScreen = ScreenState.TABS
            selectedTab = MainTab.HISTORY
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen == ScreenState.TABS) {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition",
            modifier = Modifier.padding(innerPadding)
        ) { screen ->
            when (screen) {
                ScreenState.TABS -> {
                    when (selectedTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                streakDays = streakDays,
                                totalMinutes = totalMinutes,
                                totalCalories = totalCalories,
                                recentWorkouts = history,
                                onOpenProfile = { showProfileSheet = true },
                                onStartQuickWorkout = { plan ->
                                    viewModel.startWorkout(plan)
                                    currentScreen = ScreenState.ACTIVE_WORKOUT
                                },
                                onNavigateToBuilder = {
                                    selectedTab = MainTab.BUILDER
                                },
                                onNavigateToHistory = {
                                    selectedTab = MainTab.HISTORY
                                }
                            )
                        }

                        MainTab.BUILDER -> {
                            WorkoutBuilderScreen(
                                plan = configuredPlan,
                                selectedBodyPart = selectedBodyPart,
                                selectedIntensity = selectedIntensity,
                                selectedDurationMinutes = selectedDurationMinutes,
                                onSelectBodyPart = { viewModel.selectBodyPart(it) },
                                onSelectIntensity = { viewModel.selectIntensity(it) },
                                onSelectDuration = { viewModel.selectDuration(it) },
                                onMoveExercise = { index, direction ->
                                    viewModel.moveExercise(index, direction)
                                },
                                onStartWorkout = { plan ->
                                    viewModel.startWorkout(plan)
                                    currentScreen = ScreenState.ACTIVE_WORKOUT
                                },
                                onBack = { selectedTab = MainTab.HOME }
                            )
                        }

                        MainTab.HISTORY -> {
                            ProgressHistoryScreen(
                                workouts = history,
                                totalWorkouts = totalWorkouts,
                                totalMinutes = totalMinutes,
                                totalCalories = totalCalories,
                                streakDays = streakDays,
                                onDeleteWorkout = { id -> viewModel.deleteWorkoutRecord(id) }
                            )
                        }
                    }
                }

                ScreenState.ACTIVE_WORKOUT -> {
                    ActiveWorkoutScreen(
                        state = activeState,
                        voiceState = voiceState,
                        currentVoiceMessage = currentVoiceMessage,
                        onToggleVoiceCoach = { viewModel.toggleVoiceCoach() },
                        onSelectVoice = { viewModel.selectVoiceOption(it) },
                        onSelectVoiceRate = { viewModel.setVoiceRate(it) },
                        onSelectVoicePitch = { viewModel.setVoicePitch(it) },
                        onPreviewVoice = { viewModel.previewVoice() },
                        onTogglePause = { viewModel.togglePause() },
                        onStartStep = { viewModel.startCurrentStep() },
                        onSkipRest = { viewModel.skipRest() },
                        onAdd20SecondsToRest = { viewModel.add20SecondsToRest() },
                        onNextExercise = { viewModel.nextExercise() },
                        onPreviousExercise = { viewModel.previousExercise() },
                        onCancelWorkout = {
                            viewModel.cancelWorkout()
                            currentScreen = ScreenState.TABS
                        }
                    )
                }

                ScreenState.SUMMARY -> {
                    WorkoutSummaryScreen(
                        state = activeState,
                        onFinish = {
                            viewModel.cancelWorkout()
                            currentScreen = ScreenState.TABS
                            selectedTab = MainTab.HISTORY
                        }
                    )
                }
            }
        }
    }

    if (showProfileSheet) {
        ProfileSheet(
            profile = profile,
            onDismiss = { showProfileSheet = false },
            onSave = { newProfile ->
                viewModel.saveProfile(newProfile)
                showProfileSheet = false
            }
        )
    }
}
