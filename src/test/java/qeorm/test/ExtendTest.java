package qeorm.test;

import org.junit.jupiter.api.Test;
import qeorm.test.model.Novel;
import qeorm.test.model.Tag;
import qeorm.utils.Wrap;

import java.util.HashMap;
import java.util.Map;

public class ExtendTest {
    @Test
    public void testPUtAll() {
        Novel novel = new Novel();
        novel.setAuthor("zjs");
        novel.setId(12);

        Tag tag = new Tag();
        tag.setId(1);
        tag.setMyName("dsfsd");
        tag.setAdd_time(null);

//        tag = ExtendExtUtils.extend(tag, novel);


        Wrap.getWrap(novel).setValue("tagInfo", tag);
        Map map = new HashMap() {{
            put("myName", "thisname");
            put("id", 123);
            put("aa", "dd");
            put("bb", new HashMap<>());
        }};

        Wrap.getWrap(map).setValue("tagInfo", tag);
        Wrap.getWrap(map).setValue("bb.tagInfo", tag);

        Wrap.getWrap(novel).setValue("tagInfo", map);
        System.out.println("===");
    }


    public void testWrap() {

    }
}
