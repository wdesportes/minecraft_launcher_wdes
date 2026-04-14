package fr.wdes.ui.popups.profile;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fr.wdes.OperatingSystem;
import fr.wdes.ui.SettingsTheme;
import fr.wdes.ui.lite.LiteCheckBox;
import fr.wdes.ui.lite.LiteTextField;

@SuppressWarnings("serial")
public class ProfileJavaPanel extends JPanel {
    private final ProfileEditorPopup editor;
    private final LiteCheckBox javaPathCustom = new LiteCheckBox("Executable Java");
    private final JTextField javaPathField = new LiteTextField();
    private final LiteCheckBox javaArgsCustom = new LiteCheckBox("Arguments JVM");
    private final JTextField javaArgsField = new LiteTextField();

    public ProfileJavaPanel(final ProfileEditorPopup editor) {
        this.editor = editor;

        setLayout(new BorderLayout(0, 6));
        setOpaque(false);
        SettingsTheme.styleSection(this);

        add(SettingsTheme.header("Paramètres Java (Avancé)"), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        fillDefaultValues();
        addEventHandlers();
    }

    protected void addEventHandlers() {
        javaPathCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) { updateJavaPathState(); }
        });
        javaPathField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(final DocumentEvent e) { updateJavaPath(); }
            public void insertUpdate(final DocumentEvent e)  { updateJavaPath(); }
            public void removeUpdate(final DocumentEvent e)  { updateJavaPath(); }
        });
        javaArgsCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) { updateJavaArgsState(); }
        });
        javaArgsField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(final DocumentEvent e) { updateJavaArgs(); }
            public void insertUpdate(final DocumentEvent e)  { updateJavaArgs(); }
            public void removeUpdate(final DocumentEvent e)  { updateJavaArgs(); }
        });
    }

    private JPanel buildBody() {
        final JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        int row = 0;
        body.add(javaPathCustom, SettingsTheme.labelConstraints(row));
        body.add(javaPathField,  SettingsTheme.fieldConstraints(row));
        row++;

        body.add(javaArgsCustom, SettingsTheme.labelConstraints(row));
        body.add(javaArgsField,  SettingsTheme.fieldConstraints(row));
        row++;

        return body;
    }

    protected void fillDefaultValues() {
        final String javaPath = editor.getProfile().getJavaPath();
        if(javaPath != null) {
            javaPathCustom.setSelected(true);
            javaPathField.setText(javaPath);
        }
        else {
            javaPathCustom.setSelected(false);
            javaPathField.setText(OperatingSystem.getCurrentPlatform().getJavaDir());
        }
        updateJavaPathState();

        final String args = editor.getProfile().getJavaArgs();
        if(args != null) {
            javaArgsCustom.setSelected(true);
            javaArgsField.setText(args);
        }
        else {
            javaArgsCustom.setSelected(false);
            javaArgsField.setText("-Xmx1G");
        }
        updateJavaArgsState();
    }

    private void updateJavaArgs() {
        if(javaArgsCustom.isSelected())
            editor.getProfile().setJavaArgs(javaArgsField.getText());
        else
            editor.getProfile().setJavaArgs(null);
    }

    private void updateJavaArgsState() {
        if(javaArgsCustom.isSelected()) {
            javaArgsField.setEnabled(true);
            editor.getProfile().setJavaArgs(javaArgsField.getText());
        }
        else {
            javaArgsField.setEnabled(false);
            editor.getProfile().setJavaArgs(null);
        }
    }

    private void updateJavaPath() {
        if(javaPathCustom.isSelected())
            editor.getProfile().setJavaDir(javaPathField.getText());
        else
            editor.getProfile().setJavaDir(null);
    }

    private void updateJavaPathState() {
        if(javaPathCustom.isSelected()) {
            javaPathField.setEnabled(true);
            editor.getProfile().setJavaDir(javaPathField.getText());
        }
        else {
            javaPathField.setEnabled(false);
            editor.getProfile().setJavaDir(null);
        }
    }
}
