package qeorm;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by ashen on 2017-4-14.
 */
public class RealClass {
    static Map<String, Class> map = Maps.newHashMap();
    static Map<Class, Boolean> primitive = Maps.newHashMap();

    static {
        map.put("int", Integer.class);
        map.put("boolean", Boolean.class);
        map.put("byte", Byte.class);
        map.put("short", Short.class);
        map.put("long", Long.class);
        map.put("float", Float.class);
        map.put("double", Double.class);
        map.put("char", Character.class);
        map.put("String", String.class);
        for (Map.Entry<String, Class> entry : map.entrySet()) {
            primitive.put(entry.getValue(), true);
        }

    }

    public static Class getRealClass(String className) {
        if (!map.containsKey(className)) {
            try {
                Class klass = Class.forName(className);
                map.put(className, klass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (map.containsKey(className))
            return map.get(className);
        return null;
    }

    public static boolean isPrimitive(Class klass) {
        return primitive.containsKey(klass);
    }
}
