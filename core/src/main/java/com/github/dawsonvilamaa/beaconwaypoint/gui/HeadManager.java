package com.github.dawsonvilamaa.beaconwaypoint.gui;

import com.github.dawsonvilamaa.beaconwaypoint.Main;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class HeadManager {
    private final ConcurrentHashMap<String, String> skinCache = new ConcurrentHashMap<>();
    private static final OkHttpClient client = new OkHttpClient();

    private String readUrl(String urlString) throws Exception {
        Request request = new Request.Builder()
                .url(urlString)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:139.0) Gecko/20100101 Firefox/139.0")
                .header("Accept", "application/json")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate, br, zstd")
                .header("Connection", "keep-alive")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP error code " + response.code() + " for URL: " + urlString);
            }
            String res = response.body().string();
            return res;
        }
    }

    private String getPlayerSkinBase64(String username) {
        String cachedSkin = skinCache.get(username);
        if (cachedSkin != null) {
            return cachedSkin;
        }
        else {
            Bukkit.broadcastMessage("Skin for " + username + " not cached, fetching...");
            try {
                JSONParser parser = new JSONParser();
                JSONObject uuidObj = (JSONObject) parser.parse(readUrl("https://api.mojang.com/users/profiles/minecraft/" + username));
                String uuid = (String) uuidObj.get("id");

                String profileJson = readUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
                JSONObject profileObj = (JSONObject) parser.parse(profileJson);
                JSONArray properties = (JSONArray) profileObj.get("properties");
                JSONObject property = (JSONObject) properties.toArray()[0];

                String base64Skin = (String) property.get("value");
                skinCache.put(username, base64Skin);
                return base64Skin;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void fetchSkinAsync(String username, Consumer<String> cb) {
        String cachedSkin = skinCache.get(username);
        if (cachedSkin != null) {
            cb.accept(cachedSkin);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            Bukkit.broadcastMessage("Skin for " + username + " not cached, fetching...");
            String base64Skin = null;
            try {
                JSONParser parser = new JSONParser();
                JSONObject uuidObj = (JSONObject) parser.parse(readUrl("https://api.mojang.com/users/profiles/minecraft/" + username));
                String uuid = (String) uuidObj.get("id");

                String profileJson = readUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
                JSONObject profileObj = (JSONObject) parser.parse(profileJson);
                JSONArray properties = (JSONArray) profileObj.get("properties");
                JSONObject property = (JSONObject) properties.toArray()[0];

                base64Skin = (String) property.get("value");
                if (base64Skin != null) {
                    skinCache.put(username, base64Skin);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final String result = base64Skin;

            Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
                cb.accept(result);
            });
        });
    }

    public ItemStack getPlayerHead(String base64Texture, String username) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            if (Integer.parseInt(Bukkit.getServer().getBukkitVersion().replaceAll("\\.", "").substring(0, 2)) < 119) {
                GameProfile profile = new GameProfile(UUID.randomUUID(), username);
                profile.getProperties().put("textures", new Property("textures", base64Texture));
                try {
                    Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skullMeta, profile);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            else {

            }
        }
        return head;
    }

    public InventoryGUIButton createHeadButton(InventoryGUI gui, String name, String description, String playerName) {
        InventoryGUIButton button = new InventoryGUIButton(gui, name, description, Material.PLAYER_HEAD);
        button.setName(name);
        button.setDescription(description);

        fetchSkinAsync(playerName, (String base64Texture) -> {
            Bukkit.broadcastMessage("Got " + playerName + "'s head");
            Bukkit.broadcastMessage("Slot " + button.getSlot());
            if (base64Texture != null) {
                ItemStack head = getPlayerHead(base64Texture, playerName);
                button.setItem(head);
                gui.setButton(button.getSlot(), button);
            }
            else {
                Bukkit.broadcastMessage("Couldn't get skull for " + playerName);
            }
        });
        return button;
    }
}
