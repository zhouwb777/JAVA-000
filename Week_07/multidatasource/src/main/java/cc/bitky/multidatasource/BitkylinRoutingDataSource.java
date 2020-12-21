package cc.bitky.multidatasource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

public class BitkylinRoutingDataSource extends AbstractRoutingDataSource {

    private static ThreadLocal<String> currentSql = new ThreadLocal<>();

    public static void currentSql(String sql) {
        currentSql.set(sql);
    }

    public static void clearSql() {
        currentSql.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String sql = currentSql.get();
        if (StringUtils.isBlank(sql)) {
            return "master";
        }
        if (StringUtils.startsWith(sql.toLowerCase(), "insert")) {
            return "master";
        } else if (StringUtils.startsWith(sql.toLowerCase(), "update")) {
            return "master";
        } else if (StringUtils.startsWith(sql.toLowerCase(), "select")) {
            return "slave";
        }
        return "master";
    }
}
