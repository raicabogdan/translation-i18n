package ro.raicabogdan.translationi18n.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.Nullable;
import ro.raicabogdan.translationi18n.Settings;
import ro.raicabogdan.translationi18n.indexing.TranslationKeyIndex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationIndexUtil {
    public static Map<String, String> getAllTranslations(Project project) {
        Map<String, String> translations = new HashMap<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        ID<String, String> indexId = TranslationKeyIndex.INDEX_ID;

        GlobalSearchScope scope = GlobalSearchScope.allScope(project);

        index.processAllKeys(indexId, key -> {
            List<String> values = index.getValues(indexId, key, scope);
            if (!values.isEmpty()) {
                translations.put(key, values.get(0));
            }
            return true;
        }, project);

        return translations;
    }

    public static @Nullable PsiElement getPsiElement(Project project, String translationKey) {
        TranslationPsiFileCacheService cacheService = project.getService(TranslationPsiFileCacheService.class);
        PsiFile psiFile;
        PsiFile cachedFile = cacheService.getTranslationPsiFile();

        if (cachedFile != null && cachedFile.isValid()) {
            psiFile = cachedFile;
        } else {
            psiFile = getTranslationFile(project);
        }

        if (psiFile != null) {
            for (PsiElement element : psiFile.getChildren()) {
                int singleQuoteOffset = element.getText().indexOf("'" + translationKey + "'");
                if (singleQuoteOffset != -1) {
                    return getElementAtPosition(psiFile, singleQuoteOffset);
                }

                int doubleQuoteOffset = element.getText().indexOf("\"" + translationKey + "\"");
                if (doubleQuoteOffset != -1) {
                    return getElementAtPosition(psiFile, doubleQuoteOffset);
                }
            }
        }
        return null;
    }

    private static PsiElement getElementAtPosition(PsiFile psiFile, int offset) {
        if (offset >= 0 && offset < psiFile.getTextLength()) {
            return psiFile.findElementAt(offset);
        }
        return null;
    }

    private @Nullable static PsiFile getTranslationFile(Project project) {
        TranslationPsiFileCacheService cacheService = project.getService(TranslationPsiFileCacheService.class);
        Settings settings = Settings.getInstance(project);
        if (settings.defaultLanguage.isEmpty() || settings.pathToTranslation.isEmpty()) {
            return null;
        }
        String pathToTranslationsFolder = project.getBasePath()+ "/" + settings.pathToTranslation;

        String folderBasedPath = pathToTranslationsFolder + "/" + settings.defaultLanguage + "/messages.php";
        String filenameBasedPath = pathToTranslationsFolder + "/messages." + settings.defaultLanguage + ".php";

        VirtualFile translationFile = VirtualFileUtil.findTranslationFile(folderBasedPath, filenameBasedPath);
        if (translationFile == null) {
            return null;
        }

        cacheService.setTranslationPsiFile(PsiManager.getInstance(project).findFile(translationFile));

        return cacheService.getTranslationPsiFile();
    }
}