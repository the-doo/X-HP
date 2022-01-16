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

    /**
     * 日志打印
     */
    public static final Logger LOGGER = LogManager.getLogger();

    /**
     * 文件路径
     */
    private static final Path path = FileSystems.getDefault().getPath("config", "doo.json");

    /**
     * json转换器
     */
    private static final Gson json = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 读取配置
     *
     * @param key key（modId）
     * @param t   T.class
     * @return T
     */
    public static <T> T read(String key, Class<T> t, T defaultValue) {
        try {
            FileChannel open = FileChannel.open(path, StandardOpenOption.READ);
            JsonObject read = read(open);
            JsonElement e = read.get(key);
            return e == null ? defaultValue : json.fromJson(e, t);
        } catch (NoSuchFileException ignored) {
            // 文件不存在（file not found）
        } catch (Exception e) {
            LOGGER.log(Level.WARN, "文件读取失败(read file error) : {}",  path, e);
        }
        return defaultValue;
    }

    /**
     * 读取配置，转成map
     *
     * @param open 文件
     * @return map
     * @throws Exception exception
     */
    private static JsonObject read(FileChannel open) throws Exception {
        ByteBuffer bb = ByteBuffer.allocate((int) open.size());
        if (open.size() < 1) {
            return new JsonObject();
        }
        open.read(bb);
        return json.fromJson(new String(bb.array(), StandardCharsets.UTF_8), JsonObject.class);
    }

    /**
     * 释放锁
     *
     * @param lock 锁
     */
    private static void unlock(FileLock lock) {
        if (lock != null) {
            try {
                lock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存配置
     *
     * @param key   key
     * @param value value
     */
    public static void write(String key, Object value) {
        FileLock lock = null;
        try {
            FileChannel open = FileChannel.open(path,
                    StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            // 等待获取锁10秒
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
            // 读取所有
            JsonObject read = read(open);
            read.add(key, json.toJsonTree(value));
            // 写文件
            open = lock.channel();
            // 清空
            open.truncate(0);
            // 写入
            open.write(ByteBuffer.wrap(json.toJson(read).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            LOGGER.log(Level.WARN, "文件写入失败(write file error) : {}, {}", path, value, e);
        } finally {
            unlock(lock);
        }
    }
}
