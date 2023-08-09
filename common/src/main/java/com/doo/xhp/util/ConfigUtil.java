package com.doo.xhp.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class ConfigUtil {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static Path path;

    protected static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigUtil() {
    }

    public static synchronized boolean copyTo(String fileName, JsonObject target) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        if (path == null) {
            path = FileSystems.getDefault().getPath("config", "%s.json".formatted(fileName));
        }

        try (FileChannel open = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ)) {
            int size = (int) open.size();
            if (size < 1) {
                return false;
            }

            ByteBuffer bb = ByteBuffer.allocate(size);
            open.read(bb);
            copy(target, JSON.fromJson(new String(bb.array(), StandardCharsets.UTF_8), JsonObject.class));
            return true;
        } catch (Exception ignored) {
            LOGGER.warn("Read config file {} error", path);
            write(target);
            return false;
        }
    }

    private static void copy(JsonObject target, JsonObject from) {
        if (from == null || from.size() < 1 || target == from) {
            return;
        }

        for (Map.Entry<String, JsonElement> entry : from.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                copy(target.get(entry.getKey()).getAsJsonObject(), entry.getValue().getAsJsonObject());
            } else {
                target.add(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void write(JsonObject value) {
        if (path == null) {
            return;
        }

        try (FileChannel open = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            open.truncate(0);
            open.write(ByteBuffer.wrap(JSON.toJson(value).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ignored) {
            LOGGER.warn("Write config file {} error: {}", path, value);
        }
    }

    public static NativeImage readImage(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        Path p = FileSystems.getDefault().getPath("resourcepacks", name);
        try (FileChannel open = FileChannel.open(p, StandardOpenOption.READ)) {
            int size = (int) open.size();
            if (size < 1) {
                return null;
            }

            ByteBuffer bb = ByteBuffer.allocate(size);
            open.read(bb);
            return NativeImage.read(bb.array());
        } catch (Exception ignored) {
            LOGGER.warn("Read image file {} error", path);
        }
        return null;
    }
}
