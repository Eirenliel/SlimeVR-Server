# Please developers (not translators) don't reuse a key inside another key
# or concat text with a translation string in the code, use the appropriate
# features like variables and selectors in each appropriate case!
# And also comment the string if it's something not easy to translate, so you help
# translators on what it means


## Websocket (server) status

websocket-connecting = Conectando ao servidor
websocket-connection_lost = Conexão perdida com o servidor. Reconectando...

## Update notification

version_update-title = Nova versão disponível: { $version }
version_update-description = Ao clicar em "{ version_update-update }" irá baixar o instalador do SlimeVR para você.
version_update-update = Atualizar
version_update-close = Fechar

## Tips

tips-find_tracker = Não tem certeza qual tracker é qual? Balance o tracker e ele destacará o item correspondente.
tips-do_not_move_heels = Tenha certeza de não mexer seus calcanhares durante a gravação!
tips-file_select = Arraste e solte arquivos para usar, ou <u>pesquise</u>.
tips-tap_setup = Pode tocar lentamente 2 vezes no seu tracker para o escolher em vez de o selecionar no menu.
tips-turn_on_tracker = Está usando trackers oficiais do SlimeVR? Lembre-se de <b><em> ligar o seu tracker </em></b> após conecta-lo ao computador!
tips-failed_webgl = Falha ao inicializar o WebGL.

## Body parts

body_part-NONE = Não atribuído
body_part-HEAD = Cabeça
body_part-NECK = Pescoço
body_part-RIGHT_SHOULDER = Ombro direito
body_part-RIGHT_UPPER_ARM = Braço superior direito
body_part-RIGHT_LOWER_ARM = Antebraço direito
body_part-RIGHT_HAND = Mão Direita
body_part-RIGHT_UPPER_LEG = Coxa direita
body_part-RIGHT_LOWER_LEG = Canela direita
body_part-RIGHT_FOOT = Pé direito
body_part-UPPER_CHEST = Peito Superior
body_part-CHEST = Peito
body_part-WAIST = Cintura
body_part-HIP = Quadril
body_part-LEFT_SHOULDER = Ombro esquerdo
body_part-LEFT_UPPER_ARM = Braço superior esquerdo
body_part-LEFT_LOWER_ARM = Antebraço esquerdo
body_part-LEFT_HAND = Mão esquerda
body_part-LEFT_UPPER_LEG = Coxa esquerda
body_part-LEFT_LOWER_LEG = Canela esquerda
body_part-LEFT_FOOT = Pé esquerdo
body_part-LEFT_THUMB_METACARPAL = Metacarpo do polegar esquerdo
body_part-LEFT_THUMB_PROXIMAL = Proximal do polegar esquerdo
body_part-LEFT_THUMB_DISTAL = Distal do polegar esquerdo
body_part-LEFT_INDEX_PROXIMAL = Indicador esquerdo proximal
body_part-LEFT_INDEX_INTERMEDIATE = Indicador esquerdo intermediário
body_part-LEFT_INDEX_DISTAL = Indicador esquerdo distal

## Proportions

skeleton_bone-NONE = Nada
skeleton_bone-HEAD = Deslocamento da Cabeça
skeleton_bone-NECK = Tamanho do Pescoço
skeleton_bone-torso_group = Tamanho do Tronco
skeleton_bone-UPPER_CHEST = Tamanho do Peito Superior
skeleton_bone-CHEST_OFFSET = Chest Offset
skeleton_bone-CHEST = Chest Length
skeleton_bone-WAIST = Waist Length
skeleton_bone-HIP = Hip Length
skeleton_bone-HIP_OFFSET = Compensação do Quadril
skeleton_bone-HIPS_WIDTH = Largura do Quadril
skeleton_bone-leg_group = Tamanho da Perna
skeleton_bone-UPPER_LEG = Upper Leg Length
skeleton_bone-LOWER_LEG = Lower Leg Length
skeleton_bone-FOOT_LENGTH = Tamanho do Pé
skeleton_bone-FOOT_SHIFT = Compensação do Pé
skeleton_bone-SKELETON_OFFSET = Compensação do Esqueleto
skeleton_bone-SHOULDERS_DISTANCE = Distância dos Ombros
skeleton_bone-SHOULDERS_WIDTH = Largura dos Ombros
skeleton_bone-arm_group = Tamanho do Braço
skeleton_bone-UPPER_ARM = Tamanho do Braço Superior
skeleton_bone-LOWER_ARM = Distância do Antebraço
skeleton_bone-HAND_Y = Distância da mão Y
skeleton_bone-HAND_Z = Distância da mão Z
skeleton_bone-ELBOW_OFFSET = Compensação do Cotovelo

## Tracker reset buttons

reset-reset_all = Redefinir todas as proporções
reset-reset_all_warning =
    <b>Aviso:<b>  Isso irá resetar suas proporções para serem baseadas apenas em sua altura.
    Você tem certeza que deseja fazer isso?
reset-reset_all_warning-reset = Resetar proporções
reset-reset_all_warning-cancel = Cancelar
reset-full = Reset Completo
reset-mounting = Reset de Posição
reset-yaw = Reset de guinada (yaw)

## Serial detection stuff

serial_detection-new_device-p0 = Novo dispositivo de serial detectado!
serial_detection-new_device-p1 = Insira suas credenciais de Wi-Fi!
serial_detection-new_device-p2 = Selecione o que quer fazer com ele
serial_detection-open_wifi = Conectar ao Wi-Fi
serial_detection-open_serial = Abrir o Console Serial
serial_detection-submit = Enviar!
serial_detection-close = Fechar

## Navigation bar

navbar-home = Início
navbar-body_proportions = Proporções do corpo
navbar-trackers_assign = Atribuição de Tracker
navbar-mounting = Calibragem de Posição
navbar-onboarding = Assistente de Configuração
navbar-settings = Opções

## Biovision hierarchy recording

bvh-start_recording = Gravar BVH
bvh-recording = Gravando...

## Tracking pause

tracking-unpaused = Pausar rastreamento
tracking-paused = Retomar rastreamento

## Widget: Overlay settings

widget-overlay = Overlay
widget-overlay-is_visible_label = Mostrar Overlay na SteamVR
widget-overlay-is_mirrored_label = Mostrar Overlay como espelho

## Widget: Drift compensation

widget-drift_compensation-clear = Refazer compensação de drift

## Widget: Clear Reset Mounting

widget-clear_mounting = Limpar reset de posição

## Widget: Developer settings

widget-developer_mode = Modo de desenvolvedor
widget-developer_mode-high_contrast = High contrast
widget-developer_mode-precise_rotation = Precise rotation
widget-developer_mode-fast_data_feed = Fast data feed
widget-developer_mode-filter_slimes_and_hmd = Filter slimes and HMD
widget-developer_mode-sort_by_name = Sort by name
widget-developer_mode-raw_slime_rotation = Rotação bruta
widget-developer_mode-more_info = More info

## Widget: IMU Visualizer

widget-imu_visualizer = Rotação do tracker
widget-imu_visualizer-preview = Pré-visualização
widget-imu_visualizer-hide = Esconder
widget-imu_visualizer-rotation_raw = Bruta
widget-imu_visualizer-rotation_preview = Pré-visualizar rotação
widget-imu_visualizer-acceleration = Aceleração
widget-imu_visualizer-position = Posição

## Widget: Skeleton Visualizer

widget-skeleton_visualizer-preview = Visualizar esqueleto
widget-skeleton_visualizer-hide = Esconder

## Tracker status

tracker-status-none = Sem Status
tracker-status-busy = Ocupado
tracker-status-error = Erro
tracker-status-disconnected = Desconectado
tracker-status-occluded = Ocluso
tracker-status-ok = Conectado
tracker-status-timed_out = Tempo esgotado

## Tracker status columns

tracker-table-column-name = Nome
tracker-table-column-type = Tipo
tracker-table-column-battery = Bateria
tracker-table-column-ping = Ping
tracker-table-column-tps = TPS
tracker-table-column-temperature = Temp. °C
tracker-table-column-linear-acceleration = Aceleração. X/Y/Z
tracker-table-column-rotation = Rotação X/Y/Z
tracker-table-column-position = Posição X/Y/Z
tracker-table-column-url = URL

## Tracker rotation

