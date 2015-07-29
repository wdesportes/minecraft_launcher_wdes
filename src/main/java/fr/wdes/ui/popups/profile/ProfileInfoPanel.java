package fr.wdes.ui.popups.profile;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fr.wdes.Launcher;
import fr.wdes.profile.LauncherVisibilityRule;
import fr.wdes.profile.Profile;

@SuppressWarnings("serial")
public class ProfileInfoPanel extends JPanel {
    private final ProfileEditorPopup editor;
    private final JCheckBox gameDirCustom = new JCheckBox("Répertoire du jeu:");
    private final JTextField profileName = new JTextField();
    private final JTextField gameDirField = new JTextField();
    private final JCheckBox resolutionCustom = new JCheckBox("Résolution:");
    private final JTextField resolutionWidth = new JTextField();
    private final JTextField resolutionHeight = new JTextField();
        private final JCheckBox launcherVisibilityCustom = new JCheckBox("Visibilité du launcher:");
    private final JComboBox<LauncherVisibilityRule> launcherVisibilityOption = new JComboBox<LauncherVisibilityRule>();

    public ProfileInfoPanel(final ProfileEditorPopup editor) {
        this.editor = editor;

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Informations du profil"));

        createInterface();
        fillDefaultValues();
        addEventHandlers();
    }

    protected void addEventHandlers() {
        profileName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(final DocumentEvent e) {
                ProfileInfoPanel.this.updateProfileName();
            }

            public void insertUpdate(final DocumentEvent e) {
                ProfileInfoPanel.this.updateProfileName();
            }

            public void removeUpdate(final DocumentEvent e) {
                ProfileInfoPanel.this.updateProfileName();
            }
        });
        /*
        gameDirCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                ProfileInfoPanel.this.updateGameDirState();
            }
        });
        */
        gameDirField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(final DocumentEvent e) {
                ProfileInfoPanel.this.updateGameDir();
            }

            public void insertUpdate(final DocumentEvent e) {
                ProfileInfoPanel.this.updateGameDir();
            }

            public void removeUpdate(final DocumentEvent e) {
                ProfileInfoPanel.this.updateGameDir();
            }
        });
        resolutionCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                ProfileInfoPanel.this.updateResolutionState();
            }
        });
        final DocumentListener resolutionListener = new DocumentListener() {
            public void changedUpdate(final DocumentEvent e) {
                ProfileInfoPanel.this.updateResolution();
            }

            public void insertUpdate(final DocumentEvent e) {
                ProfileInfoPanel.this.updateResolution();
            }

            public void removeUpdate(final DocumentEvent e) {
                ProfileInfoPanel.this.updateResolution();
            }
        };
        resolutionWidth.getDocument().addDocumentListener(resolutionListener);
        resolutionHeight.getDocument().addDocumentListener(resolutionListener);


        launcherVisibilityCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                ProfileInfoPanel.this.updateLauncherVisibilityState();
            }
        });
        launcherVisibilityOption.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                ProfileInfoPanel.this.updateLauncherVisibilitySelection();
            }
        });
    }

    protected void createInterface() {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.anchor = 17;

        constraints.gridy = 0;

        add(new JLabel("Nom du profil:"), constraints);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(profileName, constraints);
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        add(gameDirCustom, constraints);
        gameDirCustom.setEnabled(false);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(gameDirField, constraints);
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        final JPanel resolutionPanel = new JPanel();
        resolutionPanel.setLayout(new BoxLayout(resolutionPanel, 0));
        resolutionPanel.add(resolutionWidth);
        resolutionPanel.add(Box.createHorizontalStrut(5));
        resolutionPanel.add(new JLabel("x"));
        resolutionPanel.add(Box.createHorizontalStrut(5));
        resolutionPanel.add(resolutionHeight);

        add(resolutionCustom, constraints);
        resolutionCustom.setEnabled(false);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(resolutionPanel, constraints);
        constraints.gridwidth = 1;
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        add(launcherVisibilityCustom, constraints);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(launcherVisibilityOption, constraints);
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        for(final LauncherVisibilityRule value : LauncherVisibilityRule.values())
            launcherVisibilityOption.addItem(value);
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