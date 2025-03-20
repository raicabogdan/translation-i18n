package ro.raicabogdan.translationi18n;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "TranslationI18nPluginSettings",
        storages = {
                @Storage("translation-i18n.xml")
        }
)
public class Settings implements PersistentStateComponent<Settings>, DumbAware {

    public boolean pluginEnabled = false;
    public boolean dismissEnableNotification = false;
    public static final String DEFAULT_TRANSLATION_PATH = "translation";
    public static final boolean DEFAULT_DISPLAY_TRANSLATION = true;
    public static final String DEFAULT_QUOTE_TYPE = "single";
    public static final String DEFAULT_FILE_EXTENSIONS = "php,twig";
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String DEFAULT_FN_Name = "__";


    public String pathToTranslation = DEFAULT_TRANSLATION_PATH;
    public boolean displayTranslation = DEFAULT_DISPLAY_TRANSLATION;
    public String preferredQuotes = DEFAULT_QUOTE_TYPE;
    public String fileExtensions = DEFAULT_FILE_EXTENSIONS;
    public String defaultLanguage = DEFAULT_LANGUAGE;
    public String defaultFnName = DEFAULT_FN_Name;

    public static Settings getInstance(Project project) {
        return project.getService(Settings.class);
    }

    @Nullable
    @Override
    public Settings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull Settings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }
}
