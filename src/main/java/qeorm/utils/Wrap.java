package qeorm.utils;

import com.google.common.base.Splitter;
import org.springframework.cglib.beans.BeanMap;

import java.util.Iterator;
import java.util.Map;

public class Wrap {
    private Object target;

    public static Wrap getWrap(Object obj) {
        Wrap wrap = new Wrap();
        wrap.target = obj;
        return wrap;
    }

    public <T> T getValue(String path) {
        Iterator<String> iterator = Splitter.on(".").split(path).iterator();
        String filed;
        Object ret = target;
        while (ret != null && iterator.hasNext()) {
            filed = iterator.next();
            ret = getValue(ret, filed);
        }
        return (T) ret;
    }

    private Object getValue(Object obj, String filed) {
        Map map = toMap(obj);
        Object ret = null;
        if (map.containsKey(filed))
            ret = map.get(filed);
        return ret;
    }

    public void setValue(String path, Object val) {
        Iterator<String> iterator = Splitter.on(".").split(path).iterator();
        String filed;
        Object ret = target;
        boolean hasNext = iterator.hasNext();
        while (ret != null && hasNext) {
            filed = iterator.next();
            hasNext = iterator.hasNext();
            if (hasNext) {
                ret = getValue(ret, filed);
            } else {
                setValue(ret, filed, val);
            }
        }
    }

    private void setValue(Object obj, String filed, Object val) {
        Map map = toMap(obj);
        try {
            map.put(filed, val);
        } catch (Exception e) {
            if (map.containsKey(filed)) {
                try {
                    map.put(filed, JsonUtils.convert(val, obj.getClass().getDeclaredField(filed).getType()));
                } catch (NoSuchFieldException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static Map toMap(Object obj) {
        if (obj instanceof Map)
            return (Map) obj;
        else return BeanMap.create(obj);
    }
}
