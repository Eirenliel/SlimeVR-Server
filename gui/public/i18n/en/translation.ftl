# Please developers (not translators) don't reuse a key inside another key
# or concat text with a translation string in the code, use the appropriate
# features like variables and selectors in each appropriate case!
# And also comment the string if it's something not easy to translate, so you help
# translators on what it means

## Websocket (server) status
websocket-connecting = Connecting to the server
websocket-connection_lost = Connection lost to the server. Trying to reconnect...

## Update notification
version_update-title = New version available: { $version }
version_update-description = Clicking "{ version_update-update }" will download the SlimeVR installer for you.
version_update-update = Update
version_update-close = Close

## Tips
tips-find_tracker = Not sure which tracker is which? Shake a tracker and it will highlight the corresponding item.
tips-do_not_move_heels = Ensure your heels do not move during recording!
tips-file_select = Drag & drop files to use, or <u>browse</u>.
tips-tap_setup = You can slowly tap 2 times your tracker to choose it instead of selecting it from the menu.

## Body parts
body_part-NONE = Unassigned
body_part-HEAD = Head
body_part-NECK = Neck
body_part-RIGHT_SHOULDER = Right shoulder
body_part-RIGHT_UPPER_ARM = Right upper arm
body_part-RIGHT_LOWER_ARM = Right lower arm
body_part-RIGHT_HAND = Right hand
body_part-RIGHT_UPPER_LEG = Right thigh
body_part-RIGHT_LOWER_LEG = Right ankle
body_part-RIGHT_FOOT = Right foot
body_part-UPPER_CHEST = Upper chest
body_part-CHEST = Chest
body_part-WAIST = Waist
body_part-HIP = Hip
body_part-LEFT_SHOULDER = Left shoulder
body_part-LEFT_UPPER_ARM = Left upper arm
body_part-LEFT_LOWER_ARM = Left lower arm
body_part-LEFT_HAND = Left hand
body_part-LEFT_UPPER_LEG = Left thigh
body_part-LEFT_LOWER_LEG = Left ankle
body_part-LEFT_FOOT = Left foot
body_part-ACCESSORY = Accessory

## Proportions
skeleton_bone-NONE = None
skeleton_bone-HEAD = Head Shift
skeleton_bone-NECK = Neck Length
skeleton_bone-torso_group = Torso length
skeleton_bone-UPPER_CHEST = Upper Chest Length
skeleton_bone-CHEST_OFFSET = Chest Offset
skeleton_bone-CHEST = Chest Length
skeleton_bone-WAIST = Waist Length
skeleton_bone-HIP = Hip Length
skeleton_bone-HIP_OFFSET = Hip Offset
skeleton_bone-HIPS_WIDTH = Hips Width
skeleton_bone-leg_group = Leg length
skeleton_bone-UPPER_LEG = Upper Leg Length
skeleton_bone-LOWER_LEG = Lower Leg Length
skeleton_bone-FOOT_LENGTH = Foot Length
skeleton_bone-FOOT_SHIFT = Foot Shift
skeleton_bone-SKELETON_OFFSET = Skeleton Offset
skeleton_bone-SHOULDERS_DISTANCE = Shoulders Distance
skeleton_bone-SHOULDERS_WIDTH = Shoulders Width
skeleton_bone-arm_group = Arm length
skeleton_bone-UPPER_ARM = Upper Arm Length
skeleton_bone-LOWER_ARM = Lower Arm Length
skeleton_bone-HAND_Y = Hand Distance Y
skeleton_bone-HAND_Z = Hand Distance Z
skeleton_bone-ELBOW_OFFSET = Elbow Offset

## Tracker reset buttons
reset-reset_all = Reset all proportions
reset-full = Full Reset
reset-mounting = Reset Mounting
reset-yaw = Yaw Reset

## Serial detection stuff
serial_detection-new_device-p0 = New serial device detected!
serial_detection-new_device-p1 = Enter your Wi-Fi credentials!
serial_detection-new_device-p2 = Please select what you want to do with it
serial_detection-open_wifi = Connect to Wi-Fi
serial_detection-open_serial = Open Serial Console
serial_detection-submit = Submit!
serial_detection-close = Close

## Navigation bar
navbar-home = Home
navbar-body_proportions = Body Proportions
navbar-trackers_assign = Tracker Assignment
navbar-mounting = Mounting Calibration
navbar-onboarding = Setup Wizard
navbar-settings = Settings

## Biovision hierarchy recording
bvh-start_recording = Record BVH
bvh-recording = Recording...

## Tracking pause
tracking-unpaused = Pause tracking
tracking-paused = Unpause tracking

## Widget: Overlay settings
widget-overlay = Overlay
widget-overlay-is_visible_label = Show Overlay in SteamVR
widget-overlay-is_mirrored_label = Display Overlay as Mirror

## Widget: Drift compensation
widget-drift_compensation-clear = Clear drift compensation

## Widget: Clear Reset Mounting
widget-clear_mounting = Clear reset mounting

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
widget-imu_visualizer-rotation_raw = Raw
widget-imu_visualizer-rotation_preview = Preview
widget-imu_visualizer-rotation_hide = Hide

## Tracker status
tracker-status-none = No Status
tracker-status-busy = Busy
tracker-status-error = Error
tracker-status-disconnected = Disconnected
tracker-status-occluded = Occluded
tracker-status-ok = OK

