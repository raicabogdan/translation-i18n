package ro.raicabogdan.translationi18n.util;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public interface TranslationPsiFileCacheService {
    @Nullable
    PsiFile getTranslationPsiFile();

    void setTranslationPsiFile(@Nullable PsiFile psiFile);
}
