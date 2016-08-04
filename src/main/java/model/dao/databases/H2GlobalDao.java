package model.dao.databases;

/**
 * Utility class to instantiate GenericSqlDAO in H2 mode
 */
public class H2GlobalDao extends GenericSqlDAO {
    public H2GlobalDao() {
        super(SqlMode.H2);
    }
}
