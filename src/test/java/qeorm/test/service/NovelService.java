package qeorm.test.service;

import org.junit.Test;
import qeorm.TableStruct;
import qeorm.test.model.Novel;
import qeorm.test.model.Tag;

import java.lang.reflect.InvocationTargetException;

public class NovelService {
    @Test
    public void run() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        Tag tag = new Tag();
//        tag.setId(1);
//        tag.setMyName("dsfsd");

        Novel novel = new Novel().enhance();
        novel.setTagId(23);
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