tracker-rotation-front = Frente
tracker-rotation-front_left = Frente-Esquerda
tracker-rotation-front_right = Frente-direita
tracker-rotation-left = Esquerda
tracker-rotation-right = Direita
tracker-rotation-back = Atrás
tracker-rotation-back_left = Trás-esquerda
tracker-rotation-back_right = Trás-direita
tracker-rotation-custom = Personalizado
tracker-rotation-overriden = (substituído pelo reset de posição)

## Tracker information

tracker-infos-manufacturer = Fabricante
tracker-infos-display_name = Nome de exibição
tracker-infos-custom_name = Nome personalizado
tracker-infos-url = URL do Tracker
tracker-infos-version = Versão do firmware
tracker-infos-hardware_rev = Revisão do hardware
tracker-infos-hardware_identifier = ID do Hardware
tracker-infos-imu = Sensor IMU
tracker-infos-board_type = Placa principal
tracker-infos-network_version = Versão do protocolo
tracker-infos-magnetometer = Magnetômetro
tracker-infos-magnetometer-status-v1 =
    { $status ->
        [DISABLED] Desabilitado
        [ENABLED] Habilitado
       *[NOT_SUPPORTED] Incompatível
    }

## Tracker settings

tracker-settings-back = Voltar para lista de trackers
tracker-settings-title = Opções dos trackers
tracker-settings-assignment_section = Atribuição
tracker-settings-assignment_section-description = Qual parte do seu corpo o tracker está atribuído para.
tracker-settings-assignment_section-edit = Editar atribuição
tracker-settings-mounting_section = Posicionamento
tracker-settings-mounting_section-description = Aonde o tracker está posicionado?
tracker-settings-mounting_section-edit = Editar posição
tracker-settings-drift_compensation_section = Ligar a compensação de drift
tracker-settings-drift_compensation_section-description = Esse tracker deverá compensar pelo drift quando a compensação de drift estiver ligado?
tracker-settings-drift_compensation_section-edit = Ligar a compensação de drift
tracker-settings-use_mag = Permitir o uso do magnetômetro neste tracker
# Multiline!
tracker-settings-use_mag-description =
    Esse tracker deve usar o magnetômetro para reduzir o drift quando o uso de magnetômetro estiver permitido? <b>Não desligue seu tracker enquanto altera esta opção!</b>
    
    Você precisa permitir o uso de magnetômetro primeiro, <magSetting>clique aqui para ir para as configurações</magSetting>.
tracker-settings-use_mag-label = Permitir o uso do magnetômetro
# The .<name> means it's an attribute and it's related to the top key.
# In this case that is the settings for the assignment section.
tracker-settings-name_section = Nome do tracker
tracker-settings-name_section-description = Dê um apelido fofo :)
tracker-settings-name_section-placeholder = Coxa esquerda de NightyBeast
tracker-settings-name_section-label = Nome do tracker
tracker-settings-forget = Esquecer o tracker
tracker-settings-forget-description = Remove o tracker do servidor SlimeVR e impede que ele se conecte a ele até que o servidor seja reiniciado. A configuração do tracker não será perdida.
tracker-settings-forget-label = Esquecer o tracker

## Tracker part card info

tracker-part_card-no_name = Sem nome
tracker-part_card-unassigned = Não atribuído

## Body assignment menu

body_assignment_menu = Aonde você quer que esse tracker fique?
body_assignment_menu-description = Escolha um local onde você quer que esse tracker seja atribuído. Alternativamente você pode escolher arrumar todos os tracker de uma vez, ao invés de um por um.
body_assignment_menu-show_advanced_locations = Mostrar locais de atribuição avançados
body_assignment_menu-manage_trackers = Arrumar todos os trackers
body_assignment_menu-unassign_tracker = Desatribuir tracker

## Tracker assignment menu

# A -translation_key (with a dash in the front) means that it's a label.
# It can only be used in the translation file, it's nice for reusing names and that kind of stuff.
#
# We are using it here because english doesn't require changing the text in each case but
# maybe your language does.
-tracker_selection-part = Which tracker to assign to your
tracker_selection_menu-NONE = Which tracker do you want to be unassigned?
tracker_selection_menu-HEAD = { -tracker_selection-part } head?
tracker_selection_menu-NECK = { -tracker_selection-part } neck?
tracker_selection_menu-RIGHT_SHOULDER = { -tracker_selection-part } right shoulder?
tracker_selection_menu-RIGHT_UPPER_ARM = { -tracker_selection-part } right upper arm?
tracker_selection_menu-RIGHT_LOWER_ARM = { -tracker_selection-part } right lower arm?
tracker_selection_menu-RIGHT_HAND = { -tracker_selection-part } right hand?
tracker_selection_menu-RIGHT_UPPER_LEG = { -tracker_selection-part } right thigh?
tracker_selection_menu-RIGHT_LOWER_LEG = { -tracker_selection-part } right ankle?
tracker_selection_menu-RIGHT_FOOT = { -tracker_selection-part } right foot?
tracker_selection_menu-RIGHT_CONTROLLER = { -tracker_selection-part } right controller?
tracker_selection_menu-UPPER_CHEST = { -tracker_selection-part } peito superior?
tracker_selection_menu-CHEST = { -tracker_selection-part } chest?
tracker_selection_menu-WAIST = { -tracker_selection-part } waist?
tracker_selection_menu-HIP = { -tracker_selection-part } hip?
tracker_selection_menu-LEFT_SHOULDER = { -tracker_selection-part } left shoulder?
tracker_selection_menu-LEFT_UPPER_ARM = { -tracker_selection-part } left upper arm?
tracker_selection_menu-LEFT_LOWER_ARM = { -tracker_selection-part } left lower arm?
tracker_selection_menu-LEFT_HAND = { -tracker_selection-part } left hand?
tracker_selection_menu-LEFT_UPPER_LEG = { -tracker_selection-part } left thigh?
tracker_selection_menu-LEFT_LOWER_LEG = { -tracker_selection-part } left ankle?
tracker_selection_menu-LEFT_FOOT = { -tracker_selection-part } left foot?
tracker_selection_menu-LEFT_CONTROLLER = { -tracker_selection-part } left controller?
tracker_selection_menu-unassigned = Trackers não atribuídos
tracker_selection_menu-assigned = Trackers atribuídos
tracker_selection_menu-dont_assign = Não atribuir
# This line cares about multilines.
# <b>text</b> means that the text should be bold.
tracker_selection_menu-neck_warning =
    <b>Atenção:</b> O tracker de pescoço pode ser mortal se ajustado muito apertado,
    o strap pode cortar a circulação para sua cabeça!
tracker_selection_menu-neck_warning-done = Eu entendo os riscos
tracker_selection_menu-neck_warning-cancel = Cancelar

## Mounting menu

mounting_selection_menu = Aonde você quer que esse tracker fique?
mounting_selection_menu-close = Fechar

## Sidebar settings

settings-sidebar-title = Opções
settings-sidebar-general = Geral
settings-sidebar-tracker_mechanics = Mecânicas do Tracker
settings-sidebar-fk_settings = Opções dos trackers
settings-sidebar-gesture_control = Controle de Gestos
settings-sidebar-interface = Interface
settings-sidebar-osc_router = Roteador OSC
settings-sidebar-osc_trackers = Trackers OSC do VRChat
settings-sidebar-utils = Utilidades
settings-sidebar-serial = Console Serial
settings-sidebar-appearance = Aparência
settings-sidebar-notifications = Notificações
settings-sidebar-advanced = Avançado

## SteamVR settings

settings-general-steamvr = SteamVR
settings-general-steamvr-subtitle = Trackers do SteamVR
# Not all translation keys support multiline, only the ones that specify it will actually
# split it in lines (that also means you can split in lines however you want in those).
# The first spaces (not tabs) for indentation will be ignored, just to make the file look nice when writing.
# This one is one of this cases that cares about multilines
settings-general-steamvr-description =
    Ativar ou desativar partes específicas do tracking.
    Útil se você quer mais controle do que o SlimeVR faz.
