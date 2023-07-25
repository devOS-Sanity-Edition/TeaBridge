package one.devos.nautical.teabridge.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
}
