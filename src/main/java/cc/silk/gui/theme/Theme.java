package cc.silk.gui.theme;

import java.awt.*;

/**
 * Simple container for GUI theme colors. Add more fields as needed.
 */
public record Theme(String name, Color backgroundTop, Color backgroundBottom, Color containerBg, Color headerBg,
                    Color sidebarBg, Color panelBg, Color panelAltBg, Color accent, Color text, Color muted) {
}
