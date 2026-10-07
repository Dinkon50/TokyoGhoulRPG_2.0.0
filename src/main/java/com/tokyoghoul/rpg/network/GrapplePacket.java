package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record GrapplePacket(){
    public static void encode(GrapplePacket p,FriendlyByteBuf b){}
    public static GrapplePacket decode(FriendlyByteBuf b){return new GrapplePacket();}
    public static void handle(GrapplePacket p,Supplier<NetworkEvent.Context> c){
        c.get().enqueueWork(() -> {
            Player s=c.get().getSender(); if(s==null) return;
            GhoulData.get(s).ifPresent(d -> {
                if((d.getRace()!=GhoulData.Race.GHOUL && d.getRace()!=GhoulData.Race.HALF_GHOUL) || !d.isKaguneActive()) return;
                if(d.getSpeed() < 2 || d.getAbilityCooldown() > 0) return;
                HitResult hit=s.pick(22.0D,0.0F,false);
                if(hit.getType()!=HitResult.Type.BLOCK) return;
                Vec3 target=((BlockHitResult)hit).getLocation().add(0,0.55,0);
                Vec3 delta=target.subtract(s.position());
                double len=delta.length();
                if(len<2.0 || len>22.0) return;
                Vec3 dir=delta.normalize();
                double speed=Math.min(4.6,1.0 + len*0.18 + d.getSpeed()*0.08);
                s.setDeltaMovement(dir.scale(speed));
                s.hurtMarked=true;
                s.fallDistance=0;
                d.setAbilityCooldown(9);
                if(s.level() instanceof ServerLevel sl){
                    sl.sendParticles(ParticleTypes.CRIT,s.getX(),s.getY()+1.1,s.getZ(),14,0.25,0.45,0.25,0.08);
                    sl.sendParticles(ParticleTypes.DRAGON_BREATH,target.x,target.y,target.z,8,0.15,0.15,0.15,0.01);
                }
                d.sync(s);
            });
        });
        c.get().setPacketHandled(true);
    }
}
