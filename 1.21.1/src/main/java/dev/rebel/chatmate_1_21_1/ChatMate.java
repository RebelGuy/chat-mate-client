package dev.rebel.chatmate_1_21_1;

import com.mojang.logging.LogUtils;
import dev.rebel.chatmate_1_21_1.api.publicObjects.chat.PublicChatItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.google.gson.GsonBuilder;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ChatMate.MODID)
public class ChatMate
{
    public String serverUrl = "http://chat-mate-prod.azurewebsites.net";
    public @Nullable Long lastTimestamp = null;

    // Define mod id in a common place for everything to reference
    public static final String MODID = "chatmate";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace

    public ChatMate(FMLJavaModLoadingContext context)
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        this.onLoad();
    }

    public void onLoad()
    {
        new Timer().scheduleAtFixedRate(new TaskWrapper(this::printNextChatItems), 1000L, 1000L);
    }

    public void printNextChatItems() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String since = this.lastTimestamp == null ? "0" : this.lastTimestamp.toString();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + String.format("/api/chat?since=%s&limit=10", since)))
                .header("X-Streamer", "rebel_guy")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("Response Body:");
                System.out.println(responseBody);

                GetChatResponse parsedResponse = new GsonBuilder()
                    .serializeNulls()
                    .create()
                    .fromJson(responseBody, GetChatResponse.class);

                Gui gui = Minecraft.getInstance().gui;
                if (gui == null) {
                    return;
                }

                for (PublicChatItem item : parsedResponse.data.chat) {
                    if (item.messageParts[0].textData != null) {
                        gui.getChat().addMessage(Component.literal(item.messageParts[0].textData.text));
                    }
                }

                this.lastTimestamp = parsedResponse.data.reusableTimestamp;

            } else {
                System.out.println("Request failed with status code: " + response.statusCode());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    public class GetChatResponse extends ApiResponseBase<GetChatResponse.GetChatResponseData> {
        public static class GetChatResponseData {
            public Long reusableTimestamp;
            public PublicChatItem[] chat;
        }
    }

    public abstract class ApiResponseBase<Data> {
        /* The timestamp at which the response was generated. */
        public Long timestamp;

        /* Whether the request was process correctly. */
        public Boolean success;

        public Data data;

        public ApiResponseError error;

        /**
         * Ensures that the base structure of the response follows the expectations.
         */
        public final void assertIntegrity() throws Exception {
            StringJoiner joiner = new StringJoiner(" ");
            if (this.timestamp == null) joiner.add("The response object's `timestamp` property is null.");
            if (this.success == null) joiner.add("The response object's `success` property is null.");
            if (this.success && this.data == null)
                joiner.add("The response object's `data` property is null, but `success` is true.");
            if (!this.success) {
                if (this.error == null) {
                    joiner.add("The response object's `data` property is null, but `success` is true.");
                } else {
                    if (this.error.errorCode == null)
                        joiner.add("The response object's `error!.errorCode` property is null.");
                    if (this.error.errorType == null)
                        joiner.add("The response object's `error!.errorType` property is null.");
                }
            }

            if (joiner.length() > 0) {
                throw new Exception(joiner.toString());
            }
        }

        public static class ApiResponseError {
            public Integer errorCode;
            public String errorType;
            public String message;
        }
    }

    public static class TaskWrapper extends TimerTask {
    private final Runnable runnable;

    public TaskWrapper(Runnable runnable) {
        this.runnable = runnable;
    }

    public void run() {
        this.runnable.run();
    }
}

}
