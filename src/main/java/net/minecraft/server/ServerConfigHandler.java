/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.BanEntry;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.OperatorList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Uuids;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerConfigHandler {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final File BANNED_IPS_FILE = new File("banned-ips.txt");
    public static final File BANNED_PLAYERS_FILE = new File("banned-players.txt");
    public static final File OPERATORS_FILE = new File("ops.txt");
    public static final File WHITE_LIST_FILE = new File("white-list.txt");

    static List<String> processSimpleListFile(File file, Map<String, String[]> valueMap) throws IOException {
        List<String> list = Files.readLines(file, StandardCharsets.UTF_8);
        for (String string : list) {
            if ((string = string.trim()).startsWith("#") || string.length() < 1) continue;
            String[] strings = string.split("\\|");
            valueMap.put(strings[0].toLowerCase(Locale.ROOT), strings);
        }
        return list;
    }

    private static void lookupProfile(MinecraftServer server, Collection<String> bannedPlayers, ProfileLookupCallback callback) {
        String[] strings = (String[])bannedPlayers.stream().filter(playerName -> !StringHelper.isEmpty(playerName)).toArray(String[]::new);
        if (server.isOnlineMode()) {
            server.getGameProfileRepo().findProfilesByNames(strings, callback);
        } else {
            for (String string : strings) {
                callback.onProfileLookupSucceeded(Uuids.getOfflinePlayerProfile(string));
            }
        }
    }

    public static boolean convertBannedPlayers(final MinecraftServer server) {
        final BannedPlayerList lv = new BannedPlayerList(PlayerManager.BANNED_PLAYERS_FILE);
        if (BANNED_PLAYERS_FILE.exists() && BANNED_PLAYERS_FILE.isFile()) {
            if (lv.getFile().exists()) {
                try {
                    lv.load();
                } catch (IOException iOException) {
                    LOGGER.warn("Could not load existing file {}", (Object)lv.getFile().getName(), (Object)iOException);
                }
            }
            try {
                final HashMap<String, String[]> map = Maps.newHashMap();
                ServerConfigHandler.processSimpleListFile(BANNED_PLAYERS_FILE, map);
                ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){

                    @Override
                    public void onProfileLookupSucceeded(GameProfile profile) {
                        server.getUserCache().add(profile);
                        String[] strings = (String[])map.get(profile.getName().toLowerCase(Locale.ROOT));
                        if (strings == null) {
                            LOGGER.warn("Could not convert user banlist entry for {}", (Object)profile.getName());
                            throw new ServerConfigException("Profile not in the conversionlist");
                        }
                        Date date = strings.length > 1 ? ServerConfigHandler.parseDate(strings[1], null) : null;
                        String string = strings.length > 2 ? strings[2] : null;
                        Date date2 = strings.length > 3 ? ServerConfigHandler.parseDate(strings[3], null) : null;
                        String string2 = strings.length > 4 ? strings[4] : null;
                        lv.add(new BannedPlayerEntry(profile, date, string, date2, string2));
                    }

                    @Override
                    public void onProfileLookupFailed(String string, Exception exception) {
                        LOGGER.warn("Could not lookup user banlist entry for {}", (Object)string, (Object)exception);
                        if (!(exception instanceof ProfileNotFoundException)) {
                            throw new ServerConfigException("Could not request user " + string + " from backend systems", exception);
                        }
                    }
                };
                ServerConfigHandler.lookupProfile(server, map.keySet(), profileLookupCallback);
                lv.save();
                ServerConfigHandler.markFileConverted(BANNED_PLAYERS_FILE);
            } catch (IOException iOException) {
                LOGGER.warn("Could not read old user banlist to convert it!", iOException);
                return false;
            } catch (ServerConfigException lv2) {
                LOGGER.error("Conversion failed, please try again later", lv2);
                return false;
            }
            return true;
        }
        return true;
    }

    public static boolean convertBannedIps(MinecraftServer server) {
        BannedIpList lv = new BannedIpList(PlayerManager.BANNED_IPS_FILE);
        if (BANNED_IPS_FILE.exists() && BANNED_IPS_FILE.isFile()) {
            if (lv.getFile().exists()) {
                try {
                    lv.load();
                } catch (IOException iOException) {
                    LOGGER.warn("Could not load existing file {}", (Object)lv.getFile().getName(), (Object)iOException);
                }
            }
            try {
                HashMap<String, String[]> map = Maps.newHashMap();
                ServerConfigHandler.processSimpleListFile(BANNED_IPS_FILE, map);
                for (String string : map.keySet()) {
                    String[] strings = (String[])map.get(string);
                    Date date = strings.length > 1 ? ServerConfigHandler.parseDate(strings[1], null) : null;
                    String string2 = strings.length > 2 ? strings[2] : null;
                    Date date2 = strings.length > 3 ? ServerConfigHandler.parseDate(strings[3], null) : null;
                    String string3 = strings.length > 4 ? strings[4] : null;
                    lv.add(new BannedIpEntry(string, date, string2, date2, string3));
                }
                lv.save();
                ServerConfigHandler.markFileConverted(BANNED_IPS_FILE);
            } catch (IOException iOException) {
                LOGGER.warn("Could not parse old ip banlist to convert it!", iOException);
                return false;
            }
            return true;
        }
        return true;
    }

    public static boolean convertOperators(final MinecraftServer server) {
        final OperatorList lv = new OperatorList(PlayerManager.OPERATORS_FILE);
        if (OPERATORS_FILE.exists() && OPERATORS_FILE.isFile()) {
            if (lv.getFile().exists()) {
                try {
                    lv.load();
                } catch (IOException iOException) {
                    LOGGER.warn("Could not load existing file {}", (Object)lv.getFile().getName(), (Object)iOException);
                }
            }
            try {
                List<String> list = Files.readLines(OPERATORS_FILE, StandardCharsets.UTF_8);
                ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){

                    @Override
                    public void onProfileLookupSucceeded(GameProfile profile) {
                        server.getUserCache().add(profile);
                        lv.add(new OperatorEntry(profile, server.getOpPermissionLevel(), false));
                    }

                    @Override
                    public void onProfileLookupFailed(String string, Exception exception) {
                        LOGGER.warn("Could not lookup oplist entry for {}", (Object)string, (Object)exception);
                        if (!(exception instanceof ProfileNotFoundException)) {
                            throw new ServerConfigException("Could not request user " + string + " from backend systems", exception);
                        }
                    }
                };
                ServerConfigHandler.lookupProfile(server, list, profileLookupCallback);
                lv.save();
                ServerConfigHandler.markFileConverted(OPERATORS_FILE);
            } catch (IOException iOException) {
                LOGGER.warn("Could not read old oplist to convert it!", iOException);
                return false;
            } catch (ServerConfigException lv2) {
                LOGGER.error("Conversion failed, please try again later", lv2);
                return false;
            }
            return true;
        }
        return true;
    }

    public static boolean convertWhitelist(final MinecraftServer server) {
        final Whitelist lv = new Whitelist(PlayerManager.WHITELIST_FILE);
        if (WHITE_LIST_FILE.exists() && WHITE_LIST_FILE.isFile()) {
            if (lv.getFile().exists()) {
                try {
                    lv.load();
                } catch (IOException iOException) {
                    LOGGER.warn("Could not load existing file {}", (Object)lv.getFile().getName(), (Object)iOException);
                }
            }
            try {
                List<String> list = Files.readLines(WHITE_LIST_FILE, StandardCharsets.UTF_8);
                ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){

                    @Override
                    public void onProfileLookupSucceeded(GameProfile profile) {
                        server.getUserCache().add(profile);
                        lv.add(new WhitelistEntry(profile));
                    }

                    @Override
                    public void onProfileLookupFailed(String string, Exception exception) {
                        LOGGER.warn("Could not lookup user whitelist entry for {}", (Object)string, (Object)exception);
                        if (!(exception instanceof ProfileNotFoundException)) {
                            throw new ServerConfigException("Could not request user " + string + " from backend systems", exception);
                        }
                    }
                };
                ServerConfigHandler.lookupProfile(server, list, profileLookupCallback);
                lv.save();
                ServerConfigHandler.markFileConverted(WHITE_LIST_FILE);
            } catch (IOException iOException) {
                LOGGER.warn("Could not read old whitelist to convert it!", iOException);
                return false;
            } catch (ServerConfigException lv2) {
                LOGGER.error("Conversion failed, please try again later", lv2);
                return false;
            }
            return true;
        }
        return true;
    }

    @Nullable
    public static UUID getPlayerUuidByName(final MinecraftServer server, String name) {
        if (StringHelper.isEmpty(name) || name.length() > 16) {
            try {
                return UUID.fromString(name);
            } catch (IllegalArgumentException illegalArgumentException) {
                return null;
            }
        }
        Optional<UUID> optional = server.getUserCache().findByName(name).map(GameProfile::getId);
        if (optional.isPresent()) {
            return optional.get();
        }
        if (server.isSingleplayer() || !server.isOnlineMode()) {
            return Uuids.getOfflinePlayerUuid(name);
        }
        final ArrayList list = Lists.newArrayList();
        ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){

            @Override
            public void onProfileLookupSucceeded(GameProfile profile) {
                server.getUserCache().add(profile);
                list.add(profile);
            }

            @Override
            public void onProfileLookupFailed(String string, Exception exception) {
                LOGGER.warn("Could not lookup user whitelist entry for {}", (Object)string, (Object)exception);
            }
        };
        ServerConfigHandler.lookupProfile(server, Lists.newArrayList(name), profileLookupCallback);
        if (!list.isEmpty()) {
            return ((GameProfile)list.get(0)).getId();
        }
        return null;
    }

    public static boolean convertPlayerFiles(final MinecraftDedicatedServer minecraftServer) {
        final File file = ServerConfigHandler.getLevelPlayersFolder(minecraftServer);
        final File file2 = new File(file.getParentFile(), "playerdata");
        final File file3 = new File(file.getParentFile(), "unknownplayers");
        if (!file.exists() || !file.isDirectory()) {
            return true;
        }
        File[] files = file.listFiles();
        ArrayList<String> list = Lists.newArrayList();
        for (File file4 : files) {
            String string2;
            String string = file4.getName();
            if (!string.toLowerCase(Locale.ROOT).endsWith(".dat") || (string2 = string.substring(0, string.length() - ".dat".length())).isEmpty()) continue;
            list.add(string2);
        }
        try {
            final String[] strings = list.toArray(new String[list.size()]);
            ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){

                @Override
                public void onProfileLookupSucceeded(GameProfile gameProfile) {
                    minecraftServer.getUserCache().add(gameProfile);
                    UUID uUID = gameProfile.getId();
                    this.convertPlayerFile(file2, this.getPlayerFileName(gameProfile.getName()), uUID.toString());
                }

                @Override
                public void onProfileLookupFailed(String string, Exception exception) {
                    LOGGER.warn("Could not lookup user uuid for {}", (Object)string, (Object)exception);
                    if (!(exception instanceof ProfileNotFoundException)) {
                        throw new ServerConfigException("Could not request user " + string + " from backend systems", exception);
                    }
                    String string2 = this.getPlayerFileName(string);
                    this.convertPlayerFile(file3, string2, string2);
                }

                private void convertPlayerFile(File playerDataFolder, String fileName, String uuid) {
                    File file22 = new File(file, fileName + ".dat");
                    File file32 = new File(playerDataFolder, uuid + ".dat");
                    ServerConfigHandler.createDirectory(playerDataFolder);
                    if (!file22.renameTo(file32)) {
                        throw new ServerConfigException("Could not convert file for " + fileName);
                    }
                }

                private String getPlayerFileName(String string) {
                    String string2 = null;
                    for (String string3 : strings) {
                        if (string3 == null || !string3.equalsIgnoreCase(string)) continue;
                        string2 = string3;
                        break;
                    }
                    if (string2 == null) {
                        throw new ServerConfigException("Could not find the filename for " + string + " anymore");
                    }
                    return string2;
                }
            };
            ServerConfigHandler.lookupProfile(minecraftServer, Lists.newArrayList(strings), profileLookupCallback);
        } catch (ServerConfigException lv) {
            LOGGER.error("Conversion failed, please try again later", lv);
            return false;
        }
        return true;
    }

    static void createDirectory(File directory) {
        if (directory.exists()) {
            if (directory.isDirectory()) {
                return;
            }
            throw new ServerConfigException("Can't create directory " + directory.getName() + " in world save directory.");
        }
        if (!directory.mkdirs()) {
            throw new ServerConfigException("Can't create directory " + directory.getName() + " in world save directory.");
        }
    }

    public static boolean checkSuccess(MinecraftServer server) {
        boolean bl = ServerConfigHandler.checkListConversionSuccess();
        bl = bl && ServerConfigHandler.checkPlayerConversionSuccess(server);
        return bl;
    }

    private static boolean checkListConversionSuccess() {
        boolean bl = false;
        if (BANNED_PLAYERS_FILE.exists() && BANNED_PLAYERS_FILE.isFile()) {
            bl = true;
        }
        boolean bl2 = false;
        if (BANNED_IPS_FILE.exists() && BANNED_IPS_FILE.isFile()) {
            bl2 = true;
        }
        boolean bl3 = false;
        if (OPERATORS_FILE.exists() && OPERATORS_FILE.isFile()) {
            bl3 = true;
        }
        boolean bl4 = false;
        if (WHITE_LIST_FILE.exists() && WHITE_LIST_FILE.isFile()) {
            bl4 = true;
        }
        if (bl || bl2 || bl3 || bl4) {
            LOGGER.warn("**** FAILED TO START THE SERVER AFTER ACCOUNT CONVERSION!");
            LOGGER.warn("** please remove the following files and restart the server:");
            if (bl) {
                LOGGER.warn("* {}", (Object)BANNED_PLAYERS_FILE.getName());
            }
            if (bl2) {
                LOGGER.warn("* {}", (Object)BANNED_IPS_FILE.getName());
            }
            if (bl3) {
                LOGGER.warn("* {}", (Object)OPERATORS_FILE.getName());
            }
            if (bl4) {
                LOGGER.warn("* {}", (Object)WHITE_LIST_FILE.getName());
            }
            return false;
        }
        return true;
    }

    private static boolean checkPlayerConversionSuccess(MinecraftServer server) {
        File file = ServerConfigHandler.getLevelPlayersFolder(server);
        if (file.exists() && file.isDirectory() && (file.list().length > 0 || !file.delete())) {
            LOGGER.warn("**** DETECTED OLD PLAYER DIRECTORY IN THE WORLD SAVE");
            LOGGER.warn("**** THIS USUALLY HAPPENS WHEN THE AUTOMATIC CONVERSION FAILED IN SOME WAY");
            LOGGER.warn("** please restart the server and if the problem persists, remove the directory '{}'", (Object)file.getPath());
            return false;
        }
        return true;
    }

    private static File getLevelPlayersFolder(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.PLAYERS).toFile();
    }

    private static void markFileConverted(File file) {
        File file2 = new File(file.getName() + ".converted");
        file.renameTo(file2);
    }

    static Date parseDate(String dateString, Date fallback) {
        Date date2;
        try {
            date2 = BanEntry.DATE_FORMAT.parse(dateString);
        } catch (ParseException parseException) {
            date2 = fallback;
        }
        return date2;
    }

    static class ServerConfigException
    extends RuntimeException {
        ServerConfigException(String message, Throwable cause) {
            super(message, cause);
        }

        ServerConfigException(String message) {
            super(message);
        }
    }
}