## Tracker status columns
tracker-table-column-name = Name
tracker-table-column-type = Type
tracker-table-column-battery = Battery
tracker-table-column-ping = Ping
tracker-table-column-tps = TPS
tracker-table-column-temperature = Temp. °C
tracker-table-column-linear-acceleration = Accel. X/Y/Z
tracker-table-column-rotation = Rotation X/Y/Z
tracker-table-column-position = Position X/Y/Z
tracker-table-column-url = URL

## Tracker rotation
tracker-rotation-front = Front
tracker-rotation-front_left = Front-Left
tracker-rotation-front_right = Front-Right
tracker-rotation-left = Left
tracker-rotation-right = Right
tracker-rotation-back = Back
tracker-rotation-back_left = Back-Left
tracker-rotation-back_right = Back-Right
tracker-rotation-custom = Custom
tracker-rotation-overriden = (overriden by mounting reset)

## Tracker information
tracker-infos-manufacturer = Manufacturer
tracker-infos-display_name = Display Name
tracker-infos-custom_name = Custom Name
tracker-infos-url = Tracker URL
tracker-infos-version = Firmware Version
tracker-infos-hardware_rev = Hardware Revision
tracker-infos-hardware_identifier = Hardware ID
tracker-infos-imu = IMU Sensor
tracker-infos-board_type = Main board

## Tracker settings
tracker-settings-back = Go back to trackers list
tracker-settings-title = Tracker settings
tracker-settings-assignment_section = Assignment
tracker-settings-assignment_section-description = What part of the body the tracker is assigned to.
tracker-settings-assignment_section-edit = Edit assignment
tracker-settings-mounting_section = Mounting position
tracker-settings-mounting_section-description = Where is the tracker mounted?
tracker-settings-mounting_section-edit = Edit mounting
tracker-settings-drift_compensation_section = Allow drift compensation
tracker-settings-drift_compensation_section-description = Should this tracker compensate for its drift when drift compensation is enabled?
tracker-settings-drift_compensation_section-edit = Allow drift compensation
# The .<name> means it's an attribute and it's related to the top key.
# In this case that is the settings for the assignment section.
tracker-settings-name_section = Tracker name
tracker-settings-name_section-description = Give it a cute nickname :)
tracker-settings-name_section-placeholder = NightyBeast's left leg

## Tracker part card info
tracker-part_card-no_name = No name
tracker-part_card-unassigned = Unassigned

## Body assignment menu
body_assignment_menu = Where do you want this tracker to be?
body_assignment_menu-description = Choose a location where you want this tracker to be assigned. Alternatively you can choose to manage all trackers at once instead of one by one.
body_assignment_menu-show_advanced_locations = Show advanced assign locations
body_assignment_menu-manage_trackers = Manage all trackers
body_assignment_menu-unassign_tracker = Unassign tracker

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
tracker_selection_menu-UPPER_CHEST = { -tracker_selection-part } upper chest?
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

tracker_selection_menu-unassigned = Unassigned Trackers
tracker_selection_menu-assigned = Assigned Trackers
tracker_selection_menu-dont_assign = Unassign

# This line cares about multilines.
# <b>text</b> means that the text should be bold.
tracker_selection_menu-neck_warning =
    <b>Warning:</b> A neck tracker can be deadly if adjusted too tightly,
    the strap could cut the circulation to your head!
tracker_selection_menu-neck_warning-done = I understand the risks
tracker_selection_menu-neck_warning-cancel = Cancel

## Mounting menu
mounting_selection_menu = Where do you want this tracker to be?
mounting_selection_menu-close = Close

## Sidebar settings
settings-sidebar-title = Settings
settings-sidebar-general = General
settings-sidebar-tracker_mechanics = Tracker mechanics
settings-sidebar-fk_settings = Tracking settings
settings-sidebar-gesture_control = Gesture control
settings-sidebar-interface = Interface
settings-sidebar-osc_router = OSC router
settings-sidebar-osc_trackers = VRChat OSC Trackers
settings-sidebar-utils = Utilities
settings-sidebar-serial = Serial console
settings-sidebar-appearance = Appearance
settings-sidebar-notifications = Notifications

## SteamVR settings
settings-general-steamvr = SteamVR
settings-general-steamvr-subtitle = SteamVR trackers
# Not all translation keys support multiline, only the ones that specify it will actually
# split it in lines (that also means you can split in lines however you want in those).
# The first spaces (not tabs) for indentation will be ignored, just to make the file look nice when writing.
# This one is one of this cases that cares about multilines
settings-general-steamvr-description =
    Enable or disable specific SteamVR trackers.
    Useful for games or apps that only support certain trackers.
settings-general-steamvr-trackers-waist = Waist
settings-general-steamvr-trackers-chest = Chest
settings-general-steamvr-trackers-feet = Feet
settings-general-steamvr-trackers-knees = Knees
settings-general-steamvr-trackers-elbows = Elbows
settings-general-steamvr-trackers-hands = Hands

## Tracker mechanics
settings-general-tracker_mechanics = Tracker mechanics
settings-general-tracker_mechanics-filtering = Filtering
# This also cares about multilines
settings-general-tracker_mechanics-filtering-description =
    Choose the filtering type for your trackers.
    Prediction predicts movement while smoothing smoothens movement.
