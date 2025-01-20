# Please developers (not translators) don't reuse a key inside another key
# or concat text with a translation string in the code, use the appropriate
# features like variables and selectors in each appropriate case!
# And also comment the string if it's something not easy to translate, so you help
# translators on what it means


## Websocket (server) status

websocket-connecting = Conectando al servidor
websocket-connection_lost = Conexión al servidor perdida. Intentando reconectar...

## Update notification

version_update-title = Nueva versión disponible: { $version }
version_update-description = Presionando «{ version_update-update }» descargara un instalador de SlimeVR para usar.
version_update-update = Actualizar
version_update-close = Cerrar

## Tips

tips-find_tracker = ¿No estás seguro de cuál sensor es cuál? Agita un sensor y se resaltará donde está asignado.
tips-do_not_move_heels = ¡Asegúrate de no mover los talones en la grabación!
tips-file_select = Arrastra y suelta archivos para usarlos, o <u>selecciónalos<u>.
tips-tap_setup = Puedes tocar lentamente 2 veces el sensor para seleccionarlo en lugar de seleccionarlo desde el menú.
tips-turn_on_tracker = ¿Estas usando trackers de SlimeVR oficiales? ¡Recuerda <b><em>encender tus trackers<em><b> después de conectarlos al PC!
tips-failed_webgl = Fallo al inicializar WebGL.

## Body parts

body_part-NONE = Sin asignar
body_part-HEAD = Cabeza
body_part-NECK = Cuello
body_part-RIGHT_SHOULDER = Hombro derecho
body_part-RIGHT_UPPER_ARM = Brazo superior derecho
body_part-RIGHT_LOWER_ARM = Antebrazo derecho
body_part-RIGHT_HAND = Mano derecha
body_part-RIGHT_UPPER_LEG = Muslo derecho
body_part-RIGHT_LOWER_LEG = Tobillo derecho
body_part-RIGHT_FOOT = Pie derecho
body_part-UPPER_CHEST = Pecho superior
body_part-CHEST = Pecho
body_part-WAIST = Cintura
body_part-HIP = Cadera
body_part-LEFT_SHOULDER = Hombro izquierdo
body_part-LEFT_UPPER_ARM = Brazo superior izquierdo
body_part-LEFT_LOWER_ARM = Antebrazo izquierdo
body_part-LEFT_HAND = Mano izquierda
body_part-LEFT_UPPER_LEG = Muslo izquierdo
body_part-LEFT_LOWER_LEG = Tobillo izquierdo
body_part-LEFT_FOOT = Pie izquierdo

## Proportions

skeleton_bone-NONE = Nada
skeleton_bone-HEAD = Inclinación de cabeza
skeleton_bone-NECK = Largo del cuello
skeleton_bone-torso_group = Largo del torso
skeleton_bone-UPPER_CHEST = Largo del pecho superior
skeleton_bone-CHEST_OFFSET = Chest Offset
skeleton_bone-CHEST = Largo del pecho
skeleton_bone-WAIST = Largo de la cintura
skeleton_bone-HIP = Largo de la cadera
skeleton_bone-HIP_OFFSET = Desplazamiento de la cadera
skeleton_bone-HIPS_WIDTH = Ancho de la cadera
skeleton_bone-leg_group = Largo de la pierna
skeleton_bone-UPPER_LEG = Largo del muslo
skeleton_bone-LOWER_LEG = Largo de la tibia
skeleton_bone-FOOT_LENGTH = Largo del pie
skeleton_bone-FOOT_SHIFT = Desplazamiento de pies
skeleton_bone-SKELETON_OFFSET = Desplazamiento del esqueleto
skeleton_bone-SHOULDERS_DISTANCE = Distancia de los hombros
skeleton_bone-SHOULDERS_WIDTH = Ancho de los hombros
skeleton_bone-arm_group = Largo del brazo
skeleton_bone-UPPER_ARM = Largo del brazo superior
skeleton_bone-LOWER_ARM = Distancia del antebrazo
skeleton_bone-HAND_Y = Distancia Y de la mano
skeleton_bone-HAND_Z = Distancia Z de la mano
skeleton_bone-ELBOW_OFFSET = Desplazamiento del codo

## Tracker reset buttons

reset-reset_all = Reiniciar todas las proporciones
reset-reset_all_warning =
    <b>Advertencia:</b> Esto reiniciará tus proporciones para ser basadas solamente en tu altura.
    ¿Estás seguro de que quieres seguir?
reset-reset_all_warning-reset = Reiniciar proporciones
reset-reset_all_warning-cancel = Cancelar
reset-full = Reinicio completo
reset-mounting = Reinicio de montura
reset-yaw = Reinicio horizontal

## Serial detection stuff

serial_detection-new_device-p0 = ¡Nuevo dispositivo serial detectado!
serial_detection-new_device-p1 = ¡Ingresa tus credenciales del Wi-Fi!
serial_detection-new_device-p2 = Por favor selecciona que quieres hacer con el
serial_detection-open_wifi = Conectarse al Wi-Fi
serial_detection-open_serial = Abrir consola serial
serial_detection-submit = ¡Enviar!
serial_detection-close = Cerrar

## Navigation bar

navbar-home = Inicio
navbar-body_proportions = Proporciones corporales
navbar-trackers_assign = Asignación de sensores
navbar-mounting = Calibración de montura
navbar-onboarding = Asistente de configuración
navbar-settings = Ajustes

## Biovision hierarchy recording

bvh-start_recording = Grabar BVH
bvh-recording = Grabando...

## Tracking pause

tracking-unpaused = Pausar el tracking
tracking-paused = Reanudar el tracking

## Widget: Overlay settings

widget-overlay = Overlay
widget-overlay-is_visible_label = Mostrar interfaz en SteamVR
widget-overlay-is_mirrored_label = Mostrar interfaz reflejada

## Widget: Drift compensation

widget-drift_compensation-clear = Olvidar compensación de drift

## Widget: Clear Reset Mounting

widget-clear_mounting = Olvidar reinicio de montura

## Widget: Developer settings

widget-developer_mode = Developer Mode
widget-developer_mode-high_contrast = High contrast
widget-developer_mode-precise_rotation = Precise rotation
widget-developer_mode-fast_data_feed = Fast data feed
widget-developer_mode-filter_slimes_and_hmd = Filter slimes and HMD
widget-developer_mode-sort_by_name = Sort by name
widget-developer_mode-raw_slime_rotation = Raw rotation
widget-developer_mode-more_info = More info

## Widget: IMU Visualizer

widget-imu_visualizer = Rotation
widget-imu_visualizer-preview = Vista previa
widget-imu_visualizer-hide = Ocultar
widget-imu_visualizer-rotation_raw = Raw
widget-imu_visualizer-rotation_preview = Preview
widget-imu_visualizer-acceleration = Aceleración
widget-imu_visualizer-position = Posición

## Widget: Skeleton Visualizer

widget-skeleton_visualizer-preview = Previsualización del esqueleto
widget-skeleton_visualizer-hide = Ocultar

## Tracker status

tracker-status-none = Sin estado
tracker-status-busy = Ocupado
tracker-status-error = Error
tracker-status-disconnected = Desconectado
tracker-status-occluded = Ocluido
tracker-status-ok = Conectado
tracker-status-timed_out = Conexión interrumpida

## Tracker status columns

tracker-table-column-name = Nombre
tracker-table-column-type = Tipo
tracker-table-column-battery = Batería
tracker-table-column-ping = Latencia
tracker-table-column-tps = TPS
tracker-table-column-temperature = Temp. °C
tracker-table-column-linear-acceleration = Accel. X/Y/Z
tracker-table-column-rotation = Rotación X/Y/Z
tracker-table-column-position = Posición X/Y/Z
tracker-table-column-url = URL

## Tracker rotation

tracker-rotation-front = Frente
tracker-rotation-front_left = Frente izquierdo
tracker-rotation-front_right = Frente derecho
tracker-rotation-left = Izquierda
tracker-rotation-right = Derecha
tracker-rotation-back = Atrás
tracker-rotation-back_left = Atrás izquierdo
tracker-rotation-back_right = Atrás derecho
tracker-rotation-custom = Personalizado
tracker-rotation-overriden = (siendo invalidado por el reinicio de montura)

## Tracker information

