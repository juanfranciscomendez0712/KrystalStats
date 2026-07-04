package com.krystalmc.stats;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        // Extraer las llaves del DamageSource
        String damageNamespace = event.getDamageSource().getDamageType().getKey().getNamespace();
        String damageKey = event.getDamageSource().getDamageType().getKey().getKey();

        // 1. Ignorar daño del vacío
        // Realmente esto es código absoleto, debido a que antes yo usaba daño vacío para poder ignorar armadura, antes de crear este complemento.
        // Sin embargo, una vez creé este complemento y lo fuí actualizando, ya no es realmente necesario, pese a ello no lo he eliminado.
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID || 
            damageKey.equals("out_of_bounds") || 
            damageKey.equals("void")) {
            return;
        }

        // Ignorar el daño que proviene de la CustomDamageAPI para evitar doble reducción
        if (damageNamespace.equals("krystal") && damageKey.equals("damage")) {
            return;
        }

        LivingEntity victim = (LivingEntity) event.getEntity();

        // 2. Obtener Daño Bruto
        double originalDamage = event.getDamage();

        // [NUEVO] Reducir daño masivo de explosiones (Ender Crystals, TNT, etc) antes de entrar al sistema

        // Esta fue una adición muy reciente, debido a que me dí cuenta que las explosiones solían llegar a cantidades de daño entrante exageradamente grandes
        // lo cual rompía el balance de los calculos dentro del juego.
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || 
            event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
            damageKey.contains("explosion")) {
            
            originalDamage /= 2.0; // Se divide en 2.0 para obtener un número mas manejable
            
            if (KrystalStats.debugMode) {
                KrystalStats.getInstance().getLogger().info("--- [DEBUG DAÑO] EXPLOSIÓN DETECTADA ---");
                KrystalStats.getInstance().getLogger().info("Daño dividido por 2.0. Nuevo daño base: " + originalDamage);
            }
        }

        // Si el dano original fue ya reducido a 0 por algun motivo, salir.
        if (originalDamage <= 0) return;
        
        if (KrystalStats.debugMode) {
            KrystalStats.getInstance().getLogger().info("--- [DEBUG DAÑO] Calculando para " + victim.getName() + " ---");
            KrystalStats.getInstance().getLogger().info("Daño bruto entrante al sistema: " + originalDamage);
        }

        // 3. Obtener estadisticas
        double defense = StatsManager.getFlatDefense(victim);
        double reductionPoints = StatsManager.getCustomProtection(victim, event.getCause());
        double toughness = StatsManager.getArmorToughness(victim);

        if (KrystalStats.debugMode) {
            KrystalStats.getInstance().getLogger().info("Estadísticas de la víctima: Defensa=" + defense + ", Puntos de Reducción=" + reductionPoints + ", Toughness=" + toughness);
        }

        boolean isFallDamage = event.getCause() == EntityDamageEvent.DamageCause.FALL;
        int featherFallingLevel = isFallDamage ? StatsManager.getFeatherFallingLevel(victim) : 0;

        // 4. Obtener reduccion calculada
        double finalDamage = DamageCalculator.calculateFinalDamage(originalDamage, reductionPoints, defense, toughness, isFallDamage, featherFallingLevel);

        if (KrystalStats.debugMode) {
            KrystalStats.getInstance().getLogger().info("Daño final aplicado tras cálculo: " + finalDamage);
            KrystalStats.getInstance().getLogger().info("---------------------------------------------------");
        }

        // 5. Modificar el evento
        // Establecer Daño Base al Daño Entrante Real.
        event.setDamage(finalDamage);

        // Establecer modificadores vanilla de Armadura y Magia a 0 pues el complemento hace sus propios calculos de reducción.
        if (event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
        }
        if (event.isApplicable(EntityDamageEvent.DamageModifier.MAGIC)) {
            event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);
        }
        // Nota: Resistencia (poción) no se toca, pues la verdad me gusta su funcionalidad vanilla.
    }
}
