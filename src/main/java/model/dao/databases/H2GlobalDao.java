package model.dao.databases;

import org.springframework.stereotype.Component;

/**
 * Utility class to instantiate GenericSqlDAO in H2 mode
 */
@Component
public class H2GlobalDao extends GenericSqlDAO {
    public H2GlobalDao() {
        super(SqlMode.H2);
    }
}
