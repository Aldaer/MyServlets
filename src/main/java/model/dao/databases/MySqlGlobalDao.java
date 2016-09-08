package model.dao.databases;

import org.springframework.stereotype.Component;

/**
 * Utility class to instantiate GenericSqlDAO in MySql mode
 */
@Component
public class MySqlGlobalDao extends GenericSqlDAO {
    public MySqlGlobalDao() {
        super(SqlMode.MY_SQL);
    }
}