tracker-infos-manufacturer = Fabricante
tracker-infos-display_name = Nombre
tracker-infos-custom_name = Nombre personalizado
tracker-infos-url = URL del sensor
tracker-infos-version = Versión del firmware
tracker-infos-hardware_rev = Revisión del hardware
tracker-infos-hardware_identifier = ID del hardware
tracker-infos-imu = Sensor IMU
tracker-infos-board_type = Placa principal
tracker-infos-network_version = Versión del protocolo
tracker-infos-magnetometer = Magnetómetro
tracker-infos-magnetometer-status-v1 =
    { $status ->
        [DISABLED] Deshabilitado
        [ENABLED] Habilitado
       *[NOT_SUPPORTED] No soportado
    }

## Tracker settings

tracker-settings-back = Volver a la lista de sensores
tracker-settings-title = Ajustes de los sensores
tracker-settings-assignment_section = Asignación
tracker-settings-assignment_section-description = Parte del cuerpo asignado al sensor.
tracker-settings-assignment_section-edit = Editar asignación
tracker-settings-mounting_section = Posición de montura
tracker-settings-mounting_section-description = ¿Dónde está montado el sensor?
tracker-settings-mounting_section-edit = Editar montura
tracker-settings-drift_compensation_section = Permitir compensación de desviación
tracker-settings-drift_compensation_section-description = ¿Este sensor deberia compensar la desviación?
tracker-settings-drift_compensation_section-edit = Permitir compensación de desviación
tracker-settings-use_mag = Permitir el uso del magnetómetro en este tracker
# Multiline!
tracker-settings-use_mag-description =
    ¿Debería este tracker usar el magnetómetro para reducir la desviacion cuando se permite el uso del magnetómetro? <b>¡Por favor, no apagues tu tracker mientras alternas esto!</b>
    
    Primero debes permitir el uso del magnetómetro, <magSetting>haga clic aquí para ir al ajuste</magSetting>.
tracker-settings-use_mag-label = Permitir el uso del magnetómetro
# The .<name> means it's an attribute and it's related to the top key.
# In this case that is the settings for the assignment section.
tracker-settings-name_section = Nombre del sensor
tracker-settings-name_section-description = Dale un apodo bonito :)
tracker-settings-name_section-placeholder = Pata izquierda de Manteca
tracker-settings-name_section-label = Nombre del sensor
tracker-settings-forget = Olvidar tracker
tracker-settings-forget-description = Remueve el tracker del servidor de SlimeVR y lo previene de conectarse hasta que el servidor se reinicie. La configuración del tracker no se perderá.
tracker-settings-forget-label = Olvidar tracker

## Tracker part card info

tracker-part_card-no_name = Sin nombre
tracker-part_card-unassigned = Sin asignar

## Body assignment menu

body_assignment_menu = ¿Dónde quieres que esté este sensor?
body_assignment_menu-description = Selecciona la posición donde quieres asignar este sensor. También puedes escoger administrar todos los sensores al mismo tiempo en vez de uno por uno.
body_assignment_menu-show_advanced_locations = Mostrar posiciones de asignación avanzadas
body_assignment_menu-manage_trackers = Administrar todos los sensores
body_assignment_menu-unassign_tracker = Desasignar sensor

## Tracker assignment menu

# A -translation_key (with a dash in the front) means that it's a label.
# It can only be used in the translation file, it's nice for reusing names and that kind of stuff.
#
# We are using it here because english doesn't require changing the text in each case but
# maybe your language does.
-tracker_selection-part = ¿Qué sensor asignar a tu
tracker_selection_menu-NONE = ¿Qué sensor quieres que esté sin asignar?
tracker_selection_menu-HEAD = { -tracker_selection-part } cabeza?
tracker_selection_menu-NECK = { -tracker_selection-part } cuello?
tracker_selection_menu-RIGHT_SHOULDER = { -tracker_selection-part } hombro derecho?
tracker_selection_menu-RIGHT_UPPER_ARM = { -tracker_selection-part } brazo superior derecho?
tracker_selection_menu-RIGHT_LOWER_ARM = { -tracker_selection-part } antebrazo derecho?
tracker_selection_menu-RIGHT_HAND = { -tracker_selection-part } mano derecha?
tracker_selection_menu-RIGHT_UPPER_LEG = { -tracker_selection-part } muslo derecho?
tracker_selection_menu-RIGHT_LOWER_LEG = { -tracker_selection-part } tobillo derecho?
tracker_selection_menu-RIGHT_FOOT = { -tracker_selection-part } pie derecho?
tracker_selection_menu-RIGHT_CONTROLLER = { -tracker_selection-part } mando derecho?
tracker_selection_menu-UPPER_CHEST = { -tracker_selection-part } pecho superior?
tracker_selection_menu-CHEST = { -tracker_selection-part } pecho?
tracker_selection_menu-WAIST = { -tracker_selection-part } cintura?
tracker_selection_menu-HIP = { -tracker_selection-part } cadera?
tracker_selection_menu-LEFT_SHOULDER = { -tracker_selection-part } hombro izquierdo?
tracker_selection_menu-LEFT_UPPER_ARM = { -tracker_selection-part } brazo superior izquierdo?
tracker_selection_menu-LEFT_LOWER_ARM = { -tracker_selection-part } antebrazo izquierdo?
tracker_selection_menu-LEFT_HAND = { -tracker_selection-part } mano izquierda?
tracker_selection_menu-LEFT_UPPER_LEG = { -tracker_selection-part } muslo izquierdo?
tracker_selection_menu-LEFT_LOWER_LEG = { -tracker_selection-part } tobillo izquiero?
tracker_selection_menu-LEFT_FOOT = { -tracker_selection-part } pie izquierdo?
tracker_selection_menu-LEFT_CONTROLLER = { -tracker_selection-part } mando izquierdo?
tracker_selection_menu-unassigned = Sensores sin asignar
tracker_selection_menu-assigned = Sensores asignados
tracker_selection_menu-dont_assign = No asignar
# This line cares about multilines.
# <b>text</b> means that the text should be bold.
tracker_selection_menu-neck_warning =
    <b>Advertencia:</b> Un sensor de cuello puede ser mortal si se ajusta demasiado,
    ¡La correa podría cortar la circulación a tu cabeza!
tracker_selection_menu-neck_warning-done = Entiendo los riesgos
tracker_selection_menu-neck_warning-cancel = Cancelar

## Mounting menu

mounting_selection_menu = ¿Dónde quieres colocar el sensor?
mounting_selection_menu-close = Cerrar

## Sidebar settings

settings-sidebar-title = Ajustes
settings-sidebar-general = General
settings-sidebar-tracker_mechanics = Mecánicas del sensor
settings-sidebar-fk_settings = Ajustes de FK
settings-sidebar-gesture_control = Control de gestos
settings-sidebar-interface = Interfaz
settings-sidebar-osc_router = Router OSC
settings-sidebar-osc_trackers = Sensores OSC de VRChat
settings-sidebar-utils = Utilidades
settings-sidebar-serial = Consola serial
settings-sidebar-appearance = Apariencia
settings-sidebar-notifications = Notificaciones
settings-sidebar-advanced = Avanzado

## SteamVR settings

settings-general-steamvr = SteamVR
settings-general-steamvr-subtitle = Sensores en SteamVR
# Not all translation keys support multiline, only the ones that specify it will actually
# split it in lines (that also means you can split in lines however you want in those).
# The first spaces (not tabs) for indentation will be ignored, just to make the file look nice when writing.
# This one is one of this cases that cares about multilines
settings-general-steamvr-description =
    Habilita o deshabilita sensores de SteamVR en específico.
    Útil para juegos y aplicaciones que solo soportan ciertos sensores..
settings-general-steamvr-trackers-waist = Cintura
settings-general-steamvr-trackers-chest = Pecho
settings-general-steamvr-trackers-left_foot = Pie izquierdo
settings-general-steamvr-trackers-right_foot = Pie derecho
settings-general-steamvr-trackers-left_knee = Rodilla izquierda
settings-general-steamvr-trackers-right_knee = Rodilla derecha
settings-general-steamvr-trackers-left_elbow = Codo izquierdo
settings-general-steamvr-trackers-right_elbow = Codo derecho
settings-general-steamvr-trackers-left_hand = Mano izquierda
settings-general-steamvr-trackers-right_hand = Mano derecha
settings-general-steamvr-trackers-tracker_toggling = Asignación automatica de trackers
settings-general-steamvr-trackers-tracker_toggling-description = Automáticamente se encarga de prender o apagar los trackers de SteamVR dependiendo de los trackers asignados a tu cuerpo
settings-general-steamvr-trackers-tracker_toggling-label = Asignación automatica de trackers
settings-general-steamvr-trackers-hands-warning =
    <b>Advertencia:</b> los trackers de mano reemplazaran los controles del VR.
    ¿Estás seguro?
