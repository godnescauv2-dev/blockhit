package com.seudominio.blockhitmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class BlockHitListener {

    private final Minecraft mc = Minecraft.getMinecraft();

    // =========================================================
    // CONFIGURAÇÕES
    // =========================================================

    // Distância máxima para considerar o alvo
    private static final double RANGE = 4.5D;

    // Tempo segurando block antes de dar o hit
    // 50 ms = aproximadamente 1 tick
    private static final long BLOCK_DURATION = 60L;

    // Tempo que o W fica solto durante o W-Tap
    private static final long WTAP_DURATION = 40L;

    // true  = W-Tap no primeiro hit
    // false = começa sem W-Tap
    private static final boolean START_WITH_WTAP = true;

    // =========================================================
    // ESTADO DO BLOCKHIT
    // =========================================================

    private boolean blocking = false;
    private long blockStartTime = 0L;

    private boolean attackHappened = false;

    private Entity target = null;

    // =========================================================
    // ESTADO DO W-TAP
    // =========================================================

    private boolean wTapping = false;
    private long wTapStartTime = 0L;

    // Alternância:
    // true  = próximo hit recebe W-Tap
    // false = próximo hit não recebe W-Tap
    private boolean nextHitWTap = START_WITH_WTAP;

    // =========================================================
    // TICK
    // =========================================================

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END)
            return;

        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        EntityPlayerSP player = mc.thePlayer;

        // =====================================================
        // PROCESSA W-TAP
        // =====================================================

        if (wTapping) {

            long elapsed =
                    System.currentTimeMillis() - wTapStartTime;

            if (elapsed >= WTAP_DURATION) {

                /*
                 * Só pressiona W novamente se o jogador
                 * ainda estiver segurando W fisicamente.
                 */
                if (mc.gameSettings.keyBindForward.isKeyDown()) {

                    KeyBinding.setKeyBindState(
                            mc.gameSettings.keyBindForward.getKeyCode(),
                            true
                    );
                }

                wTapping = false;
            }

            /*
             * Enquanto o W-Tap está acontecendo,
             * não inicia outro BlockHit.
             */
            return;
        }

        // =====================================================
        // ESTADO DAS TECLAS
        // =====================================================

        boolean attackKeyDown =
                mc.gameSettings.keyBindAttack.isKeyDown();

        // =====================================================
        // DETECTA ALVO
        // =====================================================

        updateTarget(player);

        // =====================================================
        // SEM ATAQUE / SEM ALVO
        // =====================================================

        if (!attackKeyDown || target == null) {

            stopBlocking();

            /*
             * Quando o botão de ataque é solto,
             * libera o próximo ciclo.
             */
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
        // VERIFICA TEMPO DO BLOCK
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
    // ATUALIZA O ALVO
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
    // COMEÇA BLOCK
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
    // PARA BLOCK
    // =========================================================

    private void stopBlocking() {

        if (!blocking)
            return;

        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindUseItem.getKeyCode(),
                false
        );

        blocking = false;
    }

    // =========================================================
    // EXECUTA HIT
    // =========================================================

    private void executeHit(EntityPlayerSP player) {

        // Primeiro solta o block
        stopBlocking();

        // Atualiza o alvo antes do ataque
        updateTarget(player);

        if (target == null)
            return;

        /*
         * Guarda se este hit deve receber W-Tap
         * ANTES de alternar para o próximo.
         */
        boolean doWTap = nextHitWTap;

        // =====================================================
        // HIT
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
        // ALTERNÂNCIA 1 SIM / 1 NÃO
        // =====================================================

        nextHitWTap = !nextHitWTap;
    }

    // =========================================================
    // W-TAP
    // =========================================================

    private void performWTap(EntityPlayerSP player) {

        /*
         * Reset do sprint diretamente no jogador.
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
         * Inicia o intervalo do W-Tap.
         */
        wTapping = true;

        wTapStartTime =
                System.currentTimeMillis();
    }
}
