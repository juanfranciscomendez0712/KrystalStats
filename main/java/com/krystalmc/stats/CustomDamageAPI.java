package com.krystalmc.stats;

import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;

public class CustomDamageAPI {

    /**
     * Aplica daño custom a una entidad utilizando la API de KrystalStats.
     * 
     * @param victim La entidad que recibe el daño
     * @param damage Daño bruto inicial
     * @param cause El tipo de daño vanilla
     * @param directEntity Entidad física (ej. flecha) (opcional)
     * @param sourceEntity Atacante real (ej. jugador) (opcional)
     * @param location Localización del daño (opcional)
     * @param ignoreProtection Pct. de Protection a ignorar (0.0 a 1.0)
     * @param ignoreDefense Pct. de Defense a ignorar (0.0 a 1.0)
     * @param ignoreToughness Pct. de Toughness a ignorar (0.0 a 1.0)
     */
    public static void applyCustomDamage(LivingEntity victim, double damage, EntityDamageEvent.DamageCause cause, 
                                         Entity directEntity, Entity sourceEntity, Location location,
                                         double ignoreProtection, double ignoreDefense, double ignoreToughness) {
        
        // 1. Obtener estadisticas base
        double defense = StatsManager.getFlatDefense(victim);
        double protection = StatsManager.getCustomProtection(victim, cause);
        double toughness = StatsManager.getArmorToughness(victim);

        // 2. Aplicar penetraciones
        defense = Math.max(0, defense * (1.0 - ignoreDefense));
        protection = Math.max(0, protection * (1.0 - ignoreProtection));
        toughness = Math.max(0, toughness * (1.0 - ignoreToughness));

        boolean isFallDamage = cause == EntityDamageEvent.DamageCause.FALL;
        int featherFallingLevel = isFallDamage ? StatsManager.getFeatherFallingLevel(victim) : 0;

        // 3. Calculo
        double finalDamage = DamageCalculator.calculateFinalDamage(damage, protection, defense, toughness, isFallDamage, featherFallingLevel);

        // 4. Aplicar daño
        // AHORA (Mejor práctica, tipado seguro)
        DamageType krystalType = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.DAMAGE_TYPE)
                .get(MiBootstrap.KRYSTAL_DAMAGE_KEY);
        DamageSource.Builder sourceBuilder = DamageSource.builder(krystalType != null ? krystalType : DamageType.GENERIC);
        if (directEntity != null) sourceBuilder.withDirectEntity(directEntity);
        if (sourceEntity != null) sourceBuilder.withCausingEntity(sourceEntity);
        if (location != null) sourceBuilder.withDamageLocation(location);

        // Se usa el DamageSource para invocar damage
        victim.damage(finalDamage, sourceBuilder.build());
    }
}