package ai.dataanalytic.sharedlibrary.util;

public class StringUtils {

    // Private constructor to prevent instantiation
    private StringUtils() {}

    public static boolean hasNullOrEmptyFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