settings-general-tracker_mechanics-filtering-type = Filtering type
settings-general-tracker_mechanics-filtering-type-none = No filtering
settings-general-tracker_mechanics-filtering-type-none-description = Use rotations as is. Will not do any filtering.
settings-general-tracker_mechanics-filtering-type-smoothing = Smoothing
settings-general-tracker_mechanics-filtering-type-smoothing-description = Smooths movements but adds some latency.
settings-general-tracker_mechanics-filtering-type-prediction = Prediction
settings-general-tracker_mechanics-filtering-type-prediction-description = Reduces latency and makes movements more snappy, but may increase jitter.
settings-general-tracker_mechanics-filtering-amount = Amount
settings-general-tracker_mechanics-drift_compensation = Drift compensation
# This cares about multilines
settings-general-tracker_mechanics-drift_compensation-description =
    Compensates IMU yaw drift by applying an inverse rotation.
    Change amount of compensation and up to how many resets are taken into account.
settings-general-tracker_mechanics-drift_compensation-enabled-label = Drift compensation
settings-general-tracker_mechanics-drift_compensation-amount-label = Compensation amount
settings-general-tracker_mechanics-drift_compensation-max_resets-label = Use up to x last resets

## FK/Tracking settings
settings-general-fk_settings = Tracking settings

# Floor clip:
# why the name - came from the idea of noclip in video games, but is the opposite where clipping to the floor is a desired feature
# definition - Prevents the foot trackers from going lower than they where when a reset was performed
settings-general-fk_settings-leg_tweak-floor_clip = Floor clip
# Skating correction:
# why the name - without this enabled the feet will often slide across the ground as if your skating across the ground,
# since this largely prevents this it corrects for it hence skating correction (note this may be renamed to sliding correction)
# definition - Guesses when each foot is in contact with the ground and uses that information to improve tracking
settings-general-fk_settings-leg_tweak-skating_correction = Skating correction
settings-general-fk_settings-leg_tweak-toe_snap = Toe snap
settings-general-fk_settings-leg_tweak-foot_plant = Foot plant
settings-general-fk_settings-leg_tweak-skating_correction-amount = Skating correction strength
settings-general-fk_settings-leg_tweak-skating_correction-description = Skating-correction corrects for ice skating but can decrease accuracy in certain movement patterns. When enabling this make sure to full reset and recalibrate in game.
settings-general-fk_settings-leg_tweak-floor_clip-description = Floor-clip can Reduce or even eliminates clipping through the floor. When enabling this, make sure to full reset and recalibrate in game.
settings-general-fk_settings-leg_tweak-toe_snap-description = Toe-snap attempts to guess the rotation of your feet if feet trackers are not in use.
settings-general-fk_settings-leg_tweak-foot_plant-description = Foot-plant rotates your feet to be parallel to the ground when in contact.
settings-general-fk_settings-leg_fk = Leg tracking
settings-general-fk_settings-arm_fk = Arm tracking
settings-general-fk_settings-arm_fk-description = Force arms to be tracked from the HMD even if positional hand data is available.
settings-general-fk_settings-arm_fk-force_arms = Force arms from HMD
settings-general-fk_settings-skeleton_settings-toggles = Skeleton toggles
settings-general-fk_settings-skeleton_settings-description = Toggle skeleton settings on or off. It is recommended to leave these on.
settings-general-fk_settings-skeleton_settings-extended_spine_model = Extended spine model
settings-general-fk_settings-skeleton_settings-extended_pelvis_model = Extended pelvis model
settings-general-fk_settings-skeleton_settings-extended_knees_model = Extended knee model
settings-general-fk_settings-skeleton_settings-ratios = Skeleton ratios
settings-general-fk_settings-skeleton_settings-ratios-description = Change the values of skeleton settings. You may need to adjust your proportions after changing these.
settings-general-fk_settings-skeleton_settings-impute_waist_from_chest_hip = Impute waist from chest to hip
settings-general-fk_settings-skeleton_settings-impute_waist_from_chest_legs = Impute waist from chest to legs
settings-general-fk_settings-skeleton_settings-impute_hip_from_chest_legs = Impute hip from chest to legs
settings-general-fk_settings-skeleton_settings-impute_hip_from_waist_legs = Impute hip from waist to legs
settings-general-fk_settings-skeleton_settings-interp_hip_legs = Average the hip's yaw and roll with the legs'
settings-general-fk_settings-skeleton_settings-interp_knee_tracker_ankle = Average the knee trackers' yaw and roll with the ankles'

settings-general-fk_settings-self_localization-title = Mocap mode
settings-general-fk_settings-self_localization-description = Mocap Mode allows the skeleton to roughly track its own position without a headset or other trackers. Note that this requires feet and head trackers to work and is still experimental.

settings-general-fk_settings-vive_emulation-title = Vive emulation
settings-general-fk_settings-vive_emulation-description = Emulate the waist tracker problems that Vive trackers have. This is a joke and makes tracking worse.
settings-general-fk_settings-vive_emulation-label = Enable Vive emulation

