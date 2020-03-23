package qeorm.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by asheng on 2015/6/26 0026.
 */
public class ExtendUtils {
    public static String EXTEND = "extend";
    public static String ONE2ONE = "one2One";
    public static String ONE2MANY = "one2Many";

    public static <T> T extend(T o1, Object o2) {
        if (o2 == null) return o1;

        Map m1 = Wrap.toMap(o1);
        Map m2 = Wrap.toMap(o2);
        m1.putAll(m2);
        return o1;
    }


    public static <T> List<T> extend(List<T> l1, List l2, String self, String mappedBy) {
        List<T> list = new ArrayList<T>();
        Map<String, Object> data = new HashMap<String, Object>();
        for (int i = 0; i < l2.size(); i++) {
            Object cur = l2.get(i);
            data.put(Wrap.getWrap(cur).getValue(mappedBy).toString(), cur);
//            Map map = JsonUtils.convert(l2.get(i), HashMap.class);
//            data.put(map.get(mappedBy).toString(), map);
        }

        for (int i = 0; i < l1.size(); i++) {
            T cur = l1.get(i);
            Object selfVal = Wrap.getWrap(cur).getValue(self);
            list.add(extend(cur, (selfVal == null || selfVal.toString().equals("")) ? null : data.get(selfVal.toString())));

//            Map m1 = JsonUtils.convert(l1.get(i), HashMap.class);
//            list.add((T) JsonUtils.convert(extend(m1, m1.get(self) == null ? null : data.get(m1.get(self).toString())), l1.get(i).getClass()));
        }
        return list;
    }

    public static <T> List<T> extendOne2One(List<T> l1, List l2, String self, String mappedBy, String fillKey) {
        List<T> list = new ArrayList<T>();
        Map<String, Object> data = new HashMap<String, Object>();
        for (int i = 0; i < l2.size(); i++) {
            Object cur = l2.get(i);
            data.put(Wrap.getWrap(cur).getValue(mappedBy).toString(), cur);
//            Map map = JsonUtils.convert(l2.get(i), HashMap.class);
//            data.put(map.get(mappedBy).toString(), map);
        }

        for (int i = 0; i < l1.size(); i++) {
            T cur = l1.get(i);
            Object selfVal = Wrap.getWrap(cur).getValue(self);
            if (selfVal != null && !selfVal.toString().equals("")) {
                Wrap.getWrap(cur).setValue(fillKey, data.get(selfVal.toString()));
            }
            list.add(cur);

//            Map m1 = JsonUtils.convert(l1.get(i), HashMap.class);
//            m1.put(fillKey, m1.get(self) == null ? null : data.get(m1.get(self).toString()));
//            list.add((T) JsonUtils.convert(m1, l1.get(i).getClass()));
        }
        return list;
    }

    public static <T> List<T> extendOne2Many(List<T> l1, List l2, String self, String mappedBy, String fillKey) {
        List<T> list = new ArrayList<T>();
        Map<String, List> data = new HashMap<String, List>();
        String key;
        Map map;
        for (int i = 0; i < l2.size(); i++) {
            Object cur = l2.get(i);
            key = Wrap.getWrap(cur).getValue(mappedBy).toString();
            if (!data.containsKey(key))
                data.put(key, new ArrayList());
            data.get(key).add(cur);

//            map = JsonUtils.convert(l2.get(i), HashMap.class);
//            key = map.get(mappedBy).toString();
//            if (!data.containsKey(key))
//                data.put(key, new ArrayList());
//            data.get(key).add(map);
        }

        for (int i = 0; i < l1.size(); i++) {
            T cur = l1.get(i);
            Object selfVal = Wrap.getWrap(cur).getValue(self);
            if (selfVal != null && !selfVal.toString().equals("")) {
                Wrap.getWrap(cur).setValue(fillKey, data.get(selfVal.toString()));
            }
            list.add(cur);

//
//            Map m1 = JsonUtils.convert(l1.get(i), HashMap.class);
//            m1.put(fillKey, m1.get(self) == null ? null : data.get(m1.get(self).toString()));
//            list.add((T) JsonUtils.convert(m1, l1.get(i).getClass()));
        }
        return list;
    }
}