settings-general-steamvr-trackers-hands-warning-cancel = Cancelar
settings-general-steamvr-trackers-hands-warning-done = Sí

## Tracker mechanics

settings-general-tracker_mechanics = Mecánicas del sensor
settings-general-tracker_mechanics-filtering = Filtrado
# This also cares about multilines
settings-general-tracker_mechanics-filtering-description =
    Selecciona un tipo de filtro para tus sensores.
    Predicción predice el movimiento mientras que el suavizado suaviza el movimiento.
settings-general-tracker_mechanics-filtering-type = Tipos de filtro
settings-general-tracker_mechanics-filtering-type-none = Sin filtro
settings-general-tracker_mechanics-filtering-type-none-description = Usa las rotaciones como son. No se realizará ningún filtrado.
settings-general-tracker_mechanics-filtering-type-smoothing = Suavizado
settings-general-tracker_mechanics-filtering-type-smoothing-description = Suaviza los movimientos pero añade algo de latencia.
settings-general-tracker_mechanics-filtering-type-prediction = Predicción
settings-general-tracker_mechanics-filtering-type-prediction-description = Reduce la latencia y los movimientos serán más inmediatos, pero puede incrementar la inestabilidad.
settings-general-tracker_mechanics-filtering-amount = Cantidad
settings-general-tracker_mechanics-yaw-reset-smooth-time = Tiempo de interpolación para el reinicio horizontal (0s desactiva el suavizado)
settings-general-tracker_mechanics-drift_compensation = Compensación de desviación
# This cares about multilines
settings-general-tracker_mechanics-drift_compensation-description =
    Compensa la desviación del eje vertical de los sensores aplicando una rotación inversa.
    Cambia la fuerza de la compensación y hasta cuantos reinicios tomar en cuenta.
settings-general-tracker_mechanics-drift_compensation-enabled-label = Compensación de desviación
settings-general-tracker_mechanics-drift_compensation-prediction = Compensación mediante la predicción del desvío
# This cares about multilines
settings-general-tracker_mechanics-drift_compensation-prediction-description =
    Predice la desviación horizontal y compensa cuando esta mas allá del rango previamente medido.
    Activa esto si el sensor esta girando continuamente en el eje horizontal.
settings-general-tracker_mechanics-drift_compensation-prediction-label = Compensación mediante la predicción del desvío
settings-general-tracker_mechanics-drift_compensation_warning =
    <b>Advertencia:</b> Solo usa la compensación de desviación si necesitas reiniciar
    muy seguido (cada ~5-10 minutos).
    
    Algunos IMUs propensos a reinicios frecuentes incluyen:
    Joy-Cons, owoTrack y MPU (sin un firmware reciente).
settings-general-tracker_mechanics-drift_compensation_warning-cancel = Cancelar
settings-general-tracker_mechanics-drift_compensation_warning-done = Entiendo
settings-general-tracker_mechanics-drift_compensation-amount-label = Fuerza de la compensación
settings-general-tracker_mechanics-drift_compensation-max_resets-label = Utilizar hasta los últimos x reinicios
settings-general-tracker_mechanics-save_mounting_reset = Guardar calibración automática de reinicio de montura
settings-general-tracker_mechanics-save_mounting_reset-description =
    Guarda las calibraciones automáticas del reinicio de montura para los trackers entre reinicios. Útil
    para cuando se usa un traje donde los trackers no se mueven entre sesiones. <b>¡No se recomienda para usuarios típicos!</b>
settings-general-tracker_mechanics-save_mounting_reset-enabled-label = Guardar reinicio de montura
settings-general-tracker_mechanics-use_mag_on_all_trackers = Usar el magnetómetro en todos los trackers IMU que lo admitan
settings-general-tracker_mechanics-use_mag_on_all_trackers-description =
    Usa el magnetómetro en todos los trackers que tienen un firmware compatible, lo que reduce la desviación en entornos magnéticos estables.
    Se puede desactivar por sensor en la configuración del sensor. <b>¡Por favor, no apagues ninguno de los trackers mientras activas esta opción!</b>
settings-general-tracker_mechanics-use_mag_on_all_trackers-label = Usar magnetómetro en trackers

## FK/Tracking settings

settings-general-fk_settings = Ajustes de sensores
# Floor clip:
# why the name - came from the idea of noclip in video games, but is the opposite where clipping to the floor is a desired feature
# definition - Prevents the foot trackers from going lower than they where when a reset was performed
settings-general-fk_settings-leg_tweak-floor_clip = Clip del suelo
# Skating correction:
# why the name - without this enabled the feet will often slide across the ground as if your skating across the ground,
# since this largely prevents this it corrects for it hence skating correction (note this may be renamed to sliding correction)
# definition - Guesses when each foot is in contact with the ground and uses that information to improve tracking
settings-general-fk_settings-leg_tweak-skating_correction = Corrección del patinaje
settings-general-fk_settings-leg_tweak-toe_snap = Encajado de dedos
settings-general-fk_settings-leg_tweak-foot_plant = Plantado del pie
settings-general-fk_settings-leg_tweak-skating_correction-amount = Agresividad de la corrección del patinaje
settings-general-fk_settings-leg_tweak-skating_correction-description = Corrección del patinaje corrige el patinaje en hielo que sucede en los pies, pero puede disminuir la precisión de ciertos patrones de movimientos. Al activar esto, asegúrate de realizar un reinicio completo y recalibrar en el juego.
settings-general-fk_settings-leg_tweak-floor_clip-description = El clip del suelo puede reducir o incluso evitar que tus pies atraviesen el suelo. Al activar esto, asegúrate de realizar un reinicio completo y recalibrar en el juego.
settings-general-fk_settings-leg_tweak-toe_snap-description = El encajado de dedos intenta adivinar la rotación de los pies si sus respectivos trackers no están en uso.
settings-general-fk_settings-leg_tweak-foot_plant-description = El plantado del pie rota los pies para que sean paralelos con el suelo al entrar en contacto.
settings-general-fk_settings-leg_fk = Tracking de piernas
settings-general-fk_settings-leg_fk-reset_mounting_feet-description = Activar reinicio de montura para el pie mediante el pararse de puntillas.
settings-general-fk_settings-leg_fk-reset_mounting_feet = Reinicio de montura de pies
settings-general-fk_settings-arm_fk = Trackeo de brazos
settings-general-fk_settings-arm_fk-description = Cambia cómo el movimiento de los brazos es detectado.
settings-general-fk_settings-arm_fk-force_arms = Forzar brazos desde el HMD
settings-general-fk_settings-reset_settings = Reiniciar ajustes
settings-general-fk_settings-reset_settings-reset_hmd_pitch-description = Restablece la inclinación del HMD (rotación vertical) al realizar un reinicio completo. Útil si se lleva un HMD en la frente para VTubing o mocap. No habilitar para VR.
settings-general-fk_settings-reset_settings-reset_hmd_pitch = Reiniciar la inclinación del HMD
settings-general-fk_settings-arm_fk-reset_mode-description = Cambiar que pose de brazos es esperada para el reinicio de montura.
settings-general-fk_settings-arm_fk-back = Detrás
settings-general-fk_settings-arm_fk-back-description = El modo predeterminado, con el brazo yendo por detrás y el antebrazo yendo para adelante.
settings-general-fk_settings-arm_fk-tpose_up = T-pose (arriba)
settings-general-fk_settings-arm_fk-tpose_up-description = Espera que tus brazos estén abajo hacia los lados durante un reinicio completo, y 90 grados hacia los lados durante un reinicio de montura.
settings-general-fk_settings-arm_fk-tpose_down = T-pose (abajo)
settings-general-fk_settings-arm_fk-tpose_down-description = Espera que tus brazos estén 90 grados arriba hacia los lados durante un reinicio completo, y abajo hacia los lados durante un reinicio de montura.
settings-general-fk_settings-arm_fk-forward = Delante
settings-general-fk_settings-arm_fk-forward-description = Espera que tus brazos estén 90 grados para delante. Útil para VTubing.
settings-general-fk_settings-skeleton_settings-toggles = Interruptores del esqueleto
settings-general-fk_settings-skeleton_settings-description = Habilita o deshabilita los ajustes de esqueleto. Es recomendado dejar estos ajustes habilitados.
settings-general-fk_settings-skeleton_settings-extended_spine_model = Modelo extendido de la columna
settings-general-fk_settings-skeleton_settings-extended_pelvis_model = Modelo extendido del pelvis
settings-general-fk_settings-skeleton_settings-extended_knees_model = Modelo extendido de la rodilla
settings-general-fk_settings-skeleton_settings-ratios = Radios del esqueleto
settings-general-fk_settings-skeleton_settings-ratios-description = Cambia los valores de los ajustes del esqueleto. Podes llegar a necesitar reajustar tus proporciones después de cambiar estos valores.
settings-general-fk_settings-skeleton_settings-impute_waist_from_chest_hip = Imputar de la cintura al pecho hasta la cadera
settings-general-fk_settings-skeleton_settings-impute_waist_from_chest_legs = Imputar de la cintura al pecho hasta las piernas
settings-general-fk_settings-skeleton_settings-impute_hip_from_chest_legs = Imputar de la cadera al pecho hasta las piernas
settings-general-fk_settings-skeleton_settings-impute_hip_from_waist_legs = Imputar de la cadera a la cintura hasta las piernas
settings-general-fk_settings-skeleton_settings-interp_hip_legs = Promediar la rotación del eje vertical y horizontal de la cadera con la de las piernas
settings-general-fk_settings-skeleton_settings-interp_knee_tracker_ankle = Promediar la rotación del eje vertical y horizontal de los trackers de las rodillas con la de los tobillos
settings-general-fk_settings-skeleton_settings-interp_knee_ankle = Promediar la rotación del eje vertical y horizontal de las rodillas con la de los tobillos
settings-general-fk_settings-self_localization-title = Modo mocap
settings-general-fk_settings-self_localization-description = El modo mocap permite al esqueleto rastrear de forma aproximada su propia posición sin un casco o otros sensores. Nota que esto requiere que los trackers en el pie y cabeza estén y sigue siendo experimental.
settings-general-fk_settings-vive_emulation-title = Vive emulation
settings-general-fk_settings-vive_emulation-description = Emulate the waist tracker problems that Vive trackers have. This is a joke and makes tracking worse.
settings-general-fk_settings-vive_emulation-label = Habilitar emulación de Vive