## Gesture control settings (tracker tapping)
settings-general-gesture_control = Gesture control
settings-general-gesture_control-subtitle = Tap based resets
settings-general-gesture_control-description = Allows for resets to be triggered by tapping a tracker. The tracker highest up on your torso is used for Yaw Reset, the tracker highest up on your left leg is used for Full Reset, and the tracker highest up on your right leg is used for Mounting Reset. Taps must occur within the time limit of 0.3 seconds times the number of taps to be recognized.
# This is a unit: 3 taps, 2 taps, 1 tap
# $amount (Number) - Amount of taps (touches to the tracker's case)
settings-general-gesture_control-taps = { $amount ->
    [one] 1 tap
    *[other] { $amount } taps
}
# This is a unit: 3 trackers, 2 trackers, 1 tracker
# $amount (Number) - Amount of trackers
settings-general-gesture_control-trackers = { $amount ->
    [one] 1 tracker
    *[other] { $amount } trackers
}
settings-general-gesture_control-yawResetEnabled = Enable tap to yaw reset
settings-general-gesture_control-yawResetDelay = Yaw reset delay
settings-general-gesture_control-yawResetTaps = Taps for yaw reset
settings-general-gesture_control-fullResetEnabled = Enable tap to full reset
settings-general-gesture_control-fullResetDelay = Full reset delay
settings-general-gesture_control-fullResetTaps = Taps for full reset
settings-general-gesture_control-mountingResetEnabled = Enable tap to reset mounting
settings-general-gesture_control-mountingResetDelay = Mounting reset delay
settings-general-gesture_control-mountingResetTaps = Taps for mounting reset
# The number of trackers that can have higher acceleration before a tap is rejected
settings-general-gesture_control-numberTrackersOverThreshold = Trackers over threshold
settings-general-gesture_control-numberTrackersOverThreshold-description = Increase this value if tap detection is not working. Do not increase it above what is needed to make tap detection work as it would cause more false positives.

## Appearance settings
settings-interface-appearance = Appearance
settings-general-interface-dev_mode = Developer Mode
settings-general-interface-dev_mode-description = This mode can be useful if you need in-depth data or to interact with connected trackers on a more advanced level.
settings-general-interface-dev_mode-label = Developer Mode
settings-general-interface-theme = Color theme
settings-general-interface-lang = Select language
settings-general-interface-lang-description = Change the default language you want to use.
settings-general-interface-lang-placeholder = Select the language to use
# Keep the font name untranslated
settings-interface-appearance-font = GUI font
settings-interface-appearance-font-description = This changes the font used by the interface.
settings-interface-appearance-font-placeholder = Default font
settings-interface-appearance-font-os_font = OS font
settings-interface-appearance-font-slime_font = Default font
settings-interface-appearance-font_size = Base font scaling
settings-interface-appearance-font_size-description = This affects the font size of the whole interface except this settings panel.

## Notification settings
settings-interface-notifications = Notifications
settings-general-interface-serial_detection = Serial device detection
settings-general-interface-serial_detection-description = This option will show a pop-up every time you plug a new serial device that could be a tracker. It helps improving the setup process of a tracker.
settings-general-interface-serial_detection-label = Serial device detection
settings-general-interface-feedback_sound = Feedback sound
settings-general-interface-feedback_sound-description = This option will play a sound when a reset is triggered.
settings-general-interface-feedback_sound-label = Feedback sound
settings-general-interface-feedback_sound-volume = Feedback sound volume

## Serial settings
settings-serial = Serial Console
# This cares about multilines
settings-serial-description =
    This is a live information feed for serial communication.
    May be useful if you need to know the firmware is acting up.
settings-serial-connection_lost = Connection to serial lost, Reconnecting...
settings-serial-reboot = Reboot
settings-serial-factory_reset = Factory Reset
# This cares about multilines
# <b>text</b> means that the text should be bold
settings-serial-factory_reset-warning =
    <b>Warning:</b> This will reset the tracker to factory settings.
    Which means Wi-Fi and calibration settings <b>will all be lost!</b>
settings-serial-factory_reset-warning-ok = I know what I'm doing
settings-serial-factory_reset-warning-cancel = Cancel
settings-serial-get_infos = Get Infos
settings-serial-serial_select = Select a serial port
settings-serial-auto_dropdown_item = Auto

## OSC router settings
settings-osc-router = OSC router
# This cares about multilines
settings-osc-router-description =
    Forward OSC messages from another program.
    Useful for using another OSC program with VRChat for example.
settings-osc-router-enable = Enable
settings-osc-router-enable-description = Toggle the forwarding of messages.
settings-osc-router-enable-label = Enable
settings-osc-router-network = Network ports
# This cares about multilines
settings-osc-router-network-description =
    Set the ports for listening and sending data.
    These can be the same as other ports used in the SlimeVR server.
settings-osc-router-network-port_in =
    .label = Port In
    .placeholder = Port in (default: 9002)
settings-osc-router-network-port_out =
    .label = Port Out
    .placeholder = Port out (default: 9000)
settings-osc-router-network-address = Network address
settings-osc-router-network-address-description = Set the address to send out data at.
settings-osc-router-network-address-placeholder = IPV4 address

## OSC VRChat settings
settings-osc-vrchat = VRChat OSC Trackers
# This cares about multilines
settings-osc-vrchat-description =
    Change VRChat-specific settings to receive HMD data and send
    tracker data for FBT without SteamVR (ex. Quest standalone).
    This also allows you to receive trackers under the OSCTrackers standard.
    This will also send accessory trackers' rotations for custom avatars.
