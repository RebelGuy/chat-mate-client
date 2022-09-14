The ChatMate mod uses the `chat-mate-server` (deployed to Azure) as an interface to get near real-time YouTube and Twitch livestream chat messages. Messages are displayed in Minecraft chat.
It also features a collection of streaming and messaging management tools.

# Project Details

This project was initialised using the `ForgeTemplate` repository for 1.8.9.

Before starting, ensure the latest JRE from https://www.java.com/en/download/ is installed (fixes :applyBinaryPatches error). If, at any point, something goes wrong during setup, the workspace can be reset with `gradlew clean cleanCache --refresh-dependencies` (fixes :fixMcSources error). Alternatively, the Gradle cache can be manually deleted under `C:\Users\<User>\.gradle`. If some files are locked for being in use, task-kill "Open JDK Platform Binary" and end the "java.exe" process from the Resource Monitor -> CPU section.

If, at any point, there is a Java version error when running a Gradle task, run the task within IntelliJ instead of CMD.

The Java 8 JDK (for building) and Java 17 JDK (for development) must be installed.
https://adoptium.net/archive.html?variant=openjdk8&jvmVariant=hotspot
- Ensure JAVA_HOME is not set in either the user or system environment variables
- Ensure both the Java 17 and Java 8 `\bin` paths are set in the system environment `Path` variable
- Under Project Structure -> Platform Settings -> SDKs add the JDK home path to the Java 17 JDK folder - **failing to do so may cause IntelliJ to freeze upon project setup**
- Ensure the default IntelliJ Java version is Java 8 (File -> Project Structure -> Project Settings -> Project -> SDK -> Choose 1.8/Java 8).

 **Once IntelliJ had set up the project workspace**, the command `gradlew setupDecompWorkspace` for setting up the environment (which includes a decompilation of Minecraft - if this is not desired, use `gradlew setupDevWorkspace` instead). Next, `gradlew genIntellijRuns` was run for generating the Gradle tasks. Note that the Gradle Java version was manually set in the `gradle.properties` file by adding the line `java.import.gradle.java.home=C:/Program Files/Eclipse Adoptium/jdk-8.0.312.7-hotspot` (the same folder as the default Project SDK picked in the step above). Furthermore, the `gradle-wrapper.properties` distribution URL was set to `distributionUrl=https\://services.gradle.org/distributions/gradle-6.8.3-all.zip`.

If developing in VSCode, may need to first open the project in IntelliJ to generate all required files. Install the `Extension Pack for Java` and `Gradle for Java` extensions and make sure the 1.8/Java 8 folder is set in the `org.eclipse.buildship.core.prefs` file under `java.home`.

