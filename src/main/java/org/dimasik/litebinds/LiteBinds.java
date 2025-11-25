package org.dimasik.litebinds;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dimasik.litebinds.command.CommandExecutor;
import org.dimasik.litebinds.database.DatabaseManager;
import org.dimasik.litebinds.listeners.EventListener;
import org.dimasik.litebinds.menu.listeners.MenuListener;
import org.dimasik.litebinds.menu.menus.Menu;

@Getter
public final class LiteBinds extends JavaPlugin {
    @Getter
    private static LiteBinds instance;
    private DatabaseManager databaseManager;
    private EventListener eventListener;

    @Override
    public void onEnable() {
        instance = this;
        setupDatabase();
        setupCommands();
        setupListeners();
    }

    private void setupDatabase(){
        super.saveDefaultConfig();
        FileConfiguration config = super.getConfig();
        databaseManager = new DatabaseManager(
                config.getString("mysql.host", "localhost"),
                config.getInt("mysql.port", 3306),
                config.getString("mysql.user", "root"),
                config.getString("mysql.password", "сайнес гпт кодер"),
                config.getString("mysql.database", "lite_binds")
        );
    }

    private void setupCommands(){
        var cmd = getCommand("binds");
        var cmdExec = new CommandExecutor();
        cmd.setExecutor(cmdExec);
        cmd.setTabCompleter(cmdExec);
    }

    private void setupListeners(){
        new MenuListener().register();
        PluginManager pluginManager = super.getServer().getPluginManager();
        eventListener = new EventListener();
        pluginManager.registerEvents(eventListener, this);
    }

    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.getOpenInventory().getTopInventory().getHolder() instanceof Menu){
                player.closeInventory();
            }
        }
    }
}