settings-osc-vrchat-enable = Enable
settings-osc-vrchat-enable-description = Toggle the sending and receiving of data.
settings-osc-vrchat-enable-label = Enable
settings-osc-vrchat-network = Network ports
settings-osc-vrchat-network-description = Set the ports for listening and sending data to VRChat.
settings-osc-vrchat-network-port_in =
    .label = Port In
    .placeholder = Port in (default: 9001)
settings-osc-vrchat-network-port_out =
    .label = Port Out
    .placeholder = Port out (default: 9000)
settings-osc-vrchat-network-address = Network address
settings-osc-vrchat-network-address-description = Choose which address to send out data to VRChat (check your Wi-Fi settings on your device).
settings-osc-vrchat-network-address-placeholder = VRChat ip address
settings-osc-vrchat-network-trackers = Trackers
settings-osc-vrchat-network-trackers-description = Toggle the sending of specific trackers via OSC.
settings-osc-vrchat-network-trackers-chest = Chest
settings-osc-vrchat-network-trackers-hip = Hip
settings-osc-vrchat-network-trackers-knees = Knees
settings-osc-vrchat-network-trackers-feet = Feet
settings-osc-vrchat-network-trackers-elbows = Elbows

## VMC OSC settings
settings-osc-vmc = Virtual Motion Capture
# This cares about multilines
settings-osc-vmc-description =
    Change settings specific to the VMC (Virtual Motion Capture) protocol
        to send and received bone and tracker data from and to other apps.
settings-osc-vmc-enable = Enable
settings-osc-vmc-enable-description = Toggle the sending and receiving of data.
settings-osc-vmc-enable-label = Enable
settings-osc-vmc-network = Network ports
settings-osc-vmc-network-description = Set the ports for listening and sending data via VMC.
settings-osc-vmc-network-port_in =
    .label = Port In
    .placeholder = Port in (default: 39540)
settings-osc-vmc-network-port_out =
    .label = Port Out
    .placeholder = Port out (default: 39539)
settings-osc-vmc-network-address = Network address
settings-osc-vmc-network-address-description = Choose which address to send out data at via VMC.
settings-osc-vmc-network-address-placeholder = IPV4 address
settings-osc-vmc-vrm = VRM Model
settings-osc-vmc-vrm-description = Load a VRM model to allow head anchor and enable a higher compatibility with other applications.
settings-osc-vmc-vrm-model_unloaded = No model loaded
settings-osc-vmc-vrm-model_loaded = { $titled ->
    *[false] Untitled model loaded
    [true] Model loaded: { $name }
}
settings-osc-vmc-vrm-file_select = Drag & drop a model to use, or <u>browse</u>
settings-osc-vmc-anchor_hip = Anchor at hips
settings-osc-vmc-anchor_hip-description = Anchor the tracking at the hips, useful for seated VTubing. If disabling, load a VRM model.
settings-osc-vmc-anchor_hip-label = Anchor at hips

## Setup/onboarding menu
onboarding-skip = Skip setup
onboarding-continue = Continue
onboarding-wip = Work in progress
onboarding-previous_step = Previous step
onboarding-setup_warning =
    <b>Warning:</b> The initial setup is required for good tracking,
    it is needed if this is your first time using SlimeVR.
onboarding-setup_warning-skip = Skip setup
onboarding-setup_warning-cancel = Continue setup

## Wi-Fi setup
onboarding-wifi_creds-back = Go Back to introduction
onboarding-wifi_creds = Input Wi-Fi credentials
# This cares about multilines
onboarding-wifi_creds-description =
    The Trackers will use these credentials to connect wirelessly.
    Please use the credentials that you are currently connected to.
onboarding-wifi_creds-skip = Skip Wi-Fi settings
onboarding-wifi_creds-submit = Submit!
onboarding-wifi_creds-ssid =
    .label = Wi-Fi name
    .placeholder = Enter Wi-Fi name
onboarding-wifi_creds-password =
    .label = Password
    .placeholder = Enter password

## Mounting setup
onboarding-reset_tutorial-back = Go Back to Mounting calibration
onboarding-reset_tutorial = Reset tutorial
onboarding-reset_tutorial-explanation = While you use your trackers they might get out of alignment because of IMU yaw drift, or because you might have moved them physically. You have several ways to fix this.
onboarding-reset_tutorial-skip = Skip step
# Cares about multiline
onboarding-reset_tutorial-0 = Tap { $taps } times the highlighted tracker for triggering yaw reset.

    This will make the trackers face the same direction as your HMD.
# Cares about multiline
onboarding-reset_tutorial-1 = Tap { $taps } times the highlighted tracker for triggering full reset.

    You need to be standing for this (i-pose). There is a 3 seconds delay (configurable) before it actually happens.
    This fully resets the position and rotation of all your trackers. It should fix most issues.
# Cares about multiline
onboarding-reset_tutorial-2 = Tap { $taps } times the highlighted tracker for triggering mounting reset.

    Mounting reset helps on how the trackers are actually put on you, so if you accidentally moved them and changed how they are oriented by a big amount, this will help.

    You need to be on a pose like you are skiing like it's shown on the Automatic Mounting wizard and you have a 3 second delay (configurable) before it gets triggered.