settings-general-steamvr-trackers-waist = Cintura
settings-general-steamvr-trackers-chest = Peito
settings-general-steamvr-trackers-left_foot = Pé esquerdo
settings-general-steamvr-trackers-right_foot = Pé direito
settings-general-steamvr-trackers-left_knee = Joelho esquerdo
settings-general-steamvr-trackers-right_knee = Joelho direito
settings-general-steamvr-trackers-left_elbow = Cotovelo esquerdo
settings-general-steamvr-trackers-right_elbow = Cotovelo direito
settings-general-steamvr-trackers-left_hand = Mão esquerda
settings-general-steamvr-trackers-right_hand = Mão direita
settings-general-steamvr-trackers-tracker_toggling = Atribuição automática de trackers
settings-general-steamvr-trackers-tracker_toggling-description = Liga ou desliga automaticamente os trackers SteamVR dependendo das suas configurações atuais dos trackers
settings-general-steamvr-trackers-tracker_toggling-label = Atribuição automática de trackers
settings-general-steamvr-trackers-hands-warning =
    <b>Aviso:</b> os trackers de mão substituirão seus controles.
    Tem certeza?
settings-general-steamvr-trackers-hands-warning-cancel = Cancelar
settings-general-steamvr-trackers-hands-warning-done = Sim

## Tracker mechanics

settings-general-tracker_mechanics = Mecânicas do Tracker
settings-general-tracker_mechanics-filtering = Filtros
# This also cares about multilines
settings-general-tracker_mechanics-filtering-description =
    Escolha o tipo de filtro para seus trackers.
    Predição prediz movimentação enquanto suavização suaviza o movimento.
settings-general-tracker_mechanics-filtering-type = Tipo de filtro
settings-general-tracker_mechanics-filtering-type-none = Sem filtro
settings-general-tracker_mechanics-filtering-type-none-description = Utiliza as rotações como registradas. Sem qualquer tipo de filtro.
settings-general-tracker_mechanics-filtering-type-smoothing = Suavização
settings-general-tracker_mechanics-filtering-type-smoothing-description = Suaviza o movimento porém introduz um pouco de latência.
settings-general-tracker_mechanics-filtering-type-prediction = Predição
settings-general-tracker_mechanics-filtering-type-prediction-description = Reduz latência e torna os movimentos mais responsivos, porém aumenta tremulação (Jitter).
settings-general-tracker_mechanics-filtering-amount = Quantidade
settings-general-tracker_mechanics-yaw-reset-smooth-time = Suavisação do reset de guinada (yaw). (0s desativa a suavização)
settings-general-tracker_mechanics-drift_compensation = Compensação de drift
# This cares about multilines
settings-general-tracker_mechanics-drift_compensation-description =
    Compensa o drift de guinada (yaw) aplicando uma rotação inversa.
    Mudar a quantidade de compensação e até quantos resets vão ser levados em conta.
settings-general-tracker_mechanics-drift_compensation-enabled-label = Compensação de drift
settings-general-tracker_mechanics-drift_compensation-prediction = Predição de compensação de drift
# This cares about multilines
settings-general-tracker_mechanics-drift_compensation-prediction-description =
    Antecipa a compensação de drift além do intervalo medido anteriormente.
    Ative essa opção se o tracker estiver girando continuamente no eixo de guinada (yaw).
settings-general-tracker_mechanics-drift_compensation-prediction-label = Predição de compensação de drift
settings-general-tracker_mechanics-drift_compensation_warning =
    <b>Aviso:</b> Use a compensação de drift somente se você precisar resetar
    com muita frequência (a cada 5 a 10 minutos).
    
    Algumas IMUs sujeitas a resets mais frequentes incluem:
    Joy-Cons, owoTrack e MPUs (sem firmware recente).
settings-general-tracker_mechanics-drift_compensation_warning-cancel = Cancelar
settings-general-tracker_mechanics-drift_compensation_warning-done = Eu entedi
settings-general-tracker_mechanics-drift_compensation-amount-label = Quantidade de compensação
settings-general-tracker_mechanics-drift_compensation-max_resets-label = Use até x últimos resets
settings-general-tracker_mechanics-save_mounting_reset = Salvar a calibragem automática de posição
settings-general-tracker_mechanics-save_mounting_reset-description =
    Salva as calibrações automáticas de reset de posição para os trackers entre as reinicializações. Útil
    ao usar uma roupa em que os trackers não se movem entre as sessões. <b>Não recomendado para usuários normais!</b>
settings-general-tracker_mechanics-save_mounting_reset-enabled-label = Salvar reset de Posição
settings-general-tracker_mechanics-use_mag_on_all_trackers = Usar o magnetômetro em todos os trackers IMUs compatíveis
settings-general-tracker_mechanics-use_mag_on_all_trackers-description =
    Usa o magnetômetro em todos os trackers com firmware compatível, reduzindo o drift em ambientes magneticamente estáveis.
    Essa opção pode ser desativada indivualmente nas configurações de cada tracker. <b>Não desligue nenhum dos trackers enquanto altera esta opção!</b>
settings-general-tracker_mechanics-use_mag_on_all_trackers-label = Usar o magnetômetro nos trackers

## FK/Tracking settings

settings-general-fk_settings = Opções de Tracker
# Floor clip:
# why the name - came from the idea of noclip in video games, but is the opposite where clipping to the floor is a desired feature
# definition - Prevents the foot trackers from going lower than they where when a reset was performed
settings-general-fk_settings-leg_tweak-floor_clip = Clipping de chão
# Skating correction:
# why the name - without this enabled the feet will often slide across the ground as if your skating across the ground,
# since this largely prevents this it corrects for it hence skating correction (note this may be renamed to sliding correction)
# definition - Guesses when each foot is in contact with the ground and uses that information to improve tracking
settings-general-fk_settings-leg_tweak-skating_correction = Correção de deslize
settings-general-fk_settings-leg_tweak-toe_snap = Encaixar os dedos do pé
settings-general-fk_settings-leg_tweak-foot_plant = Pé plantado
settings-general-fk_settings-leg_tweak-skating_correction-amount = Força da correção de deslize
settings-general-fk_settings-leg_tweak-skating_correction-description = A correção de deslize corrige o efeito de patinar, mas pode diminuir a precisão em certos padrões de movimento. Ativando essa correção, certifique-se de redefinir totalmente e recalibrar no jogo.
settings-general-fk_settings-leg_tweak-floor_clip-description = Clipping de chão pode reduzir e até eliminar o clipping através do chão. Ao ativar isso, certifique-se de redefinir completamente e recalibrar no jogo.
settings-general-fk_settings-leg_tweak-toe_snap-description = Encaixar os dedos do pé, tenta adivinhar a rotação dos seus pés se os trackers dos pés não estiverem em uso.
settings-general-fk_settings-leg_tweak-foot_plant-description = Pé plantado gira os pés para ficarem paralelos ao chão quando em contato.
settings-general-fk_settings-leg_fk = Tracking de pernas
settings-general-fk_settings-leg_fk-reset_mounting_feet-description = Ativar o Reset de Posição dos pés ao ficar nas pontas dos pés.
settings-general-fk_settings-leg_fk-reset_mounting_feet = Reset de Posição dos pés
settings-general-fk_settings-arm_fk = Opções do Braço
settings-general-fk_settings-arm_fk-description = Muda o jeito que os braços são rastreados.
settings-general-fk_settings-arm_fk-force_arms = Forçar braços do HMD
settings-general-fk_settings-reset_settings = Redefinir configurações
settings-general-fk_settings-reset_settings-reset_hmd_pitch-description = Redefine a inclinação (rotação vertical) do HMD ao fazer um reset completo. Útil se estiver usando um HMD na testa para VTubing ou captura de movimento. Não ative para VR.
settings-general-fk_settings-reset_settings-reset_hmd_pitch = Redefinir a inclinação do HMD
settings-general-fk_settings-arm_fk-reset_mode-description = Mudar a pose do braço esperada para o reset de posição.
settings-general-fk_settings-arm_fk-back = Atrás
settings-general-fk_settings-arm_fk-back-description = O modo padrão, com os braços voltados para trás e os antebraços para frente.
settings-general-fk_settings-arm_fk-tpose_up = T-pose (cima)
settings-general-fk_settings-arm_fk-tpose_up-description = Espera que seus braços fiquem para baixo nas laterais durante o reset completo e 90 graus para os lados durante o reset de posição.
settings-general-fk_settings-arm_fk-tpose_down = T-pose (baixo)
settings-general-fk_settings-arm_fk-tpose_down-description = Espera que seus braços fiquem 90 graus para os lados durante o reset completo e para baixo nas laterais durante o reset de posição.
settings-general-fk_settings-arm_fk-forward = Frente
settings-general-fk_settings-arm_fk-forward-description = Espera que seus braços estejam 90 graus à frente. Útil para VTubing.
settings-general-fk_settings-skeleton_settings-toggles = Opções do esqueleto
settings-general-fk_settings-skeleton_settings-description = Ligar ou desligar opções do esqueleto. É recomendado deixar eles ligados.
settings-general-fk_settings-skeleton_settings-extended_spine_model = Modelo de coluna estendida
settings-general-fk_settings-skeleton_settings-extended_pelvis_model = Modelo de pélvis estendida
settings-general-fk_settings-skeleton_settings-extended_knees_model = Modelo de joelho estendido
settings-general-fk_settings-skeleton_settings-ratios = Proporções do esqueleto
settings-general-fk_settings-skeleton_settings-ratios-description = Mude os valores das configurações do esqueleto. Pode ser necessário ajustar suas proporções depois de alterá-las.
settings-general-fk_settings-skeleton_settings-impute_waist_from_chest_hip = Imputar cintura do peito ao quadril
settings-general-fk_settings-skeleton_settings-impute_waist_from_chest_legs = Imputar cintura do peito às pernas
settings-general-fk_settings-skeleton_settings-impute_hip_from_chest_legs = Imputar o quadril do peito às pernas
settings-general-fk_settings-skeleton_settings-impute_hip_from_waist_legs = Imputar quadril da cintura às pernas
settings-general-fk_settings-skeleton_settings-interp_hip_legs = Calcular a média da guinada e rolamento do quadril com as pernas
settings-general-fk_settings-skeleton_settings-interp_knee_tracker_ankle = Calcular a média da guinada e rolamento dos trackers dos joelhos com os tornozelos
settings-general-fk_settings-skeleton_settings-interp_knee_ankle = Calcular a média da guinada e rolamento do joelho com os tornozelos
settings-general-fk_settings-self_localization-title = Modo mocap
settings-general-fk_settings-self_localization-description = O modo mocap permite que o esqueleto rastreie aproximadamente sua própria posição sem um headset ou outros trackers. Observe que isso requer trackers de pés e cabeça para funcionar e ainda é experimental.
settings-general-fk_settings-vive_emulation-title = Emulação de Vive
settings-general-fk_settings-vive_emulation-description = Emule o problema de tracker da cintura que o Vive tracker tem. Isso é uma piada e faz o tracking ficar pior.
settings-general-fk_settings-vive_emulation-label = Ativar emulação de Vive

