package dev.auroradev.api.gui.main;


import dev.auroradev.api.gui.enums.BorderType;
import dev.auroradev.api.gui.manager.GuiManager;
import dev.auroradev.struct.Animation.API.IAnimation;
import dev.auroradev.struct.Animation.Animation;
import dev.auroradev.struct.Animation.MultiSlotAnimation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AuroraGui {
    private final String name;
    private String title;
    private int rows;
    private Inventory inventory;
    private Map<Integer, Consumer<InventoryClickEvent>> clickActions;
    private GuiManager manager;
    private int currentPage;
    private List<ItemStack> paginatedItems;
    private Map<Integer, Consumer<InventoryClickEvent>> paginatedActions;
    private int itemsPerPage;
    private final Map<Integer, IAnimation> animations;
    private final Map<Integer, BukkitTask> animationTasks;
    private final List<PendingAnimation> pendingAnimations;
    private ItemStack[] borderItems;

    private static class PendingAnimation {
        final int slot;
        final IAnimation animation;

        PendingAnimation(int slot, IAnimation animation) {
            this.slot = slot;
            this.animation = animation;
        }
    }

    public AuroraGui(String name) {
        this.name = name;
        this.title = name;
        this.rows = 3;
        this.clickActions = new HashMap<>();
        this.paginatedItems = new ArrayList<>();
        this.paginatedActions = new HashMap<>();
        this.currentPage = 0;
        this.itemsPerPage = 7;
        this.animations = new HashMap<>();
        this.animationTasks = new HashMap<>();
        this.pendingAnimations = new ArrayList<>();
        this.borderItems = null;
    }

    public AuroraGui title(String title) {
        this.title = title;
        return this;
    }

    public AuroraGui rows(int rows) {
        if (rows < 1 || rows > 6) throw new IllegalArgumentException("Rows must be 1-6");
        this.rows = rows;
        this.itemsPerPage = (rows - 2) * 7;
        return this;
    }

    public AuroraGui setBorder(BorderType type, ItemStack borderItem) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        if (borderItem == null) return this;
        borderItems = new ItemStack[rows * 9];
        switch (type) {
            case TOP:
                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, borderItem);
                    borderItems[i] = borderItem;
                }
                break;
            case BOTTOM:
                for (int i = (rows - 1) * 9; i < rows * 9; i++) {
                    inventory.setItem(i, borderItem);
                    borderItems[i] = borderItem;
                }
                break;
            case LEFT:
                for (int i = 0; i < rows * 9; i += 9) {
                    inventory.setItem(i, borderItem);
                    borderItems[i] = borderItem;
                }
                break;
            case RIGHT:
                for (int i = 8; i < rows * 9; i += 9) {
                    inventory.setItem(i, borderItem);
                    borderItems[i] = borderItem;
                }
                break;
            case FULL:
                for (int i = 0; i < 9; i++) {
                    inventory.setItem(i, borderItem);
                    inventory.setItem((rows - 1) * 9 + i, borderItem);
                    borderItems[i] = borderItem;
                    borderItems[(rows - 1) * 9 + i] = borderItem;
                }
                for (int i = 0; i < rows * 9; i += 9) {
                    inventory.setItem(i, borderItem);
                    inventory.setItem(i + 8, borderItem);
                    borderItems[i] = borderItem;
                    borderItems[i + 8] = borderItem;
                }
                break;
        }
        return this;
    }

    public AuroraGui addItem(ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        int slot = findNextEmptySlot();
        if (slot >= 0) {
            inventory.setItem(slot, item);
            if (clickAction != null) clickActions.put(slot, clickAction);
        }
        return this;
    }

    public AuroraGui addItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        if (slot >= 0 && slot < rows * 9) {
            inventory.setItem(slot, item);
            if (clickAction != null) clickActions.put(slot, clickAction);
        }
        return this;
    }

    public AuroraGui setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        int targetSlot = slot == -1 ? findNextEmptySlot() : slot;
        if (targetSlot >= 0 && targetSlot < rows * 9) {
            inventory.setItem(targetSlot, item);
            clickActions.remove(targetSlot);
            if (clickAction != null) clickActions.put(targetSlot, clickAction);
        }
        return this;
    }

    public AuroraGui clearGui(boolean preserveBorder) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        ItemStack[] borderItemsCopy = preserveBorder ? borderItems : null;
        inventory.clear();
        clickActions.clear();
        paginatedItems.clear();
        paginatedActions.clear();
        animations.clear();
        animationTasks.values().forEach(BukkitTask::cancel);
        animationTasks.clear();
        pendingAnimations.clear();
        if (preserveBorder && borderItemsCopy != null) {
            for (int i = 0; i < borderItemsCopy.length; i++) {
                if (borderItemsCopy[i] != null) {
                    inventory.setItem(i, borderItemsCopy[i]);
                    borderItems[i] = borderItemsCopy[i];
                }
            }
        } else {
            borderItems = null;
        }
        return this;
    }

    public AuroraGui updateItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        if (slot >= 0 && slot < rows * 9) {
            inventory.setItem(slot, item);
            clickActions.remove(slot);
            if (clickAction != null) clickActions.put(slot, clickAction);
            animations.remove(slot);
            if (animationTasks.containsKey(slot)) {
                animationTasks.get(slot).cancel();
                animationTasks.remove(slot);
            }
        }
        return this;
    }

    public AuroraGui addPaginatedItems(List<ItemStack> items, Consumer<InventoryClickEvent> clickAction) {
        for (ItemStack item : items) {
            paginatedItems.add(item);
            int slot = paginatedItems.size() - 1;
            if (clickAction != null) paginatedActions.put(slot, clickAction);
        }
        updatePage();
        return this;
    }

    public AuroraGui nextPage() {
        if ((currentPage + 1) * itemsPerPage < paginatedItems.size()) {
            currentPage++;
            updatePage();
        }
        return this;
    }

    public AuroraGui prevPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
        return this;
    }

    public AuroraGui addAnimation(int slot, Animation animation) {
        return addCustomAnimation(slot, animation);
    }

    public AuroraGui addCustomAnimation(int slot, IAnimation animation) {
        if (slot < 0 || slot >= rows * 9 || animation == null) return this;
        pendingAnimations.add(new PendingAnimation(slot, animation));
        return this;
    }

    private void scheduleAnimation(int slot, IAnimation animation) {
        if (manager == null) {
            throw new IllegalStateException("Cannot schedule animation: GuiManager not set");
        }
        animations.put(slot, animation);
        animation.init();

        if (animation instanceof MultiSlotAnimation) {
            MultiSlotAnimation multiAnim = (MultiSlotAnimation) animation;
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), () -> {
                if (!animations.containsKey(slot) || !animationTasks.containsKey(slot)) return;

                Map<Integer, MultiSlotAnimation.AnimationSlot> frame = multiAnim.getNextFrame();
                for (int s : frame.keySet()) {
                    inventory.setItem(s, null);
                    clickActions.remove(s);
                }
                for (Map.Entry<Integer, MultiSlotAnimation.AnimationSlot> entry : frame.entrySet()) {
                    int s = entry.getKey();
                    MultiSlotAnimation.AnimationSlot animSlot = entry.getValue();
                    if (animSlot.getItem() != null) {
                        inventory.setItem(s, animSlot.getItem());
                    }
                    if (animSlot.getClickAction() != null) {
                        clickActions.put(s, animSlot.getClickAction());
                    }
                }

                if (!multiAnim.shouldContinue()) {
                    animations.remove(slot);
                    animationTasks.get(slot).cancel();
                    animationTasks.remove(slot);
                }
            }, 0L, multiAnim.getDuration());
            animationTasks.put(slot, task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), () -> {
                if (!animations.containsKey(slot) || !animationTasks.containsKey(slot)) return;

                IAnimation anim = animations.get(slot);
                ItemStack item = anim.getNextItem();
                Consumer<InventoryClickEvent> action = anim.getClickAction();

                if (item != null) {
                    inventory.setItem(slot, item);
                }
                if (action != null) {
                    clickActions.put(slot, action);
                } else {
                    clickActions.remove(slot);
                }

                if (!anim.shouldContinue()) {
                    animations.remove(slot);
                    animationTasks.get(slot).cancel();
                    animationTasks.remove(slot);
                }
            }, 0L, animation.getDuration());
            animationTasks.put(slot, task);
        }
    }

    private void updatePage() {
        int[] contentSlots = new int[itemsPerPage];
        int startSlot = 10;
        for (int i = 0; i < itemsPerPage; i++) {
            contentSlots[i] = startSlot + (i % 7) + (i / 7) * 9;
        }
        for (int slot : contentSlots) {
            if (!animations.containsKey(slot)) {
                inventory.setItem(slot, null);
                clickActions.remove(slot);
            }
        }

        int startIndex = currentPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage && (startIndex + i) < paginatedItems.size(); i++) {
            int slot = contentSlots[i];
            if (!animations.containsKey(slot)) {
                ItemStack item = paginatedItems.get(startIndex + i);
                Consumer<InventoryClickEvent> action = paginatedActions.get(startIndex + i);
                inventory.setItem(slot, item);
                if (action != null) clickActions.put(slot, action);
            }
        }

        title = title.replaceAll("Page \\d+", "Page " + (currentPage + 1));
        if (inventory != null) {
            Inventory newInventory = Bukkit.createInventory(null, rows * 9, title);
            newInventory.setContents(inventory.getContents());
            inventory = newInventory;
            for (Player viewer : getViewers()) {
                viewer.openInventory(newInventory);
            }
        }
    }

    public AuroraGui update() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, rows * 9, title);
            return this;
        }

        for (int i = 0; i < rows * 9; i++) {
            if (!animations.containsKey(i)) {
                inventory.setItem(i, null);
                clickActions.remove(i);
            }
        }

        if (borderItems != null) {
            for (int i = 0; i < borderItems.length; i++) {
                if (borderItems[i] != null && !animations.containsKey(i)) {
                    inventory.setItem(i, borderItems[i]);
                }
            }
        }

        updatePage();

        for (Player viewer : getViewers()) {
            viewer.updateInventory();
        }

        return this;
    }

    private List<Player> getViewers() {
        List<Player> viewers = new ArrayList<>();
        for (org.bukkit.entity.HumanEntity viewer : inventory.getViewers()) {
            if (viewer instanceof Player) {
                viewers.add((Player) viewer);
            }
        }
        return viewers;
    }

    public AuroraGui register(GuiManager manager) {
        if (manager == null) throw new IllegalArgumentException("GuiManager not set");
        manager.registerGui(this);
        for (PendingAnimation pending : new ArrayList<>(pendingAnimations)) {
            scheduleAnimation(pending.slot, pending.animation);
        }
        pendingAnimations.clear();
        return this;
    }

    private int findNextEmptySlot() {
        for (int i = 0; i < rows * 9; i++) {
            if (inventory.getItem(i) == null && !animations.containsKey(i)) return i;
        }
        return -1;
    }

    public void setManager(GuiManager manager) {
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public void open(Player player) {
        if (inventory == null) inventory = Bukkit.createInventory(null, rows * 9, title);
        manager.openGui(player, this);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Consumer<InventoryClickEvent> action = clickActions.get(event.getSlot());
        if (action != null) action.accept(event);
    }

    public Inventory getInventory() {
        return inventory;
    }
}