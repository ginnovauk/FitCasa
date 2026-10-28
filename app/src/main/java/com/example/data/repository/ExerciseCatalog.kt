package com.example.data.repository

import com.example.data.model.BodyPart
import com.example.data.model.Exercise
import com.example.data.model.IntensityLevel

object ExerciseCatalog {

    val allExercises: List<Exercise> = listOf(
        // CUERPO COMPLETO
        Exercise(
            id = "jumping_jacks",
            name = "Saltos de Tijera (Jumping Jacks)",
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 30,
            caloriesPerMinute = 9.3f,
            lottieAsset = "exercises/lottie/jumping_jacks.json",
            instructions = listOf(
                "Párate con los pies juntos y los brazos a los lados.",
                "Da un salto abriendo las piernas a la anchura de los hombros mientras elevas las manos por encima de la cabeza.",
                "Regresa a la posición inicial con otro salto suave sobre la punta de los pies."
            ),
            breathingTips = "Inhala al saltar abriendo, exhala al cerrar el cuerpo.",
            commonMistakes = "Caer con los talones duros en el suelo; amortigua siempre con las rodillas ligeramente flexionadas."
        ),
        Exercise(
            id = "mountain_climbers",
            name = "Escaladores de Montaña",
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 30,
            defaultReps = 24,
            caloriesPerMinute = 9.3f,
            lottieAsset = "exercises/lottie/mountain_climbers.json",
            instructions = listOf(
                "Comienza en posición de plancha alta con las manos directamente bajo los hombros.",
                "Lleva una rodilla hacia el pecho sin levantar la cadera.",
                "Alterna rápidamente de pierna con un movimiento explosivo y rítmico."
            ),
            breathingTips = "Mantén una respiración rítmica y continua, sin contener el aire.",
            commonMistakes = "Elevar demasiado la pelvis o dejar caer la zona lumbar."
        ),
        Exercise(
            id = "high_knees",
            name = "Rodillas Altas al Pecho",
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 30,
            defaultReps = 35,
            caloriesPerMinute = 9.3f,
            lottieAsset = "exercises/lottie/high_knees.json",
            instructions = listOf(
                "Corre en el lugar elevando las rodillas a la altura de la cadera.",
                "Mantén el pecho erguido y acompaña el movimiento balanceando los brazos.",
                "Aterriza suavemente sobre las puntas de los pies con ritmo rápido."
            ),
            breathingTips = "Inhala en 2 pasos, exhala en 2 pasos.",
            commonMistakes = "Inclinarse hacia atrás mientras se suben las piernas."
        ),
        Exercise(
            id = "burpees",
            name = "Burpees Dinámicos",
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 11.7f,
            lottieAsset = "exercises/lottie/burpees.json",
            instructions = listOf(
                "Desde posición de pie, agáchate y coloca las manos en el suelo.",
                "Lanza los pies hacia atrás para quedar en posición de plancha.",
                "Regresa los pies de un salto hacia las manos y salta verticalmente extendiendo los brazos."
            ),
            breathingTips = "Exhala en el salto ascendente, inhala al descender al suelo.",
            commonMistakes = "Arquear la espalda lumbar al saltar hacia atrás."
        ),

        // PECHO
        Exercise(
            id = "push_ups_knees",
            name = "Flexiones con Rodillas Apoyadas",
            bodyPart = BodyPart.PECHO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 4.7f,
            lottieAsset = "exercises/lottie/push_ups_knees.json",
            instructions = listOf(
                "Apoya las rodillas y las palmas de las manos a una anchura superior a los hombros.",
                "Mantén una línea recta desde la cabeza hasta las rodillas contrayendo el abdomen.",
                "Baja el pecho hacia el suelo doblando los codos a 45 grados y empuja con fuerza."
            ),
            breathingTips = "Inhala al bajar el pecho, exhala al empujar hacia arriba.",
            commonMistakes = "Dejar caer la cabeza o hiperextender el cuello."
        ),
        Exercise(
            id = "push_ups_standard",
            name = "Flexiones de Pecho Clásicas",
            bodyPart = BodyPart.PECHO,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 15,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/push_ups_standard.json",
            instructions = listOf(
                "Colócate en posición de plancha alta con los pies juntos y el abdomen firme.",
                "Desciende controladamente hasta que el pecho quede a pocos centímetros del suelo.",
                "Empuja el suelo extendiendo los brazos sin bloquear bruscamente los codos."
            ),
            breathingTips = "Inhala en la bajada, exhala al subir con potencia.",
            commonMistakes = "Abrir los codos en ángulo de 90 grados respecto al torso; deben formar una flecha (45°)."
        ),
        Exercise(
            id = "push_ups_diamond",
            name = "Flexiones Diamante (Pecho y Tríceps)",
            bodyPart = BodyPart.PECHO,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 7.6f,
            lottieAsset = "exercises/lottie/push_ups_diamond.json",
            instructions = listOf(
                "Junta los dedos pulgares e índices en el suelo formando un triángulo o diamante bajo el pecho.",
                "Baja el centro del pecho hacia el centro de las manos manteniendo los codos pegados.",
                "Empuja activando la zona central del pectoral y tríceps."
            ),
            breathingTips = "Inhala al descender, exhala al subir.",
            commonMistakes = "Hacer rebotes cortos sin completar el rango completo de movimiento."
        ),

        // ABDOMEN Y CORE
        Exercise(
            id = "crunches",
            name = "Crunches Abdominales",
            bodyPart = BodyPart.ABDOMEN,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 18,
            caloriesPerMinute = 4.4f,
            lottieAsset = "exercises/lottie/crunches.json",
            instructions = listOf(
                "Acuéstate boca arriba con las rodillas dobladas y los pies firmes en el suelo.",
                "Coloca las manos detrás de las orejas sin tirar del cuello.",
                "Eleva los omóplatos apretando fuertemente el abdomen y baja con control."
            ),
            breathingTips = "Exhala al subir y contraer el core, inhala al bajar los hombros.",
            commonMistakes = "Jalar la nuca con las manos forzando las cervicales."
        ),
        Exercise(
            id = "plank",
            name = "Plancha Abdominal Isométrica",
            bodyPart = BodyPart.ABDOMEN,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 40,
            defaultReps = 12,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/plank.json",
            instructions = listOf(
                "Apoya los antebrazos y las puntas de los pies en el suelo.",
                "Forma una línea recta perfecta desde la cabeza hasta los talones.",
                "Aprieta glúteos y ombligo hacia la columna sin dejar caer la pelvis."
            ),
            breathingTips = "Respira de forma controlada y profunda sin aguantar la respiración.",
            commonMistakes = "Hundir la zona lumbar o levantar la cadera en triángulo."
        ),
        Exercise(
            id = "bicycle_crunches",
            name = "Abdominales Bicicleta Cruzados",
            bodyPart = BodyPart.ABDOMEN,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 35,
            defaultReps = 24,
            caloriesPerMinute = 5.8f,
            lottieAsset = "exercises/lottie/bicycle_crunches.json",
            instructions = listOf(
                "Acuéstate boca arriba con las piernas levantadas en ángulo de 90 grados.",
                "Lleva el codo derecho hacia la rodilla izquierda mientras extiendes la pierna derecha.",
                "Alterna de lado de forma fluida rotando desde la caja torácica."
            ),
            breathingTips = "Exhala en cada cruce con contracción diagonal.",
            commonMistakes = "Mover solo los codos en vez de rotar el tronco completo."
        ),

        // PIERNAS Y GLÚTEOS
        Exercise(
            id = "squats_bodyweight",
            name = "Sentadillas Clásicas",
            bodyPart = BodyPart.PIERNAS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 35,
            defaultReps = 16,
            caloriesPerMinute = 6.4f,
            lottieAsset = "exercises/lottie/squats_bodyweight.json",
            instructions = listOf(
                "Párate con los pies separados al ancho de los hombros y puntas ligeramente abiertas.",
                "Flexiona caderas y rodillas como si fueras a sentarte en una silla baja.",
                "Baja hasta que los muslos queden paralelos al suelo y vuelve a subir empujando los talones."
            ),
            breathingTips = "Inhala al descender, exhala con fuerza al subir.",
            commonMistakes = "Dejar que las rodillas colapsen hacia adentro o despegar los talones."
        ),
        Exercise(
            id = "lunges_alternating",
            name = "Zancadas Alternas",
            bodyPart = BodyPart.PIERNAS,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 16,
            caloriesPerMinute = 5.8f,
            lottieAsset = "exercises/lottie/lunges_alternating.json",
            instructions = listOf(
                "Da un paso amplio hacia adelante con una pierna manteniendo el torso recto.",
                "Flexiona ambas rodillas hasta que formen un ángulo de 90 grados.",
                "Empuja con el talón delantero para regresar a la posición inicial y alterna."
            ),
            breathingTips = "Inhala en el paso hacia abajo, exhala al regresar al centro.",
            commonMistakes = "Que la rodilla adelantada sobrepase excesivamente la punta del pie."
        ),
        Exercise(
            id = "glute_bridge",
            name = "Puente de Glúteos y Femoral",
            bodyPart = BodyPart.PIERNAS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 35,
            defaultReps = 15,
            caloriesPerMinute = 4.7f,
            lottieAsset = "exercises/lottie/glute_bridge.json",
            instructions = listOf(
                "Acuéstate boca arriba con las rodillas dobladas y los pies planos cerca de los glúteos.",
                "Eleva la pelvis contrayendo los glúteos hasta alinear rodillas, caderas y hombros.",
                "Sostén 1 segundo arriba y baja despacio sin tocar del todo el suelo."
            ),
            breathingTips = "Exhala al subir la pelvis, inhala al descender.",
            commonMistakes = "Arquear la zona lumbar en lugar de extender la cadera con los glúteos."
        ),
        Exercise(
            id = "wall_sit",
            name = "Sentadilla Isométrica en Pared",
            bodyPart = BodyPart.PIERNAS,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 5.2f,
            lottieAsset = "exercises/lottie/wall_sit.json",
            instructions = listOf(
                "Apoya la espalda plana contra una pared y deslízate hacia abajo.",
                "Detente cuando los muslos formen un ángulo de 90 grados con el suelo.",
                "Apoya las manos en los muslos o déjalas a los lados, resistiendo la postura."
            ),
            breathingTips = "Respira con calma y mantén el abdomen firme.",
            commonMistakes = "Apoyar las manos en las rodillas para quitar carga a las piernas."
        ),

        // BRAZOS
        Exercise(
            id = "triceps_dips",
            name = "Fondos de Tríceps",
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 14,
            caloriesPerMinute = 5.2f,
            lottieAsset = "exercises/lottie/triceps_dips.json",
            instructions = listOf(
                "Apoya las palmas sobre una silla firme, sillón o en el suelo detrás de ti.",
                "Con los codos apuntando hacia atrás, flexiónalos bajando la cadera cerca del soporte.",
                "Empuja con las palmas extendiendo los brazos y contrayendo los tríceps."
            ),
            breathingTips = "Inhala al bajar el cuerpo, exhala al extender los brazos.",
            commonMistakes = "Alejar la espalda del apoyo o encoger los hombros hacia las orejas."
        ),

        // ESPALDA Y HOMBROS
        Exercise(
            id = "superman",
            name = "Superman Lumbar",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 14,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/superman.json",
            instructions = listOf(
                "Acuéstate boca abajo con brazos y piernas estirados completamente.",
                "Eleva simultáneamente el pecho, los brazos y los muslos del suelo unos centímetros.",
                "Aprieta la zona lumbar y glúteos en el punto alto y desciende con suavidad."
            ),
            breathingTips = "Inhala en el suelo, exhala al despegar las extremidades.",
            commonMistakes = "Tironear con el cuello mirando hacia arriba en lugar de mantener la mirada al suelo."
        ),
        Exercise(
            id = "cobra_stretch",
            name = "Cobra y Extensión Dorsal",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 2.9f,
            lottieAsset = "exercises/lottie/cobra_stretch.json",
            instructions = listOf(
                "Acuéstate boca abajo con las palmas apoyadas a los lados del pecho.",
                "Empuja suavemente extendiendo el tronco hacia arriba abriendo el pecho.",
                "Aleja los hombros de las orejas y siente la descompresión de la espalda."
            ),
            breathingTips = "Inhala profundo abriendo la caja torácica, exhala relajando la tensión.",
            commonMistakes = "Bloquear los codos bruscamente si sientes molestia en la zona lumbar."
        ),

        // BRAZOS (sin equipamiento)
        Exercise(
            id = "wall_push_ups",
            name = "Flexiones en la Pared",
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/wall_push_ups.json",
            instructions = listOf(
                "Apoya las palmas en la pared a la altura de los hombros, un poco más separadas que el ancho de los hombros.",
                "Da un paso atrás y mantén el cuerpo en línea recta desde la cabeza hasta los talones.",
                "Flexiona los codos acercando el pecho a la pared y empuja para volver, sin bloquear los codos de golpe."
            ),
            breathingTips = "Inhala al acercarte a la pared, exhala al empujar.",
            commonMistakes = "Dejar caer la cadera o encoger los hombros hacia las orejas."
        ),
        Exercise(
            id = "triceps_dips_floor",
            name = "Fondos de Tríceps en el Suelo",
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 4.7f,
            lottieAsset = "exercises/lottie/triceps_dips_floor.json",
            instructions = listOf(
                "Siéntate en el borde de una silla estable con las manos al lado de las caderas y los dedos hacia delante.",
                "Desliza la cadera fuera del asiento con las piernas extendidas y la espalda cerca de la silla.",
                "Baja flexionando los codos hacia atrás hasta unos 90 grados y empuja para subir."
            ),
            breathingTips = "Inhala al bajar, exhala al empujar hacia arriba.",
            commonMistakes = "Alejar la espalda del asiento o encoger los hombros hacia las orejas."
        ),
        Exercise(
            id = "kneeling_triceps_extension",
            name = "Extensión de Tríceps de Rodillas",
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 3.5f,
            lottieAsset = "exercises/lottie/kneeling_triceps_extension.json",
            instructions = listOf(
                "Arrodíllate con las manos en el suelo bajo los hombros y los dedos hacia delante.",
                "Mantén el cuerpo en línea recta desde la cabeza hasta las rodillas y el abdomen firme.",
                "Flexiona los codos hacia atrás bajando el pecho y empuja para extender los brazos."
            ),
            breathingTips = "Inhala al bajar el pecho, exhala al extender los codos.",
            commonMistakes = "Abrir los codos hacia los lados en lugar de mantenerlos pegados al torso."
        ),
        Exercise(
            id = "biceps_self_resistance",
            name = "Curl de Bíceps con Auto-Resistencia",
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 14,
            caloriesPerMinute = 3.5f,
            lottieAsset = "exercises/lottie/biceps_self_resistance.json",
            instructions = listOf(
                "Túmbate de lado con las piernas extendidas y la cabeza apoyada en el brazo de abajo.",
                "Pega el brazo de arriba al costado y flexiona el codo llevando la mano hacia el hombro.",
                "Haz fuerza con la mano contra el propio brazo para frenar la bajada y repite."
            ),
            breathingTips = "Exhala al flexionar el codo, inhala al bajar con control.",
            commonMistakes = "Mover el hombro o el torso para ayudar al brazo; solo debe trabajar el codo."
        ),
        Exercise(
            id = "close_grip_push_ups",
            name = "Flexiones con Manos Juntas",
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/close_grip_push_ups.json",
            instructions = listOf(
                "Colócate en plancha alta con las manos juntas bajo el pecho y el abdomen firme.",
                "Baja el cuerpo manteniendo los codos pegados a las costillas.",
                "Empuja con las palmas para extender los brazos y volver a la plancha."
            ),
            breathingTips = "Inhala en la bajada, exhala al empujar.",
            commonMistakes = "Abrir los codos hacia fuera, lo que quita trabajo al tríceps y carga el hombro."
        ),
        Exercise(
            id = "elbow_dips",
            name = "Fondos de Codos en Silla",
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 12,
            caloriesPerMinute = 5.8f,
            lottieAsset = "exercises/lottie/triceps_dips.json",
            instructions = listOf(
                "Siéntate en el borde de una silla estable y agarra el borde con las manos junto a la cadera.",
                "Desliza la cadera hacia fuera y apoya los talones en el suelo con las piernas semiflexionadas.",
                "Baja flexionando los codos y empuja hasta estirar los brazos sin bloquear del todo."
            ),
            breathingTips = "Inhala al descender, exhala al subir.",
            commonMistakes = "Bajar demasiado y sentir presión en el hombro; detente a unos 90 grados de codo."
        ),
        Exercise(
            id = "side_push_up",
            name = "Flexiones Laterales de Tríceps",
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 10,
            caloriesPerMinute = 5.2f,
            lottieAsset = "exercises/lottie/side_push_up.json",
            instructions = listOf(
                "Túmbate de lado con las piernas extendidas y una encima de la otra.",
                "Apoya la mano de abajo bajo el hombro y empuja para levantar la cadera del suelo.",
                "Sube el brazo libre hacia el techo formando una línea recta y baja con control. Cambia de lado a mitad del ejercicio."
            ),
            breathingTips = "Exhala al elevar la cadera, inhala al bajar.",
            commonMistakes = "Dejar caer la cadera o adelantar el hombro de apoyo por delante de la mano."
        ),
        Exercise(
            id = "body_up",
            name = "Fondos Elevados de Tríceps",
            bodyPart = BodyPart.BRAZOS,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 10,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/body_up.json",
            instructions = listOf(
                "Apoya las palmas en un banco firme o en el borde de una mesa estable, con los dedos hacia delante.",
                "Extiende las piernas al frente apoyando los talones y mantén el cuerpo recto.",
                "Baja flexionando los codos pegados al cuerpo y empuja con las palmas para subir."
            ),
            breathingTips = "Inhala al bajar, exhala al empujar con fuerza.",
            commonMistakes = "Usar una superficie inestable o dejar caer la cadera por falta de abdomen."
        ),

        // ESPALDA Y HOMBROS (sin equipamiento)
        Exercise(
            id = "towel_row",
            name = "Remo de Pie con Toalla",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 15,
            caloriesPerMinute = 5.8f,
            lottieAsset = "exercises/lottie/inverted_row_table.json",
            instructions = listOf(
                "Sostén una toalla enrollada con las dos manos frente al pecho, con los pies al ancho de los hombros.",
                "Flexiona un poco las rodillas e inclina el torso hacia delante manteniendo la espalda recta.",
                "Tira de la toalla hacia el pecho juntando los omóplatos y vuelve despacio."
            ),
            breathingTips = "Exhala al tirar de la toalla, inhala al extender los brazos.",
            commonMistakes = "Redondear la espalda o encoger el cuello al juntar los omóplatos."
        ),
        Exercise(
            id = "prone_elbow_lift",
            name = "Elevación de Codos Boca Abajo",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 14,
            caloriesPerMinute = 3.5f,
            lottieAsset = "exercises/lottie/prone_elbow_lift.json",
            instructions = listOf(
                "Túmbate boca abajo con las piernas extendidas y las manos bajo los hombros.",
                "Aprieta el abdomen y empuja suavemente con las palmas para separar el pecho del suelo.",
                "Lleva los codos hacia atrás juntando los omóplatos, mantén un segundo y baja despacio."
            ),
            breathingTips = "Exhala al juntar los omóplatos, inhala al bajar.",
            commonMistakes = "Arquear la zona lumbar o levantar demasiado la cabeza; el movimiento nace de la espalda alta."
        ),
        Exercise(
            id = "scapula_push_up",
            name = "Flexiones Escapulares",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 4.1f,
            lottieAsset = "exercises/lottie/scapula_push_up.json",
            instructions = listOf(
                "Colócate en plancha alta con los brazos estirados y el abdomen firme.",
                "Sin doblar los codos, deja que el pecho descienda separando las escápulas.",
                "Empuja el suelo para volver a subir y junta las escápulas. El recorrido es corto y controlado."
            ),
            breathingTips = "Inhala al dejar caer el pecho, exhala al empujar.",
            commonMistakes = "Doblar los codos y convertirlo en una flexión normal."
        ),
        Exercise(
            id = "sphinx_hold",
            name = "Esfinge Isométrica",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 2.9f,
            lottieAsset = "exercises/lottie/sphinx_hold.json",
            instructions = listOf(
                "Túmbate boca abajo con los antebrazos apoyados y los codos bajo los hombros.",
                "Aprieta el abdomen y eleva el pecho separándolo del suelo sin encoger los hombros.",
                "Mantén la postura respirando con calma y con el cuello en posición neutra."
            ),
            breathingTips = "Respira profundo y lento, sin aguantar el aire.",
            commonMistakes = "Hundir la zona lumbar o mirar demasiado hacia arriba."
        ),
        Exercise(
            id = "towel_squat_row",
            name = "Remo en Sentadilla con Toalla",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 15,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/towel_squat_row.json",
            instructions = listOf(
                "Con la toalla en las manos, baja a media sentadilla manteniendo el pecho arriba.",
                "Desde esa posición, tira de la toalla hacia el pecho llevando los codos hacia atrás.",
                "Extiende los brazos y sube de la sentadilla para completar la repetición."
            ),
            breathingTips = "Exhala al tirar y al subir, inhala al bajar.",
            commonMistakes = "Perder la curvatura lumbar al inclinarse o tirar solo con los brazos."
        ),
        Exercise(
            id = "inverted_row_table",
            name = "Remo Invertido Bajo Mesa Firme",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.INTERMEDIO,
            defaultDurationSeconds = 35,
            defaultReps = 12,
            caloriesPerMinute = 7f,
            lottieAsset = "exercises/lottie/inverted_row_table.json",
            instructions = listOf(
                "Túmbate bajo una mesa robusta y agarra el borde con las manos algo más separadas que los hombros.",
                "Apoya los talones en el suelo con las piernas semiflexionadas y el cuerpo recto.",
                "Tira del borde llevando el pecho hacia la mesa juntando los omóplatos y baja con control. Usa solo una mesa estable que no se vuelque."
            ),
            breathingTips = "Exhala al subir el pecho, inhala al bajar.",
            commonMistakes = "Usar mesas inestables o dejar caer la cadera; si la mesa se mueve, cambia de ejercicio."
        ),
        Exercise(
            id = "reverse_plank_leg_lift",
            name = "Plancha Inversa con Elevación",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.AVANZADO,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 5.2f,
            lottieAsset = "exercises/lottie/reverse_plank_leg_lift.json",
            instructions = listOf(
                "Siéntate con las rodillas flexionadas y las palmas apoyadas detrás de la cadera.",
                "Empuja con las manos y los pies para elevar la cadera hasta formar una línea recta.",
                "Desde arriba, eleva una pierna extendida y baja la cadera para repetir alternando."
            ),
            breathingTips = "Exhala al elevar la cadera, inhala al descender.",
            commonMistakes = "Dejar caer la cabeza hacia atrás o bloquear los codos con tensión."
        ),
        Exercise(
            id = "upward_dog",
            name = "Perro Mirando Arriba",
            bodyPart = BodyPart.ESPALDA,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 30,
            defaultReps = 12,
            caloriesPerMinute = 2.9f,
            lottieAsset = "exercises/lottie/cobra_stretch.json",
            instructions = listOf(
                "Túmbate boca abajo con las piernas extendidas y las manos junto a las costillas bajas.",
                "Empuja el suelo para elevar el torso y los muslos, abriendo el pecho.",
                "Lleva los hombros atrás y abajo, mantén la postura unos segundos y baja despacio."
            ),
            breathingTips = "Inhala al abrir el pecho, exhala al bajar.",
            commonMistakes = "Comprimir la zona lumbar; reparte el estiramiento entre pecho y espalda alta."
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
            name = "Círculos de Tobillos",
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 20,
            defaultReps = 10,
            caloriesPerMinute = 2.3f,
            lottieAsset = "exercises/lottie/warmup_ankle_circles.json",
            instructions = listOf(
                "Apóyate en una pared o apoyo ligero con un pie ligeramente elevado.",
                "Gira el tobillo en círculos amplios, 10 veces en cada dirección.",
                "Cambia de pie y repite el movimiento con la misma amplitud."
            ),
            breathingTips = "Respira tranquilo y continuo; el calentamiento nunca debe agotarte.",
            commonMistakes = "Hacer círculos muy rápidos o pequeños; la movilidad se gana con amplitud, no con prisa.",
            isWarmUp = true
        ),
        Exercise(
            id = "warmup_wrist_circles",
            name = "Círculos de Muñecas",
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 20,
            defaultReps = 10,
            caloriesPerMinute = 2.3f,
            lottieAsset = "exercises/lottie/warmup_wrist_circles.json",
            instructions = listOf(
                "Extiende los brazos a la altura de los hombros con las manos abiertas.",
                "Gira las muñecas en círculos amplios, 10 veces hacia delante.",
                "Repite 10 círculos hacia atrás manteniendo los brazos firmes."
            ),
            breathingTips = "Inhala y exhala al ritmo de los círculos, sin contener el aire.",
            commonMistakes = "Mover solo los dedos en vez de girar la muñeca completa.",
            isWarmUp = true
        ),
        Exercise(
            id = "warmup_lateral_stretch",
            name = "Estiramiento Lateral de Pie",
            bodyPart = BodyPart.CUERPO_COMPLETO,
            intensity = IntensityLevel.PRINCIPIANTE,
            defaultDurationSeconds = 25,
            defaultReps = 8,
            caloriesPerMinute = 2.7f,
            lottieAsset = "exercises/lottie/lateral_stretch.json",
            instructions = listOf(
                "Párate erguido con los pies al ancho de los hombros.",
                "Eleva un brazo y lleva el tronco lentamente al lado contrario, sintiendo el estiramiento lateral.",
                "Mantén 3 segundos, vuelve al centro y cambia de lado."
            ),
            breathingTips = "Exhala al inclinarte de lado e inhala al volver al centro.",
            commonMistakes = "Girar el tronco hacia delante; el movimiento es puramente lateral.",
            isWarmUp = true
        )
    )

    fun getExercisesByBodyPart(bodyPart: BodyPart): List<Exercise> {
        return if (bodyPart == BodyPart.CUERPO_COMPLETO) {
            allExercises
        } else {
            allExercises.filter { it.bodyPart == bodyPart || it.bodyPart == BodyPart.CUERPO_COMPLETO }
        }
    }

    fun getExerciseById(id: String): Exercise? {
        val all = allExercises + warmUpExercises
        return all.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
