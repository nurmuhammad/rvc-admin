package rvc.admin;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Script;
import org.javalite.activejdbc.Base;
import rvc.admin.init.Config;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author nurmuhammad
 */

public class Database {

    public static Database instance = new Database();

    JdbcConnectionPool pool;

    DataSource dataSource;

    Database() {
        pool = JdbcConnectionPool.create(Config.get("database.jdbc.url"), Config.get("database.user"), Config.get("database.password"));
        pool.setMaxConnections(Config.get("ds.pool.max-size", 30));

        /*JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(Config.get("database.jdbc.url"));
        ds.setUser(Config.get("database.user"));
        ds.setPassword(Config.get("database.password"));
        dataSource = ds;*/

    }

    public static JdbcConnectionPool pool() {
        return instance.pool;
    }

    public static void open() {
        Base.open(pool());
//        Base.open(instance.dataSource);
    }

    public static void close() {
        Base.close();
    }

    public static void backupSql() throws SQLException {
        String dburl = Config.get("database.jdbc.url");
        String[] bkp = {"-url", dburl, "-user", Config.get("database.user"), "-password", Config.get("database.password")};
        Script.main(bkp);
    }
}
