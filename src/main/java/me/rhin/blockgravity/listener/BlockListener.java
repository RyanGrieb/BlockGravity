package me.rhin.blockgravity.listener;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import me.rhin.blockgravity.BlockGravity;
import me.rhin.blockgravity.event.BlockCreateEvent;
import me.rhin.blockgravity.event.BlockDestroyEvent;
import net.md_5.bungee.api.ChatColor;

public class BlockListener implements Listener {

	// TODO: Move these statics to an acceptable class

	public static int getBlockStrength(Block block) {

		if (!block.getType().isSolid())
			return 0;

		return block.hasMetadata("BLOCK_STRENGTH") ? block.getMetadata("BLOCK_STRENGTH").get(0).asInt()
				: (BlockGravity.DEFAULT_BLOCK_STRENGTH + 1);
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

		while (block.hasMetadata("BLOCK_STRENGTH"))
			block = block.getRelative(BlockFace.DOWN);

		return block.getType().isSolid();
	}

	private static void revertAdjBlockStrengths(Block block, ArrayList<Block> prevBlocks,
			ArrayList<Block> blocksToUpdate) {

		if (!isSupportBlock(block)) {

			// Set all the blocks to be destroyed. (Only user placed ones)
			if (block.hasMetadata("BLOCK_STRENGTH"))
				block.setMetadata("BLOCK_STRENGTH", new FixedMetadataValue(BlockGravity.getInstance(), 0));

			// If we are not adj to a support block
			for (BlockFace face : getAllBlockFaces()) {

				if (block.getRelative(face).hasMetadata("BLOCK_STRENGTH") && block.getRelative(face).getType().isSolid()
						&& !prevBlocks.contains(block.getRelative(face))) {

					prevBlocks.add(block);
					revertAdjBlockStrengths(block.getRelative(face), prevBlocks, blocksToUpdate);
				}
			}
		} else {
			// We find the support block here, so update it.
			blocksToUpdate.add(block);
			return;
		}

		if (!prevBlocks.contains(block))
			prevBlocks.add(block);
	}

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
					Block highestAdjStrengthBlock = null;

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
		BlockCreateEvent blockCreateEvent = new BlockCreateEvent(event, event.getBlock());
		Bukkit.getPluginManager().callEvent(blockCreateEvent);

		if (blockCreateEvent.isCancelled())
			event.setCancelled(true);
	}

	@EventHandler
	public void blockBreakEvent(BlockBreakEvent event) {
		BlockDestroyEvent blockDestroyEvent = new BlockDestroyEvent(event, event.getBlock());
		Bukkit.getPluginManager().callEvent(blockDestroyEvent);

		if (blockDestroyEvent.isCancelled())
			event.setCancelled(true);
	}

	@EventHandler
	public void explosionPrimeEvent(ExplosionPrimeEvent event) {
		Block block = event.getEntity().getLocation().getBlock();

		if (block.hasMetadata("BLOCK_STRENGTH")) {

			BlockDestroyEvent blockDestroyEvent = new BlockDestroyEvent(event, block);
			Bukkit.getPluginManager().callEvent(blockDestroyEvent);

			if (blockDestroyEvent.isCancelled())
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void entityExplodeEvent(EntityExplodeEvent event) {

		// FIXME: This is really inefficient.
		for (Block block : event.blockList()) {

			if (!block.hasMetadata("BLOCK_STRENGTH"))
				continue;

			BlockDestroyEvent blockDestroyEvent = new BlockDestroyEvent(event, block);
			Bukkit.getPluginManager().callEvent(blockDestroyEvent);
		}
	}

	@EventHandler
	public void onBlockFall(EntityChangeBlockEvent event) {

		if ((event.getEntityType() == EntityType.FALLING_BLOCK)) {

			// FIXME: I don't like using the runnable here, but we need everything to fall
			// in place first.
			Bukkit.getScheduler().runTaskLater(BlockGravity.getInstance(), () -> {

				BlockCreateEvent blockCreateEvent = new BlockCreateEvent(event, event.getBlock());
				Bukkit.getPluginManager().callEvent(blockCreateEvent);

				if (blockCreateEvent.isCancelled())
					event.setCancelled(true);

			}, 1);
		}

	}

	@EventHandler
	public void blockCreateEvent(BlockCreateEvent event) {

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

		if (blockStrength < 1 && !block.getRelative(BlockFace.DOWN).getType().isSolid()) {

			Location blockLocation = block.getLocation().add(0.5, 0, 0.5);
			blockLocation.getWorld().spawnFallingBlock(blockLocation, block.getBlockData());
			event.setCancelled(true);
			return;
		} else if (blockStrength < 1 && block.getRelative(BlockFace.DOWN).getType().isSolid()) {

			// FIXME: All blocks under this should just fall.
			if (event.getEventType() instanceof BlockPlaceEvent) {
				BlockPlaceEvent placeEvent = (BlockPlaceEvent) event.getEventType();
				placeEvent.getPlayer()
						.sendMessage(ChatColor.GRAY + "You hear the ground crack, you pick up the block...");
			}

			if (event.getEventType() instanceof EntityChangeBlockEvent) {
				EntityChangeBlockEvent changeBlockEvent = (EntityChangeBlockEvent) event.getEventType();
				changeBlockEvent.getBlock().setType(Material.AIR);
				changeBlockEvent.getEntity().remove();
			}

			event.setCancelled(true);

			return;
		}

		block.setMetadata("BLOCK_STRENGTH", new FixedMetadataValue(BlockGravity.getInstance(), blockStrength));
		BlockGravity.getInstance().getBlockStrengthDebugger().addBlock(block);

		// Then recursively update any adj blocks that have a strength difference > 1.
		updateAdjBlockStrengths(block);
	}

	@EventHandler
	public void blockDestroyEvent(BlockDestroyEvent event) {

		Block block = event.getBlock();

		if (block.hasMetadata("BLOCK_STRENGTH"))
			block.removeMetadata("BLOCK_STRENGTH", BlockGravity.getInstance());

		BlockGravity.getInstance().getBlockStrengthDebugger().removeBlock(block);

		Bukkit.getScheduler().runTaskLater(BlockGravity.getInstance(), () -> {

			ArrayList<Block> blocksTraversed = new ArrayList<>();
			ArrayList<Block> blocksToUpdate = new ArrayList<>();

			revertAdjBlockStrengths(block, blocksTraversed, blocksToUpdate);

			for (Block blockToUpdate : blocksToUpdate) {
				updateAdjBlockStrengths(blockToUpdate);
			}

			for (Block blockTraversed : blocksTraversed) {
				if (blockTraversed.hasMetadata("BLOCK_STRENGTH") && getBlockStrength(blockTraversed) < 1) {

					blockTraversed.removeMetadata("BLOCK_STRENGTH", BlockGravity.getInstance());
					BlockGravity.getInstance().getBlockStrengthDebugger().removeBlock(blockTraversed);

					Location blockLocation = blockTraversed.getLocation().add(0.5, 0, 0.5);
					blockTraversed.getWorld().spawnFallingBlock(blockLocation, blockTraversed.getBlockData());
					blockTraversed.setType(Material.AIR);
				}
			}

		}, 1);
	}
}
