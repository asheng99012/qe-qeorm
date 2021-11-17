package qeorm.test.service;

import org.junit.jupiter.api.Test;
import qeorm.test.model.Novel;

import java.lang.reflect.InvocationTargetException;

public class NovelService {
    @Test
    public void run() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        Tag tag = new Tag().enhance();
//        tag.setId(1);
//        tag.setMyName("dsfsd");
//        tag.setAdd_time(null);
//        tag.setIs_show(null);
//        tag.update();

        Novel novel = new Novel();
        novel.setId(12);
        //novel.setTagId(23);
        novel=novel.selectOne();
//        novel.notIgnoreNull();
//        novel.insert();
//        List<Novel> list= novel.selectWithrelation();

        novel = novel.enhance();
        novel.getTagInfo();

//        novel.getTagInfo();
//        Class klass = TableStruct.getTableStruct(novel.getClass().getName()).getRelationStructList().stream()
//                .filter(r -> r.getFillKey().equals("tagInfo")).findFirst().get().getClazz();
//
//        novel.getClass().getMethod("set" + "getTagInfo".substring(3), klass).invoke(novel, new Object[]{tag});
//        novel = novel.enhance();
    }
}
