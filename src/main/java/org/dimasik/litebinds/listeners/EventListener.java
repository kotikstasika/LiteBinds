package org.dimasik.litebinds.listeners;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.dimasik.litebinds.LiteBinds;
import org.dimasik.litebinds.database.ActionType;
import org.dimasik.litebinds.database.PlayerActions;
import org.dimasik.litebinds.utils.Parser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EventListener implements Listener {
    private final Map<UUID, ItemStack> lastHeldItems = new HashMap<>();
    private final Map<String, PlayerActions> playerActionsCache = new ConcurrentHashMap<>();

    private final Plugin holyLiteItemsPlugin;
    private final Plugin holyBackPackPlugin;
    private NamespacedKey itemTypeKey;
    private NamespacedKey backpackLevelKey;

    public EventListener() {
        holyLiteItemsPlugin = Bukkit.getPluginManager().getPlugin("HolyLiteItems");
        holyBackPackPlugin = Bukkit.getPluginManager().getPlugin("HolyBackPack");

        if (holyLiteItemsPlugin != null) {
            itemTypeKey = new NamespacedKey(holyLiteItemsPlugin, "item-type");
        }
        if (holyBackPackPlugin != null) {
            backpackLevelKey = new NamespacedKey(holyBackPackPlugin, "backpack-level");
        }

        Bukkit.getScheduler().runTaskTimer(LiteBinds.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack newItem = player.getInventory().getItemInHand();
                if (newItem != null) {
                    lastHeldItems.put(player.getUniqueId(), newItem.clone());
                }
            }
        }, 0, 1);
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP || event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SWAP_OFFHAND || event.getClick() == ClickType.DOUBLE_CLICK) {
            lastHeldItems.remove(event.getWhoClicked().getUniqueId());
        }
    }

    @EventHandler
    public void on(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        ItemStack lastHeldItem = lastHeldItems.get(player.getUniqueId());

        if (lastHeldItem != null && lastHeldItem.isSimilar(droppedItem) && droppedItem.getType() == Material.NETHERITE_SWORD) {

            PlayerActions playerActions = getCachedPlayerActions(player.getName());
            ActionType actionType = playerActions.getActionDrop();
            if (actionType != ActionType.NONE) {
                if (!event.isCancelled()) {
                    trigger(event, player, actionType);
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack offHandItem = event.getOffHandItem();

        if (offHandItem != null && offHandItem.getType() == Material.NETHERITE_SWORD) {
            PlayerActions playerActions = getCachedPlayerActions(player.getName());
            ActionType actionType = playerActions.getActionSwap();
            if (actionType != ActionType.NONE) {
                if (!event.isCancelled()) {
                    trigger(event, player, actionType);
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Material blockType = event.getClickedBlock().getType();
            if (isInteractableBlock(blockType)) {
                return;
            }
        }

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.NETHERITE_SWORD) {
            PlayerActions playerActions = getCachedPlayerActions(player.getName());
            ActionType actionType = playerActions.getActionInteract();
            if (actionType != ActionType.NONE) {
                if (!event.isCancelled()) {
                    trigger(event, player, actionType);
                }
            }
        }
    }

    private boolean isInteractableBlock(Material material) {
        return material == Material.CHEST || material == Material.TRAPPED_CHEST || material == Material.ENDER_CHEST || material == Material.SHULKER_BOX || material == Material.WHITE_SHULKER_BOX || material == Material.ORANGE_SHULKER_BOX || material == Material.MAGENTA_SHULKER_BOX || material == Material.LIGHT_BLUE_SHULKER_BOX || material == Material.YELLOW_SHULKER_BOX || material == Material.LIME_SHULKER_BOX || material == Material.PINK_SHULKER_BOX || material == Material.GRAY_SHULKER_BOX || material == Material.LIGHT_GRAY_SHULKER_BOX || material == Material.CYAN_SHULKER_BOX || material == Material.PURPLE_SHULKER_BOX || material == Material.BLUE_SHULKER_BOX || material == Material.BROWN_SHULKER_BOX || material == Material.GREEN_SHULKER_BOX || material == Material.RED_SHULKER_BOX || material == Material.BLACK_SHULKER_BOX || material == Material.ACACIA_DOOR || material == Material.BIRCH_DOOR || material == Material.DARK_OAK_DOOR || material == Material.JUNGLE_DOOR || material == Material.OAK_DOOR || material == Material.SPRUCE_DOOR || material == Material.IRON_DOOR || material == Material.ACACIA_TRAPDOOR || material == Material.BIRCH_TRAPDOOR || material == Material.DARK_OAK_TRAPDOOR || material == Material.JUNGLE_TRAPDOOR || material == Material.OAK_TRAPDOOR || material == Material.SPRUCE_TRAPDOOR || material == Material.IRON_TRAPDOOR || material == Material.FURNACE || material == Material.BLAST_FURNACE || material == Material.SMOKER || material == Material.ANVIL || material == Material.CHIPPED_ANVIL || material == Material.DAMAGED_ANVIL || material == Material.ENCHANTING_TABLE || material == Material.BREWING_STAND || material == Material.DISPENSER || material == Material.DROPPER || material == Material.HOPPER || material == Material.BEACON || material == Material.CRAFTING_TABLE || material == Material.BARREL || material == Material.CARTOGRAPHY_TABLE || material == Material.FLETCHING_TABLE || material == Material.GRINDSTONE || material == Material.LECTERN || material == Material.LOOM || material == Material.SMITHING_TABLE || material == Material.STONECUTTER || material == Material.BELL || material == Material.NOTE_BLOCK || material == Material.JUKEBOX || material == Material.COMPARATOR || material == Material.REPEATER || material == Material.LEVER || material == Material.STONE_BUTTON || material == Material.OAK_BUTTON || material == Material.SPRUCE_BUTTON || material == Material.BIRCH_BUTTON || material == Material.JUNGLE_BUTTON || material == Material.ACACIA_BUTTON || material == Material.DARK_OAK_BUTTON || material == Material.CRIMSON_BUTTON || material == Material.WARPED_BUTTON || material == Material.POLISHED_BLACKSTONE_BUTTON || material == Material.RESPAWN_ANCHOR || material == Material.CONDUIT || material == Material.CAMPFIRE || material == Material.SOUL_CAMPFIRE || material.isInteractable();
    }

    private PlayerActions getCachedPlayerActions(String playerName) {
        return playerActionsCache.computeIfAbsent(playerName, name -> {
            try {
                return LiteBinds.getInstance().getDatabaseManager().getPlayerActions(name).get().orElseGet(() -> new PlayerActions(name, ActionType.NONE, ActionType.NONE, ActionType.NONE));
            } catch (Exception e) {
                return new PlayerActions(name, ActionType.NONE, ActionType.NONE, ActionType.NONE);
            }
        });
    }

    public void invalidatePlayerCache(String playerName) {
        playerActionsCache.remove(playerName);
    }

    private void trigger(Cancellable event, Player player, ActionType actionType) {
        switch (actionType) {
            case SNOWBALL -> {
                if (holyLiteItemsPlugin == null || itemTypeKey == null) {
                    return;
                }
                ItemStack foundItem = findItemByType(player, "snowball");
                if (foundItem != null) {
                    event.setCancelled(true);
                    Snowball snowball = player.launchProjectile(Snowball.class);
                    snowball.setItem(foundItem);
                    snowball.setShooter(player);
                    PlayerLaunchProjectileEvent ev = new PlayerLaunchProjectileEvent(player, foundItem, snowball);
                    Bukkit.getServer().getPluginManager().callEvent(ev);
                    if (!ev.isCancelled()) {
                        foundItem.setAmount(foundItem.getAmount() - 1);
                    }
                } else {
                    event.setCancelled(true);
                    sendNotFoundMessage(player, "&x&0&0&B&C&C&CК&x&0&0&C&B&D&Dо&x&0&0&D&B&E&Eм&x&0&0&E&B&F&F &x&0&0&E&B&F&Fс&x&0&0&E&B&F&Fн&x&0&0&E&B&F&Fе&x&0&0&D&B&E&Eг&x&0&0&C&B&D&Dа");
                }
            }
            case JAKE -> {
                if (holyLiteItemsPlugin == null || itemTypeKey == null) {
                    return;
                }
                ItemStack foundItem = findItemByType(player, "jake");
                if (foundItem != null) {
                    event.setCancelled(true);
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, foundItem, null, BlockFace.DOWN));
                } else {
                    event.setCancelled(true);
                    sendNotFoundMessage(player, "&x&C&8&9&4&0&0С&x&D&2&9&B&0&0в&x&D&C&A&2&0&0е&x&E&6&A&A&0&0т&x&F&0&B&1&0&0и&x&F&B&B&9&0&0л&x&F&B&B&9&0&0ь&x&F&B&B&9&0&0н&x&F&B&B&9&0&0и&x&F&B&B&9&0&0к&x&F&B&B&9&0&0 &x&F&B&B&9&0&0Д&x&F&2&B&2&0&0ж&x&E&A&A&C&0&0е&x&E&1&A&6&0&0й&x&D&9&A&0&0&0к&x&D&0&9&A&0&0а");
                }
            }
            case ALTERNATIVE_TRAP -> {
                if (holyLiteItemsPlugin == null || itemTypeKey == null) {
                    return;
                }
                ItemStack foundItem = findItemByType(player, "alternative_trap");
                if (foundItem != null) {
                    event.setCancelled(true);
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, foundItem, null, BlockFace.DOWN));
                } else {
                    event.setCancelled(true);
                    sendNotFoundMessage(player, "&x&C&1&5&B&D&3Т&x&C&9&6&E&D&8р&x&D&0&8&1&D&Dа&x&D&0&8&1&D&Dп&x&C&9&6&E&D&8к&x&C&1&5&B&D&3а");
                }
            }
            case STAN -> {
                if (holyLiteItemsPlugin == null || itemTypeKey == null) {
                    return;
                }
                ItemStack foundItem = findItemByType(player, "stan");
                if (foundItem != null) {
                    event.setCancelled(true);
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, foundItem, null, BlockFace.DOWN));
                } else {
                    event.setCancelled(true);
                    sendNotFoundMessage(player, "&x&E&F&E&F&E&FС&x&E&6&E&6&E&6т&x&D&C&D&C&D&Cа&x&D&3&D&3&D&3н");
                }
            }
            case TRAP -> {
                if (holyLiteItemsPlugin == null || itemTypeKey == null) {
                    return;
                }
                ItemStack foundItem = findItemByType(player, "explosive_trap");
                if (foundItem != null) {
                    event.setCancelled(true);
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, foundItem, null, BlockFace.DOWN));
                } else {
                    event.setCancelled(true);
                    sendNotFoundMessage(player, "&x&6&7&6&A&8&5В&x&6&C&6&F&8&Bз&x&7&1&7&4&9&2р&x&7&6&7&A&9&9ы&x&7&B&7&F&A&0в&x&8&1&8&5&A&7н&x&8&1&8&5&A&7а&x&8&1&8&5&A&7я&x&8&1&8&5&A&7 &x&8&1&8&5&A&7т&x&8&1&8&5&A&7р&x&7&B&7&F&A&0а&x&7&6&7&A&9&9п&x&7&1&7&4&9&2к&x&6&C&6&F&8&Bа");
                }
            }
            case EXPLOSIVE -> {
                if (holyLiteItemsPlugin == null || itemTypeKey == null) {
                    return;
                }
                ItemStack foundItem = findItemByType(player, "explosive_stuff");
                if (foundItem != null) {
                    event.setCancelled(true);
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, foundItem, null, BlockFace.DOWN));
                } else {
                    event.setCancelled(true);
                    sendNotFoundMessage(player, "&x&C&8&2&7&0&0В&x&D&2&2&9&0&0з&x&D&C&2&B&0&0р&x&E&6&2&D&0&0ы&x&F&0&2&F&0&0в&x&F&B&3&1&0&0н&x&F&B&3&1&0&0а&x&F&B&3&1&0&0я&x&F&B&3&1&0&0 &x&F&B&3&1&0&0ш&x&F&B&3&1&0&0т&x&F&0&2&F&0&0у&x&E&6&2&D&0&0ч&x&D&C&2&B&0&0к&x&D&2&2&9&0&0а");
                }
            }
            case BACKPACK -> {
                if (holyBackPackPlugin == null || backpackLevelKey == null) {
                    return;
                }
                ItemStack foundItem = findBackpackItem(player);
                if (foundItem != null) {
                    event.setCancelled(true);
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, foundItem, null, BlockFace.DOWN));
                } else {
                    event.setCancelled(true);
                    sendNotFoundMessage(player, "&x&C&3&0&B&A&5Р&x&D&B&0&C&B&9ю&x&F&3&0&D&C&Eк&x&F&3&0&D&C&Eз&x&D&B&0&C&B&9а&x&C&3&0&B&A&5к &7(IV уровень)");
                }
            }
        }
    }

    private ItemStack findItemByType(Player player, String itemType) {
        if (player == null || player.getInventory() == null) {
            return null;
        }
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null) {
                String type = itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(itemTypeKey, PersistentDataType.STRING, "none");
                if (type.equalsIgnoreCase(itemType)) {
                    return itemStack;
                }
            }
        }
        return null;
    }

    private ItemStack findBackpackItem(Player player) {
        if (player == null || player.getInventory() == null) {
            return null;
        }
        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null) {
                Integer level = itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(backpackLevelKey, PersistentDataType.INTEGER, -1);
                if (level == 4) {
                    return itemStack;
                }
            }
        }
        return null;
    }

    private void sendNotFoundMessage(Player player, String itemName) {
        String message = Parser.color("&x&F&F&2&A&0&0▶ " + itemName + " &fне найден в инвентаре. Пополните запасы, чтобы быстро использовать его.");
        player.sendMessage(message);
    }
}