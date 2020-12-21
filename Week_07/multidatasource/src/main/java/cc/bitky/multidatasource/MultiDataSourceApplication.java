package cc.bitky.multidatasource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class})
public class MultiDataSourceApplication implements CommandLineRunner {

    private static final String INSERT_SQL = "INSERT INTO `account`(`uid`, `name`, `password`) VALUES (:uid, :name, :pwd)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM account";

    @Autowired
    private cc.bitky.multidatasource.BitkylinRoutingDataSource bitkylinRoutingDataSource;

    @Resource(name = "ssNamedParameterJdbcTemplate")
    private NamedParameterJdbcTemplate ssNamedParameterJdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(MultiDataSourceApplication.class, args);
    }

    @Override
    public void run(String... args) throws JsonProcessingException {
        MapSqlParameterSource[] masterArr = randomAccounts("master");
        MapSqlParameterSource[] slaveArr = randomAccounts("slave");
        MapSqlParameterSource[] ssArr = randomAccounts("ss");
        // Spring 读写分离
        masterNamedParameterJdbcTemplate(bitkylinRoutingDataSource, INSERT_SQL).batchUpdate(INSERT_SQL, masterArr);

        // shardingSphere-JDBC 读写分离
        ssNamedParameterJdbcTemplate.batchUpdate(INSERT_SQL, ssArr);

        // 数据SELECT操作
        List<Map<String, Object>> list1 = masterNamedParameterJdbcTemplate(bitkylinRoutingDataSource, SELECT_ALL_SQL).queryForList(SELECT_ALL_SQL, (Map<String, ?>) null);
        List<Map<String, Object>> list2 = ssNamedParameterJdbcTemplate.queryForList(SELECT_ALL_SQL, (Map<String, ?>) null);
        ObjectMapper objectMapper = new ObjectMapper();
        log.info(objectMapper.writeValueAsString(list1));
        log.info(objectMapper.writeValueAsString(list2));
    }

    private MapSqlParameterSource[] randomAccounts(String sourceName) {
        List<MapSqlParameterSource> list = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            MapSqlParameterSource source = create(sourceName, i);
            list.add(source);
        }
        return list.toArray(new MapSqlParameterSource[0]);
    }

    private MapSqlParameterSource create(String sourceName, int num) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("uid", UUID.randomUUID().toString());
        parameterSource.addValue("name", sourceName + num);
        parameterSource.addValue("pwd", "pwd" + num);
        return parameterSource;
    }

    private NamedParameterJdbcTemplate masterNamedParameterJdbcTemplate(DataSource masterDataSource, String insertSql) {
        cc.bitky.multidatasource.BitkylinRoutingDataSource.currentSql(insertSql);
        return new NamedParameterJdbcTemplate(new JdbcTemplate(masterDataSource));
    }
}
