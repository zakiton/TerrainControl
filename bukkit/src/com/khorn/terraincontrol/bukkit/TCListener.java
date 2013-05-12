package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.generator.resourcegens.TreeGen;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;

import java.util.Random;

public class TCListener implements Listener
{
    private TCPlugin tcPlugin;
    private Random random;

    private TreeGen treeGenerator = new TreeGen();

    public TCListener(TCPlugin plugin)
    {
        this.tcPlugin = plugin;
        this.random = new Random();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(WorldInitEvent event)
    {
        this.tcPlugin.WorldInit(event.getWorld());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStructureGrow(StructureGrowEvent event)
    {
        BukkitWorld bukkitWorld = this.tcPlugin.worlds.get(event.getWorld().getUID());
        if (bukkitWorld == null)
            return;

        int x = event.getLocation().getBlockX();
        int y = event.getLocation().getBlockY();
        int z = event.getLocation().getBlockZ();

        int biomeId = bukkitWorld.getCalculatedBiomeId(x, z);
        if (biomeId >= bukkitWorld.getSettings().biomeConfigs.length || bukkitWorld.getSettings().biomeConfigs[biomeId] == null)
            return;

        BiomeConfig biomeConfig = bukkitWorld.getSettings().biomeConfigs[biomeId];
        Resource sapling;

        switch (event.getSpecies())
        {
            case REDWOOD:
                sapling = biomeConfig.SaplingTypes[1];
                break;
            case BIRCH:
                sapling = biomeConfig.SaplingTypes[2];
                break;
            case JUNGLE:
            case SMALL_JUNGLE:
            case JUNGLE_BUSH:
                sapling = biomeConfig.SaplingTypes[3];
                break;
            default:
                sapling = biomeConfig.SaplingTypes[0];
                break;
        }

        if (sapling == null)
            sapling = biomeConfig.SaplingResource;

        if (sapling != null)
        {
            treeGenerator.SpawnTree(bukkitWorld, this.random, sapling, x, y, z);
            event.getBlocks().clear();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Sends the packet
        // Because the player doesn't send the REGISTER packet immediately, we
        // have to wait a second (20 ticks).
        Bukkit.getScheduler().runTaskLater(tcPlugin, new TCSender(tcPlugin, event.getPlayer()), 20);
    }
    
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        // Resends the packet so that the biomes are right again
        Bukkit.getScheduler().runTaskLater(tcPlugin, new TCSender(tcPlugin, event.getPlayer()), 1);
    }

}
