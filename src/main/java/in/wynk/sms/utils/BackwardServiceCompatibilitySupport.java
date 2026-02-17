package in.wynk.sms.utils;

import java.util.HashMap;
import java.util.Map;

public class BackwardServiceCompatibilitySupport {

    private static Map<String, String> aliasMap = new HashMap<>();

    static {
        aliasMap.put("WYNK", "music");
        aliasMap.put("airteltv", "airtelxstream");
        aliasMap.put("AIRTEL_TV", "airtelxstream");
        aliasMap.put("AIRTEL_BOOKS", "books_sms");
    }

    public static String resolve(String serviceName, String defaultValue) {
        return aliasMap.containsKey(serviceName) ? aliasMap.get(serviceName): defaultValue;
    }

}
