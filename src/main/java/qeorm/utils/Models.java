package qeorm.utils;

import qeorm.ModelBase;
import qeorm.SpringUtils;
import qeorm.intercept.IModelmodify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Models {
    static List<IModelmodify> modelmodifyList;

    public static List<IModelmodify> getModelmodifyList() {
        if (modelmodifyList == null) {
            List<IModelmodify> list;
            try {
                Map<String, IModelmodify> map = SpringUtils.getApplicationContext().getBeansOfType(IModelmodify.class);
                list = new ArrayList<>(map.values());
            } catch (Exception e) {
                list = new ArrayList<>();
            }
            modelmodifyList = list;
        }
        return modelmodifyList;
    }

    public static <T extends ModelBase> void recordModifyLog(T orgin, T current) {
        List<IModelmodify> list = getModelmodifyList();
        list.forEach(modify -> {
            modify.recordLog(orgin, current);
        });
    }
}
