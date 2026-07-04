package com.krystalmc.stats;

public class DamageCalculator {

    // Renombré 'protection' a 'reductionPoints' para representar el valor sumado de todo sin el límite
    public static double calculateFinalDamage(double originalDamage, double reductionPoints, double defense, double toughness, boolean isFallDamage, int featherFallingLevel) {
        // Fase 1: Defensa Plana
        double damageAfterDefense;
        if (defense <= 0.0) {
            damageAfterDefense = Math.max(1.0, originalDamage);
            if (KrystalStats.debugMode) {
                KrystalStats.getInstance().getLogger().info("[Calc] Fase 1 (Saltada) - Daño tras Defensa Plana (" + originalDamage + " - 0.0) = " + damageAfterDefense);
            }
        } else {
            damageAfterDefense = Math.max(1.0, originalDamage - defense);
            if (KrystalStats.debugMode) {
                KrystalStats.getInstance().getLogger().info("[Calc] Fase 1 - Daño tras Defensa Plana (" + originalDamage + " - " + defense + ") = " + damageAfterDefense);
            }
        }

        // Fase 2 y 3: Reducción Base y Eficacia
        double realReduction = 0.0;
        if (reductionPoints > 0.0) {
            
            // 1. Calculamos el Límite Porcentual (Limitado estrictamente al 90%)
            // Lo dividimos en 100 para que trabaje en formato decimal (0.90)
            double maxReductionPercent = Math.min(90.0, reductionPoints) / 100.0;
            
            // 2. Calculamos la eficiencia contra el impacto
            // Eficiencia = (Puntos de reducción + Dureza / 2) / (Puntos de reducción + Daño Entrante)
            double efficiency = (reductionPoints + (toughness / 2.0)) / (reductionPoints + damageAfterDefense);
            
            // 3. Multiplicamos la reducción porcentual por la eficiencia
            realReduction = maxReductionPercent * efficiency;
            
            // Medida de seguridad: Si la eficiencia supera el 100% (golpes muy débiles vs armaduras muy duras), 
            // limitamos la reducción resultante para que NUNCA pase del 90% original.
            realReduction = Math.max(0.0, Math.min(0.90, realReduction));
            
            if (KrystalStats.debugMode) {
                KrystalStats.getInstance().getLogger().info("[Calc] Fase 2 y 3 - Puntos de Reducción Totales = " + reductionPoints);
                KrystalStats.getInstance().getLogger().info("[Calc] % de Reducción Base = " + (maxReductionPercent * 100) + "% | Multiplicador de Eficiencia = " + (efficiency * 100) + "%");
                KrystalStats.getInstance().getLogger().info("[Calc] Reducción Aplicada final = " + (realReduction * 100) + "%");
            }
        } else {
            if (KrystalStats.debugMode) {
                KrystalStats.getInstance().getLogger().info("[Calc] Fase 2 y 3 (Saltadas) - Reducción nula por falta de armadura/protección.");
            }
        }
        
        // Fase 4: Ajuste por Daño de Caída (Intacto)
        if (isFallDamage) {
            double armorReduction = Math.min(0.15, realReduction);
            double featherFallingReduction = featherFallingLevel * 0.15;
            realReduction = Math.min(0.90, armorReduction + featherFallingReduction);
            if (KrystalStats.debugMode) {
                KrystalStats.getInstance().getLogger().info("[Calc] Es Daño Por Caída. Reducción ajustada = " + realReduction);
            }
        }
        
        // Daño Final
        double finalDamage = damageAfterDefense * (1.0 - realReduction);

        if (KrystalStats.debugMode) {
            KrystalStats.getInstance().getLogger().info("[Calc] Fase 4 - Reducción Real de Daño (%) = " + (realReduction * 100) + "%");
            KrystalStats.getInstance().getLogger().info("[Calc] Daño temporal antes de limite mínimo: " + finalDamage);
        }

        if (finalDamage < 0.20) {
            finalDamage = 0.20;
        }

        return finalDamage;
    }
}