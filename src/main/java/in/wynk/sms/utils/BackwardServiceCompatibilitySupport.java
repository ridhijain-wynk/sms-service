package in.wynk.sms.utils;

import java.util.HashMap;
import java.util.Map;

public class BackwardServiceCompatibilitySupport {

    private static Map<String, String> aliasMap = new HashMap<>();

    static {
        aliasMap.put("WYNK", "music");
        aliasMap.put("WIFI", "music");
        aliasMap.put("VIDEO", "music");
        aliasMap.put("airteltv", "airtelxstream");
        aliasMap.put("AIRTEL_TV", "airtelxstream");
        aliasMap.put("AIRTEL_MOVIES", "airtelxstream");
        aliasMap.put("AIRTEL_VIDEOS", "airtelxstream");
        aliasMap.put("AIRTEL_BOOKS", "books_sms");
        aliasMap.put("AIRTEL_GAMES", "airtelgaming");
        aliasMap.put("AIRTEL_GAMING", "airtelgaming");
        aliasMap.put("GAMES", "airtelgaming");
        aliasMap.put("BNKR_FIT", "bunkerfit");
    }

    public static String resolve(String serviceName, String defaultValue) {
        return aliasMap.containsKey(serviceName) ? aliasMap.get(serviceName): defaultValue;
    }

}
