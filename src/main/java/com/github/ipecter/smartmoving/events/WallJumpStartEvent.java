package com.github.ipecter.smartmoving.events;

import com.github.ipecter.smartmoving.SMPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WallJumpStartEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final SMPlayer smartMovingPlayer;

    @Getter
    @Setter
    private boolean isCancelled;

    public WallJumpStartEvent(SMPlayer smPlayer) {
        this.smartMovingPlayer = smPlayer;
    }

    public Player getPlayer() {
        return smartMovingPlayer.getPlayer();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
