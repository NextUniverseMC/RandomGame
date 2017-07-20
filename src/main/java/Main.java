import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main extends JavaPlugin {
    private int votes = 0;
    private int task;

    @Override
    public void onDisable() {
        Random ran = new Random();
        int amount = getConfig().getInt("amount");
        int minigame = ran.nextInt(amount - 1 + 1) + 1;
        BufferedWriter writer = null;
        File file = new File(this.getDataFolder(), "result.txt");
        if(!file.exists()){
            try {
                file.createNewFile();
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(minigame);
            } catch (IOException e1) {
                getLogger().severe("************ RANDOMGAME FATAL ERROR ************");
                getLogger().severe("Could not write to results.txt!");
                getLogger().severe("PLEASE NOTE: This means the minigame will NOT be changed");
                getLogger().severe("THIS ERROR NEEDS TO BE FIXED IMMEDIATELY - ALL ONLINE PLAYERS WILL BE NOTIFIED");
                getLogger().severe("************ STACKTRACE - THIS NEEDS TO BE INSPECTED ************");
                e1.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }

        }
    }

    @Override
    public void onEnable() {
        votes=0;
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nextminigame")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("The console cannot vote!");
            }
            else {
                votes++;
                if (votes >= Math.round(Bukkit.getOnlinePlayers().size() / 2)) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[NextUniverse] " + ChatColor.GRAY + "Your vote has been noted");
                    Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.GRAY + "A majority has been reached, the minigame will be switched in " + ChatColor.WHITE + "5" + ChatColor.GRAY + " minutes!");

                    task = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
                        int countdown = 3;
                        public void run() {
                            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.GRAY + "The minigame will be switched in " + ChatColor.WHITE + (countdown+1) + ChatColor.GRAY + " minutes!");
                            countdown--;
                            if (countdown == 0) {
                                getServer().getScheduler().cancelTask(task);
                                smallerCounter();
                            }
                        }
                    }, 1200L, 1200L);
                }
                else {
                    int majority = Math.round(Bukkit.getOnlinePlayers().size() / 2);
                    int needed = majority - votes;
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + "[NextUniverse] " + ChatColor.GRAY + "Your vote has been noted");
                    Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.WHITE + sender.getName() +  ChatColor.GRAY + " has just voted to switch the minigame! " + ChatColor.WHITE + needed + ChatColor.GRAY + " are still needed to switch!");

                }

            }

        }
        return true;
    }
    public void smallerCounter() {
        task = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            int countdown = 4;
            public void run() {
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.GRAY + "The minigame will be switched in " + ChatColor.WHITE + (countdown+1) + 0 + ChatColor.GRAY + " seconds!");
                countdown--;
                if (countdown == 0) {
                    getServer().getScheduler().cancelTask(task);
                    evenSmallerCounter();
                }
            }
        }, 200L, 200L);
    }
    public void evenSmallerCounter() {
        task = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            int countdown = 5;
            public void run() {
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.GRAY + "The minigame will be switched in " + ChatColor.WHITE + countdown + ChatColor.GRAY + " seconds!");
                countdown--;
                if (countdown == 0) {
                    getServer().getScheduler().cancelTask(task);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.kickPlayer(ChatColor.LIGHT_PURPLE + "Minigame is switching! The server will be back with a new game soon!");
                        // TODO Switch servers rather than just kicking players
                    }
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "save-all");
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
                }
            }
        }, 100L, 20L);
    }
}
