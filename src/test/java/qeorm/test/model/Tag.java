package qeorm.test.model;

import qeorm.ModelBase;
import qeorm.annotation.Column;
import qeorm.annotation.OneToOne;
import qeorm.annotation.Table;

import java.util.Date;

/**
 * Created by ashen on 2017-2-5.
 */
@Table(where = " id={id} and name like '%{name}%'")
public class Tag extends ModelBase {
    Integer id;
    @Column("name")
    private String myName;
    Integer visit;
    Date add_time;
    Integer is_show;
    @OneToOne(self = "id", mappedBy = "id")
    Tag self;

    public Tag getSelf() {
        return self;
    }

    public void setSelf(Tag self) {
        this.self = self;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public Integer getVisit() {
        return visit;
    }

    public void setVisit(Integer visit) {
        this.visit = visit;
    }

    public Date getAdd_time() {
        return add_time;
    }

    public void setAdd_time(Date add_time) {
        this.add_time = add_time;
    }

    public Integer getIs_show() {
        return is_show;
    }

    public void setIs_show(Integer is_show) {
        this.is_show = is_show;
    }
}
