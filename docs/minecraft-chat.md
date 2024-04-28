# Chat component
ChatMate introduces a number of new chat components for handling animations, images, and custom interactions. To add a new component:

- Add the component in the `/gui/chat` folder, inheriting from the `ChatComponentBase` class.
- Add a new conditional to the `CompoenntHelpers.trimComponent` method for handling the new component.
- Add a new conditional to the `ChatComponentRenderer.drawChatComponent` method for rendering the new component.
- Add a new condition to the `CustomGuiNewChat.getChatComponent` method for handling the new component spatially.

# Custom Chat
ChatMate uses a custom implementation of the `GuiNewChat` gui object (this is what renders the chat lines, emojis,
handles `IChatComponents`, manages smooth scrolling, etc). There are a couple of points to note on how this works:

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

# Emojis
Custom ChatMate emojis and public emojis are rendered according to the URL returned by the server. Upon first encountering a new emoji, the Client will download and cache the image to the data folder. Any subsequent requests will then attempt to retrieve the data from the cache, instead of making another download request. This cache system is especially effective for custom emojis due to their immutability - every version of every emoji will never change, and we will never have to invalidate emoji-version caches.

Custom emojis are cached in the `/custom-emoji` folder within the Mod's data folder, while public emojis are cached in the `/emoji` folder.

Internally, emojis are rendered in chat using the `ResolvableTexture` class. This class uses a placeholder to reserve space in the chat window, and asynchronously loads the required data from the web or file system. If multiple requests are fired off at the same time, all sets of read/write operations for a given custom emoji-version or public emoji are done in series (using a queue system), multiple sets of operations for different entities are done in parallel. The logic for this is implemented in the `PersistentCacheService`.