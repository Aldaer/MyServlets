package model.dao.databases;

/**
 * Utility class to instantiate GenericSqlDAO in MySql mode
 */
public class MySqlGlobalDao extends GenericSqlDAO {
    public MySqlGlobalDao() {
        super(SqlMode.MY_SQL);
    }
}
