package be.ephec.padel_backend.config.DataSource;

public class DataSourceContext {

    private static final ThreadLocal<DataSourceType> context = new ThreadLocal<>();

    public static void set(DataSourceType type) {
        context.set(type);
    }

    public static DataSourceType get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}