## Gesture control settings (tracker tapping)

settings-general-gesture_control = Control de gestos
settings-general-gesture_control-subtitle = Reinicio basado en toques
settings-general-gesture_control-description = Permite la ejecución de un reinicio al tocar un sensor. El sensor más alto en el torso es utilizado para el reinicio rápido, el sensor más alto en tu pierna izquierda es utilizado para el reinicio, y el sensor más alto en tu pierna derecha es utilizado para reiniciar la montura. Cabe destacar que los toques deben suceder dentro de 0.6 segundos para ser registrados.
# This is a unit: 3 taps, 2 taps, 1 tap
# $amount (Number) - Amount of taps (touches to the tracker's case)
settings-general-gesture_control-taps =
    { $amount ->
        [one] 1 toque
       *[other] { $amount } toques
    }
# This is a unit: 3 trackers, 2 trackers, 1 tracker
# $amount (Number) - Amount of trackers
settings-general-gesture_control-trackers =
    { $amount ->
        [one] 1 tracker
       *[other] { $amount } trackers
    }
settings-general-gesture_control-yawResetEnabled = Activar toques para reinicio horizontal
settings-general-gesture_control-yawResetDelay = Retraso del reinicio horizontal
settings-general-gesture_control-yawResetTaps = Toques para reinicio horizontal
settings-general-gesture_control-fullResetEnabled = Activar toques para reinicio completo
settings-general-gesture_control-fullResetDelay = Retraso del reinicio completo
settings-general-gesture_control-fullResetTaps = Toques para reinicio completo
settings-general-gesture_control-mountingResetEnabled = Activar toques para reinicio de montura
settings-general-gesture_control-mountingResetDelay = Retraso del reinicio de montura
settings-general-gesture_control-mountingResetTaps = Toques para reinicio de montura
# The number of trackers that can have higher acceleration before a tap is rejected
settings-general-gesture_control-numberTrackersOverThreshold = Trackers sobre el límite
settings-general-gesture_control-numberTrackersOverThreshold-description = Aumente este valor si la detección de toques no funciona. No lo aumente mas de lo necesario para que funciona ya que puede causar que la detección tenga más falsas positivas.

## Appearance settings

settings-interface-appearance = Apariencia
settings-general-interface-dev_mode = Modo desarrollador
settings-general-interface-dev_mode-description = Este modo puede ser útil si es que necesitas información a fondo o para un nivel de interacción más avanzado con los sensores conectados.
settings-general-interface-dev_mode-label = Modo desarrollador
settings-general-interface-theme = Tema de color
settings-general-interface-show-navbar-onboarding = Mostrar «{ navbar-onboarding }» en la barra de navegación
settings-general-interface-show-navbar-onboarding-description = Esto cambia si el botón "{ navbar-onboarding }" se muestra en la barra de navegación.
settings-general-interface-show-navbar-onboarding-label = Mostrar «{ navbar-onboarding }»
settings-general-interface-lang = Selecciona un idioma
settings-general-interface-lang-description = Cambia el idioma que quieras usar.
settings-general-interface-lang-placeholder = Selecciona el idioma a utilizar
# Keep the font name untranslated
settings-interface-appearance-font = Fuente de la interfaz
settings-interface-appearance-font-description = Esto cambia el estilo de letra utilizado por la interfaz.
settings-interface-appearance-font-placeholder = Fuente predeterminada
settings-interface-appearance-font-os_font = Fuente del sistema operativo
settings-interface-appearance-font-slime_font = Fuente predeterminada
settings-interface-appearance-font_size = Tamaño base de la fuente
settings-interface-appearance-font_size-description = Esto afecta al tamaño de las letras en toda la interfaz excepto en este panel de ajustes.
settings-interface-appearance-decorations = Usar las decoraciones nativas del sistema
settings-interface-appearance-decorations-description = Esto no renderizará la barra superior de la interfaz y en cambio usará la del sistema operativo.
settings-interface-appearance-decorations-label = Usar decoraciones nativas

## Notification settings

settings-interface-notifications = Notificaciones
settings-general-interface-serial_detection = Detección de dispositivo serial
settings-general-interface-serial_detection-description = Esta opción mostrará un notificación cada vez que conectes un nuevo dispositivo serial que pueda ser un sensor. Ayuda a mejorar el proceso de configuración de un sensor.
settings-general-interface-serial_detection-label = Detección de dispositivo serial
settings-general-interface-feedback_sound = Sonido de feedback
settings-general-interface-feedback_sound-description = Esta opción reproducirá un sonido cuando se realice un reinicio.
settings-general-interface-feedback_sound-label = Sonido de feedback
settings-general-interface-feedback_sound-volume = Volumen del sonido de feedback
settings-general-interface-connected_trackers_warning = Advertencia de trackers conectados
settings-general-interface-connected_trackers_warning-description = Esta opción hará que aparezca un pop-up cada vez que intentas salir de SlimeVR mientras tienes uno o más trackers conectados. Te recuerda de apagar tus trackers cuando ya paraste de usarlos así preservas la duración de la batería.
settings-general-interface-connected_trackers_warning-label = Advertencia de trackers conectados al salir
settings-general-interface-use_tray = Minimizar a la bandeja del sistema
settings-general-interface-use_tray-description = Permite cerrar la ventana sin cerrar el servidor de SlimeVR para que puedas continuar usándolo sin que te moleste la interfaz.
settings-general-interface-use_tray-label = Minimizar a la bandeja del sistema
settings-general-interface-discord_presence = Compartir actividad en Discord
settings-general-interface-discord_presence-description = Le dice a tu cliente de Discord que estás usando SlimeVR junto con la cantidad de sensores IMU que estás usando.
settings-general-interface-discord_presence-label = Compartir actividad en Discord
settings-general-interface-discord_presence-message =
    { $amount ->
        [0] Recolectando slimes
        [one] Usando 1 sensor
        [many] Usando { $amount } de sensores
       *[other] Usando { $amount } sensores
    }

## Serial settings

settings-serial = Consola serial
# This cares about multilines
settings-serial-description =
    Esta es la comunicación serial actualizada en vivo.
    Puede ser util para saber si el firmware tiene problemas.
settings-serial-connection_lost = Conexión serial perdida, reconectando...
settings-serial-reboot = Reinciar
settings-serial-factory_reset = Restauración de fábrica
# This cares about multilines
# <b>text</b> means that the text should be bold
settings-serial-factory_reset-warning =
    <b>Advertencia:</b> Esto reiniciará el sensor a sus ajustes de fábrica.
    ¡Esto significa que los ajustes de calibración y Wi-Fi <b>se perderán</b>!
