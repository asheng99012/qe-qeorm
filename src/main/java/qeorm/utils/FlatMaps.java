package qeorm.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatMaps {
    public static Map<String, Object> flatMap(Map<String, Object> params) {
        Map<String, Object> map = new HashMap<>();
        flatMap(map, "", params);
        return map;
    }

    public static void flatMap(Map<String, Object> map, String prefix, Map<String, Object> params) {
        if (params == null) return;
        params.forEach((key, val) -> {
            String curKey = prefix + key;
            if (val instanceof Map) {
                flatMap(map, curKey + ".", (Map) val);
            } else if (val instanceof List) {
                List list = (List) val;
                if (list.size() > 0 && list.get(0) instanceof Map) {
                    flatMap(map, curKey + ".", (Map) list.get(0));
                }
            } else {
                map.put(curKey, val);
            }
        });
    }
}
