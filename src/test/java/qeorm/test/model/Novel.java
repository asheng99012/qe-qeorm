package qeorm.test.model;

import qeorm.ModelBase;
import qeorm.annotation.Table;

import qeorm.ModelBase;
import qeorm.annotation.Column;
import qeorm.annotation.OneToOne;
import qeorm.annotation.Table;

/**
 * Created by ashen on 2017-2-5.
 */

/**
 * @Table 是必须的注解，
 * masterDbName 可以为空，当为空时，默认取当前MODEL命名空间的第三的值+Master，以此类为例，默认为 coreMaster
 * slaveDbName 同上，默认为 coreSlave
 * primaryKey 数据库主键字段，默认为id
 * tableName 默认为 类 的名称
 * where model执行select 、delete 、count时的where条件，默认为 id={id}
 * @Column 字段注解，当数据库字段同model字段名称一致时，可以省略，不一致时候，需配置此项
 * @Transient 当某字段与数据库字段没有对应关系时，字段需加上此注解
 * @OneToOne 当某字段是另外一个类时，添加此注解，意思是：当前类的【self】字段对应另外一个类的【mapperBy】字段
 * @OneToMany 同上
 * <p>
 * <p>
 * 只要继承 ModelBase ，此model就有了以下方法：
 * select 根据当前类的条件筛选数据，返回当前类List
 * selectWithrelation 在select的基础上，也会返回有@OneToOne和@OneToMany的字段的值
 * selectOne 返回一个实例
 * selectOneWithrelation
 * count
 * save
 * insert
 * update 注意，当update时，where条件为  id={id}
 */
@Table(where = " id={id} and name like '%{name}%'",masterDbName = "defaultMaster" ,slaveDbName = "userSlave")
public class Novel extends ModelBase {
    private Integer id;
    private String name;
    @Column("name")
    private String myName;
    private String author;
    @Column("tag_id")
    private Integer tagId;
    private String desc;
    @OneToOne(self = "tagId", mappedBy = "id")
    private Tag tagInfo;
    @OneToOne(self = "id", mappedBy = "id")
    private Novel self;

    public Novel getSelf() {
        return self;
    }

    public void setSelf(Novel self) {
        this.self = self;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public Tag getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(Tag tagInfo) {
        this.tagInfo = tagInfo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
