package qeorm.test.dao;

import qeorm.test.model.Tag;
import qeorm.annotation.SqlParam;

import java.util.List;


/**
 * Created by ashen on 2017-2-5.
 */

/**
 * 接口，命名规则为：IxxxDao，接口的类名必须与相应的sql配置文件中的nameSpace一致
 * 方法的名称必须与配置文件中的 sqlConfig 的id 一致
 * 方法的返回类型必须与配置文件中相应的sql返回值格式能对应上
 * 方法的参数，可以只有一个model类型的参数，否则，参数必须用@SqlParam注解，值必须与配置文件中的sql的参数能对应上
 */

public interface ITagDao {

    List<Tag> getTagList(Tag tag);

    int count(@SqlParam("id") int id, @SqlParam("name") String name);

    Tag findTagById(@SqlParam("id") int id);
}
