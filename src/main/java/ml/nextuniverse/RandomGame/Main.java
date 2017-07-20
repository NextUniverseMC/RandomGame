package ml.nextuniverse.RandomGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.*;

public class Main extends JavaPlugin implements Listener {
    private static HashMap<Integer, Integer> gameVotes = new HashMap<Integer, Integer>();
    private static List<String> mainVotes = new ArrayList<String>();
    private boolean countdownStarted = false;
    private int players = 1;

    private int task;

    JedisPoolConfig poolConfig;
    JedisPool jedisPool;
    Jedis subscriberJedis;
    Subscriber subscriber;

    public static Inventory myInventory = Bukkit.createInventory(null, 9, "Pick what minigame you want");

    @Override
    public void onDisable() {
        subscriber.unsubscribe();
        jedisPool.returnResource(subscriberJedis);

        int votes = 0;
        int game = 0;
        if (!gameVotes.isEmpty()) {
            for(Map.Entry<Integer, Integer> entry : gameVotes.entrySet()) {
                int key = entry.getKey();
                int value = entry.getValue();
                if (votes == 0) {
                    votes = value;
                    game = key;
                }
                if (votes < value) {
                    votes = value;
                    game = key;
                }
            }
        }
        Random ran = new Random();
        double percent = votes / players;
        if (percent > 1) {
            percent = 1;
        }
        double willusegame = Math.random();
        boolean yes = false;
        if (willusegame <= percent) {
            yes = true;
        }
        int amount = 2;
        System.out.println(amount);
        int minigame;
        if (!yes) {
            minigame = ran.nextInt(amount - 1 + 1) + 1;
        }
        else {
            minigame = game;
        }
        File f = new File("plugins/RandomGame");
        if(!f.exists()){
            f.mkdir();
        }
        BufferedWriter writer = null;
        System.out.println(minigame);

            try {
                PrintWriter writer2 = new PrintWriter("newnumber.txt", "UTF-8");
                writer2.println(minigame);
                writer2.close();
            } catch (IOException e1) {
                getLogger().severe("************ RANDOMGAME FATAL ERROR ************");
                getLogger().severe("Could not write to newnumber.txt!");
                getLogger().severe("PLEASE NOTE: This means the minigame will NOT be changed");
                getLogger().severe("************ STACKTRACE - THIS NEEDS TO BE INSPECTED ************");
                e1.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (Exception e) {
                    getLogger().warning("Could not close writer!");
                }
            }

    }

    @Override
    public void onEnable() {
        poolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool(poolConfig, "localhost", 6379, 0);
        subscriberJedis = jedisPool.getResource();
        subscriber = new Subscriber();

        mainVotes.clear();
        gameVotes.clear();

        getServer().getPluginManager().registerEvents(this, this);

        Jedis jedis = new Jedis("localhost");


        new Thread(new Runnable() {
            public void run() {
                try {
                    getLogger().info("Subscribing to \"RandomGame\". This thread will be blocked.");
                    subscriberJedis.subscribe(subscriber, "RandomGame");
                    getLogger().info("Subscription ended.");
                } catch (Exception e) {
                    getLogger().severe("Subscribing failed." + e);
                }
            }
        }).start();

        jedis.publish("RandomGame", "ServerStarted");



    }


    public static void sendDataToSocket(String data, String id){

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vote")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("The console cannot vote!");
            }

