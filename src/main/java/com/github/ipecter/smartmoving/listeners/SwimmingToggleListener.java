package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SmartMovingManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;

public class SwimmingToggleListener implements Listener {


    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onEntityToggleSwim(EntityToggleSwimEvent e) {
        if (!e.isSwimming() && e.getEntityType().equals(EntityType.PLAYER))
            if (smartMovingManager.isCrawling((Player) e.getEntity())) {
                e.setCancelled(true);
            }
    }

}
