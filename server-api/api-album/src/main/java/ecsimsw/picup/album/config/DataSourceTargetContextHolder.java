package ecsimsw.picup.album.config;

import java.util.Optional;

public class DataSourceTargetContextHolder {

    private static final ThreadLocal<DataSourceType> dataSourceTargetContext = new ThreadLocal<>();

    public static void setContext(DataSourceType dataSourceType) {
        dataSourceTargetContext.set(dataSourceType);
    }

    public static Optional<DataSourceType> getTargetContext(){
        final DataSourceType targetSource = dataSourceTargetContext.get();
        if(targetSource == null) {
            return Optional.empty();
        }
        return Optional.of(targetSource);
    }

    public static void clearContext(){
        dataSourceTargetContext.remove();
    }
}
