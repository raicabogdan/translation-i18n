package ro.raicabogdan.translationi18n;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.jgit.annotations.Nullable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import ro.raicabogdan.translationi18n.util.VirtualFileUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;

public class SettingsForm implements Configurable {
    private final Project project;
    private boolean restartNeeded = false;
    private JCheckBox pluginEnabled;

    private JPanel panel;
    private TextFieldWithBrowseButton pathToTranslationTextField;
    private JButton pathToTranslationTextFieldReset;

    private JTextField fileExtensionTextField;
    private JButton fileExtensionTextFieldReset;

    private JTextField defaultLanguage;
    private JComboBox<String> preferredQuotes;
    private JCheckBox displayTranslation;
    private JTextField defaultFnName;

    public SettingsForm(Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Trans I18n";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        pathToTranslationTextField.addBrowseFolderListener(createBrowseFolderListener(pathToTranslationTextField.getTextField(), FileChooserDescriptorFactory.createSingleFolderDescriptor()));
        pathToTranslationTextFieldReset.addMouseListener(createResetPathButtonMouseListener(pathToTranslationTextField.getTextField(), Settings.DEFAULT_TRANSLATION_PATH));

        fileExtensionTextFieldReset.addMouseListener(createResetPathButtonMouseListener(fileExtensionTextField, Settings.DEFAULT_FILE_EXTENSIONS));

        return panel;
    }

    @Override
    public boolean isModified() {
        if (!pluginEnabled.isSelected() == getSettings().pluginEnabled
            || !displayTranslation.isSelected() == getSettings().displayTranslation
            || !this.defaultLanguage.getText().equals(getSettings().defaultLanguage)
        ) {
            restartNeeded = true;
        }

        return !pluginEnabled.isSelected() == getSettings().pluginEnabled
                || !this.pathToTranslationTextField.getText().equals(getSettings().pathToTranslation)
                || !displayTranslation.isSelected() == getSettings().displayTranslation
                || !Objects.requireNonNull(preferredQuotes.getSelectedItem()).toString().equals(getSettings().preferredQuotes)
                || !this.fileExtensionTextField.getText().equals(getSettings().fileExtensions)
                || !this.defaultLanguage.getText().equals(getSettings().defaultLanguage)
                || !this.defaultFnName.getText().equals(getSettings().defaultFnName);
    }

    @Override
    public void apply() {
        getSettings().pluginEnabled = pluginEnabled.isSelected();

        getSettings().pathToTranslation = pathToTranslationTextField.getText();
        getSettings().displayTranslation = displayTranslation.isSelected();
        getSettings().preferredQuotes = Objects.requireNonNull(preferredQuotes.getSelectedItem()).toString();
        getSettings().defaultLanguage = defaultLanguage.getText();
        getSettings().fileExtensions = fileExtensionTextField.getText();
        getSettings().defaultFnName = defaultFnName.getText();

        if (restartNeeded) {
            if (project != null) {
                RestartNotification.showRestartNotification(project);
            }
        }
    }

    @Override
    public void reset() {
        updateUIFromSettings();
    }

    private Settings getSettings() {
        return Settings.getInstance(project);
    }

    private void updateUIFromSettings() {

        pluginEnabled.setSelected(getSettings().pluginEnabled);

        pathToTranslationTextField.setText(getSettings().pathToTranslation);
        displayTranslation.setSelected(getSettings().displayTranslation);
        preferredQuotes.setSelectedItem(getSettings().preferredQuotes);
        defaultLanguage.setText(getSettings().defaultLanguage);
        fileExtensionTextField.setText(getSettings().fileExtensions);
        defaultFnName.setText(getSettings().defaultFnName);
    }

    private TextBrowseFolderListener createBrowseFolderListener(final JTextField textField, final FileChooserDescriptor fileChooserDescriptor) {
        return new TextBrowseFolderListener(fileChooserDescriptor) {
            @Override
            public void actionPerformed(ActionEvent e) {
                VirtualFile projectDirectory = VirtualFileUtil.getProjectBaseDir(project);
                VirtualFile selectedFile = FileChooser.chooseFile(
                        fileChooserDescriptor,
                        project,
                        VfsUtil.findRelativeFile(textField.getText(), projectDirectory)
                );

                if (null == selectedFile) {
                    return; // Ignore but keep the previous path
                }

                String path = VfsUtil.getRelativePath(selectedFile, projectDirectory, '/');
                if (null == path) {
                    path = selectedFile.getPath();
                }

                textField.setText(path);
            }
        };
    }

    private MouseListener createResetPathButtonMouseListener(final JTextField textField, final String defaultValue) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                textField.setText(defaultValue);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        };
    }
}