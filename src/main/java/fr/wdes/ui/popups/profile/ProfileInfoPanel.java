package fr.wdes.ui.popups.profile;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fr.wdes.Launcher;
import fr.wdes.profile.LauncherVisibilityRule;
import fr.wdes.profile.Profile;
import fr.wdes.ui.SettingsTheme;
import fr.wdes.ui.lite.LiteCheckBox;
import fr.wdes.ui.lite.LiteComboBox;
import fr.wdes.ui.lite.LiteTextField;

@SuppressWarnings("serial")
public class ProfileInfoPanel extends JPanel {
    private final ProfileEditorPopup editor;
    private final LiteCheckBox gameDirCustom = new LiteCheckBox("Répertoire du jeu");
    private final JTextField profileName = new LiteTextField();
    private final JTextField gameDirField = new LiteTextField();
    private final LiteCheckBox resolutionCustom = new LiteCheckBox("Résolution personnalisée");
    private final JTextField resolutionWidth = new LiteTextField();
    private final JTextField resolutionHeight = new LiteTextField();
    private final LiteCheckBox launcherVisibilityCustom = new LiteCheckBox("Visibilité du launcher");
    private final LiteComboBox<LauncherVisibilityRule> launcherVisibilityOption = new LiteComboBox<LauncherVisibilityRule>();

