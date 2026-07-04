package com.krystalmc.stats;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.damage.DamageScaling;
import org.bukkit.damage.DamageType;

import java.util.List;

public class MiBootstrap implements PluginBootstrap {

    // Tipo de daño personalizado
    public static final TypedKey<DamageType> KRYSTAL_DAMAGE_KEY = TypedKey.create(
            RegistryKey.DAMAGE_TYPE, Key.key("krystal", "damage")
    );

    @Override
    public void bootstrap(BootstrapContext context) {
        LifecycleEventManager<BootstrapContext> manager = context.getLifecycleManager();

        // 1. REGISTRAR EL TIPO DE DAÑO
        manager.registerEventHandler(RegistryEvents.DAMAGE_TYPE.compose().newHandler(event -> {
            event.registry().register(KRYSTAL_DAMAGE_KEY, builder -> {
                builder.damageScaling(DamageScaling.NEVER) // El daño no escala con la dificultad
                       .exhaustion(0.1f)             // Cuánta hambre quita recibirlo
                       .messageId("krystal_damage");    // Mensaje de muerte
            });
        }));

        // 2. AÑADIR LAS TAGS (Crucial para ignorar la armadura Vanilla)
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.DAMAGE_TYPE), event -> {
            // Hacemos que este daño ignore la armadura base y la dureza
            event.registrar().addToTag(DamageTypeTagKeys.BYPASSES_ARMOR, List.of(KRYSTAL_DAMAGE_KEY));
            // Hacemos que ignore los encantamientos de protección
            event.registrar().addToTag(DamageTypeTagKeys.BYPASSES_ENCHANTMENTS, List.of(KRYSTAL_DAMAGE_KEY));
            // Dejamos que las pociones (como Resistencia) y los escudos sigan funcionando de forma vanilla
        });
    }
}
