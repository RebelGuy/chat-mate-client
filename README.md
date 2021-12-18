The ChatMate mod uses the locally run `chat-mate-server` as an interface to get near real-time YouTube livestream chat messages. Messages are displayed in Minecraft chat.

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

 **Once IntelliJ had set up the project workspace**, the command  `gradlew setupDecompWorkspace` for setting up the environment (which includes a decompilation of Minecraft - if this is not desired, use `gradlew setupDevWorkspace` instead). Next, `gradlew genIntellijRuns` was run for generating the Gradle tasks. Note that the Gradle Java version was manually set in the `gradle.properties` file by adding the line `java.import.gradle.java.home=C:/Program Files/Eclipse Adoptium/jdk-8.0.312.7-hotspot` (the same folder as the default Project SDK picked in the step above). Furthermore, the `gradle-wrapper.properties` distribution URL was set to `distributionUrl=https\://services.gradle.org/distributions/gradle-6.8.3-all.zip`.

If developing in VSCode, may need to first open the project in IntelliJ to generate all required files. Install the `Extension Pack for Java` and `Gradle for Java` extensions and make sure the 1.8/Java 8 folder is set in the `org.eclipse.buildship.core.prefs` file under `java.home`.

## File Encoding
In order to allow characters such as `'§'`, Java files must be encoding using UTF8, and the Java VM must be told of this.
- In the IntelliJ settings, set global and project encodings to UTF8
- In the system environment variables, add `JAVA_TOOL_OPTIONS` with value `-Dfile.encoding=UTF8` (see [explanation](https://stackoverflow.com/questions/361975/setting-the-default-java-character-encoding))

## Building
To build the project, use `gradlew build`.

Build output: `chat-mate-client/build/libs/*.jar`.

Debug partial .minecraft folder: `chat-mate-client/run/` 

# Change Log
## v1.3 - The Database Update
- Added Config option to disable mod sounds
- Improved API response class structure
- Fixed chat filter exploit using Minecraft formatting

## v1.2 - The Development Update
- Added chat-mention colour highlight and sound effect, hardcoded for now for only `Rebel_Guy` and some variants
- Improved filter algorithm to allow for special selectors:
  - `#` to comment out a line
  - `+` to whitelist a word
  - `*` to match any character
  - `[` and `]` (for use at the start/beginning of the filtered word) to match the word only if its start/end corresponds to `[`/`]`

## v1.1 - The Encoding Update
- Added colour formatting to Minecraft chat messages
- Added button in main menu to enable/disable the mod
- Added simple message filter
- Emojis and other special unicode characters are now displayed directly if the resource pack supports it
- Fixed encoding issues

## v1.0
- Initial release
- Simple fetching and displaying of chat messages