## Gesture control settings (tracker tapping)

settings-general-gesture_control = Controle de gestos
settings-general-gesture_control-subtitle = Resets baseados em toques
settings-general-gesture_control-description = Faz com oque os resets sejam ativados tocando um tracker. O Tracker mais alto no seu torso é usado para o Reset Rápido, o tracker mais alto na sua perna esquerda é usado para o Reset, e o tracker mais alto na sua perna direita é usado para o Reset de Posição. Os toques devem ocorrer dentro de 0.6 segundos para serem registrados.
# This is a unit: 3 taps, 2 taps, 1 tap
# $amount (Number) - Amount of taps (touches to the tracker's case)
settings-general-gesture_control-taps =
    { $amount ->
        [one] 1 tap
       *[other] { $amount } taps
    }
# This is a unit: 3 trackers, 2 trackers, 1 tracker
# $amount (Number) - Amount of trackers
settings-general-gesture_control-trackers =
    { $amount ->
        [one] tracker
        [many] trackers
       *[other] trackers
    }
settings-general-gesture_control-yawResetEnabled = Ativar toque para reset de guinada
settings-general-gesture_control-yawResetDelay = Atraso do reset de guinada
settings-general-gesture_control-yawResetTaps = Toques para reset de guinada
settings-general-gesture_control-fullResetEnabled = Habilitar toque para reset completo
settings-general-gesture_control-fullResetDelay = Atraso no reset completo
settings-general-gesture_control-fullResetTaps = Toques para reset completo
settings-general-gesture_control-mountingResetEnabled = Toques para o reset de posição
settings-general-gesture_control-mountingResetDelay = Delay do reset de posição
settings-general-gesture_control-mountingResetTaps = Toques para o reset de posição
# The number of trackers that can have higher acceleration before a tap is rejected
settings-general-gesture_control-numberTrackersOverThreshold = Trackers acima do limite
settings-general-gesture_control-numberTrackersOverThreshold-description = Aumente esse valor se a detecção de toque não estiver funcionando. Não aumente o valor acima do necessário para que a detecção de toque funcione, pois isso causaria mais falsos positivos.

## Appearance settings

settings-interface-appearance = Aparência
settings-general-interface-dev_mode = Modo de desenvolvedor
settings-general-interface-dev_mode-description = Este modo pode ser útil se precisar de dados específicos ou para interagir com trackers conectados a um nível mais avançado
settings-general-interface-dev_mode-label = Modo de desenvolvedor
settings-general-interface-theme = Cor do tema
settings-general-interface-show-navbar-onboarding = Mostrar "{ navbar-onboarding }" na barra de navegação
settings-general-interface-show-navbar-onboarding-description = Isso muda se o botão "{ navbar-onboarding }" for exibido na barra de navegação.
settings-general-interface-show-navbar-onboarding-label = Mostrar "{ navbar-onboarding }"
settings-general-interface-lang = Selecione o idioma
settings-general-interface-lang-description = Alterar o idioma padrão que pretende utilizar
settings-general-interface-lang-placeholder = Selecione o idioma que vai usar
# Keep the font name untranslated
settings-interface-appearance-font = Fonte da interface
settings-interface-appearance-font-description = Isso altera a fonte usada pela interface.
settings-interface-appearance-font-placeholder = Fonte padrão
settings-interface-appearance-font-os_font = Fonte do sistema
settings-interface-appearance-font-slime_font = Fonte padrão
settings-interface-appearance-font_size = Escala da fonte
settings-interface-appearance-font_size-description = Isso afeta o tamanho da fonte de toda a interface, exceto neste painel de configurações.
settings-interface-appearance-decorations = Use as decorações nativas do sistema
settings-interface-appearance-decorations-description = Quando essa opção estiver ativada, a barra de título do SlimeVR não será exibida, mas será substituída pela barra de título nativa do sistema.
settings-interface-appearance-decorations-label = Usar a barra de título nativa do sistema

## Notification settings

settings-interface-notifications = Notificações
settings-general-interface-serial_detection = Detecção de dispositivo serial
settings-general-interface-serial_detection-description = Esta opção mostrará um pop-up toda vez que você conectar um novo dispositivo serial que pode ser um tracker. Ajuda a melhorar o processo de configuração de um tracker
settings-general-interface-serial_detection-label = Detecção de dispositivo serial
settings-general-interface-feedback_sound = Som de feedback
settings-general-interface-feedback_sound-description = Essa opção reproduzirá um som quando um reset for acionado.
settings-general-interface-feedback_sound-label = Som de feedback
settings-general-interface-feedback_sound-volume = Volume do som de feedback
settings-general-interface-connected_trackers_warning = Aviso de trackers conectados
settings-general-interface-connected_trackers_warning-description = Essa opção exibirá um pop-up toda vez que você tentar fechar o SlimeVR enquanto tiver um ou mais trackers conectados. Ela o lembrará de desligar os trackers quando você terminar para preservar a vida útil da bateria.
settings-general-interface-connected_trackers_warning-label = Aviso de trackers conectados ao fechar
settings-general-interface-use_tray = Minimizar para bandeja do sistema
settings-general-interface-use_tray-description = Permite que você feche a janela sem fechar o servidor do SlimeVR, para que possa continuar usando-o sem que a interface gráfica o incomode.
settings-general-interface-use_tray-label = Minimizar para a bandeja do sistema
settings-general-interface-discord_presence = Compartilhar atividade no Discord
settings-general-interface-discord_presence-description = Informa ao seu Discord que o SlimeVR está aberto, juntamente com o número de trackers IMU que você está utilizando.
settings-general-interface-discord_presence-label = Compartilhar atividade no Discord
settings-general-interface-discord_presence-message =
    { $amount ->
        [0] Dando uma voltinha
        [one] Usando 1 tracker
       *[other] Usando { $amount } trackers
    }

## Serial settings

settings-serial = Console Serial
# This cares about multilines
settings-serial-description =
    Este é um feed de informações ao vivo para comunicação serial.
    Pode ser útil se você precisar saber se o firmware está tendo problemas.
