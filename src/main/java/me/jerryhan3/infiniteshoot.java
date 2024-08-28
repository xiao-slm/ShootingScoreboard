package me.jerryhan3;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Objects;
import java.util.ServiceConfigurationError;

public final class infiniteshoot extends JavaPlugin implements Listener, CommandExecutor {

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
        Objects.requireNonNull(Bukkit.getPluginCommand("runScoreboard")).setExecutor(this); // 确保命令名称与注册的一致
        Objects.requireNonNull(Bukkit.getPluginCommand("Scoreboardreload")).setExecutor(this); // 确保命令名称与注册的一致
        config = getConfig();

        getLogger().info(config.getString("name"));

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
            if (canRunScoreboard && event.getEntity() instanceof Arrow arrow) {
                LivingEntity shooter = arrow.getShooter() instanceof LivingEntity ? (LivingEntity) arrow.getShooter() : null;
                Block targetBlock = event.getHitBlock();
                if (targetBlock == null) return;
                if (targetBlock.getType() != Material.TARGET) return;
                BlockFace targetFace = event.getHitBlockFace();
                if (targetFace == null) {
                    getLogger().warning("击中面不存在！已跳过检测。");
                    return;
                }
                if (!targetFace.isCartesian()) {
                    getLogger().warning("击中面类型异常！已跳过检测。");
                    return;
                }
                String shooterName = shooter != null ? shooter.getName() : "未知";
                double midX = targetBlock.getX() + 0.5 + (double) targetFace.getModX() / 2;
                double midY = targetBlock.getY() + 0.5 + (double) targetFace.getModY() / 2;
                double midZ = targetBlock.getZ() + 0.5 + (double) targetFace.getModZ() / 2;
                // Delay 1 tick for the full update of arrow's position
                BukkitScheduler scheduler = Bukkit.getScheduler();
                scheduler.runTask(this, () -> {
                    Location location = arrow.getLocation();
                    double endX = location.getX() - midX;
                    double endY = location.getY() - midY;
                    double endZ = location.getZ() - midZ;
                    double endl = Math.sqrt((endX*endX)+(endY*endY)+(endZ*endZ));
                    double acc = - Math.log10(endl);

                    String strX = String.format("%.20f", endX);
                    String strY = String.format("%.20f", endY);
                    String strZ = String.format("%.20f", endZ);
                    String strl = String.format("%.20f", endl);
                    String strAcc = String.format("%.20f", acc);

                    getLogger().info("箭矢停在了: " + location.toString());
                    getLogger().info("击中面：" + targetFace + "，中心坐标：(" + midX + "," + midY + "," + midZ + ")");
                    getLogger().info("射箭人: " + shooterName);
                    getLogger().info("距离中心: X绝对值=" + endX + ", Y绝对值=" + endY + ", Z绝对值=" + endZ);
                    getLogger().info("总体距离：" + endl + "，精确度：" + acc);

                    boolean canrun = config.getBoolean("serverChat.run");

                    if (canrun) {
                        String all = config.getString("serverChat.text");
                        if (all == null) {
                            getLogger().warning("输出格式为空！无法在游戏内输出结果。");
                            return;
                        }
                        all=all.replace("%l%",strl);
                        all=all.replace("%x%",strX);
                        all=all.replace("%y%",strY);
                        all=all.replace("%z%",strZ);
                        all=all.replace("%p%",shooterName);
                        all=all.replace("%acc%",strAcc);
                        Bukkit.broadcast(all, Server.BROADCAST_CHANNEL_USERS);
                    }
                });
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(isEnabled){
            if (label.equalsIgnoreCase("runScoreboard")) {
                //TODO: 改用权限判断命令执行资格
                if (sender.isOp()) {
                    canRunScoreboard = !canRunScoreboard; // 切换状态
                    getLogger().info(canRunScoreboard ? "OP启动了追踪目标" : "OP关闭了追踪目标");
                    sender.sendMessage(canRunScoreboard ? "§a已启动追踪目标" : "§c已关闭追踪目标"); // 更改消息以反映实际功能
                    return true;
                }
                else {
                    sender.sendMessage("§c你没有权限执行这个命令！");
                    return false;
                }
            } else if (label.equalsIgnoreCase("Scoreboardreload")) {
                if (sender.isOp()) {
                    reloadConfig();
                    return true;
                } else {
                    sender.sendMessage("§c你没有权限执行这个命令！");
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
