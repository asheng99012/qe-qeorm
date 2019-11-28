package qeorm;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import qeorm.annotation.SqlParam;
import qeorm.intercept.IFunIntercept;
import qeorm.utils.JsonUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by ashen on 2017-1-25.
 */
public class SqlConfigProxy<T> implements MethodInterceptor, FactoryBean<T> {
    private Logger logger = LoggerFactory.getLogger(SqlConfigProxy.class);
    private Class<T> mapperInterface;

    private static Map<Method, MethodTruct> mapMethodTruct = Maps.newConcurrentMap();

    public SqlConfigProxy() {

    }

    public SqlConfigProxy(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (method.getName().equals("toString")) return null;
        this.completeMethodSqlConfig(method);
        String sqlId = mapperInterface.getCanonicalName() + "." + method.getName();
        MethodTruct truct = mapMethodTruct.get(method);

        SqlResult result;
        if (truct.getParameterCount() == 0)
            result = SqlExecutor.exec(sqlId);
        else if (truct.getParameterCount() == 1 && truct.getParameters().size() == 0) {
            result = SqlExecutor.exec(sqlId, objects[0]);
        } else if (truct.getParameterCount() != truct.getParameters().size()) {
            logger.error("{}的某些参数没有加注解", sqlId);
            throw new Exception("参数个数不正确");
        } else {
            Map<String, Object> map = Maps.newHashMap();
            for (int i = 0; i < truct.getParameters().size(); i++) {
                if (objects[i] != null)
                    map.put(truct.getParameters().get(i), objects[i]);
            }
            result = SqlExecutor.exec(sqlId, map);
        }
        if (result.getResult() != null) {
            if (RealClass.isPrimitive(truct.getKlass())) {
                return JsonUtils.convert(result.getResult(), truct.getKlass());
            } else {
                List list = (List) result.getResult();
                if (truct.isList())
                    return list;
//                    return JsonUtils.convert(list, truct.getType());
                else if (list.size() > 0)
                    return list.get(0);
//                    return JsonUtils.convert(list.get(0), truct.getKlass());
            }
        }
        return null;
    }

    public Object intercept_bak(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (method.getName().equals("toString")) return null;
        this.completeMethodSqlConfig(method);
        String sqlId = mapperInterface.getCanonicalName() + "." + method.getName();
        int parameterCount = method.getParameterTypes().length;
        Annotation[][] an = method.getParameterAnnotations();
        int anCount = 0;
        Map<String, Object> map = Maps.newHashMap();
        for (int i = 0; i < an.length; i++) {
            for (int j = 0; j < an[i].length; j++) {
                anCount++;
                SqlParam t = (SqlParam) an[i][j];
                map.put(t.value(), objects[i]);
            }
        }
        SqlResult result;
        if (parameterCount == 0)
            result = SqlExecutor.exec(sqlId);
        else if (parameterCount == 1 && anCount == 0) {
            result = SqlExecutor.exec(sqlId, objects[0]);
        } else if (parameterCount != anCount) {
            logger.error("{}的某些参数没有加注解", sqlId);
            throw new Exception("参数个数不正确");
        } else {
            result = SqlExecutor.exec(sqlId, map);
        }
        if (result.getResult() != null) {
            if (result.getResult() instanceof List) {
                List list = (List) result.getResult();
                if (list.size() > 0 && !method.getReturnType().isInstance(list))
                    return JsonUtils.convert(list.get(0), method.getGenericReturnType());
                else
                    return JsonUtils.convert(list, method.getGenericReturnType());
            } else {
                return result.getResult();
            }
        }
        return null;
    }

    public T getObject() throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(new Class[]{mapperInterface});
        enhancer.setCallback(this);
        return (T) enhancer.create();
    }

    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    public boolean isSingleton() {
        return true;
    }

    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    private void completeMethodSqlConfig(Method method) throws IllegalAccessException, InstantiationException {
        if (mapMethodTruct.containsKey(method)) return;
        SqlConfig sqlConfig = SqlConfigManager.getSqlConfig(mapperInterface.getCanonicalName() + "." + method.getName());
        if (!Strings.isNullOrEmpty(sqlConfig.getReturnType())) return;

        Class klass = MethodTruct.getRealClass(method.getGenericReturnType());
        if (klass != null) {
            sqlConfig.setReturnType(klass.getName());

            if (!RealClass.isPrimitive(klass) && !klass.getName().equals("java.util.Map")) {
                try {

                    Object obj = klass.newInstance();
                    if (obj instanceof ModelBase) {
                        sqlConfig.setFunIntercepts("all", TableStruct.instance);
                    }
                    if (obj instanceof IFunIntercept) {
                        sqlConfig.setFunIntercepts("all", (IFunIntercept) obj);
                    }

                } catch (Exception e) {
                    logger.error("实例化对象失败：" + klass.getName());
                    //throw e;
                }
            }

        }
        this.completeSqlIntercepts(sqlConfig);
        mapMethodTruct.put(method, new MethodTruct(method));
    }


    private void completeSqlIntercepts(SqlConfig sqlConfig) throws IllegalAccessException, InstantiationException {
        if (sqlConfig.getSqlIntercepts().isEmpty()) return;
        Class klazz;
        try {
            klazz = Class.forName(sqlConfig.getReturnType());
            for (SqlConfig sc : sqlConfig.getSqlIntercepts()) {
                completeSqlIntercepts(sc);
                if (!Strings.isNullOrEmpty(sc.getReturnType()) || Strings.isNullOrEmpty(sc.getFillKey())) continue;
                Field field = klazz.getDeclaredField(sc.getFillKey());
                Class klass = MethodTruct.getRealClass(field.getGenericType());

                if (klass != null) {
                    sc.setReturnType(klass.getName());

                    if (!RealClass.isPrimitive(klass) && !klass.getName().equals("java.util.Map")) {
                        try {
                            Object obj = klass.newInstance();
                            if (obj instanceof ModelBase) {
                                sqlConfig.setFunIntercepts("all", TableStruct.instance);
                            }
                            if (obj instanceof IFunIntercept) {
                                sqlConfig.setFunIntercepts("all", (IFunIntercept) obj);
                            }

                        } catch (Exception e) {
                            logger.error("实例化对象失败：" + klass.getName());
                        }
                    }

                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }
}

class MethodTruct {
    private Method method;
    Type type;
    Class klass;
    List<String> parameters = Lists.newArrayList();
    boolean isList;
    int parameterCount;

    public MethodTruct(Method _method) {
        this.method = _method;
        this.type = method.getGenericReturnType();
        this.klass = getRealClass(type);
        this.isList = type instanceof ParameterizedType;
        this.parameterCount = method.getParameterTypes().length;
        Annotation[][] an = method.getParameterAnnotations();
        for (int i = 0; i < an.length; i++) {
            for (int j = 0; j < an[i].length; j++) {
                SqlParam t = (SqlParam) an[i][j];
                this.parameters.add(t.value());
            }
        }

    }

    public int getParameterCount() {
        return parameterCount;
    }

    public static Class getRealClass(Type type) {
        Class<?> klass = null;
        if (type instanceof Class<?>) {
            klass = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            Type[] ts = ((ParameterizedType) type).getActualTypeArguments();
            if (ts.length > 0) {
                Type t = ts[0];
                klass = (Class<?>) t;
            }
        }
        return RealClass.getRealClass(klass.getName());
//        if (klass != null && !klass.isPrimitive()) {
//            return klass;
//        }
//        return null;
    }

    public Type getType() {
        return type;
    }

    public Class getKlass() {
        return klass;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public boolean isList() {
        return isList;
    }
}