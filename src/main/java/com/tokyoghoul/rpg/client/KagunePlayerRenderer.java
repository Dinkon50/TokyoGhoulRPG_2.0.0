package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.client.model.KaguneModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class KagunePlayerRenderer {
    private static KaguneModel model;
    private static final ResourceLocation TEXTURE = new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune/rinkaku.png");

    public static void setModel(KaguneModel m) { model = m; }

    @SubscribeEvent
    public static void render(RenderPlayerEvent.Post e) {
        if (model == null) model = new KaguneModel(Minecraft.getInstance().getEntityModels().bakeLayer(ClientSetup.KAGUNE_LAYER));
        Player player = e.getEntity();
        GhoulData.get(player).ifPresent(d -> {
            if (!d.isKaguneActive() || (d.getRace() != GhoulData.Race.GHOUL && d.getRace() != GhoulData.Race.HALF_GHOUL)) return;
            PoseStack pose = e.getPoseStack();
            pose.pushPose();
            pose.translate(0.0, 1.48, 0.20);
            pose.scale(0.78f, 0.78f, 0.78f);
            float walk = (float)Math.sqrt(player.getDeltaMovement().horizontalDistanceSqr());
            model.animate(player.tickCount + e.getPartialTick(), walk, true, player.swingTime > 0, player.isUsingItem(), player.getDeltaMovement().horizontalDistanceSqr() > 0.30);
            MultiBufferSource buffers = e.getMultiBufferSource();
            VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            model.renderToBuffer(pose, vc, e.getPackedLight(), net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);
            pose.popPose();

            // Animated RC aura: lightweight client-side particles orbiting the active Kagune.
            // They are emitted only once every few ticks to avoid particle spam.
            if (player.level().isClientSide && player.tickCount % 3 == 0) {
                float time = player.tickCount + e.getPartialTick();
                float pulse = 0.55f + 0.30f * (float)Math.sin(time * 0.18f);
                float radius = 0.55f + 0.16f * (float)Math.sin(time * 0.11f);
                float phase = time * 0.23f;
                boolean ccg = d.getRace() == GhoulData.Race.CCG;
                // Ghouls: crimson RC aura. Half-ghouls: brighter crimson pulse.
                float red = ccg ? 0.20f : 0.85f;
                float green = ccg ? 0.55f : 0.04f;
                float blue = ccg ? 1.00f : 0.10f;
                for (int i = 0; i < 3; i++) {
                    float a = phase + i * 2.094f;
                    double px = player.getX() + Math.cos(a) * radius;
                    double py = player.getY() + 0.45 + 0.45 * Math.sin(time * 0.16f + i);
                    double pz = player.getZ() + Math.sin(a) * radius;
                    player.level().addParticle(
                        new DustParticleOptions(new Vector3f(red, green, blue), 0.75f + pulse * 0.45f),
                        px, py, pz, 0.0, 0.015 + pulse * 0.01, 0.0
                    );
                }
            }
        });
    }

    private KagunePlayerRenderer() {}
}
