package me.angrypostman.freeforall.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.angrypostman.freeforall.FreeForAll;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Updater {

    private FreeForAll plugin = null;
    private String latestVersion = null;

    public Updater(FreeForAll plugin) {
        this.plugin = plugin;
    }

    private boolean checkHigher(String currentVersion, String newVersion) {
        String current = toReadable(currentVersion);
        String newVers = toReadable(newVersion);
        return current.compareTo(newVers) < 0;
    }

    public void checkUpdate(String currentVersion) throws IOException {

        URL url = new URL("https://api.spiget.org/v2/resources/81/versions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0"); // Mozilla 5.0 User-Agent

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);

        JsonElement element = new JsonParser().parse(reader);
        JsonArray jsonArray = element.getAsJsonArray();

        //GET /resources/{version}/versions returns in Oldest > Newest order
        //So we need to reverse it to get the latest version
        jsonArray = reverseArray(jsonArray);

        JsonObject object = jsonArray.get(0).getAsJsonObject();

        reader.close();
        inputStream.close();
        connection.disconnect();

        if (!checkHigher(currentVersion, object.get("name").getAsString())) return;

        this.latestVersion = object.get("name").getAsString();

    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public boolean hasUpdate() throws IOException {
        checkUpdate(plugin.getConfiguration().getVersion());
        return getLatestVersion() != null;
    }

    private String toReadable(String version) {
        String[] split = Pattern.compile(".", Pattern.LITERAL).split(version.replace("v", ""));
        StringBuilder versionBuilder = new StringBuilder();
        for (String s : split) {
            versionBuilder.append(String.format("%4s", s));
        }
        version = versionBuilder.toString();
        return version;
    }

    private JsonArray reverseArray(JsonArray jsonArray) {
        JsonArray arr = new JsonArray();
        for (int i = jsonArray.size()-1; i >= 0; i--) {
            arr.add(jsonArray.get(i));
        }
        return arr;
    }

}
