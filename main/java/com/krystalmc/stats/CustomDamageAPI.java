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
     * Aplica daño personalizado a una entidad utilizando el sistema de estadísticas de KrystalStats.
     * @param victim           La entidad que recibe el daño.
     * @param damage           Daño bruto inicial.
     * @param cause            El tipo de daño vanilla (causa del evento).
     * @param directEntity     Entidad física que causa el daño (ej. una flecha). Opcional.
     * @param sourceEntity     Atacante real responsable del daño (ej. un jugador). Opcional.
     * @param location         Localización donde ocurre el daño. Opcional.
     * @param ignoreProtection Porcentaje de protección a ignorar (0.0 a 1.0).
     * @param ignoreDefense    Porcentaje de defensa a ignorar (0.0 a 1.0).
     * @param ignoreToughness  Porcentaje de dureza (toughness) a ignorar (0.0 a 1.0).
     */
    public static void applyCustomDamage(LivingEntity victim, double damage, EntityDamageEvent.DamageCause cause, 
                                         Entity directEntity, Entity sourceEntity, Location location,
                                         double ignoreProtection, double ignoreDefense, double ignoreToughness) {
        
        // 1. Obtener estadísticas base de la víctima
        double defense = StatsManager.getFlatDefense(victim);
        double protection = StatsManager.getCustomProtection(victim, cause);
        double toughness = StatsManager.getArmorToughness(victim);

        // 2. Aplicar los multiplicadores de penetración (se utiliza reducción porcentual)
        defense = Math.max(0, defense * (1.0 - ignoreDefense));
        protection = Math.max(0, protection * (1.0 - ignoreProtection));
        toughness = Math.max(0, toughness * (1.0 - ignoreToughness));

        boolean isFallDamage = cause == EntityDamageEvent.DamageCause.FALL;
        int featherFallingLevel = isFallDamage ? StatsManager.getFeatherFallingLevel(victim) : 0;

        // 3. Calcular el daño final mitigado
        double finalDamage = DamageCalculator.calculateFinalDamage(damage, protection, defense, toughness, isFallDamage, featherFallingLevel);

        // 4. Construir el DamageSource utilizando el registro de Paper
        DamageType krystalType = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.DAMAGE_TYPE)
                .get(MiBootstrap.KRYSTAL_DAMAGE_KEY);
        
        DamageSource.Builder sourceBuilder = DamageSource.builder(krystalType != null ? krystalType : DamageType.GENERIC);
        
        if (directEntity != null) sourceBuilder.withDirectEntity(directEntity);
        if (sourceEntity != null) sourceBuilder.withCausingEntity(sourceEntity);
        if (location != null) sourceBuilder.withDamageLocation(location);

        // 5. Aplicar el daño final a la entidad usando la API de Paper
        victim.damage(finalDamage, sourceBuilder.build());
    }
}