## Setup start
onboarding-home = Welcome to SlimeVR
onboarding-home-start = Let's get set up!

## Enter VR part of setup
onboarding-enter_vr-back = Go Back to Tracker assignent
onboarding-enter_vr-title = Time to enter VR!
onboarding-enter_vr-description = Put on all your trackers and then enter VR!
onboarding-enter_vr-ready = I'm ready

## Setup done
onboarding-done-title = You're all set!
onboarding-done-description = Enjoy your full-body experience
onboarding-done-close = Close setup

## Tracker connection setup
onboarding-connect_tracker-back = Go Back to Wi-Fi credentials
onboarding-connect_tracker-title = Connect trackers
onboarding-connect_tracker-description-p0 = Now onto the fun part, connecting all the trackers!
onboarding-connect_tracker-description-p1 = Simply connect all that aren't connected yet, through a USB port.
onboarding-connect_tracker-issue-serial = I'm having trouble connecting!
onboarding-connect_tracker-usb = USB Tracker
onboarding-connect_tracker-connection_status-none = Looking for trackers
onboarding-connect_tracker-connection_status-serial_init = Connecting to serial device
onboarding-connect_tracker-connection_status-provisioning = Sending Wi-Fi credentials
onboarding-connect_tracker-connection_status-connecting = Trying to connect to Wi-Fi
onboarding-connect_tracker-connection_status-looking_for_server = Looking for server
onboarding-connect_tracker-connection_status-connection_error = Unable to connect to Wi-Fi
onboarding-connect_tracker-connection_status-could_not_find_server = Could not find the server
onboarding-connect_tracker-connection_status-done = Connected to the Server
# $amount (Number) - Amount of trackers connected (this is a number, but you can use CLDR plural rules for your language)
# More info on https://www.unicode.org/cldr/cldr-aux/charts/22/supplemental/language_plural_rules.html
# English in this case only has 2 plural rules, which are "one" and "other",
# we use 0 in an explicit way because there is no plural rule in english for 0, so we directly say
# if $amount is 0 then we say "No trackers connected"
onboarding-connect_tracker-connected_trackers = { $amount ->
    [0] No trackers
    [one] 1 tracker
    *[other] { $amount } trackers
} connected
onboarding-connect_tracker-next = I connected all my trackers

## Tracker calibration tutorial
onboarding-calibration_tutorial = IMU Calibration Tutorial
onboarding-calibration_tutorial-subtitle = This will help reduce tracker drifting!
onboarding-calibration_tutorial-description = Every time you turn on your trackers, they need to rest for a moment on a flat surface to calibrate. Let's do the same thing by clicking the "{ onboarding-calibration_tutorial-calibrate }" button, <b>do not move them!</b>
onboarding-calibration_tutorial-calibrate = I placed my trackers on the table
onboarding-calibration_tutorial-status-waiting = Waiting for you
onboarding-calibration_tutorial-status-calibrating = Calibrating
onboarding-calibration_tutorial-status-success = Nice!
onboarding-calibration_tutorial-status-error = The tracker was moved

## Tracker assignment tutorial
onboarding-assignment_tutorial = How to prepare a Slime Tracker before putting it on
onboarding-assignment_tutorial-first_step = 1. Place a body part sticker (if you have one) on the tracker according to your choosing
# This text has a character limit of around 11 characters, so please keep it short
onboarding-assignment_tutorial-sticker = Sticker
onboarding-assignment_tutorial-second_step-v2 = 2. Attach the strap to your tracker, keeping the velcro side of the strap facing the same direction as the slime face of your tracker:
onboarding-assignment_tutorial-second_step-continuation-v2 = The velcro side for the extension should be facing up like the following image:
onboarding-assignment_tutorial-done = I put stickers and straps!

## Tracker assignment setup
onboarding-assign_trackers-back = Go Back to Wi-Fi Credentials
onboarding-assign_trackers-title = Assign trackers
onboarding-assign_trackers-description = Let's choose which tracker goes where. Click on a location where you want to place a tracker
# Look at translation of onboarding-connect_tracker-connected_trackers on how to use plurals
# $assigned (Number) - Trackers that have been assigned a body part
# $trackers (Number) - Trackers connected to the server
onboarding-assign_trackers-assigned = { $assigned } of { $trackers ->
    [one] 1 tracker
    *[other] { $trackers } trackers
} assigned
onboarding-assign_trackers-advanced = Show advanced assign locations
onboarding-assign_trackers-next = I assigned all the trackers

