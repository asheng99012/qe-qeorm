package qeorm.intercept;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import qeorm.ModelBase;
import qeorm.TableStruct;
import qeorm.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public interface IModelmodify {
    public default <T extends ModelBase> void recordLog(T orgin, T current) {
        if (canRecordLog(current.getClass())) {
            Map originMap = JsonUtils.convertWriteNull(orgin, Map.class);
            Map currentMap = JsonUtils.convertWriteNull(current, Map.class);
            MapDifference difference = Maps.difference(originMap, currentMap);
            Map diff = new HashMap();
            if (difference.entriesOnlyOnRight() != null && difference.entriesOnlyOnRight().keySet().size() > 0) {
                difference.entriesOnlyOnRight().forEach((key, val) -> {
                    diff.put(key, "null =>" + val);
                });
            }

            Map<String, MapDifference.ValueDifference<Object>> map2 = difference.entriesDiffering();
            if (!map2.isEmpty()) {
                map2.forEach((key,sdiff)-> {
                    diff.put(key,JsonUtils.toJson(sdiff.leftValue())+" => "+JsonUtils.toJson(sdiff.rightValue()));
                });
            }

            TableStruct table = TableStruct.getTableStruct(current.getClass().getName());
            recordLog(table.getTableName(), currentMap.get(table.getPrimaryField()), JsonUtils.toJson(diff));
        }
    }

    public boolean canRecordLog(Class<? extends ModelBase> cls);

    public void recordLog(String table, Object key, String diff);

}
