package com.ezinnovations.ezchat;

import com.ezinnovations.ezchat.commands.BroadcastSubcommand;
import com.ezinnovations.ezchat.commands.ChatToggleCommand;
import com.ezinnovations.ezchat.commands.EzChatMuteCommand;
import com.ezinnovations.ezchat.commands.EzChatMuteTempCommand;
import com.ezinnovations.ezchat.commands.EzChatMuteInfoCommand;
import com.ezinnovations.ezchat.commands.EzChatUnmuteCommand;
import com.ezinnovations.ezchat.commands.IgnoreListCommand;
import com.ezinnovations.ezchat.commands.ToggleMailSpyCommand;
import com.ezinnovations.ezchat.commands.ToggleSocialSpyCommand;
import com.ezinnovations.ezchat.commands.UnignoreCommand;
import com.ezinnovations.ezchat.service.SpyService;
import com.ezinnovations.ezchat.commands.EzChatLogsCommand;
import com.ezinnovations.ezchat.commands.IgnoreCommand;
import com.ezinnovations.ezchat.commands.MailCommand;
import com.ezinnovations.ezchat.commands.MessageCommand;
import com.ezinnovations.ezchat.commands.ReplyCommand;
import com.ezinnovations.ezchat.commands.ToggleMailCommand;
import com.ezinnovations.ezchat.commands.ToggleJoinLeaveMessageCommand;
import com.ezinnovations.ezchat.commands.ToggleMsgCommand;
import com.ezinnovations.ezchat.commands.StaffChatCommand;
import com.ezinnovations.ezchat.commands.ToggleStaffChatCommand;
import com.ezinnovations.ezchat.commands.ToggleServerMessageCommand;
import com.ezinnovations.ezchat.commands.ToggleDeathMessageCommand;
import com.ezinnovations.ezchat.commands.StaffAlertSubcommand;
import com.ezinnovations.ezchat.database.DatabaseManager;
import com.ezinnovations.ezchat.database.SQLiteManager;
import com.ezinnovations.ezchat.database.repository.AuditLogRepository;
import com.ezinnovations.ezchat.database.repository.CommunicationLogRepository;
import com.ezinnovations.ezchat.database.repository.IgnoreRepository;
import com.ezinnovations.ezchat.database.repository.MailRepository;
import com.ezinnovations.ezchat.database.repository.ToggleRepository;
import com.ezinnovations.ezchat.database.repository.MuteRepository;
import com.ezinnovations.ezchat.discord.AvatarUrlResolver;
import com.ezinnovations.ezchat.discord.DiscordMessageBuilder;
import com.ezinnovations.ezchat.discord.DiscordWebhookRouter;
import com.ezinnovations.ezchat.discord.DiscordWebhookService;
import com.ezinnovations.ezchat.listeners.PaperChatListener;
import com.ezinnovations.ezchat.listeners.PlayerJoinListener;
import com.ezinnovations.ezchat.listeners.JoinLeaveMessageListener;
import com.ezinnovations.ezchat.listeners.DeathMessageListener;
import com.ezinnovations.ezchat.moderation.AdvertisingCheckService;
import com.ezinnovations.ezchat.moderation.ProfanityCheckService;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import com.ezinnovations.ezchat.managers.ConfigManager;
import com.ezinnovations.ezchat.managers.FeatureManager;
import com.ezinnovations.ezchat.managers.IgnoreManager;
import com.ezinnovations.ezchat.managers.MailManager;
import com.ezinnovations.ezchat.managers.MessageManager;
import com.ezinnovations.ezchat.placeholder.EzChatPlaceholderExpansion;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.CommunicationLogService;
import com.ezinnovations.ezchat.service.MuteService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;
import com.ezinnovations.ezchat.service.StaffAlertService;
import com.ezinnovations.ezchat.service.StaffChatService;
import com.ezinnovations.ezchat.service.ServerMessageService;
import com.ezinnovations.ezchat.service.DeathMessageService;
import com.ezinnovations.ezchat.service.JoinLeaveService;
import com.ezinnovations.ezchat.utils.FloodgateHook;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class EzChat extends JavaPlugin {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern BUKKIT_HEX_PATTERN = Pattern.compile("&x(&[A-Fa-f0-9]){6}");

    private LuckPerms luckPerms;
    private ChatToggleManager chatToggleManager;
    private MessageManager messageManager;
    private FeatureManager featureManager;
    private IgnoreManager ignoreManager;
    private MailManager mailManager;
    private FloodgateHook floodgateHook;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private EzChatLogsCommand ezChatLogsCommand;
    private EzChatMuteCommand ezChatMuteCommand;
    private EzChatMuteTempCommand ezChatMuteTempCommand;
    private EzChatUnmuteCommand ezChatUnmuteCommand;
    private EzChatMuteInfoCommand ezChatMuteInfoCommand;
    private AdvertisingCheckService advertisingCheckService;
    private ProfanityCheckService profanityCheckService;
    private StaffAlertService staffAlertService;
    private StaffChatService staffChatService;
    private StaffAlertSubcommand staffAlertSubcommand;
    private BroadcastSubcommand broadcastSubcommand;
    private EzChatPlaceholderExpansion placeholderExpansion;

    @Override
    public void onEnable() {
        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        if (this.luckPerms == null) {
            getLogger().severe("[EzChat] LuckPerms not found! EzChat requires LuckPerms to function.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        saveResource("logs.yml", false);
        saveResource("discord.yml", false);
        saveResource("anti-spam.yml", false);
        saveResource("profanity.yml", false);
        saveResource("blocked-words.yml", false);
        saveResource("staff.yml", false);
        saveResource("server-message.yml", false);
        saveResource("death-message.yml", false);
        saveResource("join-leave.yml", false);
        saveResource("placeholders.yml", false);
        this.configManager = new ConfigManager(this);
        this.configManager.reload();
        this.featureManager = new FeatureManager(this);
        this.featureManager.reload();

        this.databaseManager = new SQLiteManager(this);
        this.databaseManager.initialize();

        final ToggleRepository toggleRepository = new ToggleRepository(this.databaseManager);
        final IgnoreRepository ignoreRepository = new IgnoreRepository(this.databaseManager);
        final MailRepository mailRepository = new MailRepository(this.databaseManager);
        final CommunicationLogRepository communicationLogRepository = new CommunicationLogRepository(this.databaseManager);
        final AuditLogRepository auditLogRepository = new AuditLogRepository(this.databaseManager);
        final MuteRepository muteRepository = new MuteRepository(this.databaseManager);

        final AvatarUrlResolver avatarUrlResolver = new AvatarUrlResolver(configManager.getDiscordConfig());
        final DiscordWebhookRouter discordWebhookRouter = new DiscordWebhookRouter(configManager.getDiscordConfig());
        final DiscordMessageBuilder discordMessageBuilder = new DiscordMessageBuilder(configManager.getDiscordConfig(), avatarUrlResolver);
        final DiscordWebhookService discordWebhookService = new DiscordWebhookService(this, configManager.getDiscordConfig(), discordWebhookRouter, discordMessageBuilder);
        final DiscordNotificationService discordNotificationService = new DiscordNotificationService(discordWebhookService);

        final CommunicationLogService communicationLogService = new CommunicationLogService(this, configManager.getLogsConfig(), communicationLogRepository);
        final AuditLogService auditLogService = new AuditLogService(this, configManager.getLogsConfig(), auditLogRepository);
        final MuteService muteService = new MuteService(this, configManager.getMuteConfig(), muteRepository, auditLogService);
        this.staffAlertService = new StaffAlertService(this, configManager.getStaffConfig(), communicationLogService, auditLogService);
        this.advertisingCheckService = new AdvertisingCheckService(this, configManager.getAntiSpamConfig(), auditLogService, discordNotificationService, this.staffAlertService);
        this.profanityCheckService = new ProfanityCheckService(this, configManager.getProfanityConfig(), configManager.getBlockedWordsConfig(), auditLogService, discordNotificationService);

        this.chatToggleManager = new ChatToggleManager(this, toggleRepository);
        this.chatToggleManager.load();
        this.staffChatService = new StaffChatService(this, configManager.getStaffConfig(), this.chatToggleManager, communicationLogService);
        this.staffAlertSubcommand = new StaffAlertSubcommand(this, configManager.getStaffConfig(), this.staffAlertService);
        final ServerMessageService serverMessageService = new ServerMessageService(this, configManager.getServerMessageConfig(), this.chatToggleManager, communicationLogService, auditLogService, discordNotificationService);
        this.broadcastSubcommand = new BroadcastSubcommand(this, configManager.getServerMessageConfig(), serverMessageService);
        final DeathMessageService deathMessageService = new DeathMessageService(this, configManager.getDeathMessageConfig(), this.chatToggleManager, communicationLogService, discordNotificationService);
        final JoinLeaveService joinLeaveService = new JoinLeaveService(this, configManager.getJoinLeaveConfig(), this.chatToggleManager, communicationLogService, discordNotificationService);
        this.messageManager = new MessageManager();
        this.ignoreManager = new IgnoreManager(this, ignoreRepository);
        this.ignoreManager.load();
        this.mailManager = new MailManager(this, mailRepository);
        this.mailManager.load();
        this.floodgateHook = new FloodgateHook(this);
        this.ezChatLogsCommand = new EzChatLogsCommand(this, configManager.getLogsConfig(), communicationLogService, auditLogService);
        this.ezChatMuteCommand = new EzChatMuteCommand(this, muteService, auditLogService, discordNotificationService, this.staffAlertService);
        this.ezChatMuteTempCommand = new EzChatMuteTempCommand(this, muteService, auditLogService, discordNotificationService, this.staffAlertService);
        this.ezChatUnmuteCommand = new EzChatUnmuteCommand(this, muteService, auditLogService, discordNotificationService);
        this.ezChatMuteInfoCommand = new EzChatMuteInfoCommand(this, muteService);
        final SpyService spyService = new SpyService(this, this.configManager.getStaffConfig(), this.chatToggleManager);

        registerPlaceholderExpansion();

        getServer().getPluginManager().registerEvents(new PaperChatListener(this, this.featureManager, this.chatToggleManager, this.ignoreManager, this.floodgateHook, communicationLogService, muteService, discordNotificationService, this.advertisingCheckService, this.profanityCheckService, this.staffChatService), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, this.featureManager, this.mailManager), this);
        getServer().getPluginManager().registerEvents(new DeathMessageListener(deathMessageService), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveMessageListener(joinLeaveService), this);

        if (getCommand("togglechat") != null) {
            getCommand("togglechat").setExecutor(new ChatToggleCommand(this, this.featureManager, this.chatToggleManager, auditLogService, discordNotificationService));
        } else {
            getLogger().warning("[EzChat] Failed to register /togglechat command.");
        }

        if (getCommand("msg") != null) {
            getCommand("msg").setExecutor(new MessageCommand(this, this.featureManager, this.messageManager, this.chatToggleManager, this.ignoreManager, communicationLogService, muteService, discordNotificationService, this.advertisingCheckService, this.profanityCheckService, spyService));
        } else {
            getLogger().warning("[EzChat] Failed to register /msg command.");
        }

        if (getCommand("reply") != null) {
            getCommand("reply").setExecutor(new ReplyCommand(this, this.featureManager, this.messageManager, this.chatToggleManager, this.ignoreManager, communicationLogService, muteService, discordNotificationService, this.advertisingCheckService, this.profanityCheckService, spyService));
        } else {
            getLogger().warning("[EzChat] Failed to register /reply command.");
        }

        if (getCommand("togglemsg") != null) {
            getCommand("togglemsg").setExecutor(new ToggleMsgCommand(this, this.featureManager, this.chatToggleManager, auditLogService, discordNotificationService));
        } else {
            getLogger().warning("[EzChat] Failed to register /togglemsg command.");
        }

        if (getCommand("ignore") != null) {
            getCommand("ignore").setExecutor(new IgnoreCommand(this, this.featureManager, this.ignoreManager, auditLogService, discordNotificationService));
        } else {
            getLogger().warning("[EzChat] Failed to register /ignore command.");
        }

        if (getCommand("ignorelist") != null) {
            getCommand("ignorelist").setExecutor(new IgnoreListCommand(this, this.featureManager, this.ignoreManager));
        } else {
            getLogger().warning("[EzChat] Failed to register /ignorelist command.");
        }

        if (getCommand("unignore") != null) {
            getCommand("unignore").setExecutor(new UnignoreCommand(this, this.featureManager, this.ignoreManager, auditLogService));
        } else {
            getLogger().warning("[EzChat] Failed to register /unignore command.");
        }

        if (getCommand("mail") != null) {
            getCommand("mail").setExecutor(new MailCommand(this, this.featureManager, this.mailManager, this.chatToggleManager, this.ignoreManager, this.floodgateHook, communicationLogService, auditLogService, muteService, discordNotificationService, this.advertisingCheckService, this.profanityCheckService, spyService));
        } else {
            getLogger().warning("[EzChat] Failed to register /mail command.");
        }

        if (getCommand("togglemail") != null) {
            getCommand("togglemail").setExecutor(new ToggleMailCommand(this, this.featureManager, this.chatToggleManager, auditLogService, discordNotificationService));
        } else {
            getLogger().warning("[EzChat] Failed to register /togglemail command.");
        }

        if (getCommand("togglesocialspy") != null) {
            getCommand("togglesocialspy").setExecutor(new ToggleSocialSpyCommand(this, spyService, auditLogService));
        } else {
            getLogger().warning("[EzChat] Failed to register /togglesocialspy command.");
        }

        if (getCommand("togglemailspy") != null) {
            getCommand("togglemailspy").setExecutor(new ToggleMailSpyCommand(this, spyService, auditLogService));
        } else {
            getLogger().warning("[EzChat] Failed to register /togglemailspy command.");
        }
        if (getCommand("toggleservermsg") != null) {
            getCommand("toggleservermsg").setExecutor(new ToggleServerMessageCommand(this, this.configManager.getServerMessageConfig(), serverMessageService));
        } else {
            getLogger().warning("[EzChat] Failed to register /toggleservermsg command.");
        }


        if (getCommand("toggledeathmsg") != null) {
            getCommand("toggledeathmsg").setExecutor(new ToggleDeathMessageCommand(this, this.configManager.getDeathMessageConfig(), deathMessageService));
        } else {
            getLogger().warning("[EzChat] Failed to register /toggledeathmsg command.");
        }

        if (getCommand("togglejoinleavemsg") != null) {
            getCommand("togglejoinleavemsg").setExecutor(new ToggleJoinLeaveMessageCommand(this, this.configManager.getJoinLeaveConfig(), joinLeaveService));
        } else {
            getLogger().warning("[EzChat] Failed to register /togglejoinleavemsg command.");
        }

        if (getCommand("staffchat") != null) {
            getCommand("staffchat").setExecutor(new StaffChatCommand(this, this.configManager.getStaffConfig(), this.staffChatService, this.profanityCheckService));
        } else {
            getLogger().warning("[EzChat] Failed to register /staffchat command.");
        }

        if (getCommand("sc") != null) {
            getCommand("sc").setExecutor(new StaffChatCommand(this, this.configManager.getStaffConfig(), this.staffChatService, this.profanityCheckService));
        } else {
            getLogger().warning("[EzChat] Failed to register /sc command.");
        }

        if (getCommand("togglestaffchat") != null) {
            getCommand("togglestaffchat").setExecutor(new ToggleStaffChatCommand(this, this.configManager.getStaffConfig(), this.staffChatService));
        } else {
            getLogger().warning("[EzChat] Failed to register /togglestaffchat command.");
        }

        final String[] chatPlugins = {"EssentialsChat", "VentureChat", "HeroChat", "DeluxeChat", "ChatManager", "ChatEx", "UltraChat", "TownyChat"};
        for (final String pluginName : chatPlugins) {
            if (getServer().getPluginManager().isPluginEnabled(pluginName)) {
                getLogger().warning("[EzChat] Detected " + pluginName + " which may also format chat. To avoid message duplication, disable chat formatting in " + pluginName + ".");
            }
        }
    }

    @Override
    public void onDisable() {
        if (this.chatToggleManager != null) {
            this.chatToggleManager.save();
        }

        if (this.ignoreManager != null) {
            this.ignoreManager.save();
        }

        if (this.mailManager != null) {
            this.mailManager.save();
        }

        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length >= 1 && "logs".equalsIgnoreCase(args[0]) && ezChatLogsCommand != null) {
            return ezChatLogsCommand.execute(sender, args);
        }

        if (args.length >= 1 && "mute".equalsIgnoreCase(args[0]) && ezChatMuteCommand != null) {
            return ezChatMuteCommand.execute(sender, args);
        }

        if (args.length >= 1 && "mutetemp".equalsIgnoreCase(args[0]) && ezChatMuteTempCommand != null) {
            return ezChatMuteTempCommand.execute(sender, args);
        }

        if (args.length >= 1 && "unmute".equalsIgnoreCase(args[0]) && ezChatUnmuteCommand != null) {
            return ezChatUnmuteCommand.execute(sender, args);
        }

        if (args.length >= 1 && "muteinfo".equalsIgnoreCase(args[0]) && ezChatMuteInfoCommand != null) {
            return ezChatMuteInfoCommand.execute(sender, args);
        }

        if (args.length >= 1 && "staffalert".equalsIgnoreCase(args[0]) && staffAlertSubcommand != null) {
            return staffAlertSubcommand.execute(sender, args);
        }

        if (args.length >= 1 && "broadcast".equalsIgnoreCase(args[0]) && broadcastSubcommand != null) {
            return broadcastSubcommand.execute(sender, args);
        }

        if (args.length == 1 && "reload".equals(args[0]) && sender.hasPermission("ezchat.reload")) {
            reloadConfig();
            if (configManager != null) {
                configManager.reload();
            }
            if (featureManager != null) {
                featureManager.reload();
            }
            if (advertisingCheckService != null) {
                advertisingCheckService.reload();
            }
            if (profanityCheckService != null) {
                profanityCheckService.reload();
            }
            registerPlaceholderExpansion();
            sender.sendMessage(colorize("&aEzChat has been reloaded."));
            return true;
        }

        if (args.length == 1 && "clear".equals(args[0]) && sender.hasPermission("ezchat.clearchat")) {
            for (final Player player : getServer().getOnlinePlayers()) {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage("");
                }
            }
            final String clearMessage = getConfig().getString("clear-chat-message", "&7Chat has been cleared by a staff member.");
            getServer().broadcastMessage(colorize(clearMessage));
            return true;
        }

        if (args.length == 2 && "debug".equals(args[0]) && sender.hasPermission("ezchat.debug")) {
            final Player target = getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(colorize("&cPlayer not found."));
                return true;
            }
            final CachedMetaData debugMeta = luckPerms.getPlayerAdapter(Player.class).getMetaData(target);
            sender.sendMessage(colorize("&6&lEzChat Debug: &f" + target.getName()));
            sender.sendMessage(colorize("&7Primary Group: &f" + debugMeta.getPrimaryGroup()));
            sender.sendMessage(colorize("&7Prefix: &f" + (debugMeta.getPrefix() != null ? debugMeta.getPrefix() : "&cnone")));
            sender.sendMessage(colorize("&7Suffix: &f" + (debugMeta.getSuffix() != null ? debugMeta.getSuffix() : "&cnone")));
            sender.sendMessage(colorize("&7All Prefixes (by weight):"));
            debugMeta.getPrefixes().forEach((weight, prefix) ->
                    sender.sendMessage(colorize("  &7[" + weight + "] &f" + prefix)));
            sender.sendMessage(colorize("&7All Suffixes (by weight):"));
            debugMeta.getSuffixes().forEach((weight, suffix) ->
                    sender.sendMessage(colorize("  &7[" + weight + "] &f" + suffix)));
            sender.sendMessage(colorize("&7Username-color: &f" + (debugMeta.getMetaValue("username-color") != null ? debugMeta.getMetaValue("username-color") : "&cnone")));
            sender.sendMessage(colorize("&7Message-color: &f" + (debugMeta.getMetaValue("message-color") != null ? debugMeta.getMetaValue("message-color") : "&cnone")));
            sender.sendMessage(colorize("&7Group format: &f" + (getConfig().getString("group-formats." + debugMeta.getPrimaryGroup()) != null ? "group-formats." + debugMeta.getPrimaryGroup() : "chat-format (default)")));
            sender.sendMessage(colorize("&7PAPI: &f" + (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") ? "&ahooked" : "&cnot found")));
            sender.sendMessage(colorize("&7Has ezchat.colorcodes: &f" + target.hasPermission("ezchat.colorcodes")));
            sender.sendMessage(colorize("&7Has ezchat.rgbcodes: &f" + target.hasPermission("ezchat.rgbcodes")));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            final List<String> completions = new ArrayList<>();
            if (sender.hasPermission("ezchat.reload")) completions.add("reload");
            if (sender.hasPermission("ezchat.clearchat")) completions.add("clear");
            if (sender.hasPermission("ezchat.debug")) completions.add("debug");
            if (sender.hasPermission("ezchat.logs")) completions.add("logs");
            if (sender.hasPermission("ezchat.mute")) completions.add("mute");
            if (sender.hasPermission("ezchat.mutetemp")) completions.add("mutetemp");
            if (sender.hasPermission("ezchat.staffalerts.send")) completions.add("staffalert");
            if (sender.hasPermission("ezchat.broadcast") || !(sender instanceof Player)) completions.add("broadcast");
            return completions;
        }
        if (args.length == 2 && "debug".equals(args[0]) && sender.hasPermission("ezchat.debug")) {
            return getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && "logs".equalsIgnoreCase(args[0]) && sender.hasPermission("ezchat.logs")) {
            return List.of("player", "between", "public", "msg", "mail", "search").stream()
                    .filter(v -> v.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        if (args.length >= 1 && "broadcast".equalsIgnoreCase(args[0]) && broadcastSubcommand != null) {
            return broadcastSubcommand.tabComplete(sender, args);
        }
        if (args.length == 2 && ("mute".equalsIgnoreCase(args[0]) || "mutetemp".equalsIgnoreCase(args[0]))) {
            return getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void registerPlaceholderExpansion() {
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("[EzChat] PlaceholderAPI not found; EzChat placeholders remain disabled.");
            return;
        }

        if (!this.configManager.getPlaceholdersConfig().isFeatureEnabled()) {
            getLogger().info("[EzChat] PlaceholderAPI support is disabled in placeholders.yml.");
            return;
        }

        if (this.placeholderExpansion != null) {
            return;
        }

        this.placeholderExpansion = new EzChatPlaceholderExpansion(this, this.chatToggleManager, this.configManager.getPlaceholdersConfig());
        if (this.placeholderExpansion.register()) {
            getLogger().info("[EzChat] Registered PlaceholderAPI expansion for EzChat settings placeholders.");
            return;
        }

        this.placeholderExpansion = null;
        getLogger().warning("[EzChat] Failed to register PlaceholderAPI expansion for EzChat.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public String renderConfigText(final String message) {
        return colorize(translateHexColorCodes(message));
    }

    public String buildFormat(final Player player) {
        final CachedMetaData metaData = this.luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        final String group = metaData.getPrimaryGroup();

        String format = getConfig().getString(getConfig().getString("group-formats." + group) != null ? "group-formats." + group : "chat-format");
        if (format == null) {
            format = "{prefix}{name}&r: {message}";
        }

        format = format
                .replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
                .replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
                .replace("{prefixes}", metaData.getPrefixes().keySet().stream().map(key -> metaData.getPrefixes().get(key)).collect(Collectors.joining()))
                .replace("{suffixes}", metaData.getSuffixes().keySet().stream().map(key -> metaData.getSuffixes().get(key)).collect(Collectors.joining()))
                .replace("{world}", player.getWorld().getName())
                .replace("{name}", player.getName())
                .replace("{displayname}", player.getDisplayName())
                .replace("{username-color}", metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "")
                .replace("{message-color}", metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "");

        format = translateHexColorCodes(format);
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(player, format);
        }
        format = colorize(translateHexColorCodes(format));

        return format;
    }

    public String processMessage(final Player player, final String message) {
        if (player.hasPermission("ezchat.colorcodes") && player.hasPermission("ezchat.rgbcodes")) {
            return colorize(translateHexColorCodes(message));
        } else if (player.hasPermission("ezchat.colorcodes")) {
            return colorize(stripHexCodes(message));
        } else if (player.hasPermission("ezchat.rgbcodes")) {
            return stripColorCodes(translateHexColorCodes(message));
        } else {
            return stripColorCodes(stripHexCodes(message));
        }
    }

    public String colorize(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    String translateHexColorCodes(final String message) {
        final char colorChar = ChatColor.COLOR_CHAR;

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            final String group = matcher.group(1);
            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }
        String result = matcher.appendTail(buffer).toString();

        matcher = BUKKIT_HEX_PATTERN.matcher(result);
        buffer = new StringBuffer(result.length());
        while (matcher.find()) {
            matcher.appendReplacement(buffer, matcher.group().replace('&', colorChar));
        }
        return matcher.appendTail(buffer).toString();
    }

    String stripColorCodes(final String message) {
        return message.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");
    }

    String stripHexCodes(final String message) {
        String result = message.replaceAll("&#[0-9a-fA-F]{6}", "");
        result = result.replaceAll("&x(&[0-9a-fA-F]){6}", "");
        return result;
    }
}