settings-serial-connection_lost = Conexão com o serial perdida, Reconectando...
settings-serial-reboot = Reiniciar
settings-serial-factory_reset = Restaurar para o padrão de fábrica
# This cares about multilines
# <b>text</b> means that the text should be bold
settings-serial-factory_reset-warning =
    <b>Atenção:</b> Isso vai restaurar o tracker para o padrão de fábrica.
    Que significa que as opções de Wi-Fi e calibrações <b>vão ser todos perdidos!</b>
settings-serial-factory_reset-warning-ok = Eu sei o que estou fazendo
settings-serial-factory_reset-warning-cancel = Cancelar
settings-serial-get_infos = Obter informações
settings-serial-serial_select = Selecione uma porta serial
settings-serial-auto_dropdown_item = Auto
settings-serial-get_wifi_scan = Obter varredura WiFi
settings-serial-file_type = Texto simples
settings-serial-save_logs = Salvar em arquivo

## OSC router settings

settings-osc-router = Roteador OSC
# This cares about multilines
settings-osc-router-description =
    Encaminhar mensagens OSC de outro programa.
    Útil para usar outro programa OSC com VRChat, por exemplo.
settings-osc-router-enable = Ativar
settings-osc-router-enable-description = Ligar ou desligar o encaminhamento de mensagens
settings-osc-router-enable-label = Ativar
settings-osc-router-network = Portas de rede
# This cares about multilines
settings-osc-router-network-description =
    Defina as portas para receber e enviar dados
    Esses podem ser as mesmas portas usadas no servidor do SlimeVR
settings-osc-router-network-port_in =
    .label = Porta de entrada
    .placeholder = Porta de entrada (padrão: 9002)
settings-osc-router-network-port_out =
    .label = Porta de saída
    .placeholder = Porta de saída (padrão: 9000)
settings-osc-router-network-address = Endereço de rede
settings-osc-router-network-address-description = Defina o endereço para mandar dados
settings-osc-router-network-address-placeholder = Endereço IPV4

## OSC VRChat settings

settings-osc-vrchat = VRChat OSC Trackers
# This cares about multilines
settings-osc-vrchat-description-v1 =
    Altere as configurações específicas do padrão de trackers OSC usado para enviar
    dados de rastreamento para aplicativos sem o SteamVR (por exemplo, Quest standalone).
    Certifique-se de habilitar o OSC no VRChat através do menu de ações em OSC > Habilitado.
    Para permitir o recebimento de dados do HMD e dos controles do VRChat, vá nas configurações do menu principal
    em Rastreamento e IK > Permitir o envio de dados OSC de Rastreio de RV da cabeça e do pulso.
settings-osc-vrchat-enable = Ativar
settings-osc-vrchat-enable-description = Ligar ou desligar o envio e recebimento de dados
settings-osc-vrchat-enable-label = Ativar
settings-osc-vrchat-network = Portas de rede
settings-osc-vrchat-network-description-v1 = Define as portas para receber e enviar dados. Pode ser deixado como está para o VRChat.
settings-osc-vrchat-network-port_in =
    .label = Porta de entrada
    .placeholder = Porta de entrada (padrão: 9001)
settings-osc-vrchat-network-port_out =
    .label = Porta de saída
    .placeholder = Porta de saída (padrão: 9000)
settings-osc-vrchat-network-address = Endereço de rede
settings-osc-vrchat-network-address-description-v1 = Escolha o endereço para enviar os dados. Pode ser deixado como está para o VRChat.
settings-osc-vrchat-network-address-placeholder = Endereço de ip do VRChat
settings-osc-vrchat-network-trackers = Trackers
settings-osc-vrchat-network-trackers-description = Ligar ou desligar o envio e recepção de dados.
settings-osc-vrchat-network-trackers-chest = Peito
settings-osc-vrchat-network-trackers-hip = Quadril
settings-osc-vrchat-network-trackers-knees = Joelhos
settings-osc-vrchat-network-trackers-feet = Pés
settings-osc-vrchat-network-trackers-elbows = Cotovelos

## VMC OSC settings

settings-osc-vmc = Captura virtual de movimentos
# This cares about multilines
settings-osc-vmc-description =
    Altere as configurações específicas do protocolo VMC (Virtual Motion Capture)
    para enviar dados de esqueleto do SlimeVR e receber dados de esqueleto de outros aplicativos.
settings-osc-vmc-enable = Ativar
settings-osc-vmc-enable-description = Ative o envio e o recebimento de dados.
settings-osc-vmc-enable-label = Ativar
settings-osc-vmc-network = Portas de rede
settings-osc-vmc-network-description = Defina as portas para escutar e enviar dados via VMC (Virtual Motion Capture).
settings-osc-vmc-network-port_in =
    .label = Porta de entrada
    .placeholder = Porta de entrada (padrão: 39540)
settings-osc-vmc-network-port_out =
    .label = Porta de saída
    .placeholder = Porta de saída (padrão: 39539)
settings-osc-vmc-network-address = Endereço de rede
settings-osc-vmc-network-address-description = Escolha o endereço para envio de dados via VMC (Virtual Motion Capture).
settings-osc-vmc-network-address-placeholder = Endereço IPV4
settings-osc-vmc-vrm = Modelo VRM
settings-osc-vmc-vrm-description = Carregue um modelo de VRM para permitir ancoragem de cabeça e possibilitar uma maior compatibilidade com outras aplicações.
settings-osc-vmc-vrm-untitled_model = Modelo sem título
settings-osc-vmc-vrm-file_select = Arraste e solte um modelo para usar, ou <u>navegue</u>
settings-osc-vmc-anchor_hip = Ancorar no quadril
settings-osc-vmc-anchor_hip-description = Ancorar o rastreamento no quadril, útil para VTubing sentado. Se desativar, carregue um modelo VRM.
settings-osc-vmc-anchor_hip-label = Ancorar no quadril
settings-osc-vmc-mirror_tracking = Espelhar rastreamento
settings-osc-vmc-mirror_tracking-description = Espelhar o rastreamento horizontalmente.
settings-osc-vmc-mirror_tracking-label = Espelhar rastreamento

## Advanced settings

settings-utils-advanced = Avançado
settings-utils-advanced-reset-gui = Resetar configurações da interface
settings-utils-advanced-reset-gui-description = Restaura a interface para as configurações iniciais.
settings-utils-advanced-reset-gui-label = Resetar interface
settings-utils-advanced-reset-server = Resetar configuraçõse de tracking
settings-utils-advanced-reset-server-description = Restaura as configurações de tracking para as configurações iniciais.
settings-utils-advanced-reset-server-label = Resetar tracking
settings-utils-advanced-reset-all = Resetar todas as configurações
settings-utils-advanced-reset-all-description = Restaura a configuração da interface e de tracking para as configurações iniciais.
settings-utils-advanced-reset-all-label = Resetar todas as configurações
settings-utils-advanced-reset_warning =
    { $type ->
        [gui]
            <b>Aviso:</b> Isso irá restaurar as configurações da interface gráfica para as configurações iniciais.
            Tem certeza de que deseja fazer isso?
        [server]
            <b>Aviso:</b> Isso irá restaurar as configurações de tracking para as configurações iniciais.
            Tem certeza de que deseja fazer isso?
       *[all]
            <b>Aviso:</b> Isso irá restaurar todas as suas configurações para as configurações iniciais.
            Tem certeza de que deseja fazer isso?
    }
settings-utils-advanced-reset_warning-reset = Resetar configurações
settings-utils-advanced-reset_warning-cancel = Cancelar
settings-utils-advanced-open_data = Pasta de arquivos
settings-utils-advanced-open_data-description = Abre a pasta de arquivos do SlimeVR no explorador de arquivos, a qual contém os arquivos de configuração e de registro.
settings-utils-advanced-open_data-label = Abrir pasta

## Setup/onboarding menu

onboarding-skip = Pular configurações
onboarding-continue = Continuar
onboarding-wip = Trabalho em progresso
onboarding-previous_step = Passo anterior
onboarding-setup_warning =
    <b>Aviso:</b> A configuração inicial é necessária para um rastreamento adequado,
    isso é necessário se for a primeira vez usando o SlimeVR.
onboarding-setup_warning-skip = Pular configurações
onboarding-setup_warning-cancel = Continuar configurações

## Wi-Fi setup

onboarding-wifi_creds-back = Voltar para introdução
onboarding-wifi_creds = Insira as credenciais de Wi-Fi
# This cares about multilines
onboarding-wifi_creds-description =
    Os Trackers vão usar essas credenciais para conectar à rede sem fio
    Use as credenciais da rede em que você está atualmente conectado
