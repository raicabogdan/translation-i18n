package ro.raicabogdan.translationi18n.util;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class TranslationPsiFileCacheServiceImpl implements TranslationPsiFileCacheService {
    private PsiFile translationPsiFile;

    @Override
    public @Nullable PsiFile getTranslationPsiFile() {
        return translationPsiFile;
    }

    @Override
    public void setTranslationPsiFile(@Nullable PsiFile psiFile) {
        this.translationPsiFile = psiFile;
    }
}
