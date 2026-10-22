package net.intertime.bar;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class InterBar extends JavaPlugin {

    private BarManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        manager = new BarManager(this);
        getServer().getPluginManager().registerEvents(manager, this);
        manager.start();
        getLogger().info("InterBar увімкнено!");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.shutdown();
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (a.length > 0 && a[0].equalsIgnoreCase("reload")) {
            if (!s.hasPermission("intertime.bar.admin")) {
                s.sendMessage("Немає прав.");
                return true;
            }
            reloadConfig();
            manager.reload();
            s.sendMessage("InterBar: конфіг перезавантажено.");
            return true;
        }
        if (a.length > 0 && a[0].equalsIgnoreCase("donate")) {
            if (a.length == 1) {
                if (s instanceof Player p) {
                    BarManager.msg(p, "<gray>Донат-монети: <light_purple>" + BarManager.fmt(manager.donateOf(p.getUniqueId())));
                }
                return true;
            }
            if (!s.hasPermission("intertime.bar.admin")) {
                s.sendMessage("Немає прав.");
                return true;
            }
            if (a.length < 4) {
                s.sendMessage("Використання: /bar donate <give|take|set> <гравець> <сума>");
                return true;
            }
            Player t = Bukkit.getPlayerExact(a[2]);
            long n;
            try {
                n = Long.parseLong(a[3]);
            } catch (NumberFormatException ex) {
                s.sendMessage("Сума має бути числом.");
                return true;
            }
            if (t == null) {
                s.sendMessage("Гравця не знайдено.");
                return true;
            }
            switch (a[1].toLowerCase()) {
                case "give" -> manager.addDonate(t.getUniqueId(), n);
                case "take" -> manager.addDonate(t.getUniqueId(), -n);
                case "set" -> manager.setDonate(t.getUniqueId(), n);
                default -> {
                    s.sendMessage("Використання: /bar donate <give|take|set> <гравець> <сума>");
                    return true;
                }
            }
            s.sendMessage("Донат-монети " + t.getName() + ": " + manager.donateOf(t.getUniqueId()));
            return true;
        }
        if (!(s instanceof Player p)) {
            s.sendMessage("Тільки для гравців.");
            return true;
        }
        if (a.length >= 2 && a[0].equalsIgnoreCase("toggle")) {
            String key = a[1].toLowerCase();
            if (key.equals("sidebar") || key.equals("bossbar") || key.equals("tab")) {
                manager.toggle(p, key);
                BarManager.msg(p, "<gray>Панель <white>" + key + "<gray>: "
                        + (manager.isOn(p, key) ? "<green>увімкнено" : "<red>вимкнено"));
                return true;
            }
        }
        manager.openMenu(p);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] a) {
        if (a.length == 1) {
            return List.of("toggle", "donate", "reload").stream().filter(x -> x.startsWith(a[0].toLowerCase())).toList();
        }
        if (a.length == 2 && a[0].equalsIgnoreCase("toggle")) {
            return List.of("sidebar", "bossbar", "tab").stream().filter(x -> x.startsWith(a[1].toLowerCase())).toList();
        }
        return List.of();
    }
}
