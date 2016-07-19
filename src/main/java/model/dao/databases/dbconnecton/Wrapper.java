package model.dao.databases.dbconnecton;

@SuppressWarnings("WeakerAccess")
@FunctionalInterface
public interface Wrapper<T> {
    T toSrc();
}