settings-serial-factory_reset-warning-ok = Sé lo que estoy haciendo
settings-serial-factory_reset-warning-cancel = Cancelar
settings-serial-get_infos = Obtener información
settings-serial-serial_select = Selecciona un puerto serial
settings-serial-auto_dropdown_item = Auto
settings-serial-get_wifi_scan = Obtener escaneo WiFi
settings-serial-file_type = Texto sin formato
settings-serial-save_logs = Guardar en archivo

## OSC router settings

settings-osc-router = Router OSC
# This cares about multilines
settings-osc-router-description =
    Redirecciona mensajes OSC recibidos de otro programa.
    Útil para usar otro programa OSC con VRChat por ejemplo.
settings-osc-router-enable = Habilitar
settings-osc-router-enable-description = Habilita el reenvío de mensajes.
settings-osc-router-enable-label = Habilitar
settings-osc-router-network = Puertos de conexión
# This cares about multilines
settings-osc-router-network-description =
    Establece los puertos de entrada y salida de datos
    Estos pueden ser lo mismos puertos usados en el servidor de SlimeVR.
settings-osc-router-network-port_in =
    .label = Puerto de entrada
    .placeholder = Puerto de entrada (por defecto: 9002)
settings-osc-router-network-port_out =
    .label = Puerto de salida
    .placeholder = Puerto de salida (por defecto: 9000)
settings-osc-router-network-address = Dirección de red
settings-osc-router-network-address-description = Establece la direción a la cuál se enviarán los datos.
settings-osc-router-network-address-placeholder = Dirección IPv4

## OSC VRChat settings

settings-osc-vrchat = Sensores OSC de VRChat
# This cares about multilines
settings-osc-vrchat-description-v1 =
    Cambia los ajustes específicos de los trackers OSC utilizados para enviar
    datos de seguimiento a aplicaciones sin SteamVR (ej. VRChat en Quest).
    Asegúrate de activar OSC en VRChat a través del menú Acción en OSC > Activado.
    Para permitir la recepción de datos del HMD y de los mandos desde VRChat, ve a los ajustes de tu menú principal 
    en Tracking & IK > Permitir el envío de datos OSC de seguimiento de RV de cabeza y muñeca.
settings-osc-vrchat-enable = Habilitar
settings-osc-vrchat-enable-description = Habilita el envio y recibo de datos.
settings-osc-vrchat-enable-label = Habilitar
settings-osc-vrchat-network = Puertos de conexión
settings-osc-vrchat-network-description-v1 = Establece los puertos para recibir y enviar datos. Se puede dejar sin cambiar para VRChat.
settings-osc-vrchat-network-port_in =
    .label = Puerto de entrada
    .placeholder = Puerto de entrada (por defecto: 9001)
settings-osc-vrchat-network-port_out =
    .label = Puerto de salida
    .placeholder = Puerto de salida (por defecto: 9000)
settings-osc-vrchat-network-address = Dirección de red
settings-osc-vrchat-network-address-description-v1 = Elige a qué dirección enviar los datos. Se puede dejar sin cambiar para VRChat.
settings-osc-vrchat-network-address-placeholder = Dirección IP de VRChat
settings-osc-vrchat-network-trackers = Sensores
settings-osc-vrchat-network-trackers-description = Habilita el envío de sensores específicos mediante OSC.
settings-osc-vrchat-network-trackers-chest = Pecho
settings-osc-vrchat-network-trackers-hip = Cadera
settings-osc-vrchat-network-trackers-knees = Rodillas
settings-osc-vrchat-network-trackers-feet = Pies
settings-osc-vrchat-network-trackers-elbows = Codos

## VMC OSC settings

settings-osc-vmc = Virtual Motion Capture
# This cares about multilines
settings-osc-vmc-description =
    Cambia la configuracion especifica al protocolo VMC (Virtual Motion Capture)
      para enviar datos de los huesos de SlimeVR y recibir data de los huesos de otras aplicaciones.
settings-osc-vmc-enable = Habilitar
settings-osc-vmc-enable-description = Habilita el envio y recibo de datos.
settings-osc-vmc-enable-label = Habilitar
settings-osc-vmc-network = Puertos de conexión
settings-osc-vmc-network-description = Establece los puertos de entrada y salida de datos por medio de VMC.
settings-osc-vmc-network-port_in =
    .label = Puerto de entrada
    .placeholder = Puerto de entrada (por defecto: 39540)
settings-osc-vmc-network-port_out =
    .label = Puerto de salida
    .placeholder = Puerto de salida (por defecto: 39539)
settings-osc-vmc-network-address = Dirección de red
settings-osc-vmc-network-address-description = Establece la dirección donde se enviarán los datos por VMC
settings-osc-vmc-network-address-placeholder = Dirección IPv4
settings-osc-vmc-vrm = Modelo VRM
settings-osc-vmc-vrm-description = Carga un modelo VRM para permitir el anclaje de cabeza y habilitar una mejor compatibilidad con otras aplicaciones
settings-osc-vmc-vrm-untitled_model = Modelo sin nombre
settings-osc-vmc-vrm-file_select = Arrastra y suelta un modelo para usar, o <u>selecciona uno</u>.
settings-osc-vmc-anchor_hip = Anclaje por cadera
settings-osc-vmc-anchor_hip-description = Anclar el tracking a la cadera, útil para hacer de VTuber sentado. Si lo desactivas, carga un modelo VRM.
settings-osc-vmc-anchor_hip-label = Anclaje por cadera
settings-osc-vmc-mirror_tracking = Invertir el tracking
settings-osc-vmc-mirror_tracking-description = invierte el tracking horizontalmente.
settings-osc-vmc-mirror_tracking-label = Invertir el tracking

## Advanced settings

settings-utils-advanced = Avanzado
settings-utils-advanced-reset-gui = Reiniciar ajustes de la interfaz de usuario
settings-utils-advanced-reset-gui-description = Restaura los ajustes por defecto de la interfaz.
settings-utils-advanced-reset-gui-label = Reiniciar interfaz de usuario
settings-utils-advanced-reset-server = Reiniciar los ajustes del tracking
settings-utils-advanced-reset-server-description = Restaura los ajustes por defecto para el tracking.
settings-utils-advanced-reset-server-label = Reiniciar tracking
settings-utils-advanced-reset-all = Reiniciar todos los ajustes
settings-utils-advanced-reset-all-description = Restaura los ajustes por defecto para la interfaz y el tracking.
settings-utils-advanced-reset-all-label = Reiniciar todo
settings-utils-advanced-reset_warning =
    { $type ->
        [gui]
            <b>Advertencia:</b> Esto reiniciará tus ajustes de la interfaz de usuario a sus valores predeterminados.
            ¿Estás seguro de que quieres seguir?
        [server]
            <b>Advertencia:</b> Esto reiniciará tus ajustes de seguimiento a sus valores predeterminados.
            ¿Estás seguro de que quieres seguir?
       *[all]
            <b>Advertencia:</b> Esto reiniciará todos tus ajustes a sus valores predeterminados.
            ¿Estás seguro de que quieres seguir?
    }
settings-utils-advanced-reset_warning-reset = Reiniciar ajustes
settings-utils-advanced-reset_warning-cancel = Cancelar
settings-utils-advanced-open_data = Carpeta de datos
settings-utils-advanced-open_data-description = Abre la carpeta de datos de SlimeVR en el explorador de archivos, conteniendo archivos de configuración y registros.
settings-utils-advanced-open_data-label = Abrir carpeta

## Setup/onboarding menu

onboarding-skip = Saltar configuración
onboarding-continue = Continuar
onboarding-wip = Trabajo en progreso
onboarding-previous_step = Paso anterior
onboarding-setup_warning =
    <b>Advertencia:</b> La configuración se necesita hacer para tener buen tracking,
    y es requerido si es tu primera vez usando SlimeVR.
onboarding-setup_warning-skip = Saltar configuración
onboarding-setup_warning-cancel = Continuar configuración

## Wi-Fi setup

onboarding-wifi_creds-back = Volver a la introducción
onboarding-wifi_creds = Ingresar credenciales del Wi-Fi
# This cares about multilines
onboarding-wifi_creds-description =
    Los sensores utilizarán estas credenciales para conectarse inalámbricamente.
    Por favor usa las credenciales del Wi-Fi al cuál estás conectado actualmente.
