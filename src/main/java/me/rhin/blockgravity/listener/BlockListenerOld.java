package me.rhin.blockgravity.listener;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import me.rhin.blockgravity.BlockGravity;

public class BlockListenerOld implements Listener {

	// TODO: Move these statics to an acceptable class

	public static int getBlockStrength(Block block) {

		if (!block.getType().isSolid())
			return 0;

		return block.hasMetadata("BLOCK_STRENGTH") ? block.getMetadata("BLOCK_STRENGTH").get(0).asInt()
				: (BlockGravity.DEFAULT_BLOCK_STRENGTH + 1);
	}

	private static BlockFace getFacePlaced(Block placedBlock, Block againstBlock) {
		for (BlockFace face : BlockFace.values()) {
			if (againstBlock.getRelative(face).equals(placedBlock))
				return face;
		}

		return null;
	}

	private static BlockFace[] getHorizontalBlockFaces() {
		BlockFace[] horizontalFaces = new BlockFace[4];
		horizontalFaces[0] = BlockFace.EAST;
		horizontalFaces[1] = BlockFace.WEST;
		horizontalFaces[2] = BlockFace.NORTH;
		horizontalFaces[3] = BlockFace.SOUTH;

		return horizontalFaces;
	}

	private static BlockFace[] getAllBlockFaces() {
		BlockFace[] horizontalFaces = new BlockFace[6];
		horizontalFaces[0] = BlockFace.EAST;
		horizontalFaces[1] = BlockFace.WEST;
		horizontalFaces[2] = BlockFace.NORTH;
		horizontalFaces[3] = BlockFace.SOUTH;
		horizontalFaces[4] = BlockFace.UP;
		horizontalFaces[5] = BlockFace.DOWN;
		return horizontalFaces;
	}

	private static boolean isSupportBlock(Block block) {
		// FIXME: Stop overflow err w/ placing a side block on a natural block
		if (block.hasMetadata("BLOCK_STRENGTH") && getBlockStrength(block) == (BlockGravity.DEFAULT_BLOCK_STRENGTH + 1))
			return true;

		return false;
	}

	private static int distanceFromSupportBlock(Block block) {
		return distanceFromSupportBlock(block, null);
	}

	private static int distanceFromSupportBlock(Block block, Block prevBlock) {

		if (!isSupportBlock(block)) {
			// If we are not adj to a support block
			for (BlockFace face : getHorizontalBlockFaces()) {

				if (block.getRelative(face).getType().isSolid()
						&& (prevBlock == null || !block.getRelative(face).equals(prevBlock))) {

					int distance = distanceFromSupportBlock(block.getRelative(face), block) + 1;

					if (distance > 0)
						return distance;
				}
			}

			// If we hit a dead end, return an invalid length.
			return Integer.MIN_VALUE;
		}

		return 0;
	}

	private static void revertAdjBlockStrengths(Block block, ArrayList<Block> prevBlocks) {

		if (!isSupportBlock(block)) {

			// Set all the blocks to be destroyed.
			block.setMetadata("BLOCK_STRENGTH", new FixedMetadataValue(BlockGravity.getInstance(), 0));

			// If we are not adj to a support block
			for (BlockFace face : getAllBlockFaces()) {

				if (block.getRelative(face).hasMetadata("BLOCK_STRENGTH") && block.getRelative(face).getType().isSolid()
						&& !prevBlocks.contains(block.getRelative(face))) {

					// Keep this, since we are going to be calling updateAdjBlockStrengths once we
					// find our support block.

					prevBlocks.add(block);
					revertAdjBlockStrengths(block.getRelative(face), prevBlocks);
				}
			}
		} else {
			System.out.println("Support block found! Updating adj blocks to their new values...");

			// FIXME: Fix bug described on map
			updateAdjBlockStrengths(block);

			return;
		}

		// If we couldn't find any support blocks, check our previous block array and
		// apply gravity to the blocks that apply.

		/*
		 * prevBlocks.add(block); for (Block prevBlock : prevBlocks) {
		 * 
		 * if (prevBlock.hasMetadata("BLOCK_STRENGTH") && getBlockStrength(prevBlock) <
		 * 1) {
		 * 
		 * prevBlock.removeMetadata("BLOCK_STRENGTH", BlockGravity.getInstance());
		 * BlockGravity.getInstance().getBlockStrengthDebugger().removeBlock(prevBlock);
		 * 
		 * // FIXME: For falling blocks, we need to sort this array from min Y to max Y.
		 * // Then apply a tick delay for each y difference. Location blockLocation =
		 * prevBlock.getLocation().add(0.5, 0, 0.5);
		 * blockLocation.getWorld().spawnFallingBlock(blockLocation,
		 * prevBlock.getBlockData()); prevBlock.setType(Material.AIR); } }
		 */

	}

