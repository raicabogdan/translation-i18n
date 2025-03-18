package ro.raicabogdan.translationi18n;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TranslationI18nProjectComponent {
    public static class PostStartupActivity implements ProjectActivity {
        @Nullable
        @Override
        public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
            return Unit.INSTANCE;
        }
    }
}