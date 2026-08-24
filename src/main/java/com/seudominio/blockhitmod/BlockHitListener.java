package com.seudominio.blockhitmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class BlockHitListener {

    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean blocking = false;
    private long blockStartTime = 0;
    private boolean attackHappened = false;
    private Entity target = null;

    private final double range = 4.5;
    private final long blockDuration = 60;
    private final boolean modePRE = true;

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        boolean attackKeyDown = mc.gameSettings.keyBindAttack.isKeyDown();

        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null) {
            Entity pointed = mc.objectMouseOver.entityHit;
            target = pointed.getDistanceToEntity(mc.thePlayer) <= range ? pointed : null;
        } else {
            target = null;
        }

        if (attackKeyDown && target != null) {
            if (!blocking && !attackHappened) {
                KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindUseItem.getKeyCode(),
                    true
                );

                blocking = true;
                blockStartTime = System.currentTimeMillis();
            }
        } else {
            if (blocking) {
                KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindUseItem.getKeyCode(),
                    false
                );

                blocking = false;
            }

            attackHappened = false;
        }

        if (blocking && System.currentTimeMillis() - blockStartTime >= blockDuration) {
            KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindUseItem.getKeyCode(),
                false
            );

            blocking = false;

            if (target != null && attackKeyDown) {
                mc.thePlayer.attackTargetEntityWithCurrentItem(target);
            }
        }
    }
}