    public ProfileInfoPanel(final ProfileEditorPopup editor) {
        this.editor = editor;

        setLayout(new BorderLayout(0, 6));
        setOpaque(false);
        SettingsTheme.styleSection(this);

        add(SettingsTheme.header("Informations du profil"), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        fillDefaultValues();
        addEventHandlers();
    }

    protected void addEventHandlers() {
        profileName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(final DocumentEvent e) { updateProfileName(); }
            public void insertUpdate(final DocumentEvent e)  { updateProfileName(); }
            public void removeUpdate(final DocumentEvent e)  { updateProfileName(); }
        });
        gameDirField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(final DocumentEvent e) { updateGameDir(); }
            public void insertUpdate(final DocumentEvent e)  { updateGameDir(); }
            public void removeUpdate(final DocumentEvent e)  { updateGameDir(); }
        });
        resolutionCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) { updateResolutionState(); }
        });
        final DocumentListener resolutionListener = new DocumentListener() {
            public void changedUpdate(final DocumentEvent e) { updateResolution(); }
            public void insertUpdate(final DocumentEvent e)  { updateResolution(); }
            public void removeUpdate(final DocumentEvent e)  { updateResolution(); }
        };
        resolutionWidth.getDocument().addDocumentListener(resolutionListener);
        resolutionHeight.getDocument().addDocumentListener(resolutionListener);

        launcherVisibilityCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) { updateLauncherVisibilityState(); }
        });
        launcherVisibilityOption.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) { updateLauncherVisibilitySelection(); }
        });
    }

    @SuppressWarnings("unchecked")
    private JPanel buildBody() {
        final JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        int row = 0;

        body.add(SettingsTheme.formLabel("Nom du profil"), SettingsTheme.labelConstraints(row));
        body.add(profileName, SettingsTheme.fieldConstraints(row));
        row++;

        // Game directory
        body.add(gameDirCustom, SettingsTheme.labelConstraints(row));
        gameDirCustom.setEnabled(false);
        body.add(gameDirField, SettingsTheme.fieldConstraints(row));
        row++;

        // Resolution
        final JPanel resolutionPanel = new JPanel();
        resolutionPanel.setOpaque(false);
        resolutionPanel.setLayout(new BoxLayout(resolutionPanel, BoxLayout.X_AXIS));
        resolutionPanel.add(resolutionWidth);
        resolutionPanel.add(Box.createHorizontalStrut(8));
        final JLabel x = new JLabel("x");
        x.setForeground(SettingsTheme.MUTED);
        x.setFont(SettingsTheme.font(12f));
        resolutionPanel.add(x);
        resolutionPanel.add(Box.createHorizontalStrut(8));
        resolutionPanel.add(resolutionHeight);

        body.add(resolutionCustom, SettingsTheme.labelConstraints(row));
        body.add(resolutionPanel, SettingsTheme.fieldConstraints(row));
        row++;

        // Visibility rule
        body.add(launcherVisibilityCustom, SettingsTheme.labelConstraints(row));
        body.add(launcherVisibilityOption, SettingsTheme.fieldConstraints(row));
        row++;

        for(final LauncherVisibilityRule value : LauncherVisibilityRule.values())
            launcherVisibilityOption.addItem(value);

        return body;
    }

    protected void fillDefaultValues() {
        profileName.setText(editor.getProfile().getName());

        final File gameDir = editor.getProfile().getGameDir();
        if(gameDir != null) {
            gameDirCustom.setSelected(true);
            gameDirField.setText(gameDir.getAbsolutePath());
        }
        else {
            gameDirCustom.setSelected(false);
            gameDirField.setText(editor.getLauncher().getWorkingDirectory().getAbsolutePath());
        }
        updateGameDirState();

        Profile.Resolution resolution = editor.getProfile().getResolution();
        resolutionCustom.setSelected(resolution != null);
        if(resolution == null)
            resolution = Profile.DEFAULT_RESOLUTION;
        resolutionWidth.setText(String.valueOf(Launcher.getInstance().width));
        resolutionHeight.setText(String.valueOf(Launcher.getInstance().height));
        updateResolutionState();

        final LauncherVisibilityRule visibility = editor.getProfile().getLauncherVisibilityOnGameClose();
        if(visibility != null) {
            launcherVisibilityCustom.setSelected(true);
            launcherVisibilityOption.setSelectedItem(visibility);
        }
        else {
            launcherVisibilityCustom.setSelected(false);
            launcherVisibilityOption.setSelectedItem(Profile.DEFAULT_LAUNCHER_VISIBILITY);
        }
        updateLauncherVisibilityState();
    }

    private void updateGameDir() {
        final File file = new File(gameDirField.getText());
        editor.getProfile().setGameDir(file);
    }

    private void updateGameDirState() {
        if(gameDirCustom.isSelected()) {
            gameDirField.setEnabled(true);
            editor.getProfile().setGameDir(new File(gameDirField.getText()));
        }
        else {
            gameDirField.setEnabled(false);
            editor.getProfile().setGameDir(null);
        }
    }

    private void updateLauncherVisibilitySelection() {
        final Profile profile = editor.getProfile();

        if(launcherVisibilityOption.getSelectedItem() instanceof LauncherVisibilityRule)
            profile.setLauncherVisibilityOnGameClose((LauncherVisibilityRule) launcherVisibilityOption.getSelectedItem());
    }

    private void updateLauncherVisibilityState() {
        final Profile profile = editor.getProfile();

        if(launcherVisibilityCustom.isSelected() && launcherVisibilityOption.getSelectedItem() instanceof LauncherVisibilityRule) {
            profile.setLauncherVisibilityOnGameClose((LauncherVisibilityRule) launcherVisibilityOption.getSelectedItem());
            launcherVisibilityOption.setEnabled(true);
        }
        else {
            profile.setLauncherVisibilityOnGameClose(null);
            launcherVisibilityOption.setEnabled(false);
        }
    }

    private void updateProfileName() {
        if(profileName.getText().length() > 0)
            editor.getProfile().setName(profileName.getText());
    }

    private void updateResolution() {
        try {
            final int width = Integer.parseInt(resolutionWidth.getText());
            final int height = Integer.parseInt(resolutionHeight.getText());

            editor.getProfile().setResolution(new Profile.Resolution(width, height));
        }
        catch(final NumberFormatException ignored) {
            editor.getProfile().setResolution(null);
        }
    }

    private void updateResolutionState() {
        if(resolutionCustom.isSelected()) {
            resolutionWidth.setEnabled(false);
            resolutionHeight.setEnabled(false);
            updateResolution();
        }
        else {
            resolutionWidth.setEnabled(false);
            resolutionHeight.setEnabled(false);
            editor.getProfile().setResolution(null);
        }
    }
}
