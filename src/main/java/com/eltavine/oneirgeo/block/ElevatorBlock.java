package com.eltavine.oneirgeo.block;

import net.minecraft.world.level.block.Block;

/**
 * Standing on one and jumping takes you to the next elevator block straight above; sneaking takes
 * you to the next one below. The jump itself is detected on the client, see {@code VerticalTravel}.
 */
public class ElevatorBlock extends Block {
    public ElevatorBlock(Properties properties) {
        super(properties);
    }
}
