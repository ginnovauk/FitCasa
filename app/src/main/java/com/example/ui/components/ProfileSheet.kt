package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Sex
import com.example.data.model.UserProfile
import com.example.ui.displayName
import com.example.ui.rememberAppLocale
import com.example.util.AppLanguage
import kotlin.math.roundToInt

/**
 * Hoja de perfil desde Inicio. Peso, estatura, edad y sexo alimentan la
 * estimación de kcal de los planes nuevos; no toca las sesiones ya guardadas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    profile: UserProfile,
    currentLanguage: String?,
    onSelectLanguage: (String?) -> Unit,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var weightKg by remember(profile) { mutableFloatStateOf(profile.weightKg) }
    var heightCm by remember(profile) { mutableIntStateOf(profile.heightCm) }
    var age by remember(profile) { mutableIntStateOf(profile.age) }
    var sex by remember(profile) { mutableStateOf(profile.sex) }

    val draft = profile.copy(weightKg = weightKg, heightCm = heightCm, age = age, sex = sex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = stringResource(R.string.profile_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = stringResource(R.string.profile_desc),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            ProfileSlider(
                label = stringResource(R.string.profile_weight),
                valueText = stringResource(R.string.profile_kg, weightKg.roundToInt()),
                value = weightKg,
                range = UserProfile.MIN_WEIGHT_KG..UserProfile.MAX_WEIGHT_KG,
                onValueChange = { weightKg = it.roundToInt().toFloat() },
                testTag = "profile_weight_slider"
            )

            ProfileSlider(
                label = stringResource(R.string.profile_height),
                valueText = stringResource(R.string.profile_cm, heightCm),
                value = heightCm.toFloat(),
                range = UserProfile.MIN_HEIGHT_CM.toFloat()..UserProfile.MAX_HEIGHT_CM.toFloat(),
                onValueChange = { heightCm = it.roundToInt() },
                testTag = "profile_height_slider"
            )

            ProfileSlider(
                label = stringResource(R.string.profile_age),
                valueText = stringResource(R.string.profile_years, age),
                value = age.toFloat(),
                range = UserProfile.MIN_AGE.toFloat()..UserProfile.MAX_AGE.toFloat(),
                onValueChange = { age = it.roundToInt() },
                testTag = "profile_age_slider"
            )

            Text(
                text = stringResource(R.string.profile_sex),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sex.values().forEach { option ->
                    FilterChip(
                        selected = sex == option,
                        onClick = { sex = option },
                        label = { Text(option.displayName()) },
                        modifier = Modifier.testTag("profile_sex_${option.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_with_data),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(
                            R.string.profile_bmr,
                            draft.basalMetabolicRate.roundToInt()
                        ),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(
                            R.string.profile_bmi,
                            String.format(rememberAppLocale(), "%.1f", draft.bmi)
                        ),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSave(draft.sanitized()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_save_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.profile_save), fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = {
                    val defaults = UserProfile()
                    weightKg = defaults.weightKg
                    heightCm = defaults.heightCm
                    age = defaults.age
                    sex = defaults.sex
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_reset_btn")
            ) {
                Text(stringResource(R.string.profile_reset))
            }

            // Idioma de la app. El nombre de cada idioma va en su propio idioma
            // ("Español", "English") para que siempre se reconozca.
            Text(
                text = stringResource(R.string.profile_language),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = stringResource(R.string.profile_language_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentLanguage == null,
                    onClick = { onSelectLanguage(null) },
                    label = { Text(stringResource(R.string.language_system)) },
                    modifier = Modifier.testTag("profile_language_system")
                )
                AppLanguage.options.forEach { option ->
                    FilterChip(
                        selected = currentLanguage == option.tag,
                        onClick = { onSelectLanguage(option.tag) },
                        label = { Text(option.label) },
                        modifier = Modifier.testTag("profile_language_${option.tag}")
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    testTag: String
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.testTag(testTag)
        )
    }
}
