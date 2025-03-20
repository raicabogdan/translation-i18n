package ro.raicabogdan.translationi18n.indexing;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import ro.raicabogdan.translationi18n.Settings;
import ro.raicabogdan.translationi18n.TranslationI18nProjectComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationKeyIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> INDEX_ID = ID.create("ro.raicabogdan.translationi18n.translation");
    private static final Pattern TRANSLATION_PATTERN = Pattern.compile("'([a-zA-Z0-9_]+)'\s*=>\s*'([^']+)'|\"([^\"]+)\"\s*=>\s*\"([^\"]+)\";");
    private static final Pattern FILE_PATTERN = Pattern.compile(".*/([a-z]{2})/messages\\.php|messages\\.([a-z]{2})\\.php", Pattern.CASE_INSENSITIVE);

    private final DataIndexer<String, String, FileContent> indexer = inputData -> {
        Map<String, String> map = new HashMap<>();
        if (!TranslationI18nProjectComponent.isEnabled(inputData.getProject())) {
            return map;
        }

        Settings settings = Settings.getInstance(inputData.getProject());

        Matcher file_matcher = FILE_PATTERN.matcher(inputData.getFile().getPath());
        if (file_matcher.find()) {
            if (file_matcher.group(1) != null) {
                if (file_matcher.group(1).equals(settings.defaultLanguage)) {
                    String content = inputData.getContentAsText().toString();

                    Matcher matcher = TRANSLATION_PATTERN.matcher(content);
                    while (matcher.find()) {
                        String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(3);
                        String value = "";
                        if (settings.displayTranslation) {
                            value = matcher.group(2) != null ? matcher.group(2) : matcher.group(4);
                        }

                        map.put(key, value); // Store both key and value
                    }
                }
            }
        }


        return map;
    };

    @Override
    public @NotNull ID<String, String> getName() {
        return INDEX_ID;
    }

    @Override
    public @NotNull DataIndexer<String, String, FileContent> getIndexer() {
        return indexer;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {

        return file -> {
            FileType fileType = file.getFileType();
            if (fileType.getName().equals("PHP")) {
                Matcher matcher = FILE_PATTERN.matcher(file.getPath());

                return matcher.find();
            }
            return false;
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor(); // Handles String keys
    }

    @Override
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return new EnumeratorStringDescriptor(); // Handles String values
    }

    @Override
    public int getVersion() {
        return 1; // Increased version to handle changes in indexing
    }
}