package qeorm;

import java.util.ArrayList;
import java.util.List;

public class QePage<E> extends ArrayList<E> {
    private static final long serialVersionUID = 1L;

    /**
     * 页码，从1开始
     */
    private int pageNum;
    /**
     * 页面大小
     */
    private int pageSize;
    /**
     * 起始行
     */
    private int startRow;
    /**
     * 末行
     */
    private int endRow;
    /**
     * 总数
     */
    private Long total;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 包含count查询
     */
    private boolean count = false;

    private String sql;

    public QePage() {
        super();
    }

    public QePage(int pageNum, int pageSize, boolean count, String sql) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.count = count;
        this.sql = sql;
        calculateStartAndEndRow();
    }

    public List<E> getResult() {
        return this;
    }

    public int getPages() {
        return pages;
    }

    public int getEndRow() {
        return endRow;
    }


    public int getPageNum() {
        return pageNum;
    }


    public int getPageSize() {
        return pageSize;
    }


    public int getStartRow() {
        return startRow;
    }


    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        if (total == null) total = 0l;
        this.total = total;
        if (total == -1) {
            pages = 1;
            return;
        }
        if (pageSize > 0) {
            pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        } else {
            pages = 0;
        }
    }

    public String getSql() {
        return sql;
    }

    public String getCountSql() {
        return "select count(*) from (" + sql + ") _count_t_";
    }

    /**
     * 计算起止行号
     */
    private void calculateStartAndEndRow() {
        this.startRow = this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNum > 0 ? 1 : 0);
    }

    public boolean isCount() {
        return this.count;
    }


}
