package cc.silk.module.modules.misc;

import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.gui.FriendsScreen;

public class Friends extends Module {

    public Friends() {
        super("Friends", "Manage your friends list", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.setScreen(new FriendsScreen());
        }
        setEnabled(false);
    }
}