onboarding-wifi_creds-skip = Saltar ajustes de Wi-Fi
onboarding-wifi_creds-submit = ¡Enviar!
onboarding-wifi_creds-ssid =
    .label = Nombre del WiFi
    .placeholder = Ingresa el nombre del WiFi
onboarding-wifi_creds-password =
    .label = Contraseña
    .placeholder = Ingresa la contraseña

## Mounting setup

onboarding-reset_tutorial-back = Volver a la calibración de montura
onboarding-reset_tutorial = Reiniciar tutorial
onboarding-reset_tutorial-explanation = Mientras estés usando tus trackers, estos pueden empezar a desalinearse por el drift horizontal del IMU, o porque los moviste físicamente. Hay varias formas de arreglar este tipo de problemas.
onboarding-reset_tutorial-skip = Saltar paso
# Cares about multiline
onboarding-reset_tutorial-0 =
    Toca { $taps } veces el tracker resaltado para activar el reinicio horizontal.
    
    Esto va a hacer que tus sensores miren para la misma dirección que tu HMD.
# Cares about multiline
onboarding-reset_tutorial-1 =
    Toca { $taps } veces el tracker resaltado para activar el reinicio completo.
    
    Se requiere que estas de forma parada (pose en i). Esto tiene un delay de 3 segundos (configurable) antes de que actualmente suceda.
    Esto reinicia completamente la posición y rotación de todos tus sensores, debería de arreglar la mayoría de tus problemas.
# Cares about multiline
onboarding-reset_tutorial-2 =
    Toca { $taps } veces el tracker resaltado para activar el reinicio de montura.
    
    El reinicio de montura ayuda en como tus sensores están puestos en tu cuerpo, ya que si los movistes o cambiaste para donde están orientados bastante, esto debería de ayudar.
    
    Requiere que estas en una pose como que estás esquiando, como se muestra en el tutorial de montura automática y tenes un retraso de 3 segundos (configurable) antes de que actualmente suceda.

## Setup start

onboarding-home = Bienvenido a SlimeVR
onboarding-home-start = ¡Comencemos!

## Enter VR part of setup

onboarding-enter_vr-back = Volver a la asignación de sensores
onboarding-enter_vr-title = ¡Es hora de entrar a la RV!
onboarding-enter_vr-description = ¡Ponte todos tus sensores y luego entra a la RV!
onboarding-enter_vr-ready = Estoy listo

## Setup done

onboarding-done-title = ¡Estás listo!
onboarding-done-description = Disfruta moverte en la realidad virtual
onboarding-done-close = Cerrar la guía

## Tracker connection setup

onboarding-connect_tracker-back = Volver a las credenciales Wi-Fi
onboarding-connect_tracker-title = Conecta tus sensores
onboarding-connect_tracker-description-p0-v1 = Ahora la parte divertida, ¡Conectando tus trackers!
onboarding-connect_tracker-description-p1-v1 = Conecte cada tracker de uno en uno a través de un puerto USB.
onboarding-connect_tracker-issue-serial = ¡Tengo problemas conectándolos!
onboarding-connect_tracker-usb = Sensor USB
onboarding-connect_tracker-connection_status-none = Buscando sensores
onboarding-connect_tracker-connection_status-serial_init = Conectando al dispositivo serial
onboarding-connect_tracker-connection_status-provisioning = Enviando credenciales Wi-Fi
onboarding-connect_tracker-connection_status-connecting = Enviando credenciales Wi-Fi
onboarding-connect_tracker-connection_status-looking_for_server = Buscando servidor
onboarding-connect_tracker-connection_status-connection_error = Incapaz de conectar al Wi-Fi
onboarding-connect_tracker-connection_status-could_not_find_server = No se pudo encontrar el servidor
onboarding-connect_tracker-connection_status-done = Conectado con el servidor
# $amount (Number) - Amount of trackers connected (this is a number, but you can use CLDR plural rules for your language)
# More info on https://www.unicode.org/cldr/cldr-aux/charts/22/supplemental/language_plural_rules.html
# English in this case only has 2 plural rules, which are "one" and "other",
# we use 0 in an explicit way because there is no plural rule in english for 0, so we directly say
# if $amount is 0 then we say "No trackers connected"
onboarding-connect_tracker-connected_trackers =
    { $amount ->
        [0] No hay sensores conectados
        [one] 1 sensor conectado
       *[other] { $amount } sensores conectados
    }
onboarding-connect_tracker-next = He conectado todos mis sensores

## Tracker calibration tutorial

onboarding-calibration_tutorial = Tutorial de calibración de IMU
onboarding-calibration_tutorial-subtitle = ¡Esto te ayudara a reducir la desviación del tracker!
onboarding-calibration_tutorial-description = Cada vez que enciendes tus trackers, van a necesitar descansar un ratito en una superficie plana para calibrarse. Tratemos de hacer lo mismo presionando el botón «{ onboarding-calibration_tutorial-calibrate }», <b>¡No los muevas!</b>
onboarding-calibration_tutorial-calibrate = Puse los sensores en una mesa.
onboarding-calibration_tutorial-status-waiting = Esperando por ti
onboarding-calibration_tutorial-status-calibrating = Calibrando
onboarding-calibration_tutorial-status-success = ¡Genial!
onboarding-calibration_tutorial-status-error = El tracker fue movido
onboarding-calibration_tutorial-skip = Saltar tutorial

## Tracker assignment tutorial

onboarding-assignment_tutorial = Como preparar un Tracker Slime antes de ponertelo
onboarding-assignment_tutorial-first_step = 1. Pon un sticker con la parte del cuerpo de tu elección (si tenes uno)
# This text has a character limit of around 11 characters, so please keep it short
onboarding-assignment_tutorial-sticker = Sticker
onboarding-assignment_tutorial-second_step-v2 = 2. Coloca la tira de velcro a tu tracker, manteniendo el lado del velcro de la tira, mirando para la misma dirección que la cara del slime de tu sensor:
onboarding-assignment_tutorial-second_step-continuation-v2 = El lado del velcro de la tira para la extensión deberá estar viendo para arriba como en la siguiente imagen:
onboarding-assignment_tutorial-done = ¡Puse las correas y stickers!

## Tracker assignment setup

onboarding-assign_trackers-back = Volver a las credenciales Wi-Fi
onboarding-assign_trackers-title = Asignación de sensores
onboarding-assign_trackers-description = Debes escoger dónde van los sensores. Has clic en la ubicación donde quieras colocar un sensor
# Look at translation of onboarding-connect_tracker-connected_trackers on how to use plurals
# $assigned (Number) - Trackers that have been assigned a body part
# $trackers (Number) - Trackers connected to the server
onboarding-assign_trackers-assigned =
    { $assigned } de { $trackers ->
        [one] 1 sensor asignado
       *[other] { $trackers } sensores asignados
    }
onboarding-assign_trackers-advanced = Mostrar ubicación de asignaciones avanzados.
onboarding-assign_trackers-next = He asignado todos los sensores
onboarding-assign_trackers-mirror_view = Vista espejo
onboarding-assign_trackers-option-amount =
    { $trackersCount ->
        [one] x{ $trackersCount }
        [many] x{ $trackersCount }
       *[other] x{ $trackersCount }
    }
onboarding-assign_trackers-option-label =
    { $mode ->
        [lower-body] Conjunto para inferior del cuerpo
        [core] Conjunto básico
        [enhanced-core] Conjunto básico mejorado
        [full-body] Conjunto para cuerpo completo
       *[all] Todos los sensores
    }
onboarding-assign_trackers-option-description =
    { $mode ->
        [lower-body] El mínimo para el full-body tracking en RV
        [core] + Mejor seguimiento de la columna vertebral
        [enhanced-core] + Rotación de pies
        [full-body] + Seguimiento de codos
       *[all] Todas las asignaciones de sensores disponibles
    }

## Tracker assignment warnings

