package pro.akii.pl.essenceCrafter.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class EssenceCore implements CommandExecutor, Listener {
    private final JavaPlugin plugin;

    public EssenceCore(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /essence <create|list|delete|give>");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "list":
                return handleList(sender);
            case "delete":
                return handleDelete(sender, args);
            case "give":
                return handleGive(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Invalid subcommand. Use: /essence <create|list|delete|give>");
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.only_for_players"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /essence create <name> <entity>");
            return true;
        }

        String name = args[1];
        String entityTypeStr = args[2].toUpperCase();

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityTypeStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid entity type: " + entityTypeStr);
            return true;
        }

        plugin.getConfig().set("essences." + name + ".entity", entityType.name());
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN + "Essence recipe '" + name + "' created for entity " + entityType.name());
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!plugin.getConfig().contains("essences")) {
            sender.sendMessage(ChatColor.RED + "No Essence recipes found.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "Available Essence Recipes:");
        for (String key : plugin.getConfig().getConfigurationSection("essences").getKeys(false)) {
            String entity = plugin.getConfig().getString("essences." + key + ".entity");
            sender.sendMessage(ChatColor.YELLOW + "- " + key + " (Entity: " + entity + ")");
        }
        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /essence delete <name>");
            return true;
        }

        String name = args[1];
        if (!plugin.getConfig().contains("essences." + name)) {
            sender.sendMessage(ChatColor.RED + "Essence recipe '" + name + "' not found.");
            return true;
        }

        plugin.getConfig().set("essences." + name, null);
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN + "Essence recipe '" + name + "' deleted.");
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /essence give <name> [player]");
            return true;
        }

        String name = args[1];
        String playerName = args.length >= 3 ? args[2] : sender.getName();

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found.");
            return true;
        }

        if (!plugin.getConfig().contains("essences." + name)) {
            sender.sendMessage(ChatColor.RED + "Essence recipe '" + name + "' not found.");
            return true;
        }

        String materialName = plugin.getConfig().getString("essences." + name + ".item.material", "DIAMOND");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            sender.sendMessage(ChatColor.RED + "Invalid material '" + materialName + "' in config for essence '" + name + "'.");
            return true;
        }

        String itemName = plugin.getConfig().getString("essences." + name + ".item.name", "Unknown Essence");
        ItemStack essenceItem = new ItemStack(material);
        var meta = essenceItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));

            if (plugin.getConfig().contains("essences." + name + ".item.lore")) {
                List<String> lore = plugin.getConfig().getStringList("essences." + name + ".item.lore");
                lore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));
                meta.setLore(lore);
            }

            if (plugin.getConfig().contains("essences." + name + ".item.enchantments")) {
                List<String> enchantments = plugin.getConfig().getStringList("essences." + name + ".item.enchantments");
                for (String enchantmentStr : enchantments) {
                    String[] parts = enchantmentStr.split(":");
                    if (parts.length == 2) {
                        try {
                            Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                            int level = Integer.parseInt(parts[1]);
                            if (enchantment != null) {
                                meta.addEnchant(enchantment, level, true);
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            essenceItem.setItemMeta(meta);
        }

        target.getInventory().addItem(essenceItem);
        sender.sendMessage(ChatColor.GREEN + "Gave Essence '" + name + "' to " + target.getName());
        return true;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        String entityType = entity.getType().name();

        plugin.getConfig().getConfigurationSection("essences").getKeys(false).forEach(key -> {
            String requiredEntity = plugin.getConfig().getString("essences." + key + ".entity");
            if (requiredEntity != null && requiredEntity.equalsIgnoreCase(entityType)) {
                List<String> recipe = plugin.getConfig().getStringList("essences." + key + ".recipe");
                if (playerHasRequiredItems(player, recipe)) {
                    removeRequiredItems(player, recipe);
                    giveEssenceItem(player, key);
                    player.sendMessage(ChatColor.GREEN + "You have crafted " + ChatColor.BOLD + key + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have the required items to craft this Essence.");
                }
            }
        });
    }

    private boolean playerHasRequiredItems(Player player, List<String> recipe) {
        for (String ingredient : recipe) {
            String[] parts = ingredient.split(":");
            Material material = Material.getMaterial(parts[0]);
            int amount = Integer.parseInt(parts[1]);

            if (material == null || !player.getInventory().contains(material, amount)) {
                return false;
            }
        }
        return true;
    }

    private void removeRequiredItems(Player player, List<String> recipe) {
        for (String ingredient : recipe) {
            String[] parts = ingredient.split(":");
            Material material = Material.getMaterial(parts[0]);
            int amount = Integer.parseInt(parts[1]);

            if (material != null) {
                player.getInventory().removeItem(new ItemStack(material, amount));
            }
        }
    }

    private void giveEssenceItem(Player player, String key) {
        String materialName = plugin.getConfig().getString("essences." + key + ".item.material", "DIAMOND");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) return;

        String itemName = plugin.getConfig().getString("essences." + key + ".item.name", "Unknown Essence");
        ItemStack essenceItem = new ItemStack(material);
        var meta = essenceItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));
            if (plugin.getConfig().contains("essences." + key + ".item.lore")) {
                List<String> lore = plugin.getConfig().getStringList("essences." + key + ".item.lore");
                lore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));
                meta.setLore(lore);
            }
            if (plugin.getConfig().contains("essences." + key + ".item.enchantments")) {
                List<String> enchantments = plugin.getConfig().getStringList("essences." + key + ".item.enchantments");
                for (String enchantmentStr : enchantments) {
                    String[] parts = enchantmentStr.split(":");
                    if (parts.length == 2) {
                        Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                        int level = Integer.parseInt(parts[1]);
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                        }
                    }
                }
            }
            essenceItem.setItemMeta(meta);
        }
        player.getInventory().addItem(essenceItem);
    }
}