## Tracker assignment warnings
# Note for devs, number is used for representing boolean states per bit.
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-LEFT_FOOT = Left foot is assigned but you need { $unassigned ->
    [0] the left ankle, left thigh and either the chest, hip or waist
    [1] the left thigh and either the chest, hip or waist
    [2] the left ankle and either the chest, hip or waist
    [3] either the chest, hip or waist
    [4] the left ankle and left thigh
    [5] the left thigh
    [6] the left ankle
    *[unknown] Unknown unassigned body part
} to also be assigned!
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-RIGHT_FOOT = Right foot is assigned but you need { $unassigned ->
    [0] the right ankle, right thigh and either the chest, hip or waist
    [1] the right thigh and either the chest, hip or waist
    [2] the right ankle and either the chest, hip or waist
    [3] either the chest, hip or waist
    [4] the right ankle and right thigh
    [5] the right thigh
    [6] the right ankle
    *[unknown] Unknown unassigned body part
} to also be assigned!
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-LEFT_LOWER_LEG = Left ankle is assigned but you need { $unassigned ->
    [0] the left thigh and either the chest, hip or waist
    [1] either the chest, hip or waist
    [2] the left thigh
    *[unknown] Unknown unassigned body part
} to also be assigned!
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-RIGHT_LOWER_LEG = Right ankle is assigned but you need { $unassigned ->
    [0] the right thigh and either the chest, hip or waist
    [1] either the chest, hip or waist
    [2] the right thigh
    *[unknown] Unknown unassigned body part
} to also be assigned!
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-LEFT_UPPER_LEG = Left thigh is assigned but you need { $unassigned ->
    [0] either the chest, hip or waist
    *[unknown] Unknown unassigned body part
} to also be assigned!
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-RIGHT_UPPER_LEG = Right thigh is assigned but you need { $unassigned ->
    [0] either the chest, hip or waist
    *[unknown] Unknown unassigned body part
} to also be assigned!
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-HIP = Hip is assigned but you need { $unassigned ->
    [0] the chest
    *[unknown] Unknown unassigned body part
} to also be assigned!
# $unassigned (Number) - Bits are based on BodyAssignment.ASSIGNMENT_RULES order
onboarding-assign_trackers-warning-WAIST = Waist is assigned but you need { $unassigned ->
    [0] the chest
    *[unknown] Unknown unassigned body part
} to also be assigned!

## Tracker mounting method choose
onboarding-choose_mounting = What mounting calibration method to use?
# Multiline text
onboarding-choose_mounting-description = Mounting orientation corrects for the placement of trackers on your body.
onboarding-choose_mounting-auto_mounting = Automatic mounting
# Italized text
onboarding-choose_mounting-auto_mounting-label = Experimental
onboarding-choose_mounting-auto_mounting-description = This will automatically detect the mounting directions for all of your trackers from 2 poses
onboarding-choose_mounting-manual_mounting = Manual mounting
# Italized text
onboarding-choose_mounting-manual_mounting-label = Recommended
onboarding-choose_mounting-manual_mounting-description = This will let you choose the mounting direction manually for each tracker


## Tracker manual mounting setup
onboarding-manual_mounting-back = Go Back to Enter VR
onboarding-manual_mounting = Manual Mounting
onboarding-manual_mounting-description = Click on every tracker and select which way they are mounted
onboarding-manual_mounting-auto_mounting = Automatic mounting
onboarding-manual_mounting-next = Next step

## Tracker automatic mounting setup
onboarding-automatic_mounting-back = Go Back to Enter VR
onboarding-automatic_mounting-title = Mounting Calibration
onboarding-automatic_mounting-description = For SlimeVR trackers to work, we need to assign a mounting rotation to your trackers to align them with your physical tracker mounting.
onboarding-automatic_mounting-manual_mounting = Manual mounting
onboarding-automatic_mounting-next = Next step
onboarding-automatic_mounting-prev_step = Previous step
onboarding-automatic_mounting-done-title = Mounting rotations calibrated.
onboarding-automatic_mounting-done-description = Your mounting calibration is complete!
onboarding-automatic_mounting-done-restart = Try again
onboarding-automatic_mounting-mounting_reset-title = Mounting Reset
onboarding-automatic_mounting-mounting_reset-step-0 = 1. Squat in a "skiing" pose with your legs bent, your upper body tilted forwards, and your arms bent.
onboarding-automatic_mounting-mounting_reset-step-1 = 2. Press the "Reset Mounting" button and wait for 3 seconds before the trackers' mounting rotations will reset.
onboarding-automatic_mounting-preparation-title = Preparation
onboarding-automatic_mounting-preparation-step-0 = 1. Stand upright with your arms to your sides.
onboarding-automatic_mounting-preparation-step-1 = 2. Press the "Full Reset" button and wait for 3 seconds before the trackers will reset.
onboarding-automatic_mounting-put_trackers_on-title = Put on your trackers
onboarding-automatic_mounting-put_trackers_on-description = To calibrate mounting rotations, we're gonna use the trackers you just assigned. Put on all your trackers, you can see which are which in the figure to the right.
onboarding-automatic_mounting-put_trackers_on-next = I have all my trackers on

## Tracker proportions method choose
onboarding-choose_proportions = What proportion calibration method to use?
# Multiline string
onboarding-choose_proportions-description = Body proportions are used to know the measurements of your body. They're required to calculate the trackers' positions.
    When proportions of your body don't match the ones saved, your tracking precision will be worse and you will notice things like skating or sliding, or your body not matching your avatar well.
onboarding-choose_proportions-auto_proportions = Automatic proportions
# Italized text
onboarding-choose_proportions-auto_proportions-subtitle = Recommended
onboarding-choose_proportions-auto_proportions-descriptionv2 =
    This will guess your proportions by recording a sample of your movements and passing it through an algorithm.

    <b>This requires having your HMD connected to SlimeVR!</b>
