package qeorm.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Type;

/**
 * Created by ashen on 2017-2-3.
 */
public class JsonUtils {
    public static <T> T convert(Object object, Class<T> clz) {
        if (object.getClass().getName().equals(clz.getName())) return (T) object;
        String json = "";
        if (object instanceof String) json = (String) object;
        else json = toJson(object);
        return (T) JSON.parseObject(json, clz);
    }

    public static <T> T convert(Object object, Type type) {
        if (type instanceof Class<?>)
            return (T) convert(object, (Class) type);
        if (object.getClass().getName().equals(type.getClass())) return (T) object;
        String json = "";
        if (object instanceof String) json = (String) object;
        else json = toJson(object);
        return (T) JSON.parseObject(json, type);
    }


    public static String toJson(Object object) {
        if (object == null) return "null";
        if (object instanceof String) return String.valueOf(object);
        return JSON.toJSONString(object, SerializerFeature.WriteDateUseDateFormat);
    }

}
