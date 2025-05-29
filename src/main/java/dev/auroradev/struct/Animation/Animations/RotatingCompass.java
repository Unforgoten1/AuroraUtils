package dev.auroradev.struct.Animation.Animations;


import dev.auroradev.struct.Animation.API.IAnimation;
import dev.auroradev.utils.Items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class RotatingCompass implements IAnimation {
    private final String[] directions = {"North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest"};
    private int currentDirection;
    private final ItemBuilder compass;

    public RotatingCompass() {
        this.compass = new ItemBuilder(Material.COMPASS);
        this.currentDirection = 0;
    }

    @Override
    public ItemStack getNextItem() {
        return compass.name("Compass: " + directions[currentDirection]);
    }

    @Override
    public Consumer<InventoryClickEvent> getClickAction() {
        return event -> event.getWhoClicked().sendMessage("Clicked rotating compass!");
    }

    @Override
    public int getDuration() {
        return 10; // 0.5 seconds per direction
    }

    @Override
    public boolean shouldContinue() {
        currentDirection = (currentDirection + 1) % directions.length;
        return true; // Loop indefinitely
    }

    @Override
    public void init() {
        currentDirection = 0;
    }
}