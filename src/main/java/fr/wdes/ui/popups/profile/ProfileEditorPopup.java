package fr.wdes.ui.popups.profile;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import fr.wdes.Launcher;
import fr.wdes.logger;
import fr.wdes.profile.Profile;
import fr.wdes.profile.ProfileManager;
import fr.wdes.ui.SettingsTheme;
import fr.wdes.ui.lite.LiteButton;

@SuppressWarnings("serial")
public class ProfileEditorPopup extends JPanel implements ActionListener {
    public static void showEditProfileDialog(final Launcher launcher, final Profile profile) {
        final JDialog dialog = new JDialog(launcher.getFrame(), "Paramètres du profil", true);
        final ProfileEditorPopup editor = new ProfileEditorPopup(launcher, profile);
        dialog.setContentPane(editor);
        dialog.pack();
        // Cap the popup height at the launcher frame so very tall section
        // panels don't push it off-screen on small displays.
        final Dimension pref = dialog.getPreferredSize();
        final int maxH = Math.max(280, launcher.getFrame().getHeight() - 40);
        dialog.setSize(Math.max(420, pref.width), Math.min(pref.height, maxH));
        dialog.setLocationRelativeTo(launcher.getFrame());
        dialog.setVisible(true);
    }

    private final Launcher launcher;
    private final Profile originalProfile;
    private final Profile profile;
    private final LiteButton saveButton = new LiteButton("Enregistrer");
    private final LiteButton cancelButton = new LiteButton("Annuler");
    private final ProfileInfoPanel profileInfoPanel;
    private final ProfileVersionPanel profileVersionPanel;

    private final ProfileJavaPanel javaInfoPanel;

    public ProfileEditorPopup(final Launcher launcher, final Profile profile) {
        super(true);

        this.launcher = launcher;
        originalProfile = profile;
        this.profile = new Profile(profile);
        profileInfoPanel = new ProfileInfoPanel(this);
        profileVersionPanel = new ProfileVersionPanel(this);
        javaInfoPanel = new ProfileJavaPanel(this);

        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        saveButton.setFont(SettingsTheme.font(12f));
        cancelButton.setFont(SettingsTheme.font(12f));
        saveButton.setPreferredSize(new Dimension(120, 28));
        cancelButton.setPreferredSize(new Dimension(120, 28));

        setOpaque(true);
        setBackground(SettingsTheme.BG);
        setBorder(new EmptyBorder(14, 16, 14, 16));
        setLayout(new BorderLayout(0, 10));
        createInterface();
    }

    public void actionPerformed(final ActionEvent e) {
        if(e.getSource() == saveButton)
            try {
                final ProfileManager manager = launcher.getProfileManager();
                final Map<String, Profile> profiles = manager.getProfiles();

                if(!originalProfile.getName().equals(profile.getName())) {
                    profiles.remove(originalProfile.getName());

                    while(profiles.containsKey(profile.getName()))
                        profile.setName(profile.getName() + "_");
                }

                profiles.put(profile.getName(), profile);

                manager.saveProfiles();
                manager.fireRefreshEvent();
            }
            catch(final IOException ex) {
            	logger.warn("Couldn't save profiles whilst editing " + profile.getName(), ex);
            }

        final Window window = (Window) getTopLevelAncestor();
        window.dispatchEvent(new WindowEvent(window, 201));
    }

    protected void createInterface() {
        final JPanel standardPanels = new JPanel();
        standardPanels.setOpaque(false);
        standardPanels.setLayout(new BoxLayout(standardPanels, BoxLayout.Y_AXIS));
        standardPanels.add(profileInfoPanel);
        standardPanels.add(SettingsTheme.vGap(10));
        standardPanels.add(profileVersionPanel);
        standardPanels.add(SettingsTheme.vGap(10));
        standardPanels.add(javaInfoPanel);

        add(standardPanels, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public Profile getProfile() {
        return profile;
    }
}
