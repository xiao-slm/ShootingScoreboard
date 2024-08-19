package com.slm;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;

public final class ShootingScoreboard extends JavaPlugin implements Listener, CommandExecutor {



    @Override
    public void onLoad() {
        getLogger().info("插件加载");
    }

    ConfigurationSection config;

    private boolean canRunScoreboard = false; // 更清晰的变量名
    private boolean isEnabled = true;

    @Override
    public void onEnable() {
        getLogger().info("插件运行");
        this.saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getPluginCommand("runScoreboard").setExecutor(this); // 确保命令名称与注册的一致
        Bukkit.getPluginCommand("Scoreboardreload").setExecutor(this); // 确保命令名称与注册的一致
        config = getConfig();

        getLogger().info(config.getString("name"));

        //name: "ShootingScoreboard"
        if(!config.getBoolean("enabled")){
            getLogger().info("插件被手动禁用！");
            isEnabled = false;
        }
        else {
            if(config.getBoolean("autoRun")){
                canRunScoreboard = true;
            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(isEnabled) {
            getLogger().info("PlayerJoin: " + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if(isEnabled){
            if (canRunScoreboard && event.getEntity() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getEntity();
                Location location = arrow.getLocation();
                LivingEntity shooter = arrow.getShooter() instanceof LivingEntity ? (LivingEntity) arrow.getShooter() : null;
                String shooterName = shooter != null ? shooter.getName() : "未知";

                double midX = Math.floor(location.getX()) + 0.5;
                double midZ = Math.floor(location.getZ()) + 0.5;
                double endX = location.getX() - midX;
                double endZ = location.getZ() - midZ;
                double endl = Math.sqrt((endX*endX)+(endZ*endZ));

                String strX = String.format("%.20f", endX);
                String strY = String.format("%.20f", endZ);
                String strl = String.format("%.20f", endl);

                getLogger().info("箭矢停在了: " + location.toString());
                getLogger().info("射箭人: " + shooterName);
                getLogger().info("距离中心: X绝对值=" + endX + ", Z绝对值=" + endZ + "整体距离=" + endl);

                boolean canrun=config.getBoolean("serverChat.run");

                if(canrun){
                    String all=config.getString("serverChat.text");
                    all=all.replace("%l%",strl);
                    all=all.replace("%x%",strX);
                    all=all.replace("%y%",strY);
                    all=all.replace("%p%",shooterName);
                    Bukkit.broadcastMessage(all);

                }
            }
        }

    }

    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(isEnabled){
            if (label.equalsIgnoreCase("runScoreboard")) {
                if (sender.isOp()) {
                    canRunScoreboard = !canRunScoreboard; // 切换状态
                    getLogger().info(canRunScoreboard ? "OP启动了追踪目标" : "OP关闭了追踪目标");
                    sender.sendMessage(canRunScoreboard ? "已启动追踪目标" : "已关闭追踪目标"); // 更改消息以反映实际功能
                    return true;
                }
                else {
                    sender.sendMessage("你没有权限执行这个命令！");
                    return false;
                }
            } else if (label.equalsIgnoreCase("Scoreboardreload")) {
                if (sender.isOp()) {
                    reloadConfig();
                    return true;
                } else {
                    sender.sendMessage("你没有权限执行这个命令！");
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        getLogger().info("插件注销");
    }
}