onboarding-wifi_creds-skip = Pular as configurações de Wi-Fi
onboarding-wifi_creds-submit = Enviar!
onboarding-wifi_creds-ssid =
    .label = SSID
    .placeholder = Enter SSID
onboarding-wifi_creds-password =
    .label = Password
    .placeholder = Enter password

## Mounting setup

onboarding-reset_tutorial-back = Voltar para a Calibragem de Posição
onboarding-reset_tutorial = Resetar Tutorial
onboarding-reset_tutorial-explanation = Enquanto você usa os trackers, eles podem ficar desalinhados devido ao drift de guinada (yaw) da IMU ou porque você pode ter movido os trackers fisicamente. Há várias maneiras de corrigir isso.
onboarding-reset_tutorial-skip = Pular passo
# Cares about multiline
onboarding-reset_tutorial-0 =
    Toque { $taps } vezes no tracker destacado para acionar o reset de guinada.
    
    Isso fará com que os trackers fiquem na mesma direção que o seu headset (HMD).
# Cares about multiline
onboarding-reset_tutorial-1 =
    Toque { $taps } vezes no tracker destacado para acionar o reset completo.
    
    Você precisa estar em pé para isso (pose-i). Há um atraso de 3 segundos (configurável) antes que realmente aconteça.
    Isso reseta completamente a posição e rotação de todos os seus trackers. Deve corrigir a maioria dos problemas.
# Cares about multiline
onboarding-reset_tutorial-2 =
    Toque { $taps } vezes no tracker destacado para acionar o reset de posição.
    
    O reset de posição ajuda a ajustar como os trackers estão realmente colocados em você, então, se você os moveu acidentalmente e alterou significativamente a orientação, isso ajudará.
    
    Você precisa estar em uma pose como se estivesse esquiando, como é mostrado no Assistente de Posição Automática, e há um atraso de 3 segundos (configurável) antes que seja acionado.

## Setup start

onboarding-home = Bem vindo ao SlimeVR
onboarding-home-start = Vamos configurar!

## Enter VR part of setup

onboarding-enter_vr-back = Voltar para atribuição de Trackers
onboarding-enter_vr-title = Hora de entrar no VR!
onboarding-enter_vr-description = Coloque todos os seus trackers e entre no VR!
onboarding-enter_vr-ready = Estou pronto

## Setup done

onboarding-done-title = Está tudo pronto!
onboarding-done-description = Aproveite sua experiência com full body
onboarding-done-close = Fechar o guia

## Tracker connection setup

onboarding-connect_tracker-back = Voltar para as credenciais de Wi-Fi
onboarding-connect_tracker-title = Conectar os trackers
onboarding-connect_tracker-description-p0-v1 = Agora vamos à parte divertida, conectar os trackers!
onboarding-connect_tracker-description-p1-v1 = Conecte cada tracker, um de cada vez, por meio de uma porta USB.
onboarding-connect_tracker-issue-serial = Estou tendo problemas para conectar!
onboarding-connect_tracker-usb = Tracker USB
onboarding-connect_tracker-connection_status-none = Procurando por trackers
onboarding-connect_tracker-connection_status-serial_init = Conectando ao dispositivo serial
onboarding-connect_tracker-connection_status-provisioning = Enviando credenciais de Wi-Fi
onboarding-connect_tracker-connection_status-connecting = Enviando credenciais de Wi-Fi
onboarding-connect_tracker-connection_status-looking_for_server = Procurando servidor
onboarding-connect_tracker-connection_status-connection_error = Não é possível conectar ao Wi-Fi
onboarding-connect_tracker-connection_status-could_not_find_server = Não foi possível conectar ao servidor
onboarding-connect_tracker-connection_status-done = Conectado ao servidor
# $amount (Number) - Amount of trackers connected (this is a number, but you can use CLDR plural rules for your language)
# More info on https://www.unicode.org/cldr/cldr-aux/charts/22/supplemental/language_plural_rules.html
# English in this case only has 2 plural rules, which are "one" and "other",
# we use 0 in an explicit way because there is no plural rule in english for 0, so we directly say
# if $amount is 0 then we say "No trackers connected"
onboarding-connect_tracker-connected_trackers =
    { $amount ->
        [0] No trackers
        [one] 1 tracker
       *[other] { $amount } trackers
    } connected
onboarding-connect_tracker-next = Eu conectei todos os meus trackers

## Tracker calibration tutorial

onboarding-calibration_tutorial = Tutorial de Calibração IMU.
onboarding-calibration_tutorial-subtitle = Isso ajudará a reduzir o drift dos trackers!
onboarding-calibration_tutorial-description = Cada vez que ligar seus trackers, eles precisam ficar parados por um momento em uma superfície plana para calibrar. Vamos fazer a mesma coisa clicando no botão "{ onboarding-calibration_tutorial-calibrate }", <b>não os mova!</b>
onboarding-calibration_tutorial-calibrate = Eu coloquei meus trackers na mesa
onboarding-calibration_tutorial-status-waiting = Esperando por você
onboarding-calibration_tutorial-status-calibrating = Calibrando
onboarding-calibration_tutorial-status-success = Legal!
onboarding-calibration_tutorial-status-error = O tracker foi movido
onboarding-calibration_tutorial-skip = Pular tutorial

## Tracker assignment tutorial

onboarding-assignment_tutorial = Como preparar um Slime Tracker antes de colocá-lo.
onboarding-assignment_tutorial-first_step = 1. Coloque um adesivo de parte do corpo (se tiver um) no tracker de acordo com sua escolha
# This text has a character limit of around 11 characters, so please keep it short
onboarding-assignment_tutorial-sticker = Adesivo
onboarding-assignment_tutorial-second_step-v2 = 2. Prenda a strap ao seu tracker, mantendo o lado de velcro da strap virado na mesma direção que o lado do rosto do slime em seu tracker:
onboarding-assignment_tutorial-second_step-continuation-v2 = O lado de velcro para a extensão deve estar virado para cima, como na imagem a seguir:
onboarding-assignment_tutorial-done = Eu coloquei os adesivos e as straps!

## Tracker assignment setup

onboarding-assign_trackers-back = Voltar para as credenciais de Wi-Fi
onboarding-assign_trackers-title = Atribuir trackers
onboarding-assign_trackers-description = Vamos escolher onde cada tracker vai. Clique no local onde você quer colocar o tracker
# Look at translation of onboarding-connect_tracker-connected_trackers on how to use plurals
# $assigned (Number) - Trackers that have been assigned a body part
# $trackers (Number) - Trackers connected to the server
onboarding-assign_trackers-assigned =
    { $assigned } of { $trackers ->
        [one] 1 tracker
       *[other] { $trackers } trackers
    } assigned
onboarding-assign_trackers-advanced = Mostrar locais de atribuição avançados
onboarding-assign_trackers-next = Atribui todos os trackers
onboarding-assign_trackers-mirror_view = Inverter visão
onboarding-assign_trackers-option-amount =
    { $trackersCount ->
        [one] x
       *[other] x
    }
onboarding-assign_trackers-option-label =
    { $mode ->
        [lower-body] Conjunto Lower-Body
        [core] Conjunto Core
        [enhanced-core] Conjunto Enhanced
        [full-body] Conjunto Full-Body
       *[all] Todos os trackers
    }
onboarding-assign_trackers-option-description =
    { $mode ->
        [lower-body] O mínimo para rastrear seu corpo no VR
        [core] + Rastreamento melhorado da coluna
        [enhanced-core] + Rotação dos pés
        [full-body] + Rastreamento dos cotovelos
       *[all] Todas as atribuições de trackers disponíveis
    }

## Tracker assignment warnings

