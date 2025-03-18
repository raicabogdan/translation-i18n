package ro.raicabogdan.translationi18n.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;
import ro.raicabogdan.translationi18n.LanguageFileLoader;
import ro.raicabogdan.translationi18n.Settings;
import ro.raicabogdan.translationi18n.indexing.TranslationKeyIndex;
import ro.raicabogdan.translationi18n.util.VirtualFileUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationGeneratorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        if (editor == null) return;
        Document document = editor.getDocument();
        FileType fileType = VirtualFileUtil.getFileTypeFromEditor(editor, project);

        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || selectedText.trim().isEmpty()) {
            Messages.showMessageDialog("No text selected!", "Error", Messages.getErrorIcon());
            return;
        }

        // Load settings for translation path
        Settings settings = Settings.getInstance(project);
        if (settings.pathToTranslation == null || settings.pathToTranslation.isEmpty()) {
            Messages.showMessageDialog("Translation path is not set!", "Error", Messages.getErrorIcon());
            return;
        }

        LanguageFileLoader languageFileLoader = new LanguageFileLoader(project);

        // Get language files
        List<String> languageFiles = languageFileLoader.getLanguageFiles();
        if (languageFiles.isEmpty()) {
            Messages.showMessageDialog("No language files found!", "Error", Messages.getErrorIcon());
            return;
        }
        HashMap<String, String> locales = languageFileLoader.getLocales();

        // Show dialog to input translation key and values
        TranslationDialog dialog = new TranslationDialog(selectedText, locales, settings.defaultLanguage);
        if (dialog.showAndGet()) {
            boolean writeNewTranslation = true;
            String translationKey = dialog.getTranslationKey();
            Map<String, String> translations = dialog.getTranslations();

            // start checking if translation exists
            FileBasedIndex index = FileBasedIndex.getInstance();
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            ID<String, String> indexId = TranslationKeyIndex.INDEX_ID;

            // check if translation key exists
            Collection<String> existingKeys = index.getAllKeys(indexId, project);
            Collection<String> existingValues = index.getValues(indexId, translationKey, scope);

            if (!existingValues.isEmpty()) {
                int result = Messages.showYesNoDialog("Key already exists. Do you want to use the existing key?", "Key Exists", Messages.getQuestionIcon());
                if (result == Messages.YES) {
                    writeNewTranslation = false;
                }
            }
            if (writeNewTranslation && existingKeys.contains(translations.get(settings.defaultLanguage))) {
                int result = Messages.showYesNoDialog("Value already exists as a key. Do you want to use that existing key?", "Value Exists", Messages.getQuestionIcon());
                if (result == Messages.YES) {
                    writeNewTranslation = false;
                }
            }

            if (writeNewTranslation) {
                // There is no existing translation, so save translations to files
                for (Map.Entry<String, String> locale : locales.entrySet()) {
                    if (locale != null && translations.containsKey(locale.getKey())) {
                        saveTranslation(locale.getValue(), translationKey, translations.get(locale.getKey()), settings.preferredQuotes, project);
                    }
                }
            }

            // replace the translation value with translation key
            WriteCommandAction.runWriteCommandAction(project, () -> ApplicationManager.getApplication().runWriteAction(() -> {
                String quote = settings.preferredQuotes.equals("single") ? "'" : "\"";
                String newSelectionKey = quote + translationKey + quote;

                if (dialog.isWrapInCurlyBrackets() && fileType.getName().equals("Twig")) {
                    newSelectionKey = "{{ " + settings.defaultFnName + "(" + newSelectionKey + ") }}";
                }
                document.replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), newSelectionKey);
            }));
        }
    }

    public static void saveTranslation(String filePath, String key, String value, String preferredQuotes, Project project) {
        VirtualFile virtualFile = VirtualFileUtil.findTranslationFile(filePath);
        if (virtualFile == null) {
            return;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (!(psiFile instanceof PhpFile)) {
            return;
        }
        ArrayCreationExpression translationArrayExpr = PsiTreeUtil.findChildOfType(psiFile, ArrayCreationExpression.class);
        if (translationArrayExpr == null) return;

        boolean useSingleQuotes = preferredQuotes.equals("single"); // Fetch from settings if needed
        String arrayElementText = useSingleQuotes
                ? "<?php return ['" + key + "' => '" + value + "'];"
                : "<?php return [\"" + key + "\" => \"" + value + "\"];"
            ;

        PhpPsiElement newElement = PhpPsiElementFactory.createFromText(
                project,
                ArrayHashElement.class, arrayElementText
        );

        if (newElement != null) {
            newElement.add(PhpPsiElementFactory.createComma(project));
            newElement.add(PhpPsiElementFactory.createNewLine(project));

            WriteCommandAction.runWriteCommandAction(project, () -> {
                translationArrayExpr.addBefore(newElement, translationArrayExpr.getLastChild()); // Insert before closing bracket
            });
        }
    }
}
