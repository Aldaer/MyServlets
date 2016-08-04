package model.dao.databases;

/**
 * Utility class to instantiate SqlGlobalDAO in H2 mode
 */
public class H2GlobalDao extends SqlGlobalDAO {
    H2GlobalDao() {
        super(SqlMode.H2);
    }
}
