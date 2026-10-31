package com.example.ui.components

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.util.VoiceCoachUiState
import com.example.util.VoiceOption
import kotlin.math.abs

/**
 * Panel para afinar la voz del entrenador: activación, velocidad, motor y voz concreta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCoachSettingsSheet(
    state: VoiceCoachUiState,
    onDismiss: () -> Unit,
    onToggleEnabled: () -> Unit,
    onSelectVoice: (String) -> Unit,
    onSelectRate: (Float) -> Unit,
    onSelectPitch: (Float) -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("voice_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            VoiceHeader(state = state, onToggleEnabled = onToggleEnabled)

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                VoiceIssueCard(
                    state = state,
                    onInstallVoiceData = {
                        runCatching {
                            context.startActivity(
                                Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))
            SectionTitle(stringResource(R.string.voice_speed))
            Spacer(modifier = Modifier.height(8.dp))
            RateSelector(
                selectedRate = state.speechRate,
                onSelectRate = onSelectRate
            )

            Spacer(modifier = Modifier.height(22.dp))
            SectionTitle(stringResource(R.string.voice_pitch))
            Spacer(modifier = Modifier.height(8.dp))
            PitchSelector(
                selectedPitch = state.speechPitch,
                onSelectPitch = onSelectPitch
            )

            Spacer(modifier = Modifier.height(22.dp))
            SectionTitle(stringResource(R.string.voice_list))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (state.voices.isEmpty()) {
                    stringResource(R.string.voice_none)
                } else {
                    stringResource(R.string.voice_choose)
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.voices.isEmpty()) {
                if (state.errorMessage == null) {
                    VoiceIssueCard(
                        state = state,
                        onInstallVoiceData = {
                            runCatching {
                                context.startActivity(
                                    Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    )
                }
            } else {
                state.voices.forEach { voice ->
                    VoiceRow(
                        voice = voice,
                        isSelected = voice.id == state.selectedVoiceId,
                        onClick = { onSelectVoice(voice.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = onPreview,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("voice_preview_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.voice_preview), fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.voice_tip),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun VoiceHeader(
    state: VoiceCoachUiState,
    onToggleEnabled: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.voice_title),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = state.engineLabel,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = state.isEnabled,
            onCheckedChange = { onToggleEnabled() },
            modifier = Modifier.testTag("voice_settings_switch")
        )
    }
}

@Composable
private fun VoiceIssueCard(
    state: VoiceCoachUiState,
    onInstallVoiceData: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.voice_missing_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = state.errorMessage
                    ?: stringResource(R.string.voice_missing_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp
            )

            if (state.needsVoiceData || state.voices.isEmpty()) {
                TextButton(
                    onClick = onInstallVoiceData,
                    modifier = Modifier.testTag("voice_install_data_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.voice_download), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun RateSelector(
    selectedRate: Float,
    onSelectRate: (Float) -> Unit
) {
    OptionSelector(
        options = VoiceCoachUiState.RATE_OPTIONS,
        selected = selectedRate,
        testTagPrefix = "voice_rate",
        title = { rateLabel(it) },
        subtitle = { stringResource(R.string.voice_percent, (it * 100).toInt()) },
        onSelect = onSelectRate
    )
}

@Composable
private fun PitchSelector(
    selectedPitch: Float,
    onSelectPitch: (Float) -> Unit
) {
    OptionSelector(
        options = VoiceCoachUiState.PITCH_OPTIONS,
        selected = selectedPitch,
        testTagPrefix = "voice_pitch",
        title = { pitchLabel(it) },
        subtitle = { pitchHint(it) },
        onSelect = onSelectPitch
    )
}

@Composable
private fun OptionSelector(
    options: List<Float>,
    selected: Float,
    testTagPrefix: String,
    title: @Composable (Float) -> String,
    subtitle: @Composable (Float) -> String,
    onSelect: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = abs(selected - option) < 0.01f
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(option) }
                    .testTag("${testTagPrefix}_$option"),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title(option),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = subtitle(option),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun rateLabel(rate: Float): String = when {
    rate < 0.97f -> stringResource(R.string.voice_rate_slow)
    rate > 1.05f -> stringResource(R.string.voice_rate_fast)
    else -> stringResource(R.string.voice_rate_natural)
}

@Composable
private fun pitchLabel(pitch: Float): String = when {
    pitch < 0.97f -> stringResource(R.string.voice_pitch_low)
    pitch > 1.03f -> stringResource(R.string.voice_pitch_high)
    else -> stringResource(R.string.voice_pitch_natural)
}

@Composable
private fun pitchHint(pitch: Float): String = when {
    pitch < 0.97f -> stringResource(R.string.voice_pitch_low_hint)
    pitch > 1.03f -> stringResource(R.string.voice_pitch_high_hint)
    else -> stringResource(R.string.voice_pitch_natural_hint)
}

@Composable
private fun VoiceRow(
    voice: VoiceOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("voice_option_${voice.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voice.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = buildString {
                        append(voice.qualityLabel)
                        append(" · ")
                        append(
                            if (voice.requiresNetwork) {
                                stringResource(R.string.voice_requires_network)
                            } else {
                                stringResource(R.string.voice_offline)
                            }
                        )
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
