package com.krystalmc.stats;

import java.io.IOException;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KrystalStats extends JavaPlugin {

    private static KrystalStats instance;
    public static boolean debugMode = false;
    private SkriptAddon skriptAddon;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("KrystalStats habilitado correctamente.");
        
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        
        getServer().getCommandMap().register(getName(), new StatsCommand());
        // Registrar el addon en el plugin de terceros, Skript, para poder usar funciones personalizadas.
                if (Bukkit.getPluginManager().getPlugin("Skript") != null) {
            skriptAddon = Skript.registerAddon(this);
            try {
                skriptAddon.loadClasses("com.krystalmc.stats", "skript");
                getLogger().info("¡Compatibilidad con Skript cargada exitosamente!");
            } catch (IOException e) {
                getLogger().severe("Error al cargar la compatibilidad con Skript.");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("KrystalStats deshabilitado.");
    }

    public static KrystalStats getInstance() {
        return instance;
    }
}

