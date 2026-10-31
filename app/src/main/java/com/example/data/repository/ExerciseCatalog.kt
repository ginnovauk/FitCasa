package com.example.data.repository

import com.example.R

import com.example.data.model.BodyPart
import com.example.data.model.Exercise
import com.example.data.model.IntensityLevel

object ExerciseCatalog {

    val allExercises: List<Exercise> = listOf(
        // CUERPO COMPLETO
        Exercise(
            id = "jumping_jacks",
            nameRes = R.string.ex_jumping_jacks_name,
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 30,
            caloriesPerMinute = 9.3f,
            lottieAsset = "exercises/lottie/jumping_jacks.json",
            instructionsRes = R.array.ex_jumping_jacks_instructions,
            breathingTipsRes = R.string.ex_jumping_jacks_breathing,
            commonMistakesRes = R.string.ex_jumping_jacks_mistake
        ),
        Exercise(
            id = "mountain_climbers",
            nameRes = R.string.ex_mountain_climbers_name,
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 30,
            defaultReps = 24,
            caloriesPerMinute = 9.3f,
            lottieAsset = "exercises/lottie/mountain_climbers.json",
            instructionsRes = R.array.ex_mountain_climbers_instructions,
            breathingTipsRes = R.string.ex_mountain_climbers_breathing,
            commonMistakesRes = R.string.ex_mountain_climbers_mistake
        ),
        Exercise(
            id = "high_knees",
            nameRes = R.string.ex_high_knees_name,
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 30,
            defaultReps = 35,
            caloriesPerMinute = 9.3f,
            lottieAsset = "exercises/lottie/high_knees.json",
            instructionsRes = R.array.ex_high_knees_instructions,
            breathingTipsRes = R.string.ex_high_knees_breathing,
            commonMistakesRes = R.string.ex_high_knees_mistake
        ),
        Exercise(
            id = "burpees",
            nameRes = R.string.ex_burpees_name,
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 11.7f,
            lottieAsset = "exercises/lottie/burpees.json",
            instructionsRes = R.array.ex_burpees_instructions,
            breathingTipsRes = R.string.ex_burpees_breathing,
            commonMistakesRes = R.string.ex_burpees_mistake
        ),

        // PECHO
        Exercise(
            id = "push_ups_knees",
            nameRes = R.string.ex_push_ups_knees_name,
            bodyPart = BodyPart.PECHO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 4.7f,
            lottieAsset = "exercises/lottie/push_ups_knees.json",
            instructionsRes = R.array.ex_push_ups_knees_instructions,
            breathingTipsRes = R.string.ex_push_ups_knees_breathing,
            commonMistakesRes = R.string.ex_push_ups_knees_mistake
        ),
        Exercise(
            id = "push_ups_standard",
            nameRes = R.string.ex_push_ups_standard_name,
            bodyPart = BodyPart.PECHO,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 15,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/push_ups_standard.json",
            instructionsRes = R.array.ex_push_ups_standard_instructions,
            breathingTipsRes = R.string.ex_push_ups_standard_breathing,
            commonMistakesRes = R.string.ex_push_ups_standard_mistake
        ),
        Exercise(
            id = "push_ups_diamond",
            nameRes = R.string.ex_push_ups_diamond_name,
            bodyPart = BodyPart.PECHO,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 7.6f,
            lottieAsset = "exercises/lottie/push_ups_diamond.json",
            instructionsRes = R.array.ex_push_ups_diamond_instructions,
            breathingTipsRes = R.string.ex_push_ups_diamond_breathing,
            commonMistakesRes = R.string.ex_push_ups_diamond_mistake
        ),

        // ABDOMEN Y CORE
        Exercise(
            id = "crunches",
            nameRes = R.string.ex_crunches_name,
            bodyPart = BodyPart.ABDOMEN,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 18,
            caloriesPerMinute = 4.4f,
            lottieAsset = "exercises/lottie/crunches.json",
            instructionsRes = R.array.ex_crunches_instructions,
            breathingTipsRes = R.string.ex_crunches_breathing,
            commonMistakesRes = R.string.ex_crunches_mistake
        ),
        Exercise(
            id = "plank",
            nameRes = R.string.ex_plank_name,
            bodyPart = BodyPart.ABDOMEN,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 40,
            defaultReps = 12,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/plank.json",
            instructionsRes = R.array.ex_plank_instructions,
            breathingTipsRes = R.string.ex_plank_breathing,
            commonMistakesRes = R.string.ex_plank_mistake
        ),
        Exercise(
            id = "bicycle_crunches",
            nameRes = R.string.ex_bicycle_crunches_name,
            bodyPart = BodyPart.ABDOMEN,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 35,
            defaultReps = 24,
            caloriesPerMinute = 5.8f,
            lottieAsset = "exercises/lottie/bicycle_crunches.json",
            instructionsRes = R.array.ex_bicycle_crunches_instructions,
            breathingTipsRes = R.string.ex_bicycle_crunches_breathing,
            commonMistakesRes = R.string.ex_bicycle_crunches_mistake
        ),

        // PIERNAS Y GLÚTEOS
        Exercise(
            id = "squats_bodyweight",
            nameRes = R.string.ex_squats_bodyweight_name,
            bodyPart = BodyPart.PIERNAS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 35,
            defaultReps = 16,
            caloriesPerMinute = 6.4f,
            lottieAsset = "exercises/lottie/squats_bodyweight.json",
            instructionsRes = R.array.ex_squats_bodyweight_instructions,
            breathingTipsRes = R.string.ex_squats_bodyweight_breathing,
            commonMistakesRes = R.string.ex_squats_bodyweight_mistake
        ),
        Exercise(
            id = "lunges_alternating",
            nameRes = R.string.ex_lunges_alternating_name,
            bodyPart = BodyPart.PIERNAS,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 16,
            caloriesPerMinute = 5.8f,
            lottieAsset = "exercises/lottie/lunges_alternating.json",
            instructionsRes = R.array.ex_lunges_alternating_instructions,
            breathingTipsRes = R.string.ex_lunges_alternating_breathing,
            commonMistakesRes = R.string.ex_lunges_alternating_mistake
        ),
        Exercise(
            id = "glute_bridge",
            nameRes = R.string.ex_glute_bridge_name,
            bodyPart = BodyPart.PIERNAS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 35,
            defaultReps = 15,
            caloriesPerMinute = 4.7f,
            lottieAsset = "exercises/lottie/glute_bridge.json",
            instructionsRes = R.array.ex_glute_bridge_instructions,
            breathingTipsRes = R.string.ex_glute_bridge_breathing,
            commonMistakesRes = R.string.ex_glute_bridge_mistake
        ),
        Exercise(
            id = "wall_sit",
            nameRes = R.string.ex_wall_sit_name,
            bodyPart = BodyPart.PIERNAS,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 5.2f,
            lottieAsset = "exercises/lottie/wall_sit.json",
            instructionsRes = R.array.ex_wall_sit_instructions,
            breathingTipsRes = R.string.ex_wall_sit_breathing,
            commonMistakesRes = R.string.ex_wall_sit_mistake
        ),

        // BRAZOS
        Exercise(
            id = "triceps_dips",
            nameRes = R.string.ex_triceps_dips_name,
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 14,
            caloriesPerMinute = 5.2f,
            lottieAsset = "exercises/lottie/triceps_dips.json",
            instructionsRes = R.array.ex_triceps_dips_instructions,
            breathingTipsRes = R.string.ex_triceps_dips_breathing,
            commonMistakesRes = R.string.ex_triceps_dips_mistake
        ),

        // ESPALDA Y HOMBROS
        Exercise(
            id = "superman",
            nameRes = R.string.ex_superman_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 14,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/superman.json",
            instructionsRes = R.array.ex_superman_instructions,
            breathingTipsRes = R.string.ex_superman_breathing,
            commonMistakesRes = R.string.ex_superman_mistake
        ),
        Exercise(
            id = "cobra_stretch",
            nameRes = R.string.ex_cobra_stretch_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 2.9f,
            lottieAsset = "exercises/lottie/cobra_stretch.json",
            instructionsRes = R.array.ex_cobra_stretch_instructions,
            breathingTipsRes = R.string.ex_cobra_stretch_breathing,
            commonMistakesRes = R.string.ex_cobra_stretch_mistake
        ),

        // BRAZOS (sin equipamiento)
        Exercise(
            id = "wall_push_ups",
            nameRes = R.string.ex_wall_push_ups_name,
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/wall_push_ups.json",
            instructionsRes = R.array.ex_wall_push_ups_instructions,
            breathingTipsRes = R.string.ex_wall_push_ups_breathing,
            commonMistakesRes = R.string.ex_wall_push_ups_mistake
        ),
        Exercise(
            id = "triceps_dips_floor",
            nameRes = R.string.ex_triceps_dips_floor_name,
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 4.7f,
            lottieAsset = "exercises/lottie/triceps_dips_floor.json",
            instructionsRes = R.array.ex_triceps_dips_floor_instructions,
            breathingTipsRes = R.string.ex_triceps_dips_floor_breathing,
            commonMistakesRes = R.string.ex_triceps_dips_floor_mistake
        ),
        Exercise(
            id = "kneeling_triceps_extension",
            nameRes = R.string.ex_kneeling_triceps_extension_name,
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 3.5f,
            lottieAsset = "exercises/lottie/kneeling_triceps_extension.json",
            instructionsRes = R.array.ex_kneeling_triceps_extension_instructions,
            breathingTipsRes = R.string.ex_kneeling_triceps_extension_breathing,
            commonMistakesRes = R.string.ex_kneeling_triceps_extension_mistake
        ),
        Exercise(
            id = "biceps_self_resistance",
            nameRes = R.string.ex_biceps_self_resistance_name,
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 14,
            caloriesPerMinute = 3.5f,
            lottieAsset = "exercises/lottie/biceps_self_resistance.json",
            instructionsRes = R.array.ex_biceps_self_resistance_instructions,
            breathingTipsRes = R.string.ex_biceps_self_resistance_breathing,
            commonMistakesRes = R.string.ex_biceps_self_resistance_mistake
        ),
        Exercise(
            id = "close_grip_push_ups",
            nameRes = R.string.ex_close_grip_push_ups_name,
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/close_grip_push_ups.json",
            instructionsRes = R.array.ex_close_grip_push_ups_instructions,
            breathingTipsRes = R.string.ex_close_grip_push_ups_breathing,
            commonMistakesRes = R.string.ex_close_grip_push_ups_mistake
        ),
        Exercise(
            id = "elbow_dips",
            nameRes = R.string.ex_elbow_dips_name,
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 12,
            caloriesPerMinute = 5.8f,
            lottieAsset = "exercises/lottie/triceps_dips.json",
            instructionsRes = R.array.ex_elbow_dips_instructions,
            breathingTipsRes = R.string.ex_elbow_dips_breathing,
            commonMistakesRes = R.string.ex_elbow_dips_mistake
        ),
        Exercise(
            id = "side_push_up",
            nameRes = R.string.ex_side_push_up_name,
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 10,
            caloriesPerMinute = 5.2f,
            lottieAsset = "exercises/lottie/side_push_up.json",
            instructionsRes = R.array.ex_side_push_up_instructions,
            breathingTipsRes = R.string.ex_side_push_up_breathing,
            commonMistakesRes = R.string.ex_side_push_up_mistake
        ),
        Exercise(
            id = "body_up",
            nameRes = R.string.ex_body_up_name,
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 10,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/body_up.json",
            instructionsRes = R.array.ex_body_up_instructions,
            breathingTipsRes = R.string.ex_body_up_breathing,
            commonMistakesRes = R.string.ex_body_up_mistake
        ),

        // ESPALDA Y HOMBROS (sin equipamiento)
        Exercise(
            id = "towel_row",
            nameRes = R.string.ex_towel_row_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 15,
            caloriesPerMinute = 5.8f,
            lottieAsset = "exercises/lottie/inverted_row_table.json",
            instructionsRes = R.array.ex_towel_row_instructions,
            breathingTipsRes = R.string.ex_towel_row_breathing,
            commonMistakesRes = R.string.ex_towel_row_mistake
        ),
        Exercise(
            id = "prone_elbow_lift",
            nameRes = R.string.ex_prone_elbow_lift_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 14,
            caloriesPerMinute = 3.5f,
            lottieAsset = "exercises/lottie/prone_elbow_lift.json",
            instructionsRes = R.array.ex_prone_elbow_lift_instructions,
            breathingTipsRes = R.string.ex_prone_elbow_lift_breathing,
            commonMistakesRes = R.string.ex_prone_elbow_lift_mistake
        ),
        Exercise(
            id = "scapula_push_up",
            nameRes = R.string.ex_scapula_push_up_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/scapula_push_up.json",
            instructionsRes = R.array.ex_scapula_push_up_instructions,
            breathingTipsRes = R.string.ex_scapula_push_up_breathing,
            commonMistakesRes = R.string.ex_scapula_push_up_mistake
        ),
        Exercise(
            id = "sphinx_hold",
            nameRes = R.string.ex_sphinx_hold_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 2.9f,
            lottieAsset = "exercises/lottie/sphinx_hold.json",
            instructionsRes = R.array.ex_sphinx_hold_instructions,
            breathingTipsRes = R.string.ex_sphinx_hold_breathing,
            commonMistakesRes = R.string.ex_sphinx_hold_mistake
        ),
        Exercise(
            id = "towel_squat_row",
            nameRes = R.string.ex_towel_squat_row_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 15,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/towel_squat_row.json",
            instructionsRes = R.array.ex_towel_squat_row_instructions,
            breathingTipsRes = R.string.ex_towel_squat_row_breathing,
            commonMistakesRes = R.string.ex_towel_squat_row_mistake
        ),
        Exercise(
            id = "inverted_row_table",
            nameRes = R.string.ex_inverted_row_table_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 12,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/inverted_row_table.json",
            instructionsRes = R.array.ex_inverted_row_table_instructions,
            breathingTipsRes = R.string.ex_inverted_row_table_breathing,
            commonMistakesRes = R.string.ex_inverted_row_table_mistake
        ),
        Exercise(
            id = "reverse_plank_leg_lift",
            nameRes = R.string.ex_reverse_plank_leg_lift_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 5.2f,
            lottieAsset = "exercises/lottie/reverse_plank_leg_lift.json",
            instructionsRes = R.array.ex_reverse_plank_leg_lift_instructions,
            breathingTipsRes = R.string.ex_reverse_plank_leg_lift_breathing,
            commonMistakesRes = R.string.ex_reverse_plank_leg_lift_mistake
        ),
        Exercise(
            id = "upward_dog",
            nameRes = R.string.ex_upward_dog_name,
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 2.9f,
            lottieAsset = "exercises/lottie/cobra_stretch.json",
            instructionsRes = R.array.ex_upward_dog_instructions,
            breathingTipsRes = R.string.ex_upward_dog_breathing,
            commonMistakesRes = R.string.ex_upward_dog_mistake
        ),

        // PALO Y MOVILIDAD (palo, bastón o mango de escoba)
        Exercise(
            id = "stick_shoulder_pass",
            nameRes = R.string.ex_stick_shoulder_pass_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 3.5f,
            lottieAsset = "exercises/lottie/stick_shoulder_pass.json",
            instructionsRes = R.array.ex_stick_shoulder_pass_instructions,
            breathingTipsRes = R.string.ex_stick_shoulder_pass_breathing,
            commonMistakesRes = R.string.ex_stick_shoulder_pass_mistake
        ),
        Exercise(
            id = "stick_torso_twist",
            nameRes = R.string.ex_stick_torso_twist_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 16,
            caloriesPerMinute = 3.5f,
            lottieAsset = "exercises/lottie/stick_torso_twist.json",
            instructionsRes = R.array.ex_stick_torso_twist_instructions,
            breathingTipsRes = R.string.ex_stick_torso_twist_breathing,
            commonMistakesRes = R.string.ex_stick_torso_twist_mistake
        ),
        Exercise(
            id = "stick_overhead_press",
            nameRes = R.string.ex_stick_overhead_press_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 35,
            defaultReps = 12,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/stick_overhead_press.json",
            instructionsRes = R.array.ex_stick_overhead_press_instructions,
            breathingTipsRes = R.string.ex_stick_overhead_press_breathing,
            commonMistakesRes = R.string.ex_stick_overhead_press_mistake
        ),
        Exercise(
            id = "stick_side_bend",
            nameRes = R.string.ex_stick_side_bend_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 2.9f,
            lottieAsset = "exercises/lottie/stick_side_bend.json",
            instructionsRes = R.array.ex_stick_side_bend_instructions,
            breathingTipsRes = R.string.ex_stick_side_bend_breathing,
            commonMistakesRes = R.string.ex_stick_side_bend_mistake
        ),
        Exercise(
            id = "stick_good_morning",
            nameRes = R.string.ex_stick_good_morning_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 35,
            defaultReps = 12,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/stick_good_morning.json",
            instructionsRes = R.array.ex_stick_good_morning_instructions,
            breathingTipsRes = R.string.ex_stick_good_morning_breathing,
            commonMistakesRes = R.string.ex_stick_good_morning_mistake
        ),
        Exercise(
            id = "stick_back_squat",
            nameRes = R.string.ex_stick_back_squat_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 35,
            defaultReps = 14,
            caloriesPerMinute = 5.2f,
            lottieAsset = "exercises/lottie/stick_back_squat.json",
            instructionsRes = R.array.ex_stick_back_squat_instructions,
            breathingTipsRes = R.string.ex_stick_back_squat_breathing,
            commonMistakesRes = R.string.ex_stick_back_squat_mistake
        ),
        Exercise(
            id = "stick_shoulder_windmill",
            nameRes = R.string.ex_stick_shoulder_windmill_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 10,
            caloriesPerMinute = 3.5f,
            lottieAsset = "exercises/lottie/stick_shoulder_windmill.json",
            instructionsRes = R.array.ex_stick_shoulder_windmill_instructions,
            breathingTipsRes = R.string.ex_stick_shoulder_windmill_breathing,
            commonMistakesRes = R.string.ex_stick_shoulder_windmill_mistake
        ),
        Exercise(
            id = "stick_woodchopper",
            nameRes = R.string.ex_stick_woodchopper_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 35,
            defaultReps = 14,
            caloriesPerMinute = 4.7f,
            lottieAsset = "exercises/lottie/stick_woodchopper.json",
            instructionsRes = R.array.ex_stick_woodchopper_instructions,
            breathingTipsRes = R.string.ex_stick_woodchopper_breathing,
            commonMistakesRes = R.string.ex_stick_woodchopper_mistake
        ),
        Exercise(
            id = "stick_wrist_rolls",
            nameRes = R.string.ex_stick_wrist_rolls_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 16,
            caloriesPerMinute = 2.9f,
            lottieAsset = "exercises/lottie/stick_wrist_rolls.json",
            instructionsRes = R.array.ex_stick_wrist_rolls_instructions,
            breathingTipsRes = R.string.ex_stick_wrist_rolls_breathing,
            commonMistakesRes = R.string.ex_stick_wrist_rolls_mistake
        ),
        Exercise(
            id = "stick_knee_raise",
            nameRes = R.string.ex_stick_knee_raise_name,
            bodyPart = BodyPart.PALO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 20,
            caloriesPerMinute = 4.3f,
            lottieAsset = "exercises/lottie/stick_knee_raise.json",
            instructionsRes = R.array.ex_stick_knee_raise_instructions,
            breathingTipsRes = R.string.ex_stick_knee_raise_breathing,
            commonMistakesRes = R.string.ex_stick_knee_raise_mistake
        )
    )

