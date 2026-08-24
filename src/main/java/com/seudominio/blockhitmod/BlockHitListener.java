package com.seudominio.blockhitmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class BlockHitListener {

    private final Minecraft mc = Minecraft.getMinecraft();

    // =========================================================
    // CONFIGURAÇÕES
    // =========================================================

    // Alcance máximo usado apenas para validar o alvo
    private static final double RANGE = 4.5D;

    // Tempo segurando o block antes do ataque
    private static final long BLOCK_DURATION = 60L;

    // Tempo que o W fica solto durante o W-Tap
    private static final long WTAP_DURATION = 40L;

    // true  = primeiro hit recebe W-Tap
    // false = primeiro hit não recebe W-Tap
    private static final boolean START_WITH_WTAP = true;

    // =========================================================
    // BLOCKHIT
    // =========================================================

    private boolean blocking = false;
    private long blockStartTime = 0L;

    private boolean attackHappened = false;

    private Entity target = null;

    // =========================================================
    // W-TAP
    // =========================================================

    private boolean wTapping = false;
    private long wTapStartTime = 0L;

    // 1 sim / 1 não
    private boolean nextHitWTap = START_WITH_WTAP;

    // =========================================================
    // TICK
    // =========================================================

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;

        // =====================================================
        // W-TAP EM ANDAMENTO
        // =====================================================

        if (wTapping) {

            long elapsed =
                    System.currentTimeMillis() - wTapStartTime;

            if (elapsed >= WTAP_DURATION) {

                /*
                 * Só restaura W se o jogador ainda estiver
                 * segurando a tecla fisicamente.
                 */
                if (mc.gameSettings.keyBindForward.isKeyDown()) {

                    KeyBinding.setKeyBindState(
                            mc.gameSettings.keyBindForward.getKeyCode(),
                            true
                    );
                }

                wTapping = false;
            }

            return;
        }

        // =====================================================
        // ESTADO DO ATAQUE
        // =====================================================

        boolean attackKeyDown =
                mc.gameSettings.keyBindAttack.isKeyDown();

        // =====================================================
        // ATUALIZA ALVO
        // =====================================================

        updateTarget(player);

        // =====================================================
        // SEM ATAQUE OU SEM ALVO
        // =====================================================

        if (!attackKeyDown || target == null) {

            stopBlocking();

            if (!attackKeyDown) {
                attackHappened = false;
            }

            return;
        }

        // =====================================================
        // INICIA BLOCK
        // =====================================================

        if (!blocking && !attackHappened) {

            startBlocking();

            return;
        }

        // =====================================================
        // VERIFICA DURAÇÃO DO BLOCK
        // =====================================================

        if (blocking) {

            long elapsed =
                    System.currentTimeMillis() - blockStartTime;

            if (elapsed >= BLOCK_DURATION) {

                executeHit(player);
            }
        }
    }

    // =========================================================
    // DETECTA ALVO
    // =========================================================

    private void updateTarget(EntityPlayerSP player) {

        if (mc.objectMouseOver == null) {
            target = null;
            return;
        }

        if (mc.objectMouseOver.entityHit == null) {
            target = null;
            return;
        }

        Entity pointed =
                mc.objectMouseOver.entityHit;

        if (pointed == player) {
            target = null;
            return;
        }

        if (pointed.getDistanceToEntity(player) <= RANGE) {
            target = pointed;
        } else {
            target = null;
        }
    }

    // =========================================================
    // COMEÇA O BLOCK
    // =========================================================

    private void startBlocking() {

        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindUseItem.getKeyCode(),
                true
        );

        blocking = true;

        blockStartTime =
                System.currentTimeMillis();
    }

    // =========================================================
    // PARA O BLOCK
    // =========================================================

    private void stopBlocking() {

        if (!blocking) {
            return;
        }

        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindUseItem.getKeyCode(),
                false
        );

        blocking = false;
    }

    // =========================================================
    // EXECUTA O HIT
    // =========================================================

    private void executeHit(EntityPlayerSP player) {

        // Primeiro solta o block
        stopBlocking();

        // Atualiza o alvo novamente
        updateTarget(player);

        if (target == null) {
            return;
        }

        // Guarda o estado atual do W-Tap
        boolean doWTap = nextHitWTap;

        // =====================================================
        // ATAQUE
        // =====================================================

        player.attackTargetEntityWithCurrentItem(target);

        attackHappened = true;

        // =====================================================
        // W-TAP
        // =====================================================

        if (doWTap) {

            performWTap(player);
        }

        // =====================================================
        // ALTERNÂNCIA
        // =====================================================

        nextHitWTap = !nextHitWTap;
    }

    // =========================================================
    // W-TAP
    // =========================================================

    private void performWTap(EntityPlayerSP player) {

        /*
         * Reseta o sprint do jogador.
         */
        player.setSprinting(false);

        /*
         * Solta W.
         */
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindForward.getKeyCode(),
                false
        );

        /*
         * Começa o intervalo do W-Tap.
         */
        wTapping = true;

        wTapStartTime =
                System.currentTimeMillis();
    }
}
