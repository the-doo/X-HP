package com.doo.xhp.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 补充工具
 */
public class Config {

    public static final Logger LOGGER = LogManager.getLogger();

    private static final Path path = FileSystems.getDefault().getPath("config", "doo.json");

    private static final Gson json = new GsonBuilder().setPrettyPrinting().create();

    public static <T> T read(String key, Class<T> t, T defaultValue) {
        try {
            FileChannel open = FileChannel.open(path, StandardOpenOption.READ);
            JsonObject read = read(open);
            JsonElement e = read.get(key);
            return e == null ? defaultValue : json.fromJson(e, t);
        } catch (NoSuchFileException ignored) {
        } catch (Exception e) {
            LOGGER.log(Level.WARN, "read file error : {}", path, e);
        }
        return defaultValue;
    }

    private static JsonObject read(FileChannel open) throws Exception {
        ByteBuffer bb = ByteBuffer.allocate((int) open.size());
        if (open.size() < 1) {
            return new JsonObject();
        }
        open.read(bb);
        return json.fromJson(new String(bb.array(), StandardCharsets.UTF_8), JsonObject.class);
    }

    private static void unlock(FileLock lock) {
        if (lock != null) {
            try {
                lock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void write(String key, Object value) {
        FileLock lock = null;
        try {
            FileChannel open = FileChannel.open(path,
                    StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);

            long time = System.currentTimeMillis();
            while (lock == null) {
                try {
                    lock = open.tryLock();
                } catch (OverlappingFileLockException e) {
                    if (System.currentTimeMillis() - time > 10000) {
                        throw e;
                    }
                }
            }

            JsonObject read = read(open);
            read.add(key, json.toJsonTree(value));

            open = lock.channel();

            open.truncate(0);

            open.write(ByteBuffer.wrap(json.toJson(read).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            LOGGER.log(Level.WARN, "write file error : {}, {}", path, value, e);
        } finally {
            unlock(lock);
        }
    }
}
