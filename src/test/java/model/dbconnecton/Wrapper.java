package model.dbconnecton;

@FunctionalInterface
public interface Wrapper<T> {
    T toSrc();
}
