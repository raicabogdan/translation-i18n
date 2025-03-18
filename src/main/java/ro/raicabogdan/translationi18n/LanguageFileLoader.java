package ro.raicabogdan.translationi18n;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageFileLoader {
    private static Project project;
    private final LinkedHashMap<String, String> locales = new LinkedHashMap<>();

    public LanguageFileLoader(Project project) {
        LanguageFileLoader.project = project;
    }

    public LinkedHashMap<String, String> getLocales() {
        return locales;
    }

    public List<String> getLanguageFiles() {
        Settings settings = Settings.getInstance(project);
        String basePath = project.getBasePath();
        String translationPath = settings != null ? basePath + "/" + settings.pathToTranslation : "";

        if (translationPath.isEmpty()) {
            return Collections.emptyList();
        }

        File dir = new File(translationPath);

        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }

        List<String> filePaths = new ArrayList<>();
        List<File> files = FileUtil.findFilesByMask(Pattern.compile(".*\\.php"), dir);

        for (File file : files) {
            String locale = this.extractLocaleFromFile(file.getAbsolutePath());
            if (locale != null) {
                if (locale.equals(settings.defaultLanguage)) {
                    putFirst(locales, locale, file.getAbsolutePath());
                } else {
                    locales.put(locale, file.getAbsolutePath());
                }
                filePaths.add(file.getAbsolutePath());
            }
        }
        return filePaths;
    }

    public String extractLocaleFromFile(String filePath) {
        Pattern pattern = Pattern.compile(".*/([a-z]{2})/messages\\.php|messages\\.([a-z]{2})\\.php", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(filePath);
        if (matcher.find()) {
            if (matcher.group(1) != null) {
                return matcher.group(1);
            }
            if (matcher.group(2) != null) {
                return matcher.group(2);
            }
        }
        return null;
    }

    public static <K, V> void putFirst(LinkedHashMap<K, V> map, K key, V value) {
        LinkedHashMap<K, V> newMap = new LinkedHashMap<>();
        newMap.put(key, value);
        newMap.putAll(map);
        map.clear();
        map.putAll(newMap);
    }
}