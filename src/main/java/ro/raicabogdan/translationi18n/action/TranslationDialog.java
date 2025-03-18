package ro.raicabogdan.translationi18n.action;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import ro.raicabogdan.translationi18n.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TranslationDialog extends DialogWrapper {
    private final JTextField keyField;
    private final Map<String, JTextField> translationFields;
    private final JPanel panel;
    private boolean wrapInCurlyBrackets = true;

    public TranslationDialog(String selectedText, HashMap<String, String> locales, String defaultLanguage) {
        super(true);
        setTitle("Add Translation");
        String keyFieldValue = StringUtils.snakeCase(selectedText);
        if ( (selectedText.charAt(0) == '\'' && selectedText.charAt(selectedText.length()-1) == '\'') ||
                selectedText.charAt(0) == '"' && selectedText.charAt(selectedText.length()-1) == '"') {
            wrapInCurlyBrackets = false;
        }
        String selectedValueText = selectedText.replaceAll("^\"|\"$", "").replaceAll("^'|'$", "");
        keyField = new JTextField(keyFieldValue, 1);
        translationFields = new HashMap<>();
        panel = new JPanel(new GridLayout(locales.size() + 2, 2));

        panel.add(new JLabel("Translation Key:"));
        panel.add(keyField);

        for (Map.Entry<String, String> locale : locales.entrySet()) {
            if (locale != null) {
                String defaultValue = locale.getKey().equals(defaultLanguage) ? selectedValueText : "";
                JTextField translationField = new JTextField(defaultValue, 20);

                translationFields.put(locale.getKey(), translationField);

                panel.add(new JLabel("Translation (" + locale.getKey().toUpperCase() + "):"));
                panel.add(translationField);
            }
        }

        init();
        setOKButtonText("Save");
        setCancelButtonText("Cancel");

        setResizable(false);
        getRootPane().setDefaultButton(getButton(getOKAction()));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public String getTranslationKey() {
        return keyField.getText().trim();
    }

    public boolean isWrapInCurlyBrackets() {
        return wrapInCurlyBrackets;
    }

    public Map<String, String> getTranslations() {
        Map<String, String> translations = new HashMap<>();
        for (Map.Entry<String, JTextField> entry : translationFields.entrySet()) {
            translations.put(entry.getKey(), entry.getValue().getText().trim());
        }
        return translations;
    }
}
