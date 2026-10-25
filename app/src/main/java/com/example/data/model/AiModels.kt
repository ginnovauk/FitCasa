package com.example.data.model

data class AiWorkoutRoutine(
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val estimatedCalories: Int,
    val intensity: IntensityLevel,
    val advice: String,
    val exercises: List<Exercise>
)

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface AiRoutineState {
    data object Idle : AiRoutineState
    data object Loading : AiRoutineState
    data class Success(val routine: AiWorkoutRoutine) : AiRoutineState
    data class Error(val message: String) : AiRoutineState
}

sealed interface AiChatState {
    data object Idle : AiChatState
    data object Loading : AiChatState
    data class Success(val response: String) : AiChatState
    data class Error(val message: String) : AiChatState
}
