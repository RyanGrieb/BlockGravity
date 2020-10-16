package me.rhin.blockgravity;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;

public class BlockTracker {

	private HashMap<Location, Block> placedBlocks;

	public BlockTracker() {
		this.placedBlocks = new HashMap<>();
	}

	public void setBlockStrength(Block block, int strength) {

		if (strength < 1) {

			removeBlock(block);

			Location blockLocation = block.getLocation().add(0.5, 0, 0.5);
			blockLocation.getWorld().spawnFallingBlock(blockLocation, block.getBlockData());
			return;
		}

		if (!placedBlocks.containsKey(block.getLocation())) {
			placedBlocks.put(block.getLocation(), block);
		}

		placedBlocks.get(block.getLocation()).setMetadata("BLOCK_STRENGTH",
				new FixedMetadataValue(BlockGravity.getInstance(), strength));
	}

	public void removeBlock(Block block) {

		if (block.hasMetadata("BLOCK_STRENGTH"))
			block.removeMetadata("BLOCK_STRENGTH", BlockGravity.getInstance());

		block.setType(Material.AIR);

		placedBlocks.remove(block.getLocation());
	}

}
