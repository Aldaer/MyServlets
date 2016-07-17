package dbconnecton;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;


/**
 * A  wrapper class for a blocking queue that can be marked as "destroyed"
 * @param <T> Queued object type
 */
interface DestructibleBlockingQueue<T>  {
    /**
     * Destroys this queue
     */
    void destroy();

    /**
     * Indicates whether this queue is destroyed
     * @return True if the queu
     */
    boolean isDestroyed();

    /**
     * Returns reference to backing queue
     * @return Queue reference
     */
    @NotNull BlockingQueue<T> queue();

    /**
     * Creates a destructible blocking queue
     * @param q "Backing" blocking queue
     * @param <T> Blocking queue type
     * @return "Destructible" wrapper
     */
    static<T> DestructibleBlockingQueue<T> create(@NotNull BlockingQueue<T> q) {
        return new DestructibleBlockingQueue<T>() {
            volatile boolean destroyed = false;
            @Override
            public void destroy() {
                destroyed = true;
            }

            @Override
            public boolean isDestroyed() {
                return destroyed;
            }

            @Override
            @NotNull public BlockingQueue<T> queue() {
                return q;
            }
        };
    }

}
