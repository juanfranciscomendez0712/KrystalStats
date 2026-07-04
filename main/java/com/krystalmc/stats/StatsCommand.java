package com.krystalmc.stats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsCommand extends Command {

    private static final List<String> EQUIPMENT_TYPES = Arrays.asList(
            "head", "chest", "legs", "feet", "hand", "offhand", "hands", "body", "any", "remove"
    );

    public StatsCommand() {
        super("kstats", "Comandos de KrystalStats", "/kstats", Arrays.asList());
        setPermission("krystalstats.admin");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo jugadores pueden ejecutar esto.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("krystalstats.admin")) {
            player.sendMessage("§cNo tienes permiso.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("debug")) {
            if (args.length < 2) {
                player.sendMessage("§cUsa: /kstats debug [true/false]");
                return true;
            }
            if (args[1].equalsIgnoreCase("true")) {
                KrystalStats.debugMode = true;
                player.sendMessage("§aModo debug de daño ACTIVADO. Revisa la consola.");
            } else if (args[1].equalsIgnoreCase("false")) {
                KrystalStats.debugMode = false;
                player.sendMessage("§cModo debug de daño DESACTIVADO.");
            } else {
                player.sendMessage("§cUsa: /kstats debug [true/false]");
            }
            return true;
        }

        if (args.length < 2) {
            sendHelp(player);
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cDebes tener un item en la mano.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage("§cNo se pudo obtener la metadata del item.");
            return true;
        }

        if (action.equals("equipment")) {
            String equipType = args[1].toLowerCase();

            if (!EQUIPMENT_TYPES.contains(equipType)) {
                player.sendMessage("§cTipo de equipamiento inválido.");
                return true;
            }

            if (equipType.equals("remove")) {
                meta.getPersistentDataContainer().remove(StatsManager.EQUIPMENT_TYPE_KEY);
                player.sendMessage("§aKrystal:equipment_type removido.");
            } else {
                meta.getPersistentDataContainer().set(StatsManager.EQUIPMENT_TYPE_KEY, PersistentDataType.STRING, equipType);
                player.sendMessage("§aKrystal:equipment_type establecido en " + equipType);
            }

            item.setItemMeta(meta);
            return true;
        }

        if (action.equals("defense")) {
            String defAction = args[1].toLowerCase();

            if (defAction.equals("remove")) {
                meta.getPersistentDataContainer().remove(StatsManager.DEFENSE_VALUE_KEY);
                player.sendMessage("§aKrystal:defense_value removido.");
            } else if (defAction.equals("add") || defAction.equals("set")) {
                if (args.length < 3) {
                    player.sendMessage("§cDebes especificar un valor numérico.");
                    return true;
                }
                try {
                 // Cambiamos a double para máxima compatibilidad con ExecutableItems y Minecraft
                    double val = Double.parseDouble(args[2]);
                    meta.getPersistentDataContainer().set(StatsManager.DEFENSE_VALUE_KEY, PersistentDataType.DOUBLE, val);
                    player.sendMessage("§aKrystal:defense_value establecido en " + val);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cEl valor debe ser numérico.");
                }

                
            } else {
                player.sendMessage("§cAcción inválida para defense. Usa add o remove.");
            }

            item.setItemMeta(meta);
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lKrystalStats Comandos:");
        player.sendMessage("§e/kstats equipment [head/chest/legs/feet/hand/offhand/hands/body/any/remove]");
        player.sendMessage("§e/kstats defense [add/remove] [valor]");
        player.sendMessage("§e/kstats debug [true/false]");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("krystalstats.admin")) {
            return completions;
        }

        if (args.length == 1) {
            List<String> commands = Arrays.asList("equipment", "defense", "debug");
            for (String c : commands) {
                if (c.startsWith(args[0].toLowerCase())) {
                    completions.add(c);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("equipment")) {
                for (String type : EQUIPMENT_TYPES) {
                    if (type.startsWith(args[1].toLowerCase())) {
                        completions.add(type);
                    }
                }
            } else if (sub.equals("defense")) {
                List<String> dActions = Arrays.asList("add", "set", "remove");
                for (String da : dActions) {
                    if (da.startsWith(args[1].toLowerCase())) {
                        completions.add(da);
                    }
                }
            } else if (sub.equals("debug")) {
                List<String> dbgArgs = Arrays.asList("true", "false");
                for (String da : dbgArgs) {
                    if (da.startsWith(args[1].toLowerCase())) {
                        completions.add(da);
                    }
                }
            }
        }
        return completions;
    }
}
