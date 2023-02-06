package one.devos.nautical.teabridge.util;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public class JsonUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    private static final Object2ReferenceOpenHashMap<String, Object> BACKING_MAP = new Object2ReferenceOpenHashMap<>();

    public static MapBackedJsonObject fromJsonString(String json) throws Exception {
        return MapBackedJsonObject.fromJsonString(json);
    }

    public static <T> String toJsonString(T obj, Function<MapBackedJsonObject, MapBackedJsonObject> dataMaker) throws Exception {
        return MapBackedJsonObject.toJsonString(obj, dataMaker);
    }

    public record MapBackedJsonObject(Map<String, Object> backingMap) {
        private static final Type BACKING_MAP_TYPE = TypeToken.getParameterized(Map.class, String.class, Object.class).getType();

        public static MapBackedJsonObject fromJsonString(String json) {
            return new MapBackedJsonObject(GSON.fromJson(json, BACKING_MAP_TYPE));
        }

        public static <T> String toJsonString(T obj, Function<MapBackedJsonObject, MapBackedJsonObject> dataMaker) {
            var jsonObject = new MapBackedJsonObject(BACKING_MAP);
            dataMaker.apply(jsonObject);
            var jsonString = jsonObject.toJsonString();
            BACKING_MAP.clear();
            return jsonString;
        }

        public <T> MapBackedJsonObject put(String key, T obj) {
            backingMap.putIfAbsent(key, obj);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String key) throws Exception {
            final Object obj = backingMap.get(key);
            if (obj != null) {
                return (T) backingMap.get(key);
            } else {
                throw new Exception("Key: " + key + " does not exist in " + backingMap.keySet());
            }
        }

        public String toJsonString() {
            return GSON.toJson(backingMap);
        }
    }
}
