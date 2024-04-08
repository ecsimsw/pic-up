package ecsimsw.picup.album.config;

public class DataSourceTargetContextHolder {

    private static final ThreadLocal<DataSourceType> dataSourceTargetContext = new ThreadLocal<>();

    public static void setContext(DataSourceType dataSourceType) {
        dataSourceTargetContext.set(dataSourceType);
    }

    public static boolean hasDirectTargetDataSource() {
        var targetSource = dataSourceTargetContext.get();
        return targetSource != null;
    }

    public static DataSourceType getTargetContext(){
        return dataSourceTargetContext.get();
    }

    public static void clearContext(){
        dataSourceTargetContext.remove();
    }
}
