package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tokyoghoul.rpg.TokyoGhoulRPG;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.entity.ModEntities;
import com.tokyoghoul.rpg.network.AbilityPacket;
import com.tokyoghoul.rpg.network.KagunePacket;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.RagePacket;
import com.tokyoghoul.rpg.network.GrapplePacket;
import com.tokyoghoul.rpg.screen.ProgressionScreen;
import com.tokyoghoul.rpg.client.model.KaguneModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientSetup {
    public static KeyMapping KAGUNE, PROGRESSION, RAGE, ABILITY_ONE, ABILITY_TWO, ABILITY_THREE;
    public static final ModelLayerLocation KAGUNE_LAYER = new ModelLayerLocation(new ResourceLocation(TokyoGhoulRPG.MODID, "kagune"), "main");

    public static void init(){
        var modBus=net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ClientSetup::renderers);
        modBus.addListener(ClientSetup::layers);
        modBus.addListener(ClientSetup::keys);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ClientSetup.class);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(KagunePlayerRenderer.class);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e){
        e.registerEntityRenderer(ModEntities.GHOUL_NPC.get(),
            ctx -> new SimpleHumanoidRenderer<>(ctx, new ResourceLocation("minecraft","textures/entity/player/wide/steve.png")));
        e.registerEntityRenderer(ModEntities.CCG_NPC.get(),
            ctx -> new SimpleHumanoidRenderer<>(ctx, new ResourceLocation("minecraft","textures/entity/player/wide/alex.png")));
    }


    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){
        e.registerLayerDefinition(KAGUNE_LAYER, KaguneModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e){
        KAGUNE=new KeyMapping("key.tokyoghoulrpg.kagune",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_R,"key.categories.tokyoghoulrpg");
        PROGRESSION=new KeyMapping("key.tokyoghoulrpg.progression",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_K,"key.categories.tokyoghoulrpg");
        RAGE=new KeyMapping("key.tokyoghoulrpg.rage",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_U,"key.categories.tokyoghoulrpg");
        ABILITY_ONE=new KeyMapping("key.tokyoghoulrpg.ability_one",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_Q,"key.categories.tokyoghoulrpg");
        ABILITY_TWO=new KeyMapping("key.tokyoghoulrpg.ability_two",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_E,"key.categories.tokyoghoulrpg");
        ABILITY_THREE=new KeyMapping("key.tokyoghoulrpg.ability_three",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_F,"key.categories.tokyoghoulrpg");
        e.register(KAGUNE); e.register(PROGRESSION); e.register(RAGE);
        e.register(ABILITY_ONE); e.register(ABILITY_TWO); e.register(ABILITY_THREE);
    }

    @SubscribeEvent
    public static void input(InputEvent.Key e){
        if(e.getAction()!=GLFW.GLFW_PRESS) return;
        if(KAGUNE!=null&&KAGUNE.matches(e.getKey(),e.getScanCode())) NetworkHandler.CHANNEL.sendToServer(new KagunePacket());
        if(PROGRESSION!=null&&PROGRESSION.matches(e.getKey(),e.getScanCode())) Minecraft.getInstance().setScreen(new ProgressionScreen());
        if(RAGE!=null&&RAGE.matches(e.getKey(),e.getScanCode())) NetworkHandler.CHANNEL.sendToServer(new RagePacket());
        if(ABILITY_ONE!=null&&ABILITY_ONE.matches(e.getKey(),e.getScanCode())) NetworkHandler.CHANNEL.sendToServer(new AbilityPacket(0));
        if(ABILITY_TWO!=null&&ABILITY_TWO.matches(e.getKey(),e.getScanCode())) NetworkHandler.CHANNEL.sendToServer(new GrapplePacket());
        if(ABILITY_THREE!=null&&ABILITY_THREE.matches(e.getKey(),e.getScanCode())) NetworkHandler.CHANNEL.sendToServer(new AbilityPacket(2));
    }

    private static float smoothHealth = -1f;
    private static float smoothHunger = -1f;
    private static float smoothRage = -1f;

    @SubscribeEvent
    public static void overlay(RenderGuiOverlayEvent.Post e){
        if(e.getOverlay()!=VanillaGuiOverlay.HOTBAR.type()) return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null) return;

        GuiGraphics g=e.getGuiGraphics();
        int screenW=mc.getWindow().getGuiScaledWidth();
        int screenH=mc.getWindow().getGuiScaledHeight();
        float frame=mc.getFrameTime();
        float health=Math.max(0f,Math.min(1f,mc.player.getHealth()/mc.player.getMaxHealth()));
        float hunger=Math.max(0f,Math.min(1f,ClientGhoulData.hunger()/100f));
        float rage=Math.max(0f,Math.min(1f,ClientGhoulData.rage()/100f));
        if(smoothHealth<0f){ smoothHealth=health; smoothHunger=hunger; smoothRage=rage; }
        float lerp=Math.min(1f,0.12f+frame*0.035f);
        smoothHealth += (health-smoothHealth)*lerp;
        smoothHunger += (hunger-smoothHunger)*lerp;
        smoothRage += (rage-smoothRage)*lerp;

        final int x=14, width=214, barH=11;
        float t=(mc.level==null?0f:(float)(mc.level.getGameTime()+frame));
        float pulse=0.5f+0.5f*(float)Math.sin(t*0.16f);

        drawPanel(g,x-6,8,width+12,104,0xB5090A0F);
        drawBar(g,x,24,width,barH,smoothHealth,0xFFB5162A,0xFFFF5366,"ЗДОРОВЬЕ",health<0.25f, t);
        drawBar(g,x,50,width,barH,smoothRage,ClientGhoulData.race()==GhoulData.Race.CCG?0xFF17649D:0xFF7E1021,ClientGhoulData.race()==GhoulData.Race.CCG?0xFF55B8FF:0xFFFF304C,
            ClientGhoulData.race()==GhoulData.Race.CCG?"БОЕВОЙ ДУХ":"ЯРОСТЬ",ClientGhoulData.rage()<20 && !ClientGhoulData.rageActive(), t);
        drawBar(g,x,76,width,barH,smoothHunger,0xFF6C4217,0xFFFFB84A,"ГОЛОД",hunger<0.2f, t);

        int level=ClientGhoulData.level();
        int badgeX=x+width-62;
        g.fill(badgeX-2,2,badgeX+64,19,0xFF07080C);
        g.fill(badgeX,4,badgeX+62,17,0xFF241118);
        g.fill(badgeX,4,badgeX+2,17,0xFFE13D51);
        g.drawString(mc.font,"LVL "+level,badgeX+8,7,0xFFF3D9DD,true);
        g.drawString(mc.font,"SP "+ClientGhoulData.points(),badgeX+38,7,0xFFFF8A98,false);

        String race=switch(ClientGhoulData.race()){
            case GHOUL->"ГУЛЬ";
            case HALF_GHOUL->"ПОЛУГУЛЬ";
            case CCG->"АГЕНТ CCG";
            default->"ЧЕЛОВЕК";
        };
        int raceW=mc.font.width(race)+28;
        int rx=screenW-raceW-12, ry=screenH-34;
        g.fill(rx-2,ry-2,screenW-10,ry+18,0xC5090A0F);
        g.fill(rx,ry,rx+3,ry+16,ClientGhoulData.race()==GhoulData.Race.CCG?0xFF55B8FF:0xFFE13D51);
        g.drawString(mc.font,race,rx+10,ry+4,0xFFF4F4F7,true);

        if(ClientGhoulData.rageActive()){
            int rw=(int)(8+8*pulse);
            g.fill(x-3,47,x+width+3,65,0x553F0A12);
            g.fill(x-3,47,x+rw,65,0xAAFF304C);
        }
        if(ClientGhoulData.bleedingTicks()>0){
            int by=screenH-64;
            int bw=(int)(170*Math.max(0,Math.min(1,ClientGhoulData.bleedingTicks()/(30f*20f))));
            g.fill(14,by-2,188,by+11,0xCC09090D);
            g.fill(16,by,16+bw,by+8,0xFFB01828);
            g.drawString(mc.font,"КРОВОТЕЧЕНИЕ  "+(ClientGhoulData.bleedingTicks()/20+1)+"с",16,by-14,0xFFFF6978,true);
        }
    }

    private static void drawPanel(GuiGraphics g,int x,int y,int w,int h,int color){
        g.fill(x,y,x+w,y+h,color);
        g.fill(x,y,x+w,y+1,0xFF2A2B33);
        g.fill(x,y+h-1,x+w,y+h,0xFF15161C);
    }

    private static void drawBar(GuiGraphics g,int x,int y,int w,int h,float value,int base,int bright,String label,boolean danger,float t){
        g.fill(x-2,y-2,x+w+2,y+h+2,0xFF050509);
        g.fill(x,y,x+w,y+h,0xFF24252B);
        int fill=(int)(w*Math.max(0f,Math.min(1f,value)));
        if(fill>0){
            g.fill(x,y,x+fill,y+h,base);
            int sheenX=x+(int)((t*1.6f)%(w+32))-16;
            int sx1=Math.max(x,sheenX), sx2=Math.min(x+fill,sheenX+18);
            if(sx2>sx1) g.fill(sx1,y,sx2,y+h,0x44FFFFFF);
        }
        if(danger){
            int flash=(int)(70+70*(0.5f+0.5f*(float)Math.sin(t*0.35f)));
            g.fill(x,y,x+w,y+h,(flash<<24)|0xFF2020);
        }
        g.drawString(Minecraft.getInstance().font,label,x,y-12,0xFFF0E6E8,true);
        g.drawString(Minecraft.getInstance().font,Math.round(value*100)+"%",x+w-28,y-12,bright,false);
    }

    private ClientSetup(){}
}