# Note for devs, number is used for representing boolean states per bit.
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-LEFT_FOOT =
    { $unassigned ->
        [0] El pie izquierdo está asignado, pero necesitas el tobillo izquierdo, el muslo izquierdo y que el pecho, la cadera o la cintura estén asignados.
        [1] El pie izquierdo está asignado, pero necesitas que el muslo izquierdo y el pecho, la cadera o la cintura estén asignados.
        [2] El pie izquierdo está asignado, pero necesitas que el tobillo izquierdo y el pecho, la cadera o la cintura estén asignados.
        [3] El pie izquierdo está asignado, pero necesitas que el pecho, la cadera o la cintura estén asignados.
        [4] El pie izquierdo está asignado, pero necesitas que el tobillo izquierdo y el muslo izquierdo estén asignados.
        [5] El pie izquierdo está asignado, pero necesitas que el muslo izquierdo igual esté asignado.
        [6] El pie izquierdo está asignado, pero necesitas que el tobillo izquierdo igual esté asignado.
       *[unknown] El pie izquierdo está asignado, pero necesitas asignar la parte del cuerpo desconocida sin asignar.
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-RIGHT_FOOT =
    { $unassigned ->
        [0] El pie derecho está asignado, pero necesitas el tobillo derecho, el muslo derecho y que el pecho, la cadera o la cintura estén asignados.
        [1] El pie derecho está asignado, pero necesitas el muslo derecho y el pecho, la cadera o la cintura estén asignados.
        [2] El pie derecho está asignado, pero necesitas el tobillo derecho y el pecho, la cadera o la cintura estén asignados.
        [3] El pie derecho está asignado, pero necesitas que el pecho, la cadera o la cintura estén asignados.
        [4] El pie derecho está asignado, pero necesitas que el tobillo derecho y el muslo derecho estén asignados.
        [5] El pie derecho está asignado, pero necesitas que el muslo derecho igual esté asignado.
        [6] El pie derecho está asignado, pero necesitas que el tobillo derecho igual esté asignado.
       *[unknown] El pie derecho está asignado, pero necesitas asignar la parte del cuerpo desconocida sin asignar.
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-LEFT_LOWER_LEG =
    { $unassigned ->
        [0] El tobillo izquierdo está asignado, pero necesitas el muslo izquierdo y que el pecho, la cadera o la cintura estén asignados.
        [1] El tobillo izquierdo está asignado, pero necesitas que el pecho, la cadera o la cintura estén asignados.
        [2] El tobillo izquierdo está asignado, pero necesitas que el muslo izquierdo igual esté asignado.
       *[unknown] El tobillo izquierdo está asignado, pero necesitas asignar la parte del cuerpo desconocida sin asignar.
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-RIGHT_LOWER_LEG =
    { $unassigned ->
        [0] El tobillo derecho está asignado, pero necesitas el muslo derecho y que el pecho, la cadera o la cintura estén asignados.
        [1] El tobillo derecho está asignado, pero necesitas que el pecho, la cadera o la cintura estén asignados.
        [2] El tobillo derecho está asignado, pero necesitas que el muslo derecho igual esté asignado.
       *[unknown] El tobillo derecho está asignado, pero necesitas asignar la parte del cuerpo desconocida sin asignar.
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-LEFT_UPPER_LEG =
    { $unassigned ->
        [0] El muslo izquierdo está asignado, pero necesitas que el pecho, la cadera o la cintura estén asignados.
       *[unknown] El muslo izquierdo está asignado, pero necesitas asignar la parte del cuerpo desconocida sin asignar.
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-RIGHT_UPPER_LEG =
    { $unassigned ->
        [0] El muslo derecho está asignado, pero necesitas que el pecho, la cadera o la cintura estén asignados.
       *[unknown] El muslo derecho está asignado, pero necesitas asignar la parte del cuerpo desconocida sin asignar.
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-HIP =
    { $unassigned ->
        [0] La cadera está asignada, pero necesitas que el pecho igual esté asignado.
       *[unknown] La cadera está asignada, pero necesitas asignar la parte del cuerpo desconocida sin asignar.
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-WAIST =
    { $unassigned ->
        [0] La cintura está asignada, pero necesitas que el pecho igual esté asignado.
       *[unknown] La cintura está asignada, pero necesitas asignar la parte del cuerpo desconocida sin asignar.
    }

## Tracker mounting method choose

onboarding-choose_mounting = ¿Qué método de calibración de montura quiere usar?
# Multiline text
onboarding-choose_mounting-description = La orientación de montura corrige la colocación (o orientación) de los trackers en tu cuerpo.
onboarding-choose_mounting-auto_mounting = Montura automática
# Italicized text
onboarding-choose_mounting-auto_mounting-label-v2 = Recomendado
onboarding-choose_mounting-auto_mounting-description = Esto detectará automáticamente las direcciones de montura para todos tus trackers a partir de 2 poses
onboarding-choose_mounting-manual_mounting = Montura manual
# Italicized text
onboarding-choose_mounting-manual_mounting-label-v2 = Puede que no sea lo suficientemente preciso
onboarding-choose_mounting-manual_mounting-description = Esto te permitirá elegir la dirección de montura manualmente para cada tracker.
# Multiline text
onboarding-choose_mounting-manual_modal-title =
    ¿Está seguro de que quiere
    hacer la calibración automática de montura?
onboarding-choose_mounting-manual_modal-description = <b>Está recomendado para nuevos usuarios el uso de la calibración manual de montura</b>, ya que las poses de la calibración automática pueden ser difíciles de hacer correctamente en el primer intento y requieran un poco de práctica.
onboarding-choose_mounting-manual_modal-confirm = Estoy seguro de lo que hago
onboarding-choose_mounting-manual_modal-cancel = Cancelar

## Tracker manual mounting setup

onboarding-manual_mounting-back = Volver para entrar a la RV
onboarding-manual_mounting = Montura manual
onboarding-manual_mounting-description = Has clic en todos los sensores y selecciona en que dirección están montados
onboarding-manual_mounting-auto_mounting = Montura automática
onboarding-manual_mounting-next = Siguiente paso

## Tracker automatic mounting setup

onboarding-automatic_mounting-back = Volver para entrar a la RV
onboarding-automatic_mounting-title = Calibración de montura
onboarding-automatic_mounting-description = Para que los sensores SlimeVR funcionen, necesitamos asignar una posición de montura a tus sensores para que se alinien con la montura física de tu sensor.
onboarding-automatic_mounting-manual_mounting = Establecer montura manualmente
onboarding-automatic_mounting-next = Siguiente paso
onboarding-automatic_mounting-prev_step = Paso anterior
onboarding-automatic_mounting-done-title = Ubicación de monturas calibradas.
onboarding-automatic_mounting-done-description = ¡Tu calibración de monturas está completa!
onboarding-automatic_mounting-done-restart = Volver al inicio
onboarding-automatic_mounting-mounting_reset-title = Reinicio de montura
onboarding-automatic_mounting-mounting_reset-step-0 = 1. Arrodíllate en una posición de «esquiar» con tus piernas dobladas, la parte superior de tu cuerpo inclinada hacia adelante, y tus brazos doblados.
onboarding-automatic_mounting-mounting_reset-step-1 = 2. Presiona el botón «Reinicio de montura» y espera 3 segundos hasta que se reinicie la montura.
onboarding-automatic_mounting-preparation-title = Preparación
onboarding-automatic_mounting-preparation-step-0 = 1. Párate derecho con tus brazos a los costados.
onboarding-automatic_mounting-preparation-step-1 = 2. Presiona el botón «Reiniciar» y espera 3 segundos hasta que se reinicien los sensores.
onboarding-automatic_mounting-put_trackers_on-title = Ponte tus sensores
onboarding-automatic_mounting-put_trackers_on-description = Para calibrar la ubicación de tus monturas, usaremos los sensores que has asignado. Ponte todos tus sensores, puedes ver cuál es cual en la figura de la derecha.
onboarding-automatic_mounting-put_trackers_on-next = Tengo puestos todos mis sensores

## Tracker proportions method choose

onboarding-choose_proportions = ¿Qué método de calibración de proporciones quiere usar?
# Multiline string
onboarding-choose_proportions-description-v1 =
    Las proporciones corporales son utilizadas para saber las medidas de tu cuerpo. Se requieren para calcular la posición de los trackers.
    Cuando las proporciones de tu cuerpo no encajan con las guardadas, la precisión de tu tracking será peor y notarás cosas como deslizamiento o desplazamiento, o tu cuerpo no encajando bien con tu avatar.
    <b>¡Solo necesitas medir tu cuerpo una vez!</b> A menos que estén mal o tu cuerpo haya cambiado, no necesitas hacerlo de nuevo.
onboarding-choose_proportions-auto_proportions = Proporciones automáticas
# Italicized text
onboarding-choose_proportions-auto_proportions-subtitle = Recomendado
onboarding-choose_proportions-auto_proportions-descriptionv3 =
    Esto estimará tus proporciones grabando una muestra de tus movimientos y pasándolos a través de un algoritmo.
    
    <b>¡Esto requiere tener tu visor (HMD) conectado a SlimeVR y en tu cabeza!</b>
onboarding-choose_proportions-manual_proportions = Proporciones manuales
# Italicized text
onboarding-choose_proportions-manual_proportions-subtitle = Para toques pequeños
onboarding-choose_proportions-manual_proportions-description = Esto te permitirá ajustar tus proporciones manualmente de forma directa
onboarding-choose_proportions-export = Exportar proporciones
onboarding-choose_proportions-import = Importar proporciones
onboarding-choose_proportions-import-success = Importado
onboarding-choose_proportions-import-failed = Error
onboarding-choose_proportions-file_type = Archivo de proporciones del cuerpo

## Tracker manual proportions setup

onboarding-manual_proportions-back = Volver al tutorial de reinicio
onboarding-manual_proportions-title = Proporciones de cuerpo manuales
onboarding-manual_proportions-precision = Ajuste con precisión
onboarding-manual_proportions-auto = Calibración automática
onboarding-manual_proportions-ratio = Ajustar por porcentajes

## Tracker automatic proportions setup

onboarding-automatic_proportions-back = Volver al tutorial de reinicio
onboarding-automatic_proportions-title = Mide tu cuerpo
onboarding-automatic_proportions-description = Para que los sensores de SlimeVR funcionen, necesitamos saber el largo de tus huesos. Esta breve calibración los medirá por ti.
onboarding-automatic_proportions-manual = Calibración manual
onboarding-automatic_proportions-prev_step = Paso anterior
onboarding-automatic_proportions-put_trackers_on-title = Ponte tus sensores
onboarding-automatic_proportions-put_trackers_on-description = Para calibrar tus proporciones, usaremos los sensores que acabas de asignar. Ponte todos tus sensores, puedes ver cuál es cual en la figura de la derecha.
onboarding-automatic_proportions-put_trackers_on-next = Tengo puestos todos mis sensores
onboarding-automatic_proportions-requirements-title = Requisitos
# Each line of text is a different list item
onboarding-automatic_proportions-requirements-descriptionv2 =
    Tienes suficientes sensores para mover tus pies (generalmente 5 sensores).
    Tienes tus sensores y visor encendidos y los tienes puestos.
    Tus sensores y visor están conectados al servidor de SlimeVR y están funcionando correctamente (ej: no se congela, no se desconecta, etc).
    Tu visor esta reportando datos posicionales al servidor de SlimeVR (esto generalmente significa tener SteamVR abierto y conectado a SlimeVR usando el driver de SlimeVR para SteamVR).
    Tus sensores están funcionando y están representando tus movimientos con precisión (ej: Realizaste un reinicio completo y se mueven en la dirección correcta cuando pateas, te agachas, te sientas, etc).
onboarding-automatic_proportions-requirements-next = He leído los requisitos
onboarding-automatic_proportions-check_height-title = Compruebe su altura
onboarding-automatic_proportions-check_height-description = Utilizamos tu altura como base de nuestras mediciones utilizando la altura del VR como aproximación a tu altura real, ¡Pero es mejor que compruebes tú mismo si son correctas!
# All the text is in bold!
onboarding-automatic_proportions-check_height-calculation_warning = Por favor, pulse el botón mientras está <u>de pie</u> para calcular su altura. ¡Tienes unos 3 segundos después de pulsar el botón!
onboarding-automatic_proportions-check_height-guardian_tip =
    Si está utilizando un casco de VR portable, asegúrese de tener el guardián/
    barrera activado así la altura es detectada correctamente!
onboarding-automatic_proportions-check_height-fetch_height = Estoy de pie
# Context is that the height is unknown
onboarding-automatic_proportions-check_height-unknown = Desconocida
# Shows an element below it
onboarding-automatic_proportions-check_height-hmd_height1 = La altura de tu HMD es
# Shows an element below it
onboarding-automatic_proportions-check_height-height1 = entonces tu altura real es
onboarding-automatic_proportions-check_height-next_step = Están bien
onboarding-automatic_proportions-start_recording-title = Prepárate para moverte
onboarding-automatic_proportions-start_recording-description = Ahora vamos a grabar poses y movimientos en específico. Estas serán mostradas en la siguiente ventana. ¡Prepárate para empezar cuando presiones el botón!
onboarding-automatic_proportions-start_recording-next = Empezar grabación
onboarding-automatic_proportions-recording-title = REC
onboarding-automatic_proportions-recording-description-p0 = Grabación en proceso...
onboarding-automatic_proportions-recording-description-p1 = Realiza los siguientes movimientos:
# Each line of text is a different list item
onboarding-automatic_proportions-recording-steps =
    Párate derecho, y mueve tu cabeza circularmente.
    Hace una sentadilla y dobla tu cuerpo hacia adelante. Mientras te agachas, mira hacia tu izquierda, luego a tu derecha.
    Gira la parte superior de tu cuerpo hacia la izquierda (Contra el sentido de las agujas del reloj), y extiéndete hacia el suelo.
    Gira la parte superior de tu cuerpo hacia la derecha (En el sentido de las agujas del reloj), y extiéndete hacia al suelo.
    Gira tus caderas, como si estuvieras realizando un hula hula.
    Si te queda tiempo en la grabación, puedes repetir estos pasos hasta que termine.
onboarding-automatic_proportions-recording-processing = Procesando el resultado
# $time (Number) - Seconds left for the automatic calibration recording to finish (max 20)
onboarding-automatic_proportions-recording-timer =
    { $time ->
        [one] falta 1 segundo
       *[other] faltan { $time } segundos
    }
onboarding-automatic_proportions-verify_results-title = Verificando resultados
onboarding-automatic_proportions-verify_results-description = Comprueba abajo los resultados, ¿Parecen correctos?
onboarding-automatic_proportions-verify_results-results = Grabando resultados
onboarding-automatic_proportions-verify_results-processing = Procesando resultados
onboarding-automatic_proportions-verify_results-redo = Rehacer grabación
onboarding-automatic_proportions-verify_results-confirm = Son correctos
onboarding-automatic_proportions-done-title = Cuerpo medido y guardado.
onboarding-automatic_proportions-done-description = ¡La calibración de tus proporciones corporales fue completada!
onboarding-automatic_proportions-error_modal-v2 =
    <b>Peligro:</b> ¡Hubo un error mientras se estimaban las proporciones!
    Esto es posiblemente debido a un problema con la calibración de montura. Asegúrate de que tu tracking funcione adecuadamente antes de intentarlo nuevamente.
    Por favor <docs>revisa la documentación</docs> o únete a nuestro <discord>Discord</discord> por ayuda ^_^
onboarding-automatic_proportions-error_modal-confirm = ¡Entendido!

## Home

home-no_trackers = No hay sensores detectados o asignados

## Trackers Still On notification

trackers_still_on-modal-title = Los trackers siguen encendidos
trackers_still_on-modal-description =
    Uno o más trackers siguen encendidos.
    ¿Estás seguro que quieres salir de SlimeVR?
trackers_still_on-modal-confirm = Salir de SlimeVR
trackers_still_on-modal-cancel = Espera un momento...

## Status system

status_system-StatusTrackerReset = Se recomienda realizar un reinicio completo ya que uno o más trackers están sin reiniciar.
status_system-StatusSteamVRDisconnected =
    { $type ->
        [steamvr_feeder] Actualmente no está conectado al SlimeVR Feeder
       *[other] Actualmente no está conectado a SteamVR a través del driver de SlimeVR.
    }
status_system-StatusTrackerError = El tracker { $trackerName } tiene un error.
status_system-StatusUnassignedHMD = El casco de RV debe ser asignado como un sensor de cabeza.

## Tray Menu

tray_menu-show = Mostrar
tray_menu-hide = Ocultar
tray_menu-quit = Salir

## First exit modal

tray_or_exit_modal-title = ¿Que debería hacer el botón de cerrar?
# Multiline text
tray_or_exit_modal-description =
    Esto te deja escoger si quieres cerrar el servidor o minimizarlo a la bandeja cuando se presiona el botón de cerrar.
    
    ¡Puedes cambiar esto luego en los ajustes de la interfaz!
tray_or_exit_modal-radio-exit = Salir al cerrar
tray_or_exit_modal-radio-tray = Minimizar a la bandeja del sistema
tray_or_exit_modal-submit = Guardar
tray_or_exit_modal-cancel = Cancelar

## Unknown device modal

unknown_device-modal-title = ¡Se encontró un nuevo tracker!
unknown_device-modal-description =
    Hay un tracker nuevo con la dirección MAC <b>{ $deviceid }</b>.
    ¿Lo quieres conectar a SlimeVR?
unknown_device-modal-confirm = ¡Claro!
unknown_device-modal-forget = Ignorarlo
