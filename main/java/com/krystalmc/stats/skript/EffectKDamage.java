package com.krystalmc.stats.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.krystalmc.stats.CustomDamageAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

public class EffectKDamage extends Effect {

    static {
        // Registramos 3 patrones para cubrir todas las posibilidades que mencionaste
        Skript.registerEffect(EffectKDamage.class,
            // Patrón 0: Ignora 100% de todo ("ignoring armor and defense and toughness")
            "kdamage %livingentities% by %number% [caused by %-entity%] ignoring (armor|protection) and defense and toughness",
            
            // Patrón 1: Usando porcentajes ("ignoring 45% armor and 45% defense and 50% toughness")
            "kdamage %livingentities% by %number% [caused by %-entity%] ignoring %-number%[%] (armor|protection) and %-number%[%] defense and %-number%[%] toughness",
            
            // Patrón 2: Usando porcentajes pero empezando por defense como tu segundo ejemplo ("ignoring 45% defense and 45% armor and 50% toughness")
            "kdamage %livingentities% by %number% [caused by %-entity%] ignoring %-number%[%] defense and %-number%[%] (armor|protection) and %-number%[%] toughness"
        );
    }

    private Expression<LivingEntity> victims;
    private Expression<Number> damage;
    private Expression<Entity> causeEntity;
    
    private Expression<Number> armorExpr;
    private Expression<Number> defenseExpr;
    private Expression<Number> toughnessExpr;
    
    private int matchedPattern;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.victims = (Expression<LivingEntity>) exprs[0];
        this.damage = (Expression<Number>) exprs[1];
        this.causeEntity = (Expression<Entity>) exprs[2];
        this.matchedPattern = matchedPattern;

        if (matchedPattern == 1) {
            // Orden: armor, defense, toughness
            this.armorExpr = (Expression<Number>) exprs[3];
            this.defenseExpr = (Expression<Number>) exprs[4];
            this.toughnessExpr = (Expression<Number>) exprs[5];
        } else if (matchedPattern == 2) {
            // Orden: defense, armor, toughness
            this.defenseExpr = (Expression<Number>) exprs[3];
            this.armorExpr = (Expression<Number>) exprs[4];
            this.toughnessExpr = (Expression<Number>) exprs[5];
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        LivingEntity[] v = victims.getArray(event);
        Number d = damage.getSingle(event);
        
        if (d == null || v.length == 0) return;

        Entity attacker = causeEntity != null ? causeEntity.getSingle(event) : null;
        
        double ignoreArmor = 0.0;
        double ignoreDefense = 0.0;
        double ignoreToughness = 0.0;

        // Si es el Patrón 0, se ignora todo (100% = 1.0)
        if (matchedPattern == 0) {
            ignoreArmor = 1.0;
            ignoreDefense = 1.0;
            ignoreToughness = 1.0;
        } else {
            // Convierte el valor de porcentaje de Skript (ej: 45) a tu formato decimal (0.45)
            if (armorExpr != null && armorExpr.getSingle(event) != null) {
                ignoreArmor = armorExpr.getSingle(event).doubleValue() / 100.0;
            }
            if (defenseExpr != null && defenseExpr.getSingle(event) != null) {
                ignoreDefense = defenseExpr.getSingle(event).doubleValue() / 100.0;
            }
            if (toughnessExpr != null && toughnessExpr.getSingle(event) != null) {
                ignoreToughness = toughnessExpr.getSingle(event).doubleValue() / 100.0;
            }
        }

        for (LivingEntity victim : v) {
            if (victim == null) continue;
            
            // Si hubo atacante lo marcamos como ENTITY_ATTACK, sino como CUSTOM
            EntityDamageEvent.DamageCause cause = (attacker != null) 
                    ? EntityDamageEvent.DamageCause.ENTITY_ATTACK 
                    : EntityDamageEvent.DamageCause.CUSTOM;

            // Llamada a tu API estática
            CustomDamageAPI.applyCustomDamage(
                victim, 
                d.doubleValue(), 
                cause, 
                attacker, // Direct Entity
                attacker, // Source Entity
                victim.getLocation(), 
                ignoreArmor, 
                ignoreDefense, 
                ignoreToughness
            );
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "kdamage custom effect with stats ignoring";
    }
}