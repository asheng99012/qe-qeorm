package qeorm.intercept;

import qeorm.RealClass;
import qeorm.SqlResult;
import qeorm.utils.JsonUtils;

import java.util.Map;

public class ObjectToJsonString implements IFunIntercept {
    private static ObjectToJsonString instance;

    @Override
    public void intercept(String key, Map<String, Object> rowData, SqlResult sqlResult) {
        for (String _key : rowData.keySet()) {
            Object val = rowData.get(_key);
            if (!RealClass.isPrimitive(val.getClass())) {
                rowData.put(_key, JsonUtils.toJson(val));
            }
        }
    }

    public static ObjectToJsonString getInstance() {
        if (instance == null)
            instance = new ObjectToJsonString();
        return instance;
    }
}
