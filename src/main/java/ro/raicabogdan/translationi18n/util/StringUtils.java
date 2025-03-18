package ro.raicabogdan.translationi18n.util;

public class StringUtils {
    public static String snakeCase(String input) {
        return input
                .replaceAll("[^A-Za-z0-9]", "_")
                .replaceAll("_{2,}","_")
                .replaceAll("_$","")
                .replaceAll("^_","")
                .toLowerCase();
    }
}