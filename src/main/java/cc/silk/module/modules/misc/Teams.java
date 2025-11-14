package cc.silk.module.modules.misc;

import cc.silk.SilkClient;
import cc.silk.module.Category;
import cc.silk.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Teams extends Module {

    public Teams() {
        super("Teams", "Stops you from targeting teammates", Category.MISC);
    }

    public static boolean isTeammate(Entity entity) {
        Teams teamsModule = SilkClient.INSTANCE.getModuleManager().getModule(Teams.class).get();
        if (!teamsModule.isEnabled()) {
            return false;
        }

        if (entity == null || entity.getName() == null || !(entity instanceof LivingEntity)) {
            return false;
        }

        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.player.getScoreboardTeam() == null) {
                return false;
            }

            return mc.player.isTeammate(entity);
        } catch (IllegalStateException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