    /**
     * Bloque de calentamiento común a toda rutina: movilidad articular suave de
     * tobillos, muñecas y tronco. Son ejercicios de pie y sin equipamiento, con
     * intensidad mínima, para preparar articulaciones antes del trabajo principal.
     */
    val warmUpExercises: List<Exercise> = listOf(
        Exercise(
            id = "warmup_ankle_circles",
            nameRes = R.string.ex_warmup_ankle_circles_name,
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 20,
            defaultReps = 10,
            caloriesPerMinute = 2.3f,
            lottieAsset = "exercises/lottie/warmup_ankle_circles.json",
            instructionsRes = R.array.ex_warmup_ankle_circles_instructions,
            breathingTipsRes = R.string.ex_warmup_ankle_circles_breathing,
            commonMistakesRes = R.string.ex_warmup_ankle_circles_mistake,
            isWarmUp = true
        ),
        Exercise(
            id = "warmup_wrist_circles",
            nameRes = R.string.ex_warmup_wrist_circles_name,
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 20,
            defaultReps = 10,
            caloriesPerMinute = 2.3f,
            lottieAsset = "exercises/lottie/warmup_wrist_circles.json",
            instructionsRes = R.array.ex_warmup_wrist_circles_instructions,
            breathingTipsRes = R.string.ex_warmup_wrist_circles_breathing,
            commonMistakesRes = R.string.ex_warmup_wrist_circles_mistake,
            isWarmUp = true
        ),
        Exercise(
            id = "warmup_lateral_stretch",
            nameRes = R.string.ex_warmup_lateral_stretch_name,
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 25,
            defaultReps = 8,
            caloriesPerMinute = 2.7f,
            lottieAsset = "exercises/lottie/lateral_stretch.json",
            instructionsRes = R.array.ex_warmup_lateral_stretch_instructions,
            breathingTipsRes = R.string.ex_warmup_lateral_stretch_breathing,
            commonMistakesRes = R.string.ex_warmup_lateral_stretch_mistake,
            isWarmUp = true
        )
    )

    fun getExercisesByBodyPart(bodyPart: BodyPart): List<Exercise> {
        return when (bodyPart) {
            BodyPart.CUERPO_COMPLETO -> allExercises
            // La sesión de palo es de movilidad: no debe mezclar el cardio de
            // cuerpo completo (saltos, burpees) con el trabajo con bastón.
            BodyPart.PALO -> allExercises.filter { it.bodyPart == BodyPart.PALO }
            else -> allExercises.filter {
                it.bodyPart == bodyPart || it.bodyPart == BodyPart.CUERPO_COMPLETO
            }
        }
    }

    fun getExerciseById(id: String): Exercise? {
        val all = allExercises + warmUpExercises
        return all.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
