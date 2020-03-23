package qeorm;

import com.google.common.base.Strings;
import org.springframework.core.NamedThreadLocal;

import javax.servlet.*;
import java.io.IOException;
import java.util.*;

public class CacheManager implements Filter {
    private final ThreadLocal<Map<String, Object>> resources = new NamedThreadLocal<Map<String, Object>>(CacheManager.class.getName());
    public static CacheManager instance = new CacheManager();
    private static String _isOpen = "isopen";

    public Map<String, Object> getSource() {
        Map<String, Object> map = resources.get();
        if (map == null) {
            map = new HashMap<String, Object>();
            resources.set(map);
        }
        return map;
    }

    public <T> T getSource(String key, Object defaultVal) {
        Map<String, Object> map = getSource();
        if (!map.containsKey(key)) {
            map.put(key, defaultVal);
        }
        return (T) map.get(key);
    }


    public void open() {
        getSource().put(_isOpen, true);
    }

    public void close() {
        resources.remove();
    }

    private Set<String> getEditedTable() {
        return getSource("editTable", new HashSet<>());
    }

    //是否编辑过
    public boolean isEdited(String tableName) {
        if (Strings.isNullOrEmpty(tableName)) return false;
        //当前进程是否关闭缓存
        if (!getSource().containsKey(_isOpen)) return false;
        //在当前进程内，表数据是否被修改过
        if (getEditedTable().contains(tableName)) {
            return true;
        }
        return false;
    }

    public boolean isEdited(List<String> tns) {
        if (tns != null) {
            for (int i = 0; i < tns.size(); i++) {
                if (isEdited(tns.get(i)))
                    return true;
            }
        }
        return false;
    }

    //标识为编辑过
    public void edit(String tableName) {
        if (!Strings.isNullOrEmpty(tableName) && getSource().containsKey(_isOpen)) {
            getEditedTable().add(tableName);
        }
    }

    public void edit(List<String> tns) {
        if (tns != null) {
            for (int i = 0; i < tns.size(); i++) {
                edit(tns.get(i));
            }
        }
    }

    public String getRealDbName(String dbName, List<String> tns) {
        if (isEdited(tns)) {
            dbName.replace(SqlSession.Slave, SqlSession.Master);
        }
        return dbName;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.open();
        try {
            chain.doFilter(request, response);
        } finally {
            this.close();
        }
    }

    @Override
    public void destroy() {

    }
}