## File Encoding
In order to allow characters such as `'§'`, Java files must be encoding using UTF8, and the Java VM must be told of this.
- In the IntelliJ settings, set global and project encodings to UTF8
- In the system environment variables, add `JAVA_TOOL_OPTIONS` with value `-Dfile.encoding=UTF8` (see [explanation](https://stackoverflow.com/questions/361975/setting-the-default-java-character-encoding))

## Building
To build the project, use `gradlew build -Penv=[local|debug|release]`. If the `env` project property is omitted, it will
default to `local`.
Environment properties are defined in `config.groovy`. Currently, there is no known method of setting the environment
when debugging locally - it will default to `local`. To debug using other environments, the configuration file must be
temporarily modified directly.

Build output: `chat-mate-client/build/libs/*.jar`.

Debug partial .minecraft folder: `chat-mate-client/run/`

## Profiling
Use the IntelliJ extension `VisualVM Launcher` from the Marketplace: `https://plugins.jetbrains.com/plugin/7115-visualvm-launcher/`.
Start the profiling session by using the "Debug with VisualVM 'Minecraft Client'" configuration at the top. Download and extract
VisualVM and copy the path of the executable in the `/bin` folder to the empty input field.

Once started, Minecraft can be profiled within VisualVM via the `GradleStart` application. A partial snapshot can be 
recorded within the `Sampler` tab.

## Custom Chat
We use a custom implementation of the `GuiNewChat` gui object (this is what renders the chat lines,
handles `IChatComponents`, etc). There is a couple of points to note on how this works:

The `GuiIngame` object (this is what renders all the overlay components such as chat, health bar, etc) holds its own
private final version of the `GuiNewChat`, which it uses internally, and exposes publicly via `GetChatGUI()`.

Forge has
its own implementation (`GuiIngameForge`) which it uses to fire overlay rendering events. Conveniently, the `GuiIngame`
object is used only as a public settable property of `Minecraft`. So we can simply set `Minecraft.guiIngame`
to `CustomGuiIngame extends GuiIngameForge` and override the `GetChatGUI()` so that it returns our
own `CustomGuiNewChat` instead. Calling `Minecraft.guiIngame.GetChatGUI()` from anywhere will now return our custom
implementation. **Important**: We have to wait to instantiate `CustomGuiIngame` until AFTER the Minecraft object has
completely loaded because it has a getter that returns null initially, which some overlay renderers rely on.
Also, We have to wait until right after all mods have been loaded to be able to set `Minecraft.guiIngame`, otherwise
Forge will overwrite it (it instantiates this class very late in the intialisation cycle).

Now, by listening to the chat render events fired by `GuiIngameForge`, we can prevent its default
behaviour (i.e. `cancel` the event) and use `CustomGuiNewChat.drawChat()` instead. As a result, the `GuiIngameForge`
never has a chance to interact with its private `GuiNewChat` object (which remains to be the default implementation).

## Misc
List of unicode emojis that can be printed directly in chat: https://archive.ph/dhIN8

# Change Log

## v1.17 - The Donation Update [14/9/2022]
- Added a fancy Donations section to the ChatMate Dashboard with intuitive and fluent UI
  - Lists the donations with some filtering options
  - Users can be linked or unlinked
  - When requesting to link a user, a test box with a dropdown list appears that can be used to search for users
- Started reworking the HUD system so it re-uses the InteractiveScreen ecosytem
  - Donations are received as a ChatMate event and shown by a dropdown card
  - A timer bar indicates how much longer the donation card will be visible
  - The donation card can be closed or linked to a user via the top-right icon buttons
- Donators receive chat effects whose duration depends on the donation amount and highest donator rank
  - For every dollar donated, the effect will stay for another 5, 10, or 15 minutes for Donators, Supporters, or Members, respectively
  - The effect duration is counted only while livestreams are live, and is additive among multiple donations
  - Donators+ receive a rainbow effect
  - Supporters+ receive a wave effect
  - Members receive a particle effect
- Many improvements and additions to the InteractiveScreen
- Added ApiStores for smart caching
- Added a FontEngine in place of the Forge FontRenderer for customising the rendering of text and allow for more options

## v1.16 - The Rank Update [15/8/2022]
- Added a modal for managing standard ranks, which shares its implementation with the punishment modal
- Chat messages now show the user's current highest rank, instead of the default "VIEWER"
- Improved platform separation to show a little YouTube/Twitch logo, instead of changing the rank text
- Improved API error handling

## v1.15 - The Stress Test Update [30/7/2022]
- Extended the capabilities of the vertical layout algorithm in the container element
- Improved cursor mechanics, and added the "click" cursor to more elements when hovering
- Added downward migrations to the config data
  - Makes it easier to jump between a testing and production environment

## v1.14 - The Deployment Update [13/7/2022]
- Added CI for building and testing the project when pushing to GitHub
- Added ChatMate Dashboard (WIP)
  - This will be the one place for administration, configuration, etc
- Added indicators for when the server reports an error or warning
  - Heartbeat indicator behind the status indicator
  - Time series plot in the top right corner
- Added build configurations for easily injecting different sets of environment variables
- Visual bug fixes related to modals

## v1.13 - The Punishment Update [30/5/2022]
- Added fancy modal screens for:
  - Displaying a list of the user's historic punishments
  - Viewing punishment details
  - Creating a new punishment
  - Modifying an active punishment
- New interactive features such as hover animations, tooltips, a checkbox element, centralised z-index and side effect handling, and many bugfixes

## v1.12 - The Twitch Update [1/4/2022]
- Added configuration for identifying platform-specific information
  - The status indicator and viewer count is split into two
  - `VIEWER` ranks are split into `YOUTUBE` and `TWITCH` ranks
- Bug fixes and visual improvements

## v1.11 - The Modal Update [20/3/2022]
- Created a brand new, fully fledged HTML-inspired layout engine
  - Automatic layout calculations
  - Hierarchical rendering and event propagation
  - Beautiful API via abstraction of low-level details
  - Impressive visual debugging capabilities
- The modal for modifying experience now uses the new layout engine
- The Counter and Countdown Title are now their own components
  - Both support translation, and counters support scaling
  - Can be set up via new modals by right clicking the HUD screen and selecting the relevant option from the context menu
- Small visual fixes in chat

## v1.10 - The Emoji Update [5/3/2022]
- Added Custom Emoji rendering in chat
- Visual fixes and improvements

## v1.9 - The Pre-Admin Update [20/2/2022]
- Added a huge amount of fancy in-game chat updates
  - Minecraft now renders a custom GuiChat and GuiNewChat so we can override things
  - PrecisionChatComponentText for exactly determining how a component should be positioned on a chat line
  - ContainerChatComponent for holding references to ChatComponents and custom data
  - In-place pagination of chat. Formatted nicely, includes a close button
- Added a context menu
  - For now, right clicking a user's name will allow the option to bring up their rank, or modify their experience
- Added GUI window for modifying a user's experience
- Added search command for searching for a user. Results are displayed in chat
- Custom cursors that change appearance based on the current context
- API objects now use the standardised PublicObjects and errors

## v1.8 - The Housekeeping Update [12/2/2022]
- Added `/cm ranks` command for displaying the leaderboard
  - If called without arguments, will display the full leaderboard with pagination
  - If called with a name argument, will display only a section of the leaderboard that features the specified channel
- Added a migration framework for the config file
- Various small bug fixes, visual fixes, and code improvements
- Minecraft chat entries from ChatMate are now withheld if a chat GUI is not currently visible
- Better logging

## v1.7 - The Dashboard Update v2 [5/2/2022]
- Added live viewer count to the HUD, which nicely animate changes in value
- Added info messages for level-up events using the new events endpoint
  - Levelling up a multiple of 5 will display a short static message
  - Levelling up a multiple of 20 will display a composite semi-randomly generated message
- Improved implementation of input events
  - More fine-tuned dragging of HUD components
  - Added the ability to zoom HUD components using the mouse wheel
- Mod settings are now persisted locally

## v1.6 - The Dashboard Update [28/1/2022]
- Added HUD overlay and screen
  - Currently displays the status indicator
  - Indicator can be dragged when in the HUD screen (hotkey `y`)
- Improved appearance of the Mod Config screen
- Added experimental MVC architecture for building custom and reusable GUI components

## v1.5 - The Experience Update [8/1/2022]
- Levels are displayed next to the author's name in the same style as Mineplex levels
- Added `/cm counter` command for the charity livestream
- Lots of refactoring

## v1.4 - The Test Update [30/12/2021]
- Added `/cm countdown` command
- Added unit tests
- Fixed chat filter exploits involving Minecraft chat formatting

## v1.3 - The Database Update [16/12/2021]
- Added Config option to disable mod sounds
- Improved API response class structure
- Fixed chat filter exploit using Minecraft formatting

## v1.2 - The Development Update [8/12/2021]
- Added chat-mention colour highlight and sound effect, hardcoded for now for only `Rebel_Guy` and some variants
- Improved filter algorithm to allow for special selectors:
  - `#` to comment out a line
  - `+` to whitelist a word
  - `*` to match any character
  - `[` and `]` (for use at the start/beginning of the filtered word) to match the word only if its start/end corresponds to `[`/`]`

## v1.1 - The Encoding Update [26/11/2021]
- Added colour formatting to Minecraft chat messages
- Added button in main menu to enable/disable the mod
- Added simple message filter
- Emojis and other special unicode characters are now displayed directly if the resource pack supports it
- Fixed encoding issues

## v1.0 [20/11/2021]
- Initial release
- Simple fetching and displaying of chat messages
