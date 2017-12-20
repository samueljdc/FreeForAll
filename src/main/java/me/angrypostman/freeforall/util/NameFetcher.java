package me.angrypostman.freeforall.util;

import com.google.common.collect.ImmutableList;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NameFetcher implements Callable<Map<UUID, String>>{

    private static final String PROFILE_URL="https://sessionserver.mojang.com/session/minecraft/profile/";
    private final JSONParser jsonParser=new JSONParser();
    private final List<UUID> uuids;

    public NameFetcher(final List<UUID> uuids){
        this.uuids=ImmutableList.copyOf(uuids);
    }

    public static String getNameOf(final UUID uuid) throws
                                                    Exception{
        return new NameFetcher(Collections.singletonList(uuid)).call()
                                                               .get(uuid);
    }

    @Override
    public Map<UUID, String> call() throws
                                    Exception{
        final Map<UUID, String> uuidStringMap=new HashMap<>();
        for(final UUID uuid : this.uuids){
            final HttpURLConnection connection=(HttpURLConnection) new URL(PROFILE_URL+uuid.toString()
                                                                                           .replace("-",
                                                                                                    "")).openConnection();
            final JSONObject response=(JSONObject) this.jsonParser.parse(
                    new InputStreamReader(connection.getInputStream()));
            final String name=(String) response.get("name");
            if(name==null){
                continue;
            }
            final String cause=(String) response.get("cause");
            final String errorMessage=(String) response.get("errorMessage");
            if(cause!=null&&cause.length()>0){
                throw new IllegalStateException(errorMessage);
            }
            uuidStringMap.put(uuid, name);
        }
        return uuidStringMap;
    }
}
