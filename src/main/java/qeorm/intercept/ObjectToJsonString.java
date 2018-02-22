package qeorm.intercept;

import qeorm.RealClass;
import qeorm.SqlResult;
import qeorm.utils.JsonUtils;

import java.util.Map;

public class ObjectToJsonString implements IFunIntercept {
    private static ObjectToJsonString instance;

    @Override
    public void intercept(String key, Map<String, Object> rowData, SqlResult sqlResult) {
        Object val = rowData.get(key);
        if (val != null) {
            rowData.put(key, JsonUtils.toJson(val));
        }
    }

    public static ObjectToJsonString getInstance() {
        if (instance == null)
            instance = new ObjectToJsonString();
        return instance;
    }
}
