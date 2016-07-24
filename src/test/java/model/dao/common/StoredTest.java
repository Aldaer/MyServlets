package model.dao.common;

import org.junit.Test;

import java.sql.Timestamp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StoredTest {
    private class StoredStuff implements Stored {
        @StoredField(column = "ID", auto = true)
        int stuffId;

        @StoredField("NAME")
        String stuffName;

        @StoredField("DESCRIPTION")
        String stuffDesc;
    }

    private class MoreStoredStuff extends StoredStuff {
        @StoredField("STUFF_TIME")
        Timestamp ts;
    }

    private static String REFERENCE_SQL = "INSERT INTO STUFFTABLE (NAME,DESCRIPTION) VALUES (?,?);";



    @Test
    public void generateInsertSQLTest() throws Exception {
        StoredStuff sts = new StoredStuff();
        assertThat(sts.generateInsertSQL("STUFFTABLE"), is(REFERENCE_SQL));
    }

    @Test
    public void getColumnForFieldTest() throws Exception {
        StoredStuff sts = new StoredStuff();
        assertThat(sts.getColumnForField("stuffId"), is("ID"));
        assertThat(sts.getColumnForField("stuffName"), is("NAME"));

        MoreStoredStuff msts = new MoreStoredStuff();
        assertThat(msts.getColumnForField("ts"), is("STUFF_TIME"));
        assertThat(msts.getColumnForField("stuffName"), is("NAME"));
    }

}