package qeorm.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils {
    public static Map<String, Method> getMethpodMap(Class klass) {
        Method[] methods = klass.getMethods();
        Map<String, Method> map = new HashMap<>();
        for (Method method : methods) {
            map.put(method.getName(), method);
        }
        return map;
    }

    public static void config(Object obj, Map<String, String> config) {
        Map<String, Method> map = getMethpodMap(obj.getClass());
        for (Map.Entry<String, String> kv : config.entrySet()) {
            String field = kv.getKey();
            try {
                String methodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
                if (map.containsKey(methodName)) {
                    Method method = map.get(methodName);
                    Type[] paramsTypes = method.getGenericParameterTypes();
                    Object[] params = new Object[1];
                    params[0] = JsonUtils.convert(kv.getValue(), paramsTypes[0]);
                    Object ret = method.invoke(obj, params);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }
}
