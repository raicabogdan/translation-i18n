package ro.raicabogdan.translationi18n.translation;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.twig.elements.TwigCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.raicabogdan.translationi18n.Settings;
import ro.raicabogdan.translationi18n.TranslationI18nProjectComponent;
import ro.raicabogdan.translationi18n.util.PhpElementsUtil;
import ro.raicabogdan.translationi18n.util.TranslationIndexUtil;

import java.util.*;

public class TranslationCompletionContributor {
    public static class Completion extends CompletionContributor {
        public Completion() {
            // __('<caret>', []);
            // t->_('<caret>', []);
            extend(CompletionType.BASIC, PlatformPatterns.or(
                    PhpElementsUtil.getParameterInsideMethodReferencePattern(),
                    PhpElementsUtil.getParameterInsideNewExpressionPattern(),
                    PhpElementsUtil.getParameterInsideFunctionReferencePattern(),
                    PhpElementsUtil.getParameterInsideTwigFunctionReferencePattern()
            ), new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters,
                                              @NotNull ProcessingContext context,
                                              @NotNull CompletionResultSet result) {
                    PsiElement element = parameters.getOriginalPosition();
                    if (element == null || (
                            !(element.getParent() instanceof StringLiteralExpression)
                                    && !(element.getParent() instanceof TwigCompositeElement)
                    )) {
                        return;
                    }

                    PsiFile file = element.getContainingFile();
                    Settings settings = Settings.getInstance(file.getProject());

                    if (settings.fileExtensions.isEmpty()) {
                        return;
                    }
                    String fileName = file.getName().toLowerCase();

                    Set<String> allowedExtensions = new HashSet<>();
                    for (String ext : settings.fileExtensions.split(",")) { // Example: ".php,.twig,.volt"
                        allowedExtensions.add(ext.trim().replaceFirst("^\\.", "").toLowerCase()); // Normalize extensions
                    }
                    // Extract file extension
                    int dotIndex = fileName.lastIndexOf('.');
                    if (dotIndex == -1) {
                        return;
                    }
                    String fileExtension = fileName.substring(dotIndex + 1).toLowerCase(); // Ensure lowercase comparison
                    // Check if the file extension is allowed
                    if (!allowedExtensions.contains(fileExtension)) {
                        return;
                    }

                    PsiElement parent = element.getParent();
                    boolean showAutoComplete = false;
                    if (parent instanceof TwigCompositeElement twigElem) {
                        showAutoComplete = checkTwigContext(twigElem);
                    } else {
                        PsiElement grandParent = parent.getParent().getParent();

                        if (!(grandParent instanceof FunctionReference functionReference)) {
                            return;
                        }
                        String functionName = functionReference.getName();
                        if (functionName == null) {
                            return;
                        }
                        if (functionName.equals(settings.defaultFnName) || functionName.equals("__") || functionName.equals("_")) {
                            showAutoComplete = true;
                        }
                    }

                    if (!showAutoComplete) {
                        return;
                    }

                    // Fetch translations
                    Map<String, String> translations = TranslationIndexUtil.getAllTranslations(file.getProject());

                    for (String key : translations.keySet()) {
                        result.addElement(LookupElementBuilder.create(key).withTypeText(translations.get(key)));
                    }
                }
            });
        }

        private boolean checkTwigContext(TwigCompositeElement element) {
            String userFnc = Settings.getInstance(element.getProject()).defaultFnName;
            // __('title_login')
            // t._('title')
            // userFnc('title)
            if (element.getFirstChild().getContext() != null) {
                String method = element.getFirstChild().getContext().getText();
                return method != null &&
                        (method.startsWith(userFnc + "('")
                                || method.startsWith(userFnc + "(\"")
                                || method.startsWith("__('")
                                || method.startsWith("__(\"")
                                || method.startsWith("t._('")
                                || method.startsWith("t._(\"")
                        );
            }

            return false;
        }
    }

    public static class GotoDeclaration implements GotoDeclarationHandler {
        @Override
        public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
            if (sourceElement != null &&
                    (sourceElement.getParent() instanceof StringLiteralExpression || sourceElement.getParent() instanceof TwigCompositeElement) &&
                    (PhpElementsUtil.getParameterInsideMethodReferencePattern().accepts(sourceElement)
                            || PhpElementsUtil.getParameterInsideNewExpressionPattern().accepts(sourceElement)
                            || PhpElementsUtil.getParameterInsideFunctionReferencePattern().accepts(sourceElement)
                            || PhpElementsUtil.getParameterInsideTwigFunctionReferencePattern().accepts(sourceElement)
                    )) {
                Collection<PsiElement> psiElements = new ArrayList<>();

                Project project = sourceElement.getProject();

                String translationKey = sourceElement.getText()
                        .replace("'", "").replace("\"", ""); // Strip quotes

                PsiElement psiElement = TranslationIndexUtil.getPsiElement(project, translationKey);
                if (!TranslationI18nProjectComponent.psiElementIsValid(psiElement)) {
                    return null;
                }

                psiElements.add(psiElement);

                return psiElements.toArray(new PsiElement[0]);
            }

            return null;
        }
    }
}