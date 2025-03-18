package ro.raicabogdan.translationi18n;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RestartNotification {

    public static void showRestartNotification(Project project) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("ro.raicabogdan.translationi18n.translation.RestartGroup") //ensure this ID is in your plugin.xml
                .createNotification("PhpStorm Restart Required",
                        "A setting change requires PhpStorm to be restarted for it to take effect.",
                        NotificationType.INFORMATION)
                .addAction(new AnAction("Restart PhpStorm") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        ApplicationManager.getApplication().restart();
                    }
                })
                .notify(project);
    }
}