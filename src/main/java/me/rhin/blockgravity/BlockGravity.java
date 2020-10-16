package me.rhin.blockgravity;

import org.bukkit.plugin.java.JavaPlugin;

import me.rhin.blockgravity.listener.BlockListener;

public class BlockGravity extends JavaPlugin {

	//
	// Gravity Types:
	// 1. Horizontal block placement (DONE)
	// 2. Vertical down placement (BACKBURNER)
	// 3. Horizontal block break (TODO)
	// 4. Vertical block break (TODO)
	//

	public static final int DEFAULT_BLOCK_STRENGTH = 5; // There can be four blocks from the support block.

	private static BlockGravity instance;

	private BlockStrengthDebugger blockStrengthDebugger;

	public static BlockGravity getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;

		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		this.blockStrengthDebugger = new BlockStrengthDebugger();
	}

	@Override
	public void onDisable() {

	}

	public BlockStrengthDebugger getBlockStrengthDebugger() {
		return blockStrengthDebugger;
	}

	public BlockTracker getBlockTracker() {
		// TODO Auto-generated method stub
		return null;
	}
}
