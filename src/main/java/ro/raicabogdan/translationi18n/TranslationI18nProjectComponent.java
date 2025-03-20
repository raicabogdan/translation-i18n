package ro.raicabogdan.translationi18n;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.psi.PsiElement;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.raicabogdan.translationi18n.util.NotificationUtil;

public class TranslationI18nProjectComponent {
    public static class PostStartupActivity implements ProjectActivity {
        @Nullable
        @Override
        public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
            if (!ApplicationManager.getApplication().isUnitTestMode() && !ApplicationManager.getApplication().isHeadlessEnvironment()) {
                checkIfProjectHasPluginEnabled(project);
            }

            return Unit.INSTANCE;
        }
    }

    private static void checkIfProjectHasPluginEnabled(@NotNull Project project) {
        if(!isEnabled(project) && !Settings.getInstance(project).dismissEnableNotification) {
            NotificationUtil.showEnableMessage(project);
        }
    }

    public static boolean isEnabled(@Nullable Project project) {
        return project != null && Settings.getInstance(project).pluginEnabled;
    }

    public static boolean psiElementIsValid(@Nullable PsiElement psiElement) {
        return psiElement != null && isEnabled(psiElement.getProject());
    }
}