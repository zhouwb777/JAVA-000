学习笔记

## Week05 作业题目（周四）：
2.（必做）写代码实现 Spring Bean 的装配，方式越多越好（XML、Annotation 都可以）, 提交到 Github。<br/>
[xml配置](https://github.com/zhouwb777/JAVA-000/blob/main/Week_05/SpringBeanDemo/xml-configuration) <br/>
[包扫描配置](https://github.com/zhouwb777/JAVA-000/blob/main/Week_05/SpringBeanDemo/scanpackage-configuration) <br/>
[注解配置](https://github.com/zhouwb777/JAVA-000/blob/main/Week_05/SpringBeanDemo/annotation-configuration) <br/>

## Week05 作业题目（周六）：
4.（必做）给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。<br/>

~~~
1. 构建Starter项目：fifth-week-home-work-spring-boot-starter
了解了下Starter会使用到一些注解

@Bean 实例化一个 bean;
@ConditionalOnMissingBean 是条件判断的注解，表示如果不存在对应的bean条件才成立;这里就表示如果已经有SchoolService的bean了，那么就不再进行该bean的生成;
                          这个注解十分重要，涉及到默认配置和用户自定义配置的原理。也就是说用户可以自定义一个bean;这样的话，spring容器就不需要再初始化这个默认的bean了。
@ConditionalOnProperty 是条件判断的注解，表示如果配置文件中的响应配置项数值为true,才会对该bean进行初始化
@ConditionalOnClass  条件判断的注解，表示对应的类在classpath目录下存在时，才会去解析对应的配置文件
@EnableConfigurationProperties 注解给出了该配置类所需要的配置信息类

了解了下使用到的组件

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>

        <!-- 这个依赖是为了读取配置文件 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure-processor</artifactId>
            <optional>true</optional>
        </dependency>

2. 构建Web项目：fifth-week-home-work-spring-boot

@RestController
@RequestMapping(value = "/demo")
public class DemoController {

    @Autowired
    private SchoolService schoolService;

    @GetMapping(value = "/print")
    public String print() {

        schoolService.doIt();

        return "success";
    }

}

使用start被装载的Bean,会打印出结果
~~~

6.（必做）研究一下 JDBC 接口和数据库连接池，掌握它们的设计和用法：<br/>
1）使用 JDBC 原生接口，实现数据库的增删改查操作。<br/>
2）使用事务，PrepareStatement 方式，批处理方式，改进上述操作。<br/>
3）配置 Hikari 连接池，改进上述操作。提交代码到 Github。<br/>

~~~
1. DriverManager：驱动管理对象
    1. 注册驱动：告诉程序该使用哪一个数据库驱动jar
        static void registerDriver(Driver driver) :注册与给定的驱动程序 DriverManager 。 
        写代码使用：  Class.forName("com.mysql.jdbc.Driver");
        通过查看源码发现：在com.mysql.jdbc.Driver类中存在静态代码块
         static {
                try {
                    java.sql.DriverManager.registerDriver(new Driver());
                } catch (SQLException E) {
                    throw new RuntimeException("Can't register driver!");
                }
            }
        Mysql5之后的驱动jar包可以省略注册驱动的步骤。
    2. 获取数据库连接：
        * 方法：static Connection getConnection(String url, String user, String password) 
        * 参数：
            * url：指定连接的路径
                * 语法：jdbc:mysql://ip地址(域名):端口号/数据库名称
                * 例子：jdbc:mysql://localhost:3306/db3
                * 细节：如果连接的是本机mysql服务器，并且mysql服务默认端口是3306，则url可以简写为：jdbc:mysql:///数据库名称
            * user：用户名
            * password：密码 
2. Connection：数据库连接对象
    1. 获取执行sql 的对象
        * Statement createStatement()
        * PreparedStatement prepareStatement(String sql)  
    2. 管理事务：
        * 开启事务：setAutoCommit(boolean autoCommit) ：调用该方法设置参数为false，即开启事务
        * 提交事务：commit() 
        * 回滚事务：rollback() 
3. Statement：执行sql的对象
    1. 执行sql
        * boolean execute(String sql) ：可以执行任意的sql 了解 
        * int executeUpdate(String sql) ： 执行DML（insert、update、delete）语句、DDL(create，alter、drop)语句
                                            返回值：影响的行数，可以通过这个影响的行数判断DML语句是否执行成功 返回值>0的则执行成功，反之，则失败。
        * ResultSet executeQuery(String sql)  ：执行DQL（select)语句
4. PreparedStatement：执行Sql对象，区别在于它是预编译SQL
~~~
#### 5.1 使用 JDBC 原生接口，实现数据库的增删改查操作
1. 装配Mysql驱动包
~~~
1. 加入Mysql驱动包
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
OriginalJdbcConfiguration
~~~
2. 装载数据库驱动程序，使用之前的配置注解加载Bean方式
~~~
    @Bean
    public Connection connection() throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.jdbc.Driver");

        Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.152.243:3306/syw_jdbc_demo", "root", "Dv8,zia13vvKqdly");

        return connection;
    }
~~~
3. 编写包级结构
~~~
config -- 项目自定配置类
controller -- Contoller层
dao -- 数据处理层
service -- 业务处理层
entity -- 实体类包
~~~
4. 编写dao，获取到与数据库连接，获取可以执行SQL语句的对象且执行SQL
~~~

//获取执行sql语句的statement对象
  statement = connection.createStatement();

//执行sql语句,拿到结果集
  resultSet = statement.executeQuery("SELECT * FROM account WHERE id = " + id);
~~~
5. 逐层传递对象返回结果
~~~
AccountDO(accountName=2016中秋节同步, password=57fff0ea55146baa6ac1a9999dfcd1ad, idType=, idNo=, mobile=18367002206, email=, remark=null, lastLoginDt=null, lastLoginIp=null, sha=null, sso_token=null)
~~~
6. 添加update方式
~~~
    public boolean updateAccountNameById(Integer id, String name) throws SQLException {

        try {
            statement = connection.createStatement();

            // 执行DML（insert、update、delete）语句、DDL(create，alter、drop)语句 返回值：影响的行数，可以通过这个影响的行数判断DML语句是否执行成功 返回值>0的则执行成功，反之，则失败
            return statement.executeUpdate("UPDATE account SET account_name = '" + name + "' WHERE id = " + id) > 0;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (resultSet != null) {
                resultSet.close();
            }
        }
        return true;
    }
~~~

#### 5.2 使用事务，PrepareStatement 方式，批处理方式， 

~~~
    public boolean updateBatchAccountByIds(List<Integer> ids) throws SQLException {

        connection.setAutoCommit(false); // 开启事务

        try {

            String sql = String.format(
                    "UPDATE account SET version = version + 1,update_by = 'local_dev',update_dt = NOW() WHERE id in (%s)",
                    ids.stream().map(String::valueOf).collect(Collectors.joining(","))
            );

            preparedStatement = connection.prepareStatement(sql);

            connection.commit(); // 提交事务

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException throwables) {
            connection.rollback(); // 发生异常，事务回滚
            throwables.printStackTrace();
        } finally {
            if (statement != null) {
                preparedStatement.close();
            }

            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }
~~~

#### 5.3 配置 Hikari 连接池，改进上述操作

1. 引用Hikari的jar
~~~
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>${hikaricp.version}</version>
        </dependency>
~~~
2. 注入Hikari的DataSourceConfig
~~~
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource() {
        return new HikariDataSource();
    }
~~~
3. 同步修改获取连接的方式
~~~
    @Bean
    public Connection connection() throws SQLException {

        /*HikariDataSource dataSource = new HikariDataSource();*/

        Connection connection = dataSource.getConnection();

        return connection;
    }
~~~
4. 启动应用,监控到Hikari日志打印,测试ok
~~~
2020-11-17 21:16:50.893  INFO 24612 --- [           main] com.zaxxer.hikari.HikariDataSource       : SYW_HikariCP - Starting...
2020-11-17 21:16:51.345  INFO 24612 --- [           main] com.zaxxer.hikari.HikariDataSource       : SYW_HikariCP - Start completed.
~~~