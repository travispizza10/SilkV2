package cc.silk.utils.mc;

import cc.silk.SilkClient;
import cc.silk.utils.IMinecraft;
import cc.silk.utils.other.StringUtils;
import cc.silk.utils.render.ColorUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.Objects;

@UtilityClass
public final class ChatUtil implements IMinecraft {

    private static final MutableText BRACKET_COLOR = Text.empty().setStyle(Style.EMPTY.withFormatting(Formatting.GRAY));

    public static void infoChatMessage(final String message) {
        ChatUtil.infoChatMessage(Text.literal(message));
    }

    public static void infoChatMessage(final Text message) {
        ChatUtil.chatMessage(message.copy().withColor(Type.INFO.color()), true);
    }

    public static void warningChatMessage(final String message) {
        ChatUtil.warningChatMessage(Text.literal(message));
    }

    public static void warningChatMessage(final Text message) {
        if (Objects.isNull(mc.inGameHud)) {
            SilkClient.INSTANCE.getLogger().warn(message.getString());
            return;
        }
        ChatUtil.chatMessage(message.copy().withColor(Type.WARNING.color()), true);
    }

    public static void errorChatMessage(final String message) {
        ChatUtil.errorChatMessage(Text.literal(message));
    }

    public static void errorChatMessage(final Text message) {
        if (Objects.isNull(mc.inGameHud)) {
            SilkClient.INSTANCE.getLogger().error(message.getString());
            return;
        }
        ChatUtil.chatMessage(message.copy().withColor(Type.ERROR.color()), true);
    }


    public static void emptyChatMessage(final boolean prefix) {
        ChatUtil.chatMessage(Text.literal(" "), prefix);
    }

    public static void chatMessage(final String message) {
        ChatUtil.chatMessage(Text.literal(message));
    }

    public static void chatMessage(final MutableText message) {
        ChatUtil.chatMessage(message, true);
    }

    public static void chatMessage(final String message, final boolean prefix) {
        ChatUtil.chatMessage(Text.literal(message), prefix);
    }

    public static void chatMessage(final MutableText message, final boolean prefix) {
        if (Objects.isNull(mc.inGameHud)) {
            SilkClient.INSTANCE.getLogger().info(message.getString());
            return;
        }

        final MutableText text = prefix ? ChatUtil.chatPrefix().copy().append(message) : message;
        mc.inGameHud.getChatHud().addMessage(text);
    }

    public static void addChatMessage(String text) {
        if (mc.player == null || mc.world == null || Objects.isNull(mc.inGameHud) || mc.inGameHud.getChatHud() == null) {
            SilkClient.INSTANCE.getLogger().info("[Silk] " + text);
            return;
        }
        mc.inGameHud.getChatHud().addMessage(Text.of(text));
    }

    public static MutableText colorFade(final String text, final Style style, final Color startColor, final Color endColor) {
        final MutableText mutableText = Text.empty();

        for (int i = 0; i < text.length(); i++) {
            final float percent = (float) i / (text.length() - 1);
            final Color color = ColorUtils.colorInterpolate(startColor, endColor, percent);

            mutableText.append(Text.literal(String.valueOf(text.charAt(i))).setStyle(style.withColor(TextColor.fromRgb(color.getRGB()))));
        }

        return mutableText;
    }

    public static MutableText chatPrefix() {
        return BRACKET_COLOR.copy()
                .append("[")
                .append(ChatUtil.colorFade("Silk", Style.EMPTY, new Color(0, 191, 255), new Color(0, 255, 127)))
                .append("]")
                .append(" ");
    }

    public enum Type {
        INFO(Color.GREEN),
        WARNING(Color.ORANGE),
        ERROR(Color.RED);

        private final int color;
        private final String name;

        Type(final Color color) {
            this.color = color.getRGB();
            this.name = StringUtils.normalizeEnumName(this.name());
        }

        public int color() {
            return this.color;
        }

        public String display() {
            return this.name;
        }
    }
}
