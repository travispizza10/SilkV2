package cc.silk;

import cc.silk.module.ModuleManager;
import cc.silk.module.events.MouseModuleHandler;
import cc.silk.profiles.ProfileManager;
import cc.silk.utils.jvm.ModMenuHider;
import cc.silk.utils.notification.NotificationManager;
import cc.silk.utils.render.font.FontManager;
import io.github.racoondog.norbit.EventBus;
import lombok.Getter;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@Getter
public final class SilkClient implements ClientModInitializer {
    public static final String CLIENT_VERSION = "v1.0";
    public static final boolean shouldUseMouseEvent = System.getProperty("os.name").toLowerCase().contains("windows");
    public static SilkClient INSTANCE;
    public static MinecraftClient mc;
    public final IEventBus SilkEventBus;
    public final ModuleManager moduleManager;
    public final FontManager fontManager;
    public final ProfileManager profileManager;
    public final MouseModuleHandler mouseModuleHandler;
    public final NotificationManager notificationManager;
    private final Logger logger = LoggerFactory.getLogger("Silk");

    public SilkClient() {
        INSTANCE = this;
        mc = MinecraftClient.getInstance();
        SilkEventBus = EventBus.threadSafe();
        SilkEventBus.registerLambdaFactory("cc.silk", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        this.moduleManager = new ModuleManager();
        this.fontManager = new FontManager();
        this.profileManager = new ProfileManager();
        this.mouseModuleHandler = new MouseModuleHandler();
        this.notificationManager = NotificationManager.getInstance();

        SilkEventBus.subscribe(mouseModuleHandler);
        SilkEventBus.subscribe(notificationManager);
        new Thread(() -> {
            try {
                ModMenuHider.hideFromModMenu();
                Thread.sleep(1000);
                ModMenuHider.hideFromModMenu();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void onInitializeClient() {
        // Double initialization prevention, it's already initializing in the constructor
    }
}