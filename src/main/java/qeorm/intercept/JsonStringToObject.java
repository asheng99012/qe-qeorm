package qeorm.intercept;

import qeorm.RealClass;
import qeorm.SqlResult;
import qeorm.utils.JsonUtils;

import java.util.Map;

public class JsonStringToObject implements IFunIntercept {
    private static JsonStringToObject instance;

    @Override
    public void intercept(String key, Map<String, Object> rowData, SqlResult sqlResult) {
        Object val = rowData.get(key);
        if (val != null && !val.toString().equals("")) {
            rowData.put(key, JsonUtils.convert(val, Map.class));
        }
    }

    public static JsonStringToObject getInstance() {
        if (instance == null)
            instance = new JsonStringToObject();
        return instance;
    }
}
