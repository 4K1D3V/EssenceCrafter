package pro.akii.pl.essenceCrafter;

import org.bukkit.plugin.java.JavaPlugin;
import pro.akii.pl.essenceCrafter.core.EssenceCore;

public final class EssenceCrafter extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("essence").setExecutor(new EssenceCore(this));
        getServer().getPluginManager().registerEvents(new EssenceCore(this), this);

        // Startup Logging
        try {
            String version = getConfig().getString("general.version", "Unknown Version");
            String[] start = {
                    "EssenceCrafter v" + version + " is Starting!",
                    "Made by Aki"
            };

            System.out.println(start[0]);
            System.out.println(start[1]);
        } catch (Exception e) {
            getLogger().warning("An issue occurred during startup logging: " + e.getMessage());
        }
    }
}
