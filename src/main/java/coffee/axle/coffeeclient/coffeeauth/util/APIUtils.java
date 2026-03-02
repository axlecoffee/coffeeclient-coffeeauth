package coffee.axle.coffeeclient.coffeeauth.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Mojang / Minecraft Services API utilities.
 * <p>
 * Uses only {@link java.net.HttpURLConnection} — no Apache HttpClient needed.
 */
public class APIUtils {

    private static final String PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    private static final String CHANGE_NAME_URL = "https://api.minecraftservices.com/minecraft/profile/name/";
    private static final String CHANGE_SKIN_URL = "https://api.minecraftservices.com/minecraft/profile/skins";
    private static final String ONLINE_CHECK_URL = "https://api.hypixel.net/v2/player?name=";

    /**
     * Fetches the profile name and UUID for the given bearer token.
     *
     * @return {@code [name, uuid]}
     */
    public static String[] getProfileInfo(String token) throws IOException {
        HttpURLConnection conn = openGet(PROFILE_URL, token);
        try {
            String json = readResponse(conn);
            JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
            String name = obj.get("name").getAsString();
            String uuid = obj.get("id").getAsString();
            return new String[] { name, uuid };
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Returns {@code true} when the token still belongs to the current session
     * user.
     */
    public static boolean validateSession(String token) {
        try {
            String[] info = getProfileInfo(token);
            Minecraft mc = Minecraft.getMinecraft();
            return info[0].equals(mc.getSession().getUsername())
                    && info[1].equals(mc.getSession().getPlayerID());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a player has played Hypixel before using the public API.
     * <p>
     * The Hypixel v2 API without an API key only tells us whether the player
     * exists;
     * true online status requires a valid API key. Returns {@code false} on any
     * error.
     */
    public static boolean checkOnline(String username) {
        HttpURLConnection conn = null;
        try {
            conn = openGet(ONLINE_CHECK_URL + username, null);
            int code = conn.getResponseCode();
            if (code != 200)
                return false;
            String json = readResponse(conn);
            JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
            // Without an API key the "player" field will be null, but a non-null
            // response with success=true means the player exists on Hypixel.
            return obj.has("player") && !obj.get("player").isJsonNull();
        } catch (Exception e) {
            // Silently fail — the API may be rate-limited or unreachable.
            return false;
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }

    /**
     * Changes the in-game name via Mojang API.
     *
     * @return HTTP status code
     */
    public static int changeName(String newName, String token) throws IOException {
        HttpURLConnection conn = openConnection(CHANGE_NAME_URL + newName, "PUT", token);
        try {
            return conn.getResponseCode();
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Changes the player skin via Mojang API.
     *
     * @return HTTP status code
     */
    public static int changeSkin(String url, String token) throws IOException {
        String body = String.format("{ \"variant\": \"classic\", \"url\": \"%s\"}", url);
        HttpURLConnection conn = openConnection(CHANGE_SKIN_URL, "POST", token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            return conn.getResponseCode();
        } finally {
            conn.disconnect();
        }
    }

    // ── Internal helpers ────────────────────────────────────────────────

    private static HttpURLConnection openGet(String url, String bearerToken) throws IOException {
        return openConnection(url, "GET", bearerToken);
    }

    private static HttpURLConnection openConnection(String url, String method, String bearerToken) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        if (bearerToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
        }
        return conn;
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
