package com.aldor.jdbc;

import com.aldor.utils.CryptoUtils;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.JDBCRealm;

import java.security.Principal;
import java.sql.Connection;
import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A custom JDBC realm implementation that stores passwords digested with random salt.
 */
public class SaltedRealm extends JDBCRealm {

    @Override
    public synchronized Principal authenticate(String username, String credentials) {
        return super.authenticate(username, credentials);
    }

    @Override
    public synchronized Principal authenticate(Connection dbConnection,
                                               String username,
                                               String credentials) {

        username = new String(username.getBytes(ISO_8859_1), UTF_8);  //<=== HACK!!!

        String dbCredentials = this.getPassword(username);

        if (CryptoUtils.verifySaltedHash(dbCredentials, credentials)) {
            ArrayList roles = this.getRoles(username);
            return new GenericPrincipal(username, credentials, roles);
        } else {
            return null;
        }
    }
//        super.authenticate(dbConnection, username, credentials);
}