# Note for devs, number is used for representing boolean states per bit.
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-LEFT_FOOT =
    { $unassigned ->
        [0] Pé esquerdo está atribuído, porém a canela esquerda, coxa esquerda e peito, quadril ou cintura também precisam ser atribuídos!
        [1] Pé esquerdo está atribuído, porém a coxa esquerda e peito, quadril ou cintura também precisam ser atribuídos!
        [2] Pé esquerdo está atribuído, porém a canela esquerda e peito, quadril ou cintura também precisam ser atribuídos!
        [3] Pé esquerdo está atribuído, porém peito, quadril ou cintura também precisam ser atribuídos!
        [4] Pé esquerdo está atribuído, porém a canela esquerda e coxa esquerda também precisam ser atribuídos!
        [5] Pé esquerdo está atribuído, porém a coxa esquerda também precisa ser atribuída!
        [6] Pé esquerdo está atribuído, porém a canela esquerda também precisa ser atribuída!
       *[unknown] Pé esquerdo está atribuído, porém a parte do corpo desconhecida não atribuída também precisa ser atribuída!
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-RIGHT_FOOT =
    { $unassigned ->
        [0] Pé direito está atribuído, porém a canela direita, coxa direita e peito, quadril ou cintura também precisam ser atribuídos!
        [1] Pé direito está atribuído, porém a coxa direita e peito, quadril ou cintura também precisam ser atribuídos!
        [2] Pé direito está atribuído, porém a canela direita e peito, quadril ou cintura também precisam ser atribuídos!
        [3] Pé direito está atribuído, porém peito, quadril ou cintura também precisam ser atribuídos!
        [4] Pé direito está atribuído, porém a canela direita e coxa direita também precisam ser atribuídos!
        [5] Pé direito está atribuído, porém a coxa direita também precisa ser atribuída!
        [6] Pé direito está atribuído, porém a canela direita também precisa ser atribuída!
       *[unknown] Pé direito está atribuído, porém a parte do corpo desconhecida não atribuída também precisa ser atribuída!
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-LEFT_LOWER_LEG =
    { $unassigned ->
        [0] Canela esquerda está atribuída, porém a coxa esquerda e peito, quadril ou cintura também precisam ser atribuídos!
        [1] Canela esquerda está atribuída, porém peito, quadril ou cintura também precisam ser atribuídos!
        [2] Canela esquerda está atribuída, porém a coxa direita também precisa ser atribuída!
       *[unknown] Canela esquerda está atribuída, porém a parte do corpo desconhecida não atribuída também precisa ser atribuída!
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-RIGHT_LOWER_LEG =
    { $unassigned ->
        [0] Canela direita está atribuída, porém a coxa direita e peito, quadril ou cintura também precisam ser atribuídos!
        [1] Canela direita está atribuída, porém peito, quadril ou cintura também precisam ser atribuídos!
        [2] Canela direita está atribuída, porém a coxa direita também precisa ser atribuída!
       *[unknown] Canela direita está atribuída, porém a parte do corpo desconhecida não atribuída também precisa ser atribuída!
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-LEFT_UPPER_LEG =
    { $unassigned ->
        [0] Coxa esquerda está atribuída, porém peito, quadril ou cintura também precisam ser atribuídos!
       *[unknown] Coxa esquerda está atribuída, porém a parte do corpo desconhecida não atribuída também precisa ser atribuída!
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-RIGHT_UPPER_LEG =
    { $unassigned ->
        [0] Coxa direita está atribuída, porém peito, quadril ou cintura também precisam ser atribuídos!
       *[unknown] Coxa direita está atribuída, porém a parte do corpo desconhecida não atribuída também precisa ser atribuída!
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-HIP =
    { $unassigned ->
        [0] Quadril está atribuído, porém o peito também precisa ser atribuído!
       *[unknown] Quadril está atribuído, porém a parte do corpo desconhecida não atribuída também precisa ser atribuída!
    }
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-WAIST =
    { $unassigned ->
        [0] Cintura está atribuído, porém o peito também precisa ser atribuído!
       *[unknown] Cintura está atribuído, porém a parte do corpo desconhecida não atribuída também precisa ser atribuída!
    }

## Tracker mounting method choose

onboarding-choose_mounting = Qual método de calibração de posição você deseja usar?
# Multiline text
onboarding-choose_mounting-description = A orientação de posição corrige a colocação dos trackers no seu corpo.
onboarding-choose_mounting-auto_mounting = Posição automática
# Italicized text
onboarding-choose_mounting-auto_mounting-label-v2 = Recomendado
onboarding-choose_mounting-auto_mounting-description = Isso detectará automaticamente as direções de posição para todos os seus trackers a partir de 2 poses
onboarding-choose_mounting-manual_mounting = Posição manual
# Italicized text
onboarding-choose_mounting-manual_mounting-label-v2 = Pode não ser precisa o suficiente
onboarding-choose_mounting-manual_mounting-description = Isso permitirá que você escolha manualmente a direção de posição para cada tracker
# Multiline text
onboarding-choose_mounting-manual_modal-title =
    Você tem certeza de que deseja fazer
    a calibração automática de posição?
onboarding-choose_mounting-manual_modal-description = <b>A calibração manual de posição é recomendada para novos usuários</b>, pois as poses da calibração automática podem ser difíceis de acertar no início e podem exigir certa prática.
onboarding-choose_mounting-manual_modal-confirm = Estou certo do que estou fazendo.
onboarding-choose_mounting-manual_modal-cancel = Cancelar

## Tracker manual mounting setup

onboarding-manual_mounting-back = Voltar para entrar no VR
onboarding-manual_mounting = Posicionamento Manual
onboarding-manual_mounting-description = Clique em cada tracker e selecione de que maneira estão posicionados
onboarding-manual_mounting-auto_mounting = Posicionamento automática
onboarding-manual_mounting-next = Próximo passo

## Tracker automatic mounting setup

onboarding-automatic_mounting-back = Voltar para entrar no VR
onboarding-automatic_mounting-title = Calibragem de Posicionamento
onboarding-automatic_mounting-description = Para os trackers do SlimeVR funcionar, nós precisamos atribuir a rotação de posicionamento dos seus trackers para alinhar com a posição física de seus trackers.
onboarding-automatic_mounting-manual_mounting = Definir manualmente a posição
onboarding-automatic_mounting-next = Próximo passo
onboarding-automatic_mounting-prev_step = Passo anterior
onboarding-automatic_mounting-done-title = Rotações de posição calibradas.
onboarding-automatic_mounting-done-description = Sua calibragem de posicionamento está completa!
onboarding-automatic_mounting-done-restart = Voltar ao início
onboarding-automatic_mounting-mounting_reset-title = Reset de Posição
onboarding-automatic_mounting-mounting_reset-step-0 = 1. Agache-se em uma pose de "esqui" com as pernas dobradas, a parte superior do corpo inclinada para a frente e os braços dobrados.
onboarding-automatic_mounting-mounting_reset-step-1 = 2. Pressione o botão "Resetar Posição" e aguarde 3 segundos antes que as rotações de posição dos trackers sejam redefinidas.
onboarding-automatic_mounting-preparation-title = Preparação
onboarding-automatic_mounting-preparation-step-0 = 1. Fique de pé com os braços ao lado do corpo.
onboarding-automatic_mounting-preparation-step-1 = 2. Pressione o botão "Reset" e aguarde 3 segundos antes que os trackers sejam reiniciados.
onboarding-automatic_mounting-put_trackers_on-title = Coloque seus trackers
onboarding-automatic_mounting-put_trackers_on-description = Para calibrar as rotações de posicionamento, usaremos os trackers que você atribuiu. Coloque todos os seus trackers, você pode ver qual é qual na figura na direita.
onboarding-automatic_mounting-put_trackers_on-next = Coloquei todos os meus trackers

## Tracker proportions method choose

onboarding-choose_proportions = Qual método de calibração de proporções você deseja usar?
# Multiline string
onboarding-choose_proportions-description-v1 =
    As proporções do corpo são usadas para saber as dimensões do seu corpo. Elas são necessárias para calcular as posições dos trackers.
    Quando as proporções do seu corpo não corresponderem às que estão salvas, a precisão do rastreamento será pior e você observará problemas como efeito de patinação ou deslizamento, ou que seu corpo não corresponde bem ao seu avatar.
    <b>Você precisará medir seu corpo apenas uma vez!</b> Se as medições estiverem corretas e o seu corpo não tiver passado por mudanças significativas, não há necessidade de repeti-las.
onboarding-choose_proportions-auto_proportions = Proporções automáticas
# Italicized text
onboarding-choose_proportions-auto_proportions-subtitle = Recomendado
onboarding-choose_proportions-auto_proportions-descriptionv3 =
    Isso irá estimar suas proporções gravando uma amostra de seus movimentos e passando-a por um algoritmo.
    
    <b>Isso requer ter seu headset (HMD) conectado ao SlimeVR e na sua cabeça!</b>
onboarding-choose_proportions-manual_proportions = Proporções manuais
# Italicized text
onboarding-choose_proportions-manual_proportions-subtitle = Para pequenos ajustes
onboarding-choose_proportions-manual_proportions-description = Isso permitirá que você ajuste suas proporções manualmente, modificando-as diretamente
onboarding-choose_proportions-export = Exportar proporções
onboarding-choose_proportions-import = Importar proporções
onboarding-choose_proportions-import-success = Importado
onboarding-choose_proportions-import-failed = Falhou
onboarding-choose_proportions-file_type = Arquivo de proporções do corpo

## Tracker manual proportions setup

onboarding-manual_proportions-back = Voltar para o tutorial de reset
onboarding-manual_proportions-title = Proporções de corpo manuais
onboarding-manual_proportions-precision = Ajuste de precisão
onboarding-manual_proportions-auto = Calibragem Automática
onboarding-manual_proportions-ratio = Ajustar por grupos de proporções

## Tracker automatic proportions setup

onboarding-automatic_proportions-back = Voltar para o tutorial de reset
onboarding-automatic_proportions-title = Meça seu corpo
onboarding-automatic_proportions-description = Para os trackers SlimeVR funcionarem, precisamos saber o tamanho dos seus ossos. Essa curta calibragem vai medir isso para você.
onboarding-automatic_proportions-manual = Calibragem manual
onboarding-automatic_proportions-prev_step = Passo anterior
onboarding-automatic_proportions-put_trackers_on-title = Coloque seus trackers
onboarding-automatic_proportions-put_trackers_on-description = Para calibrar suas proporções, usaremos os trackers que você atribuiu. Coloque todos os seus trackers, você pode ver quais são quais na figura à direita.
onboarding-automatic_proportions-put_trackers_on-next = Coloquei todos os meus trackers
onboarding-automatic_proportions-requirements-title = Requisitos
# Each line of text is a different list item
onboarding-automatic_proportions-requirements-descriptionv2 =
    Você possui pelo menos trackers suficientes para rastrear seus pés (geralmente 5 trackers).
    Você está com os trackers e o headset ligados, e os está usando.
    Seus trackers e o headset estão conectados ao servidor SlimeVR e estão funcionando corretamente (sem travamentos, desconexões, etc.).
    Seu headset está enviando dados de posição para o servidor SlimeVR (isso geralmente significa ter o SteamVR em execução e conectado ao SlimeVR usando o driver SteamVR do SlimeVR).
    Seu rastreamento está funcionando e representa com precisão seus movimentos (por exemplo, você realizou uma reinicialização completa e eles se movem na direção certa ao chutar, se inclinar, sentar, etc.).
onboarding-automatic_proportions-requirements-next = Eu li os requisitos.
onboarding-automatic_proportions-check_height-title = Cheque sua altura
onboarding-automatic_proportions-check_height-description = Usamos sua altura como base para nossas medições, usando a altura do headset (HMD) como uma aproximação de sua altura real, mas é melhor verificar se estão corretas por conta própria!
# All the text is in bold!
onboarding-automatic_proportions-check_height-calculation_warning = Por favor, pressione o botão enquanto estiver em pé <u>reto</u> para calcular sua altura. Você tem 3 segundos depois de pressionar o botão!
onboarding-automatic_proportions-check_height-guardian_tip =
    Se você estiver usando um óculos VR standalone, certifique-se de que seu guardião /
    limite esteja ligado para que sua altura seja a correta!
onboarding-automatic_proportions-check_height-fetch_height = Estou em pé!
# Context is that the height is unknown
onboarding-automatic_proportions-check_height-unknown = Desconhecido
# Shows an element below it
onboarding-automatic_proportions-check_height-hmd_height1 = A altura do seu HMD é
# Shows an element below it
onboarding-automatic_proportions-check_height-height1 = então, sua altura real é
onboarding-automatic_proportions-check_height-next_step = Estão bem
onboarding-automatic_proportions-start_recording-title = Esteja preparado para se mexer
onboarding-automatic_proportions-start_recording-description = Começaremos a gravar algumas poses e movimentos específicos. Estes serão solicitados na próxima tela. Esteja preparado para começar quando o botão for pressionado!
onboarding-automatic_proportions-start_recording-next = Começar Gravação
onboarding-automatic_proportions-recording-title = GRAVAR
onboarding-automatic_proportions-recording-description-p0 = Gravação em progresso...
onboarding-automatic_proportions-recording-description-p1 = Faça os movimentos apresentados abaixo:
# Each line of text is a different list item
onboarding-automatic_proportions-recording-steps =
    Fique em pé, gire sua cabeça em um círculo.
    Incline as costas para a frente e agache. Enquanto agacha, olhe para a esquerda e depois para a direita.
    Gire o tronco para a esquerda (sentido anti-horário) e incline-se em direção ao chão.
    Gire o tronco para a direita (sentido horário) e incline-se em direção ao chão.
    Gire os quadris em um movimento circular como se estivesse usando um bambolê.
    Se houver tempo restante na gravação, você pode repetir esses passos até que termine.
onboarding-automatic_proportions-recording-processing = Processando o resultado
# $time (Number) - Seconds left for the automatic calibration recording to finish (max 20)
onboarding-automatic_proportions-recording-timer =
    { $time ->
        [one] 1 second left
       *[other] { $time } seconds left
    }
onboarding-automatic_proportions-verify_results-title = Verificar os resultados
onboarding-automatic_proportions-verify_results-description = Verifique os resultados abaixo, parecem corretos?
onboarding-automatic_proportions-verify_results-results = Gravando os resultados
onboarding-automatic_proportions-verify_results-processing = Processando o resultado
onboarding-automatic_proportions-verify_results-redo = Refazer a gravação
onboarding-automatic_proportions-verify_results-confirm = Eles estão corretos
onboarding-automatic_proportions-done-title = Corpo medido e salvo.
onboarding-automatic_proportions-done-description = Sua calibragem de proporção de corpo está completa!
onboarding-automatic_proportions-error_modal-v2 =
    <b>Aviso:</b> Ocorreu um erro ao calcular as proporções!
    Isso provavelmente é um problema da calibragem de posição. Verifique se o rastreamento está funcionando corretamente antes de tentar novamente.
    Por favor, <docs>verifique a  documentação</docs> ou entre em nosso <discord> Discord</discord> para obter ajuda ^_^
onboarding-automatic_proportions-error_modal-confirm = Entendido!

## Home

home-no_trackers = Nenhum tracker detectado ou atribuído

## Trackers Still On notification

trackers_still_on-modal-title = Trackers ainda ligados.
trackers_still_on-modal-description =
    Um ou mais trackers ainda estão ligados.
    Você ainda deseja sair do SlimeVR?
trackers_still_on-modal-confirm = Sair do SlimeVR
trackers_still_on-modal-cancel = Aguarde...

## Status system

status_system-StatusTrackerReset = É recomendado realizar um reset completo, pois um ou mais trackers estão desajustados.
status_system-StatusSteamVRDisconnected =
    { $type ->
        [steamvr_feeder] Atualmente não conectado ao aplicativo SlimeVR Feeder.
       *[other] Atualmente não conectado ao SteamVR via driver SlimeVR.
    }
status_system-StatusTrackerError = O tracker { $trackerName } tem um erro.
status_system-StatusUnassignedHMD = O headset (HMD) deve ser designado como o tracker da cabeça.

## Tray Menu

tray_menu-show = Mostrar
tray_menu-hide = Esconder
tray_menu-quit = Sair

## First exit modal

tray_or_exit_modal-title = Qual deve ser a função do botão Fechar?
# Multiline text
tray_or_exit_modal-description =
    Isso permite que você escolha se deseja sair do servidor ou minimizá-lo na bandeja ao pressionar o botão Fechar.
    
    Você pode alterar isso depois nas configurações da interface!
tray_or_exit_modal-radio-exit = Sair ao Fechar
tray_or_exit_modal-radio-tray = Minimizar para a bandeja do sistema
tray_or_exit_modal-submit = Salvar
tray_or_exit_modal-cancel = Cancelar

## Unknown device modal

unknown_device-modal-title = Um novo tracker foi encontrado!
unknown_device-modal-description =
    Há um novo tracker com o endereço MAC <b>{ $deviceId }</b>.
    Deseja conectá-lo ao SlimeVR?
unknown_device-modal-confirm = Claro!
unknown_device-modal-forget = Ignore-o
