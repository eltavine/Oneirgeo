package com.eltavine.oneirgeo.world.gen.scene.rooms;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Materials of one band of rooms; {@code trim} is the lowest row of every wall, {@code hiddenDoor}
 * is the passage door set into a few walls, and {@code decay} is the share of wall blocks fallen out.
 */
public record RoomStyle(BlockState floor, BlockState wall, BlockState trim, BlockState ceiling, BlockState light, double lightChance,
                        BlockState hiddenDoor, double decay) {
}
