package com.tokyoghoul.rpg.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.world.entity.player.Player;

public class KaguneModel extends EntityModel<Player> {
    public static final String LAYER = "kagune";
    private final ModelPart root;
    private final ModelPart[] a = new ModelPart[4];
    private final ModelPart[] b = new ModelPart[4];
    private final ModelPart[] c = new ModelPart[4];

    public KaguneModel(ModelPart root) {
        this.root = root;
        for (int i = 0; i < 4; i++) {
            a[i] = root.getChild("a" + i);
            b[i] = a[i].getChild("b" + i);
            c[i] = b[i].getChild("c" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int i = 0; i < 4; i++) {
            float side = i < 2 ? -1.0f : 1.0f;
            float y = i % 2 == 0 ? 0.5f : -0.5f;
            PartDefinition pa = root.addOrReplaceChild("a" + i,
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.6f, -1.6f, -1.6f, 3.2f, 7.0f, 3.2f, new CubeDeformation(0.25f)),
                PartPose.offset(3.1f * side, 0.0f + y, 2.7f));
            PartDefinition pb = pa.addOrReplaceChild("b" + i,
                CubeListBuilder.create().texOffs(0, 12).addBox(-1.35f, -1.35f, -1.35f, 2.7f, 7.0f, 2.7f, new CubeDeformation(0.18f)),
                PartPose.offset(0, 6.0f, 0));
            pb.addOrReplaceChild("c" + i,
                CubeListBuilder.create().texOffs(0, 24).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.1f)),
                PartPose.offset(0, 5.8f, 0));
        }
        return LayerDefinition.create(mesh, 32, 32);
    }

    public void animate(float time, float speed, boolean active, boolean attacking, boolean blocking, boolean dashing) {
        float idle = (float)Math.sin(time * 0.075f) * 0.10f;
        float motion = Math.min(1.0f, speed * 0.7f);
        for (int i = 0; i < 4; i++) {
            float side = i < 2 ? -1 : 1;
            float phase = i * 0.9f;
            float wave = (float)Math.sin(time * 0.13f + phase) * (0.20f + motion * 0.22f);
            float secondary = (float)Math.sin(time * 0.21f + phase * 1.7f) * 0.08f;
            a[i].xRot = -0.28f + wave + idle;
            a[i].yRot = side * (0.22f + (float)Math.sin(time * 0.09f + phase) * 0.12f);
            a[i].zRot = side * (0.18f + wave * 0.35f);
            b[i].xRot = 0.34f + wave * 1.25f;
            b[i].yRot = side * wave * 0.8f;
            b[i].zRot = side * wave * 0.5f;
            c[i].xRot = 0.30f + wave * 1.7f;
            c[i].yRot = side * wave * 1.25f;
            c[i].zRot = side * (wave * 0.8f + secondary);
            if (attacking) {
                float snap = 0.35f + 0.10f * (float)Math.sin(time * 0.65f + phase);
                a[i].yRot += side * (0.48f + snap); b[i].yRot += side * (0.82f + snap); c[i].yRot += side * (1.10f + snap);
                a[i].xRot -= 0.22f; b[i].xRot -= 0.34f; c[i].xRot -= 0.18f;
            }
            if (blocking) {
                a[i].xRot -= 0.42f; a[i].zRot *= 0.35f; b[i].zRot *= 0.25f; c[i].zRot *= 0.20f;
            }
            if (dashing) {
                float dashWave = 0.18f * (float)Math.sin(time * 0.45f + phase);
                a[i].xRot -= 0.65f + dashWave; b[i].xRot -= 0.85f + dashWave; c[i].xRot -= 0.95f + dashWave;
                a[i].zRot += side * 0.20f; b[i].zRot += side * 0.30f; c[i].zRot += side * 0.38f;
            }
            if (!active) {
                a[i].xRot *= 0.22f; b[i].xRot *= 0.18f; c[i].xRot *= 0.12f;
                a[i].yRot *= 0.25f; b[i].yRot *= 0.20f; c[i].yRot *= 0.15f;
            }
        }
    }

    @Override public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        animate(ageInTicks, limbSwingAmount, true, entity.swingTime > 0, entity.isUsingItem(), entity.getDeltaMovement().horizontalDistanceSqr() > 0.30);
    }

    @Override public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer vc,
                                         int light, int overlay, float r, float g, float b, float alpha) {
        root.render(pose, vc, light, overlay, r, g, b, alpha);
    }
}
