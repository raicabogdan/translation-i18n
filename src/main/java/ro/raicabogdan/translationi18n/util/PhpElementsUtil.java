package ro.raicabogdan.translationi18n.util;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.twig.elements.TwigCompositeElement;

public class PhpElementsUtil {
    static public PsiElementPattern.Capture<PsiElement> getParameterInsideMethodReferencePattern() {
        return PlatformPatterns
                .psiElement()
                .withParent(
                        PlatformPatterns.psiElement(StringLiteralExpression.class)
                                .withParent(
                                        PlatformPatterns.psiElement(ParameterList.class)
                                                .withParent(
                                                        PlatformPatterns.psiElement(MethodReference.class)
                                                )
                                )
                )
                .withLanguage(PhpLanguage.INSTANCE);
    }

    static public PsiElementPattern.Capture<PsiElement> getParameterInsideNewExpressionPattern() {
        return PlatformPatterns
                .psiElement()
                .withParent(
                        PlatformPatterns.psiElement(StringLiteralExpression.class)
                                .withParent(
                                        PlatformPatterns.psiElement(ParameterList.class)
                                                .withParent(
                                                        PlatformPatterns.psiElement(NewExpression.class)
                                                )
                                )
                )
                .withLanguage(PhpLanguage.INSTANCE);
    }

    static public PsiElementPattern.Capture<PsiElement> getParameterInsideFunctionReferencePattern() {
        return PlatformPatterns
                .psiElement()
                .withParent(
                        PlatformPatterns.psiElement()
                                .withParent(
                                        PlatformPatterns.psiElement(ParameterList.class)
                                                .withParent(
                                                        PlatformPatterns.psiElement(FunctionReference.class)
                                                )
                                )
                )
                .withLanguage(PhpLanguage.INSTANCE);
    }

    static public PsiElementPattern.Capture<PsiElement> getParameterInsideTwigFunctionReferencePattern() {
        return PlatformPatterns
                .psiElement()
                .withParent(
                        PlatformPatterns.psiElement(TwigCompositeElement.class)
                );
    }
}