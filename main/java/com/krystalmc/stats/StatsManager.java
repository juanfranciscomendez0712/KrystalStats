package com.krystalmc.stats;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class StatsManager {

    public static final NamespacedKey EQUIPMENT_TYPE_KEY = new NamespacedKey("krystal", "equipment_type");
    public static final NamespacedKey DEFENSE_VALUE_KEY = new NamespacedKey("krystal", "defense_value");

    public static double getFlatDefense(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return 0.0;

        double totalDefense = 0.0;

        // Leer armadura y ambas manos
        ItemStack[] items = new ItemStack[]{
                equipment.getHelmet(),
                equipment.getChestplate(),
                equipment.getLeggings(),
                equipment.getBoots(),
                equipment.getItemInMainHand(),
                equipment.getItemInOffHand()
        };

        for (ItemStack item : items) {
            if (item != null && item.hasItemMeta()) {
                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                double defVal = 0.0;
                boolean hasDefense = false;
                // El uso del lector hibrído es por compatibilidad, sucede que anteriormente usaba float
                // cosa que causaba problemas con algunos plugins que manejan items en el servidor, que
                // trabajaban con double, por lo que hice que este plugin registrara la defensa en double,
                // y pudiera analizarlo tanto en double como en float.

                // Lector híbrido seguro para evitar bugs con ExecutableItems
                if (pdc.has(DEFENSE_VALUE_KEY, PersistentDataType.DOUBLE)) {
                    // Si el ítem es nuevo o ya pasó por ExecutableItems, es un DOUBLE
                    defVal = pdc.get(DEFENSE_VALUE_KEY, PersistentDataType.DOUBLE);
                    hasDefense = true;
                } else if (pdc.has(DEFENSE_VALUE_KEY, PersistentDataType.FLOAT)) {
                    // Si es un ítem viejo que todavía es un FLOAT, lo lee correctamente
                    defVal = pdc.get(DEFENSE_VALUE_KEY, PersistentDataType.FLOAT);
                    hasDefense = true;
                }

                if (hasDefense) {
                    totalDefense += defVal;
                    if (KrystalStats.debugMode) {
                        KrystalStats.getInstance().getLogger().info("[Stats] Item con defensa detectado: " + item.getType() + " | Aporta: " + defVal);
                    }
                }
            }
        }

        return totalDefense;
    }

    // Este método ahora calcula y devuelve los PUNTOS DE REDUCCIÓN (Armadura + Encantamientos)
    public static double getCustomProtection(LivingEntity entity, EntityDamageEvent.DamageCause cause) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return 0.0;
        
        double reductionPoints = 0.0;
        
        // 1. Puntos de armadura vanilla: Cada punto se multiplica por 2.5
        if (entity.getAttribute(Attribute.ARMOR) != null) {
            double armorVal = entity.getAttribute(Attribute.ARMOR).getValue();
            double armorReduction = armorVal * 2.5;
            reductionPoints += armorReduction;
            
            if (KrystalStats.debugMode && armorVal > 0) {
                KrystalStats.getInstance().getLogger().info("[Stats] Puntos de armadura vanilla: " + armorVal + " -> Aporta en Reducción: " + armorReduction);
            }
        }

        // 2. Cálculo por encantamientos
        ItemStack[] armors = equipment.getArmorContents();
        for (ItemStack armor : armors) {
            if (armor == null) continue;
            
            int protLevel = armor.getEnchantmentLevel(Enchantment.PROTECTION);
            double itemProtPoints = 0.0;
            
            // Protección común: se multiplica por 1.5
            if (protLevel > 0) {
                itemProtPoints += protLevel * 1.5;
            }
            
            // Identificar el nivel de protección especializada según la causa del daño
            int specProtLevel = 0;
            if (cause == EntityDamageEvent.DamageCause.PROJECTILE && armor.getEnchantmentLevel(Enchantment.PROJECTILE_PROTECTION) > 0) {
                specProtLevel = armor.getEnchantmentLevel(Enchantment.PROJECTILE_PROTECTION);
            } else if ((cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) && armor.getEnchantmentLevel(Enchantment.BLAST_PROTECTION) > 0) {
                specProtLevel = armor.getEnchantmentLevel(Enchantment.BLAST_PROTECTION);
            } else if ((cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) && armor.getEnchantmentLevel(Enchantment.FIRE_PROTECTION) > 0) {
                specProtLevel = armor.getEnchantmentLevel(Enchantment.FIRE_PROTECTION);
            }
            
            // Aplicar la lógica de la protección especializada
            if (specProtLevel > 0) {
                if (protLevel == 0) {
                    // Si no tiene protección común, multiplica por 2
                    itemProtPoints += specProtLevel * 2.0;
                } else {
                    // Si tiene protección común, añade 0.25 por nivel
                    itemProtPoints += specProtLevel * 0.25;
                }
            }
            
            reductionPoints += itemProtPoints;
            
            if (KrystalStats.debugMode && itemProtPoints > 0) {
                KrystalStats.getInstance().getLogger().info("[Stats] Protección de encanto en " + armor.getType() + " -> Aporta en Reducción: " + itemProtPoints);
            }
        }

        return reductionPoints;
    }

    public static double getArmorToughness(LivingEntity entity) {
        if (entity.getAttribute(Attribute.ARMOR_TOUGHNESS) != null) {
            return entity.getAttribute(Attribute.ARMOR_TOUGHNESS).getValue();
        }
        return 0.0;
    }

    public static int getFeatherFallingLevel(LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return 0;
        int maxLevel = 0;
        for (ItemStack armor : equipment.getArmorContents()) {
            if (armor != null && armor.getEnchantmentLevel(Enchantment.FEATHER_FALLING) > maxLevel) {
                maxLevel = armor.getEnchantmentLevel(Enchantment.FEATHER_FALLING);
            }
        }
        return maxLevel;
    }
}
