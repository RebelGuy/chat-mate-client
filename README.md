The ChatOverlay mod uses the locally run `chat-overlay-server` as an interface to get near real-time YouTube livestream chat messages. Messages are displayed in Minecraft chat.

# Project Details

This project was initialised using the `ForgeTemplate` repository for 1.8.9. Once IntelliJ had set up the project workspace, the command `gradlew genIIntellijRRuns` was run for generating the Gradle tasks.

To build the project, use `gradlew build`.

Build output: `chat-overlay-client/build/libs/*.jar`.

Debug partial .minecraft folder: `chat-overlay-client/run/` 

# Change Log

## v1.1
- Added colour formatting to Minecraft chat messages
- Added button in main menu to enable/disable the mod
- Added simple message filter
- Emojis and other special unicode characters are now displayed directly if the resource pack supports it
- Fixed encoding issues

## v1.0
- Initial release
- Simple fetching and displaying of chat messages
