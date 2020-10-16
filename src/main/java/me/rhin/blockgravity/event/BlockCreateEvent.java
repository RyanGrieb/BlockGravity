package me.rhin.blockgravity.event;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BlockCreateEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private Event eventType;
	private Block block;
	private boolean canceled;

	public BlockCreateEvent(Event eventType, Block block) {
		this.eventType = eventType;
		this.block = block;
		this.canceled = false;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public boolean isCancelled() {
		return canceled;
	}

	@Override
	public void setCancelled(boolean canceled) {
		this.canceled = canceled;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public Event getEventType() {
		return eventType;
	}

	public Block getBlock() {
		return block;
	}
}
