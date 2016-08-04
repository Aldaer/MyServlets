package model.dao.databases;

/**
 * Utility class to instantiate SqlGlobalDAO in MySql mode
 */
public class MySqlGlobalDao extends SqlGlobalDAO {
    MySqlGlobalDao() {
        super(SqlMode.MY_SQL);
    }
}
