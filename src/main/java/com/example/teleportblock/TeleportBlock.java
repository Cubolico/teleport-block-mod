package com.example.teleportblock;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TeleportBlock implements ModInitializer {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(BlockPos.class, new BlockPosSerializer())
            .create();

    private static final File CONFIG_DIR = new File("config/teleportblock");
    private static final File TELEPORT_DATA_FILE = new File(CONFIG_DIR, "teleport_links.json");
    private static final File LANGUAGE_FILE = new File(CONFIG_DIR, "language.txt");
    private static final int PERMISSION_LEVEL = 4; // Admin permission level

    // Map containing teleport links and language strings
    private static Map<String, TeleportLink> teleportLinks = new HashMap<>();
    private static Map<String, String> languageStrings = new HashMap<>();

    // Variables for teleport configuration
    private BlockPos firstBlockPos = null;
    private BlockPos secondBlockPos = null;
    private String firstBlockName = null; // Name for block A
    private boolean isFirstBlockSet = false; // Check if block A is set
    private boolean isSecondBlockSet = false; // Check if block B is set

    @Override
    public void onInitialize() {
        // Initialize teleportLinks map
        teleportLinks = new HashMap<>();

        // Create configuration directory and load teleport links and language strings
        createConfigDirectory();
        loadTeleportLinks();
        loadLanguage();

        // Register teleport check event on each server tick
        ServerTickEvents.END_SERVER_TICK.register(this::checkAllPlayersTeleport);

        // Register block interaction event (honeycomb)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand == Hand.MAIN_HAND && player.getMainHandStack().getItem() == Items.HONEYCOMB) {
                BlockPos blockPos = hitResult.getBlockPos();
                if (player.hasPermissionLevel(PERMISSION_LEVEL)) {
                    return handleHoneycombInteraction(player, blockPos);
                } else {
                    player.sendMessage(Text.literal(getTranslation("no_permission")), false); // Chat message
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        // Register block break event to remove teleport links
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            String posKey = blockPosToString(pos);

            // Check if the block is associated with a teleport
            if (teleportLinks.containsKey(posKey)) {
                // Check if the player is an admin
                if (!player.hasPermissionLevel(PERMISSION_LEVEL)) {
                    // If not an admin, prevent block break
                    player.sendMessage(Text.literal(getTranslation("no_permission_break")), false); // Chat message
                    return false; // Prevent block break
                } else {
                    // If admin, allow block break and remove the link
                    removeTeleportLink(pos);
                    player.sendMessage(Text.literal(getTranslation("teleport_link_removed")), false); // Chat message
                }
            }
            return true; // Allow block break
        });

        // Register commands /tport set1, /tport set2, /tport cancel, /tport reload, and /tport help
CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
    dispatcher.register(CommandManager.literal("tport")
        .requires(source -> source.hasPermissionLevel(PERMISSION_LEVEL)) // Richiede permessi di admin
        .then(CommandManager.literal("set1")
            .then(CommandManager.argument("name", StringArgumentType.greedyString())
                .executes(context -> {
                    PlayerEntity player = context.getSource().getPlayer();
                    if (player != null && firstBlockPos != null) {
                        firstBlockName = StringArgumentType.getString(context, "name");
                        isFirstBlockSet = true;
                        player.sendMessage(Text.literal(getTranslation("block_a_set") + " " + firstBlockName), false); // Messaggio in chat
                        return 1;
                    } else {
                        player.sendMessage(Text.literal(getTranslation("select_block_a")), false); // Messaggio in chat
                        return 0;
                    }
                })
            )
        )
        .then(CommandManager.literal("set2")
            .then(CommandManager.argument("name", StringArgumentType.greedyString())
                .executes(context -> {
                    PlayerEntity player = context.getSource().getPlayer();
                    if (player != null && isFirstBlockSet && secondBlockPos != null) {
                        String secondBlockName = StringArgumentType.getString(context, "name");
                        setBlocks(player, firstBlockName, secondBlockName);
                        resetTeleportSetup();
                        return 1;
                    } else {
                        player.sendMessage(Text.literal(getTranslation("select_both_blocks")), false); // Messaggio in chat
                        return 0;
                    }
                })
            )
        )
        .then(CommandManager.literal("cancel")
            .executes(context -> {
                resetTeleportSetup();
                context.getSource().sendFeedback(() -> Text.literal(getTranslation("teleport_canceled")), false); // Messaggio in chat
                return 1;
            })
        )
        .then(CommandManager.literal("reload")
            .executes(context -> {
                loadTeleportLinks();
                loadLanguage();
                context.getSource().sendFeedback(() -> Text.literal(getTranslation("config_reloaded")), false); // Messaggio in chat
                return 1;
            })
        )
        .then(CommandManager.literal("usage")
            .executes(context -> {
                PlayerEntity player = context.getSource().getPlayer();
                if (player != null) {
                    // Visualizza i messaggi di aiuto
                    player.sendMessage(Text.literal(getTranslation("help_usage")), false); // Messaggio in chat
                    player.sendMessage(Text.literal(getTranslation("help_steps")), false); // Messaggio in chat
                }
                return 1;
            })
        )
    );
});

    }

    /**
     * Function to translate a key to a string from the language file.
     */
    private String getTranslation(String key) {
        return languageStrings.getOrDefault(key, key);
    }

    /**
     * Checks if players are walking on a teleport block and teleports them if necessary.
     */
    private void checkAllPlayersTeleport(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            checkPlayerTeleport(player);
        }
    }

    /**
     * Teleports a player if they are standing on a teleport block.
     */
    private void checkPlayerTeleport(ServerPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos().down();
        String posKey = blockPosToString(playerPos);

        if (teleportLinks.containsKey(posKey)) {
            TeleportLink linkedLocation = teleportLinks.get(posKey);
            BlockPos targetPos = linkedLocation.getDestination();
            Vec3d teleportPos = getOffsetTeleportPosition(targetPos, player);

            player.teleport(player.getServerWorld(), teleportPos.x, teleportPos.y, teleportPos.z, player.getYaw(), player.getPitch());
            player.sendMessage(Text.literal(getTranslation("teleported_to") + " " + linkedLocation.getLocationName()), true); // Message in action bar
        }
    }

    private Vec3d getOffsetTeleportPosition(BlockPos targetPos, PlayerEntity player) {
        Vec3d direction = player.getRotationVec(1.0F).normalize();
        double offsetX = direction.x;
        double offsetZ = direction.z;
        return new Vec3d(targetPos.getX() + 0.5 + offsetX, targetPos.getY() + 1, targetPos.getZ() + 0.5 + offsetZ);
    }

    /**
     * Handles interaction with a block using the honeycomb.
     */
    private ActionResult handleHoneycombInteraction(PlayerEntity player, BlockPos blockPos) {
        if (firstBlockPos == null && !isFirstBlockSet) {
            firstBlockPos = blockPos;
            player.sendMessage(Text.literal(getTranslation("block_a_selected")), false); // Chat message
        } else if (isFirstBlockSet && secondBlockPos == null && !isSecondBlockSet) {
            secondBlockPos = blockPos;
            isSecondBlockSet = true;
            player.sendMessage(Text.literal(getTranslation("block_b_selected")), false); // Chat message
        } else {
            player.sendMessage(Text.literal(getTranslation("incorrect_sequence")), false); // Chat message
        }
        return ActionResult.SUCCESS;
    }

    /**
     * Sets the teleport links between blocks A and B.
     */
    private void setBlocks(PlayerEntity player, String firstBlockName, String secondBlockName) {
        if (firstBlockPos != null && secondBlockPos != null) {
            String firstPosKey = blockPosToString(firstBlockPos);
            String secondPosKey = blockPosToString(secondBlockPos);

            if (teleportLinks.containsKey(firstPosKey) || teleportLinks.containsKey(secondPosKey)) {
                player.sendMessage(Text.literal(getTranslation("error_block_linked")), false); // Chat message
                resetTeleportSetup();
                return;
            }

            teleportLinks.put(firstPosKey, new TeleportLink(secondBlockPos, secondBlockName));
            teleportLinks.put(secondPosKey, new TeleportLink(firstBlockPos, firstBlockName));
            saveTeleportLinks();

            player.sendMessage(Text.literal(getTranslation("teleport_link_set") + " " + firstBlockName + " and " + secondBlockName), false); // Chat message
        } else {
            player.sendMessage(Text.literal(getTranslation("select_both_blocks")), false); // Chat message
        }
    }

    /**
     * Resets the current teleport setup.
     */
    private void resetTeleportSetup() {
        firstBlockPos = null;
        secondBlockPos = null;
        firstBlockName = null;
        isFirstBlockSet = false;
        isSecondBlockSet = false;
    }

    /**
     * Removes the teleport links associated with a block.
     */
    private void removeTeleportLink(BlockPos pos) {
        String posKey = blockPosToString(pos);
        if (teleportLinks.containsKey(posKey)) {
            TeleportLink linkedLocation = teleportLinks.get(posKey);
            String linkedPosKey = blockPosToString(linkedLocation.getDestination());

            teleportLinks.remove(posKey);
            teleportLinks.remove(linkedPosKey);
            saveTeleportLinks();
        }
    }

    private String blockPosToString(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private void createConfigDirectory() {
        if (!CONFIG_DIR.exists()) {
            if (CONFIG_DIR.mkdirs()) {
                System.out.println("Configuration directory created: " + CONFIG_DIR.getAbsolutePath());
            } else {
                System.err.println("Failed to create configuration directory: " + CONFIG_DIR.getAbsolutePath());
            }
        }
    }

    /**
     * Loads teleport links from the configuration file.
     */
    private void loadTeleportLinks() {
        if (TELEPORT_DATA_FILE.exists()) {
            try (FileReader reader = new FileReader(TELEPORT_DATA_FILE)) {
                Type type = new TypeToken<Map<String, TeleportLink>>() {}.getType();
                Map<String, TeleportLink> loadedLinks = GSON.fromJson(reader, type);
                if (loadedLinks != null) {
                    teleportLinks.putAll(loadedLinks);
                    System.out.println("Teleport links loaded: " + teleportLinks.size());
                } else {
                    System.out.println("No teleport links found in the file.");
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                teleportLinks = new HashMap<>();
                saveTeleportLinks();
            }
        } else {
            teleportLinks = new HashMap<>();
            saveTeleportLinks();
            System.out.println("New teleport links file created.");
        }
    }

    /**
     * Saves teleport links to the configuration file.
     */
    private void saveTeleportLinks() {
        try (FileWriter writer = new FileWriter(TELEPORT_DATA_FILE)) {
            GSON.toJson(teleportLinks, writer);
            System.out.println("Teleport links saved: " + teleportLinks.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads language strings from the configuration file.
     */
    private void loadLanguage() {
        if (LANGUAGE_FILE.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(LANGUAGE_FILE))) {
                languageStrings.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        languageStrings.put(parts[0].trim(), parts[1].trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            createDefaultLanguageFile();
        }
    }

    /**
     * Creates the default language file.
     */
    private void createDefaultLanguageFile() {
        languageStrings.put("loaded", "[TeleportBlock Mod] loaded");
        languageStrings.put("no_permission", "You do not have permission to create or modify teleports.");
        languageStrings.put("block_a_set", "Block A set with name");
        languageStrings.put("select_block_a", "You must first select block A with the honeycomb!");
        languageStrings.put("block_a_selected", "Block A selected! Use /tport set1 <name> to set the name and then select block B with the honeycomb.");
        languageStrings.put("block_b_selected", "Block B selected! Use /tport set2 <name> to link the blocks.");
        languageStrings.put("incorrect_sequence", "You must follow the correct sequence. Use /tport set1 first, then /tport set2.");
        languageStrings.put("teleport_canceled", "Teleport setup canceled.");
        languageStrings.put("teleport_link_set", "Teleport set between");
        languageStrings.put("select_both_blocks", "You must select both blocks first.");
        languageStrings.put("error_block_linked", "Error: One of the blocks is already linked!");
        languageStrings.put("teleport_link_removed", "The teleport link has been removed.");
        languageStrings.put("teleported_to", "Teleported to");
        languageStrings.put("config_reloaded", "Configuration reloaded.");
        languageStrings.put("no_permission_break", "You do not have permission to break this teleport block.");
        languageStrings.put("help_usage", "Usage: /tport set1 <name>, /tport set2 <name>, /tport cancel, /tport reload, /tport help");
        languageStrings.put("help_steps", "Steps to create a teleport:\n1. Use the honeycomb on block A\n2. Use /tport set1 <name>\n3. Use the honeycomb on block B\n4. Use /tport set2 <name> to link the blocks.");
        saveLanguage();
    }

    /**
     * Saves language strings to the configuration file.
     */
    private void saveLanguage() {
        try (FileWriter writer = new FileWriter(LANGUAGE_FILE)) {
            for (Map.Entry<String, String> entry : languageStrings.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Serializes and deserializes BlockPos in JSON format.
     */
    public static class BlockPosSerializer implements JsonSerializer<BlockPos>, JsonDeserializer<BlockPos> {
        @Override
        public JsonElement serialize(BlockPos src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", src.getX());
            obj.addProperty("y", src.getY());
            obj.addProperty("z", src.getZ());
            return obj;
        }

        @Override
        public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int x = obj.get("x").getAsInt();
            int y = obj.get("y").getAsInt();
            int z = obj.get("z").getAsInt();
            return new BlockPos(x, y, z);
        }
    }

    /**
     * Class representing a teleport link.
     */
    public static class TeleportLink {
        private final BlockPos destination;
        private final String locationName;

        public TeleportLink(BlockPos destination, String locationName) {
            this.destination = destination;
            this.locationName = locationName;
        }

        public BlockPos getDestination() {
            return destination;
        }

        public String getLocationName() {
            return locationName;
        }
    }
}
