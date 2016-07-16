package dbconnecton;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("NullableProblems")
interface DestructibleBlockingQueue<T> extends BlockingQueue<T>, Wrapper<BlockingQueue<T>> {
    void destroy();
    boolean isDestroyed();

    @Override
    default boolean add(T t) {
        return toSrc().add(t);
    }

    @Override
    default boolean offer(T t) {
        return toSrc().offer(t);
    }

    @Override
    default void put(T t) throws InterruptedException {
        toSrc().put(t);
    }

    @Override
    default boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        return toSrc().offer(t, timeout, unit);
    }

    @Override
    default T take() throws InterruptedException {
        return toSrc().take();
    }

    @Override
    default T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return toSrc().poll(timeout, unit);
    }

    @Override
    default int remainingCapacity() {
        return toSrc().remainingCapacity();
    }

    @Override
    default boolean remove(Object o) {
        return toSrc().remove(o);
    }
    @Override
    default boolean contains(Object o) {
        return toSrc().contains(o);
    }

    @Override
    default int drainTo(Collection<? super T> c) {
        return toSrc().drainTo(c);
    }

    @Override
    default int drainTo(Collection<? super T> c, int maxElements) {
        return toSrc().drainTo(c, maxElements);
    }

    @Override
    default T remove() {
        return toSrc().remove();
    }

    @Override
    default T poll() {
        return toSrc().poll();
    }

    @Override
    default T element() {
        return toSrc().element();
    }

    @Override
    default T peek() {
        return toSrc().peek();
    }

    @Override
    default int size() {
        return toSrc().size();
    }

    @Override
    default boolean isEmpty() {
        return toSrc().isEmpty();
    }

    @Override
    default @NotNull Iterator<T> iterator() {
        return toSrc().iterator();
    }

    @NotNull
    @Override
    default Object[] toArray() {
        return toSrc().toArray();
    }

    @NotNull
    @Override
    default <T1> T1[] toArray(T1[] a) {
        return toSrc().toArray(a);
    }

    @Override
    default boolean containsAll(Collection<?> c) {
        return toSrc().containsAll(c);
    }

    @Override
    default boolean addAll(Collection<? extends T> c) {
        return toSrc().addAll(c);
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        return toSrc().removeAll(c);
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        return toSrc().retainAll(c);
    }

    @Override
    default void clear() {
        toSrc().clear();
    }

    /**
     * Creates a destructible blocking queue that becomes null after calling the {@code destroy()} method
     * or when queue {@code q} gets collected by GC.
     * @param q Blocking queue to get destructible reference to
     * @param <T> Blocking queue type
     * @return Destructible reference
     */
    static<T> DestructibleBlockingQueue<T> create(BlockingQueue<T> q) {
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
            public BlockingQueue<T> toSrc() {
                return q;
            }
        };
    }

}
