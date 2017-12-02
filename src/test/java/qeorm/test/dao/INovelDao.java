package qeorm.test.dao;


import qeorm.test.model.Novel;

import java.util.List;

/**
 * Created by ashen on 2017-2-5.
 */
public interface INovelDao {
    List<Novel> getNovelList(Novel novel);
}
