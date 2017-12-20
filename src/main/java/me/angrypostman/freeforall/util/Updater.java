package me.angrypostman.freeforall.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import me.angrypostman.freeforall.FreeForAll;

public class Updater{

    private static final String SPIGET_URL="https://api.spiget.org/";
    private static final String SPIGOT_URL="https://spigot.org/";
    private static final int RESOURCE_ID=81;

    public Updater(final FreeForAll plugin){
        this.plugin=plugin;
    }

    public String getLatestVersion(){
        return this.latestVersion;
    }

    public String getLatestVersionURL(){
        return this.latestVersionURL;
    }

    public boolean hasUpdate() throws
                               IOException{
        if(this.latestVersion==null){
            checkUpdate(this.plugin.getDescription()
                                   .getVersion());
        }
        return this.latestVersion!=null;
    }

    public void checkUpdate(final String currentVersion) throws
                                                         IOException{

        final URL url=new URL(String.format("%sv2/resources/%s/versions", SPIGET_URL, RESOURCE_ID));

        final HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0"); // Mozilla 5.0 User-Agent
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        try(InputStream inputStream=connection.getInputStream();
            InputStreamReader reader=new InputStreamReader(inputStream)){

            final JsonElement element=new JsonParser().parse(reader);
            JsonArray jsonArray=element.getAsJsonArray();

            //GET /resources/{resourceId}/versions returns in Oldest > Newest order
            //So we need to reverse it to get the latest version
            jsonArray=reverseArray(jsonArray);

            final JsonObject object=jsonArray.get(0)
                                             .getAsJsonObject();

            final String version=object.get("name")
                                       .getAsString();

            if(!checkHigher(currentVersion, version)){ return; }

            this.latestVersion=version;
            this.latestVersionURL=SPIGOT_URL+"/"+object.get("url")
                                                       .getAsString();
            this.latestVersionURL=this.latestVersionURL.replace("\\", "");
        }
    }

    private boolean checkHigher(final String currentVersion,
                                final String newVersion){
        return currentVersion.compareTo(newVersion)<0;
    }

    private JsonArray reverseArray(final JsonArray jsonArray){
        final JsonArray arr=new JsonArray();
        for(int i=jsonArray.size()-1; i>=0; i--){
            arr.add(jsonArray.get(i));
        }
        return arr;
    }

    private FreeForAll plugin=null;
    private String latestVersion=null;
    private String latestVersionURL=null;
}
