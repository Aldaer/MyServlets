package dbconnecton;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

/**
 * Just another implementation of a connection pool...
 */
public interface ConnectionPool extends AutoCloseable, Supplier<Connection> {
    int DEFAULT_POOL_SIZE = 5;

    static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a connection pool after setting required parameters
     */
    @SuppressWarnings("unused")
    class Builder {
        private String driver = "";
        private String url = "";
        private int poolSize = DEFAULT_POOL_SIZE;
        private String userName = "";
        private String password = "";
        private Logger LOG = new DummyLogger();

        public Builder withDriver(@Nullable String driver) {
            this.driver = driver == null ? "" : driver;
            return this;
        }

        public Builder withUrl(@Nullable String url) {
            this.url = url == null ? "" : url;
            return this;
        }

        public Builder withPoolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public Builder withUserName(@Nullable String userName) {
            this.userName = userName == null ? "" : userName;
            return this;
        }

        public Builder withPassword(@Nullable String password) {
            this.password = password == null ? "" : password;
            return this;
        }

        public Builder withLogger(@Nullable Logger logger) {
            LOG = logger == null ? new DummyLogger() : logger;
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
                        LOG.error("Could not find database driver {}", driver);
                        throw new RuntimeException();
                    }

                    try {
                        for (int i = 0; i < poolSize; i++) {
                            Connection conn = DriverManager.getConnection(url, userName, password);
                            ReusableConnection rc = ReusableConnection.create(conn, cQueue, LOG);
                            cQueue.queue().add(rc);
                        }
                    } catch (SQLException e) {
                        LOG.error("Error creating DB connection: {} ", e);
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void close() {
                    cQueue.destroy();
                    Connection c;
                    while ((c = cQueue.queue().poll()) != null)
                        try { c.close(); }
                        catch (SQLException ignored) {}
                }

                @Override
                @Nullable
                public Connection get() {
                    try {
                        Connection conn = cQueue.queue().take();
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

            };
        }

        private Builder() {
        }
    }

    @Override
    void close();
}
