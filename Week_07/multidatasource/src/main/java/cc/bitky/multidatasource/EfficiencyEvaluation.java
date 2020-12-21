package cc.bitky.multidatasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class EfficiencyEvaluation {

    private static final String JDBC_URL = "jdbc:mysql://[(host=localhost,port=3307,user=root,password=124)]/bitkyshop?allowMultiQueries=true&allowPublicKeyRetrieval=true";
    private static final String INSERT_SQL = "INSERT INTO `account`(`uid`, `name`, `password`) VALUES (?, ?, ?)";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        fromPrepareStatement();
        fromHikariCP();
    }

    private static void fromHikariCP() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(JDBC_URL);
        HikariDataSource dataSource = new HikariDataSource(config);
        JdbcTemplate template = masterJdbcTemplate(dataSource);
        StopWatch stopWatch = StopWatch.createStarted();

        template.batchUpdate(INSERT_SQL, randomAccounts("HikariCP"));
        stopWatch.stop();
        log.info("HikariCP:" + stopWatch.toString());

    }

    private static void fromPrepareStatement() throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
            PreparedStatement insert = conn.prepareStatement(INSERT_SQL);
            for (Object[] objects : randomAccounts("PrepareStatement")) {
                insert.setString(1, (String) objects[0]);
                insert.setString(2, (String) objects[1]);
                insert.setString(3, (String) objects[2]);
                insert.addBatch();
            }
            StopWatch stopWatch = StopWatch.createStarted();
            conn.setAutoCommit(false);
            insert.executeBatch();
            conn.commit();
            stopWatch.stop();
            log.info("PrepareStatement:" + stopWatch.toString());

        }
    }


    private static List<Object[]> randomAccounts(String sourceName) {
        List<Object[]> list = new ArrayList<>();
        for (int i = 1; i < 100_00; i++) {
            Object[] source = create(sourceName, i);
            list.add(source);
        }
        return list;
    }

    private static JdbcTemplate masterJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private static Object[] create(String sourceName, int num) {
        Object[] arr = new Object[3];
        arr[0] = UUID.randomUUID().toString();
        arr[1] = sourceName + num;
        arr[2] = "pwd" + num;
        return arr;
    }
}
