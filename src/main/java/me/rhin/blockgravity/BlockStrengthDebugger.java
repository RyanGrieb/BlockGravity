package me.rhin.blockgravity;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;

import me.rhin.blockgravity.listener.BlockListener;
import me.rhin.blockgravity.util.MathUtil;

public class BlockStrengthDebugger {

	public static final boolean ENABLED = false;

	private ArrayList<Block> placedBlocks;

	public BlockStrengthDebugger() {
		if (!ENABLED)
			return;

		this.placedBlocks = new ArrayList<>();

		Bukkit.getScheduler().runTaskTimer(BlockGravity.getInstance(), () -> {
			for (Block block : placedBlocks) {
				Location blockLocation = block.getLocation().add(0.5, 0.5, 0.5);
				int blockStrength = BlockListener.getBlockStrength(block);
				int redOffset = Math.abs(blockStrength - (BlockGravity.DEFAULT_BLOCK_STRENGTH + 1));
				int greenOffset = blockStrength;
				int baseColor = 255 / (BlockGravity.DEFAULT_BLOCK_STRENGTH + 1);

				DustOptions dustOptions = new DustOptions(
						Color.fromRGB(MathUtil.clampInt(baseColor * redOffset, 0, 255),
								MathUtil.clampInt(baseColor * greenOffset, 0, 255), 0),
						1);
				
				blockLocation.getWorld().spawnParticle(Particle.REDSTONE, blockLocation.clone().add(0.5, 0, 0), 1,
						dustOptions);
				blockLocation.getWorld().spawnParticle(Particle.REDSTONE, blockLocation.clone().subtract(0.5, 0, 0), 1,
						dustOptions);
				blockLocation.getWorld().spawnParticle(Particle.REDSTONE, blockLocation.clone().add(0, 0, 0.5), 1,
						dustOptions);
				blockLocation.getWorld().spawnParticle(Particle.REDSTONE, blockLocation.clone().subtract(0, 0, 0.5), 1,
						dustOptions);
				blockLocation.getWorld().spawnParticle(Particle.REDSTONE, blockLocation.clone().add(0, 0.5, 0), 1,
						dustOptions);
				blockLocation.getWorld().spawnParticle(Particle.REDSTONE, blockLocation.clone().subtract(0, 0.5, 0), 1,
						dustOptions);
			}
		}, 0, 5);
	}

	public ArrayList<Block> getPlacedBlocks() {
		return placedBlocks;
	}

	public void addBlock(Block block) {
		if (!ENABLED)
			return;

		placedBlocks.add(block);
	}

	public void removeBlock(Block block) {
		if (!ENABLED)
			return;

		placedBlocks.remove(block);
	}

}
