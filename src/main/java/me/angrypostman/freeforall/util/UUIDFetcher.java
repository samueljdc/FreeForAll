package me.angrypostman.freeforall.util;

/*
 * Copyright (c) 2015 Nate Mortensen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import com.google.common.collect.ImmutableList;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class UUIDFetcher implements Callable<Map<String, UUID>>{

    private static final double PROFILES_PER_REQUEST=100;
    private static final String PROFILE_URL="https://api.mojang.com/profiles/minecraft";
    private final JSONParser jsonParser=new JSONParser();
    private final List<String> names;
    private final boolean rateLimiting;

    public UUIDFetcher(final List<String> names){
        this(names, true);
    }

    public UUIDFetcher(final List<String> names,
                       final boolean rateLimiting){
        this.names=ImmutableList.copyOf(names);
        this.rateLimiting=rateLimiting;
    }

    public static byte[] toBytes(final UUID uuid){
        final ByteBuffer byteBuffer=ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID fromBytes(final byte[] array){
        if(array.length!=16){
            throw new IllegalArgumentException("Illegal byte array length: "+array.length);
        }
        final ByteBuffer byteBuffer=ByteBuffer.wrap(array);
        final long mostSignificant=byteBuffer.getLong();
        final long leastSignificant=byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    public static UUID getUUIDOf(final String name) throws
                                                    Exception{
        return new UUIDFetcher(Collections.singletonList(name)).call()
                                                               .get(name);
    }

    @Override
    public Map<String, UUID> call() throws
                                    Exception{
        final Map<String, UUID> uuidMap=new HashMap<>();
        final int requests=(int) Math.ceil(this.names.size()/PROFILES_PER_REQUEST);
        for(int i=0; i<requests; i++){
            final HttpURLConnection connection=createConnection();
            final String body=JSONArray.toJSONString(this.names.subList(i*100, Math.min((i+1)*100, this.names.size())));
            writeBody(connection, body);
            final JSONArray array=(JSONArray) this.jsonParser.parse(new InputStreamReader(connection.getInputStream()));

            for(final Object profile : array){
                final JSONObject jsonProfile=(JSONObject) profile;
                final String id=(String) jsonProfile.get("id");
                final String name=(String) jsonProfile.get("name");
                final UUID uuid=UUIDFetcher.getUUID(id);
                uuidMap.put(name, uuid);
            }
            if(this.rateLimiting&&i!=requests-1){ Thread.sleep(100L); }
        }
        return uuidMap;
    }

    private static void writeBody(final HttpURLConnection connection,
                                  final String body) throws
                                                     Exception{
        final OutputStream stream=connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private static HttpURLConnection createConnection() throws
                                                        Exception{
        final URL url=new URL(PROFILE_URL);
        final HttpURLConnection connection=(HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static UUID getUUID(final String id){
        return UUID.fromString(id.substring(0, 8)+"-"+id.substring(8, 12)+"-"+id.substring(12, 16)+"-"+id.substring(16,
                                                                                                                    20)+"-"+id.substring(
                20, 32));
    }
}
