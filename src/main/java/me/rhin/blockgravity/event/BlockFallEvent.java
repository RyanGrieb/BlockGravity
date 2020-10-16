package me.rhin.blockgravity.event;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BlockFallEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private Block block;

	public BlockFallEvent(Block block) {
		this.block = block;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public Block getBlock() {
		return block;
	}
}
