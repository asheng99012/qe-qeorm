package qeorm;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.ReflectionUtils;
import qeorm.annotation.SqlParam;
import qeorm.intercept.IFunIntercept;
import qeorm.utils.ExtendUtils;
import qeorm.utils.JsonUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by ashen on 2017-2-4.
 */
public class ProxyExecutor extends SqlResultExecutor {
    private static Map<String, KV> pairMap = new HashMap<>();

    public ProxyExecutor() {
    }


    public SqlResult exec() {

        TimeWatcher.watch("在数据库" + result.sqlConfig.getDbName() + "上执行" + result.getSql(), new Action() {
            @Override
            public void apply() {
                result.setResult(exec(result.getParams()));
            }
        });
        if (!result.sqlConfig.isPrimitive()) dealFunIntercept(result.getResult());
        if (result.getResult() != null) {
            if (oParams != null && oParams.containsKey("pn"))
                oParams.remove("pn");
        }
        dealSqlIntercepts();
        if (!result.sqlConfig.isPrimitive())
            dealReturnType();
        else if (result.getResult() != null)
            result.setResult(JsonUtils.convert(result.getResult(), result.getSqlConfig().getKlass()));

        return result;
    }

    public <T> T exec(Map<String, Object> map) {
        String proxy = getResult().getSqlConfig().getProxy();
        try {
            Object ret = run(proxy, map);
//            if (ret instanceof List) {
//                ret = JSON.toJSON(ret);
//            }
            return (T) ret;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }


    public Object run(String proxy, Map<String, Object> map) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
        String[] fun = proxy.split("@");
        KV kv = getObjectInfo(fun[0], fun[1]);
        Type[] paramsTypes = kv.getParamsTypes();
        List<String> args = kv.getArgs();
        Object[] params = new Object[paramsTypes.length];
        if (paramsTypes.length == 1) {
            params[0] = JsonUtils.convert(map, paramsTypes[0]);
        }
        if (paramsTypes.length > 1) {
            if (paramsTypes.length != args.size()) {
                throw new RuntimeException(proxy + "方法不正确");
            }
            for (int i = 0; i < paramsTypes.length; i++) {
                params[i] = map.get(args.get(i)) == null ? null : JsonUtils.convert(map.get(args.get(i)), paramsTypes[i]);
            }
        }

        Object ret = kv.getVal().invoke(kv.getKey(), params);
        return ret;
    }


    private KV getObjectInfo(String className, String methodName) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        String action = className + "." + methodName;
        if (!pairMap.containsKey(action)) {
            Object instanse = getInstance(Class.forName(className));
            Method method = ReflectionUtils.findMethod(instanse.getClass(), methodName, null);
            pairMap.put(action, new KV(instanse, method));
        }
        return pairMap.get(action);
    }

    private Object getInstance(Class<?> klass) throws IllegalAccessException, InstantiationException {
        Object instance;
        try {
            instance = SpringUtils.getBean(klass);
        } catch (NoSuchBeanDefinitionException e) {
            instance = klass.newInstance();
        }
        return instance;
    }

    private static class KV {
        private Object key;
        private Method val;
        Type[] paramsTypes;
        Annotation[][] an;
        List<String> args;

        public KV(Object key, Method val) {
            this.key = key;
            this.val = val;
            this.paramsTypes = val.getGenericParameterTypes();
            an = val.getParameterAnnotations();
            this.args = new ArrayList();
            for (int i = 0; i < an.length; i++) {
                for (int j = 0; j < an[i].length; j++) {
                    if (an[i][j] instanceof SqlParam) {
                        SqlParam t = (SqlParam) an[i][j];
                        args.add(t.value());
                    }
                }
            }
        }

        public Object getKey() {
            return key;
        }

        public Method getVal() {
            return val;
        }

        public Type[] getParamsTypes() {
            return paramsTypes;
        }

        public List<String> getArgs() {
            return args;
        }
    }
}
