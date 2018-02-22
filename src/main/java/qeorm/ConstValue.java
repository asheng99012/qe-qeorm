package qeorm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ConstValue<T> {
    private T value;
    private String text;

    public static <T> ConstValue create(String text, T value) {
        return new ConstValue<T>(text, value);
    }

    private ConstValue(String text, T value) {
        this.value = value;
        this.text = text;
    }

    public <T> T getValue() {
        return (T) value;
    }

    public String getText() {
        return text;
    }


    private static Map<Class, List<ConstValue>> lists = Maps.newHashMap();

    public static List<ConstValue> values(Class clazz) {
        if (!lists.containsKey(clazz)) {
            Field[] fields = clazz.getFields();
            List<ConstValue> list = Lists.newArrayList();

            try {
                for (int i = 0; i < fields.length; i++) {
                    Object val = fields[i].get(clazz);
                    if (val instanceof ConstValue)
                        list.add((ConstValue) val);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            lists.put(clazz, list);
        }
        return lists.get(clazz);
    }

    public static <T> Map<T, String> maps(Class clazz) {
        List<ConstValue> list = values(clazz);
        Map<T, String> map = Maps.newHashMap();
        for (int i = 0; i < list.size(); i++) {
            ConstValue val = list.get(i);
            map.put((T) val.getValue(), val.getText());
        }
        return map;
    }

    public static <T> String getDesc(Class clazz, T value) {
        Map<T, String> map = maps(clazz);
        if (map.containsKey(value))
            return map.get(value);
        return "";
    }
}
