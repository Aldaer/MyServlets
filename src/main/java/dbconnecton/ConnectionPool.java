package dbconnecton;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Just another implementation of a connection pool...
 */
public interface ConnectionPool extends AutoCloseable {
    int DEFAULT_POOL_SIZE = 5;

    Connection getConnection();
    int available();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String driver = "";
        private String url = "";
        private int poolSize = DEFAULT_POOL_SIZE;
        private String userName = "";
        private String password = "";
        private Logger LOG = new DummyLogger();

        public Builder withDriver(String driver) {
            this.driver = driver;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withPoolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withLogger(Logger logger) {
            LOG = logger;
            return this;
        }

        public ConnectionPool create() {
            return new ConnectionPool() {
                private final int poolSize = Builder.this.poolSize;
                private final DestructibleBlockingQueue<Connection> cQueue =
                        DestructibleBlockingQueue.create(new ArrayBlockingQueue<>(poolSize));
                {
                    try {
                        Class.forName(driver);              // Load driver to auto-register with Driver manager
                    } catch (ClassNotFoundException e) {
                        LOG.fatal("Could not find database driver {}", driver);
                        throw new RuntimeException();
                    }

                    try {
                        for (int i = 0; i < poolSize; i++) {
                            Connection conn = DriverManager.getConnection(url, userName, password);
                            ReusableConnection rc = ReusableConnection.create(conn, cQueue, LOG);
                            cQueue.add(rc);
                        }
                    } catch (SQLException e) {
                        LOG.fatal("Error creating DB connection: {} ", e);
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void close() throws SQLException {
                    cQueue.destroy();
                    Connection c;
                    while ((c = cQueue.poll()) != null) c.close();
                }

                @Override
                @Nullable public Connection getConnection() {
                    try {
                        Connection conn = cQueue.take();
                        LOG.trace("Allocating database connection {}", conn);
                        return conn;
                    } catch (InterruptedException e) {
                        LOG.error("Interrupted while getting connection from pool");
                        return null;
                    } catch (NullPointerException e) {
                        LOG.error("Can't allocate connection from closed pool");
                        return null;
                    }
                }

                @Override
                public int available() {
                    return cQueue.isDestroyed()? 0 : cQueue.size();
                }
            };
        }

        private Builder() {}
    }
}