onboarding-choose_proportions-manual_proportions = Manual proportions
# Italized text
onboarding-choose_proportions-manual_proportions-subtitle = For small touches
onboarding-choose_proportions-manual_proportions-description = This will let you adjust your proportions manually by modifying them directly
onboarding-choose_proportions-export = Export proportions
onboarding-choose_proportions-file_type = Body proportions file

## Tracker manual proportions setup
onboarding-manual_proportions-back = Go Back to Reset tutorial
onboarding-manual_proportions-title = Manual Body Proportions
onboarding-manual_proportions-precision = Precision adjust
onboarding-manual_proportions-auto = Automatic proportions
onboarding-manual_proportions-ratio = Adjust by ratio groups

## Tracker automatic proportions setup
onboarding-automatic_proportions-back = Go Back to Reset tutorial
onboarding-automatic_proportions-title = Measure your body
onboarding-automatic_proportions-description = For SlimeVR trackers to work, we need to know the length of your bones. This short calibration will measure it for you.
onboarding-automatic_proportions-manual = Manual proportions
onboarding-automatic_proportions-prev_step = Previous step
onboarding-automatic_proportions-put_trackers_on-title = Put on your trackers
onboarding-automatic_proportions-put_trackers_on-description = To calibrate your proportions, we're gonna use the trackers you just assigned. Put on all your trackers, you can see which are which in the figure to the right.
onboarding-automatic_proportions-put_trackers_on-next = I have all my trackers on
onboarding-automatic_proportions-requirements-title = Requirements
# Each line of text is a different list item
onboarding-automatic_proportions-requirements-description =
    You have at least enough trackers to track your feet (generally 5 trackers).
    You have your trackers and headset on.
    You are wearing your trackers and headset.
    Your trackers and headset are connected to the SlimeVR server.
    Your trackers and headset are working properly within the SlimeVR server.
    Your headset is reporting positional data to the SlimeVR server (this generally means having SteamVR running and connected to SlimeVR using SlimeVR's SteamVR driver).
onboarding-automatic_proportions-requirements-next = I have read the requirements
onboarding-automatic_proportions-check_height-title = Check your height
onboarding-automatic_proportions-check_height-description = We use your height as a basis of our measurements by using the HMD's height as an approximation of your actual height, but it's better to check if they are right yourself!
# All the text is in bold!
onboarding-automatic_proportions-check_height-calculation_warning = Please press the button while standing <u>upright</u> to calculate your height. You have 3 seconds after you press the button!
onboarding-automatic_proportions-check_height-fetch_height = I'm standing!
# Context is that the height is unknown
onboarding-automatic_proportions-check_height-unknown = Unknown
# Shows an element below it
onboarding-automatic_proportions-check_height-hmd_height1 = Your HMD height is
# Shows an element below it
onboarding-automatic_proportions-check_height-height1 = so your actual height is
onboarding-automatic_proportions-check_height-next_step = They are fine
onboarding-automatic_proportions-start_recording-title = Get ready to move
onboarding-automatic_proportions-start_recording-description = We're now going to record some specific poses and moves. These will be prompted in the next screen. Be ready to start when the button is pressed!
onboarding-automatic_proportions-start_recording-next = Start Recording
onboarding-automatic_proportions-recording-title = REC
onboarding-automatic_proportions-recording-description-p0 = Recording in progress...
onboarding-automatic_proportions-recording-description-p1 = Make the moves shown below:
# Each line of text is a different list item
onboarding-automatic_proportions-recording-steps =
    Standing up straight, roll your head in a circle.
    Bend your back forwards and squat. While squatting, look to your left, then to your right.
    Twist your upper body to the left (counter-clockwise), then reach down towards the ground.
    Twist your upper body to the right (clockwise), then reach down towards the ground.
    Roll your hips in a circular motion as if you're using a hula hoop.
    If there is time left on the recording, you can repeat these steps until it's finished.
onboarding-automatic_proportions-recording-processing = Processing the result
# $time (Number) - Seconds left for the automatic calibration recording to finish (max 20)
onboarding-automatic_proportions-recording-timer = { $time ->
    [one] 1 second left
    *[other] { $time } seconds left
}
onboarding-automatic_proportions-verify_results-title = Verify results
onboarding-automatic_proportions-verify_results-description = Check the results below, do they look correct?
onboarding-automatic_proportions-verify_results-results = Recording results
onboarding-automatic_proportions-verify_results-processing = Processing the result
onboarding-automatic_proportions-verify_results-redo = Redo recording
onboarding-automatic_proportions-verify_results-confirm = They're correct
onboarding-automatic_proportions-done-title = Body measured and saved.
onboarding-automatic_proportions-done-description = Your body proportions' calibration is complete!
onboarding-automatic_proportions-error_modal =
    <b>Warning:</b> An error was found while estimating proportions!
    Please <docs>check the docs</docs> or join our <discord>Discord</discord> for help ^_^
onboarding-automatic_proportions-error_modal-confirm = Understood!

## Home
home-no_trackers = No trackers detected or assigned

## Status system
status_system-StatusTrackerReset = It is recommended to perform a full reset as one or more trackers are unadjusted.
status_system-StatusSteamVRDisconnected = { $type ->
    *[steamvr] Currently not connected to SteamVR via the SlimeVR driver.
    [steamvr_feeder] Currently not connected to the SlimeVR Feeder App.
}
status_system-StatusTrackerError = The { $trackerName } tracker has an error.