	// FIXME: We need a way to stop ourselves from going back to blocks in an
	// infinite loop.
	private static void updateAdjBlockStrengths(Block block) {

		int blockStrength = getBlockStrength(block);

		for (BlockFace face : getAllBlockFaces()) {
			Block faceBlock = block.getRelative(face);

			if (!faceBlock.getType().isSolid() || !faceBlock.hasMetadata("BLOCK_STRENGTH"))
				continue;

			// If we are propping up a block above on a support block
			if (face == BlockFace.UP && blockStrength == (BlockGravity.DEFAULT_BLOCK_STRENGTH + 1)) {

				if (getBlockStrength(faceBlock) != (BlockGravity.DEFAULT_BLOCK_STRENGTH + 1)) {

					faceBlock.setMetadata("BLOCK_STRENGTH", new FixedMetadataValue(BlockGravity.getInstance(),
							(BlockGravity.DEFAULT_BLOCK_STRENGTH + 1)));

					updateAdjBlockStrengths(faceBlock);
				}

			} else {
				if (Math.abs(getBlockStrength(faceBlock) - blockStrength) > 1) {

					// Set the face block strength -1 of the strongest adj block

					Block highestAdjStrengthBlock = null;

					// TODO: Is it smart to only do this horizontaly?
					for (BlockFace adjFace : getAllBlockFaces()) {

						Block adjFaceBlock = faceBlock.getRelative(adjFace);

						if (!adjFaceBlock.getType().isSolid())
							continue;

						if (highestAdjStrengthBlock == null
								|| getBlockStrength(adjFaceBlock) > getBlockStrength(highestAdjStrengthBlock)) {
							highestAdjStrengthBlock = adjFaceBlock;
						}
					}

					faceBlock.setMetadata("BLOCK_STRENGTH", new FixedMetadataValue(BlockGravity.getInstance(),
							getBlockStrength(highestAdjStrengthBlock) - 1));

					updateAdjBlockStrengths(faceBlock);
				}
			}
		}
	}

	@EventHandler
	public void blockPlaceEvent(BlockPlaceEvent event) {
		Block block = event.getBlock();

		Block highestStrengthBlock = null;

		for (BlockFace face : getAllBlockFaces()) {

			Block faceBlock = block.getRelative(face);
			if (!faceBlock.getType().isSolid())
				continue;

			if (highestStrengthBlock == null || getBlockStrength(faceBlock) > getBlockStrength(highestStrengthBlock)) {
				highestStrengthBlock = faceBlock;
			}
		}

		// FIXME: highestStrengthBlock can still be null somehow?
		int blockStrength = -1;

		// If were a block that has a support block under it. Set it to the max strength
		if (getBlockStrength(block.getRelative(BlockFace.DOWN)) == (BlockGravity.DEFAULT_BLOCK_STRENGTH + 1)) {

			blockStrength = (BlockGravity.DEFAULT_BLOCK_STRENGTH + 1);
		} else {

			blockStrength = getBlockStrength(highestStrengthBlock) - 1;
		}

		Bukkit.broadcastMessage("Strength of block placed:" + blockStrength);

		if (blockStrength < 1 && block.getRelative(BlockFace.DOWN).getType().isSolid()) {

			Location blockLocation = block.getLocation().add(0.5, 0, 0.5);
			blockLocation.getWorld().spawnFallingBlock(blockLocation, block.getBlockData());
			event.setCancelled(true);
			return;
		} else {
			event.getPlayer().sendMessage(
					ChatColor.GRAY + "You hear the ground crack around you... You remove the block you placed.");
			event.setCancelled(true);
		}

		block.setMetadata("BLOCK_STRENGTH", new FixedMetadataValue(BlockGravity.getInstance(), blockStrength));
		BlockGravity.getInstance().getBlockStrengthDebugger().addBlock(block);

		// Then recursively update any adj blocks that have a strength difference > 1.
		updateAdjBlockStrengths(block);
	}

	@EventHandler
	public void blockBreakEvent(BlockBreakEvent event) {
		Block block = event.getBlock();

		if (block.hasMetadata("BLOCK_STRENGTH"))
			block.removeMetadata("BLOCK_STRENGTH", BlockGravity.getInstance());

		BlockGravity.getInstance().getBlockStrengthDebugger().removeBlock(block);

		// For each adj block from the broken one
		// Recursively go back to the support block, and set all the block's previous
		// strength values on the way.
		// If we can't find a support block. All blocks that we iterated over need to
		// fall.

		Bukkit.getScheduler().runTaskLater(BlockGravity.getInstance(), () -> {
			revertAdjBlockStrengths(block, new ArrayList<Block>());
		}, 1);

	}

	@EventHandler
	public void blockFromEvent(BlockFormEvent event) {
		// TODO: Handle falling blocks and others strengths.
		// Another thing to note, if we are placed above a strength 1 block. We need to
		// fall off in another x or z location.

	}
}
