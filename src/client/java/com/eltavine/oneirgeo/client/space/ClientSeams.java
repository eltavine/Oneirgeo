package com.eltavine.oneirgeo.client.space;

import com.eltavine.oneirgeo.network.SeamCrossPayload;
import com.eltavine.oneirgeo.space.SeamVolume;
import com.eltavine.oneirgeo.space.SpaceQuery;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Client prediction of seam crossings. Runs right before the local player's movement packet is
 * built, so the server learns about the crossing first and then receives the new position.
 */
public final class ClientSeams {
    private static int cooldown;

    private ClientSeams() {
    }

    public static void beforeSendPosition(LocalPlayer player) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (player.isSpectator() || player.isPassenger()) {
            return;
        }
        Vec3 position = player.position();
        Vec3 previous = new Vec3(player.xo, player.yo, player.zo);
        SeamVolume seam = SpaceQuery.seamFor(player.level(), position, position.subtract(previous),
                com.eltavine.oneirgeo.story.Story.get(player).allComplete());
        if (seam == null) {
            return;
        }
        Vec3 target = seam.apply(position);
        Vec3 shiftedPrevious = seam.apply(previous);
        player.setPos(target.x, target.y, target.z);
        player.xo = shiftedPrevious.x;
        player.yo = shiftedPrevious.y;
        player.zo = shiftedPrevious.z;
        player.xOld = shiftedPrevious.x;
        player.yOld = shiftedPrevious.y;
        player.zOld = shiftedPrevious.z;
        if (seam.rotation() != 0) {
            float turn = seam.rotateYaw(0.0F);
            player.setYRot(player.getYRot() + turn);
            player.yRotO += turn;
            player.yHeadRot += turn;
            player.yHeadRotO += turn;
            player.yBodyRot += turn;
            player.yBodyRotO += turn;
        }
        player.setDeltaMovement(seam.rotateVector(player.getDeltaMovement()));
        ClientPlayNetworking.send(new SeamCrossPayload(seam));
        cooldown = 2;
    }
}
