package cc.silk.module.modules.combat;

import cc.silk.event.impl.player.AttackEvent;
import cc.silk.module.Category;
import cc.silk.module.Module;
import cc.silk.module.setting.ModeSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Watchdog Old", "Mospixel");

    public Criticals() {
        super("Criticals", "Makes you hit every crit (BLATANT)", -1, Category.COMBAT);
        this.addSetting(mode);
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        if (isNull()) return;
        boolean willCritLegit = mc.player.fallDistance > 0.0F && !mc.player.isOnGround() && !mc.player.isClimbing() && !mc.player.isTouchingWater() && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && !mc.player.hasVehicle() && e.getTarget() instanceof LivingEntity;
        if (willCritLegit) return;

        switch (mode.getMode()) {
            case "Vanilla" -> {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getPos().x, mc.player.getPos().y + 0.2, mc.player.getPos().z, false, false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getPos().x, mc.player.getPos().y + 0.1, mc.player.getPos().z, false, false));
            }
            case "Watchdog Old" -> {
                if (mc.player.isOnGround()) {
                    mc.player.setPosition(mc.player.getX(), mc.player.getY() + 0.001D, mc.player.getZ());
                }
            }
            case "Mospixel" -> {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getPos().x, mc.player.getPos().y + 0.000000271875, mc.player.getPos().z, false, false));
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getPos().x, mc.player.getPos().y + 0., mc.player.getPos().z, false, false));
            }
        }
    }
}