            else{
                if (!mainVotes.contains(sender.getName())) {
                    mainVotes.add(sender.getName());
                    int votes = mainVotes.size();
                    getLogger().info(""+votes);
                    getLogger().info("" + Bukkit.getOnlinePlayers().size() / 2);
                    boolean test = false;
                    if (votes >= Math.round(Bukkit.getOnlinePlayers().size() / 2)) {
                        test = true;
                    }
                    getLogger().info("" + test);
                    ItemStack kitpvp = new ItemStack(Material.DIAMOND_SWORD, 1);
                    ItemMeta kitpvpMeta = kitpvp.getItemMeta();
                    kitpvpMeta.setDisplayName(ChatColor.AQUA + "KitPVP");
                    try {
                        kitpvpMeta.setLore(Arrays.asList(ChatColor.AQUA + gameVotes.get(1).toString() + ChatColor.GRAY + " have voted for this game."));
                    }
                    catch (NullPointerException e) {
                        kitpvpMeta.setLore(Arrays.asList(ChatColor.AQUA + "0" + ChatColor.GRAY + " have voted for this game."));
                    }
                    kitpvp.setItemMeta(kitpvpMeta);
                    myInventory.setItem(0, kitpvp);
                    Player p = (Player) sender;
                    p.openInventory(myInventory);
                    if (votes >= Math.round(Bukkit.getOnlinePlayers().size() / 2)) {

                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "[NextUniverse] " + ChatColor.GRAY + "Your vote has been noted");
                        if (!countdownStarted) {
                            countdownStarted = true;
                            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.GRAY + "A majority has been reached, the minigame will be switched in " + ChatColor.AQUA + "5" + ChatColor.GRAY + " minutes!");

                            task = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                                int countdown = 3;

                                public void run() {
                                    String s;
                                    if (countdown + 1 == 1) s = "";
                                    else s = "s";
                                    Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.GRAY + "The minigame will be switched in " + ChatColor.AQUA + (countdown + 1) + ChatColor.GRAY + " minute" + s + "!");
                                    countdown--;
                                    if (countdown == -1) {
                                        getServer().getScheduler().cancelTask(task);
                                        smallerCounter();
                                    }
                                }
                            }, 1200L, 1200L);
                        }
                        else {
                            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + " has just voted to switch the minigame!");
                        }
                    } else {
                        int majority = Math.round(Bukkit.getOnlinePlayers().size() / 2);
                        int needed = majority - votes;



                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "[NextUniverse] " + ChatColor.GRAY + "Your vote has been noted");
                        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + " has just voted to switch the minigame! " + ChatColor.AQUA + needed + ChatColor.GRAY + " votes are still needed to switch!");

                    }

                }
                else {
                    sender.sendMessage(ChatColor.RED + "You have already voted!");
                }
            }
            return true;
        }
        else if (command.getName().equals("mini-vote")) {
            if (args.length == 1) {
                if (gameVotes.containsKey(Integer.parseInt(args[0]))) {
                    gameVotes.put(Integer.parseInt(args[0]), (gameVotes.get(Integer.parseInt(args[0]))+ 1));
                }
                else {
                    gameVotes.put(Integer.parseInt(args[0]), 1);
                }

            }
            return true;
        }
        return false;
    }
    private void smallerCounter() {
        task = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            int countdown = 4;
            public void run() {
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.GRAY + "The minigame will be switched in " + ChatColor.AQUA + (countdown+1) + 0 + ChatColor.GRAY + " seconds!");
                countdown--;
                if (countdown == -1) {
                    getServer().getScheduler().cancelTask(task);
                    evenSmallerCounter();
                }
            }
        }, 200L, 200L);
    }
    private void evenSmallerCounter() {
        task = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            int countdown = 5;
            public void run() {
                String s;
                if (countdown == 1) s = "";
                else s = "s";
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[Announcement] " + ChatColor.GRAY + "The minigame will be switched in " + ChatColor.AQUA + countdown + ChatColor.GRAY + " second" + s + "!");
                countdown--;
                if (countdown == 0) {
                    getServer().getScheduler().cancelTask(task);

                    ServerSwitchingGameEvent event = new ServerSwitchingGameEvent();
                    Bukkit.getPluginManager().callEvent(event);

                    players= Bukkit.getServer().getOnlinePlayers().size();
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
//                        SocketAPI.connectPlayerToServer(p.getName(), "lobby");
                    }
                    jedisPool.getResource().publish("RandomGame", "ServerShutdown");


                }
            }
        }, 100L, 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.sendMessage(ChatColor.LIGHT_PURPLE + "[NextUniverse] " + ChatColor.GRAY + "Current minigame: " + ChatColor.AQUA + getConfig().getString("name") + ChatColor.GRAY + ". Don't like it? Change it with " + ChatColor.AQUA + "/vote" + ChatColor.GRAY + "!");

    }

    public static void dispatchShutdown() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getName().equals(myInventory.getName())) {
            ItemStack clicked = e.getCurrentItem();
            int s = 0;
            if (clicked.getType() == Material.DIAMOND_SWORD)
                s = 1;
            int i;
            try {
                i = gameVotes.get(s);
            }
            catch (NullPointerException ex) {
                i = 0;
            }
            gameVotes.put(s, i + 1);
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            p.closeInventory();
        }
    }
//    @EventHandler
//    public void onMessage(ReceivedDataEvent e) {
//        if (e.getChannel().equals("RandomGame")) {
//            if (e.getData().getString("ServerStatus").equals("Restart")) {
//                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
//                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
//            }
//        }
//    }
}
