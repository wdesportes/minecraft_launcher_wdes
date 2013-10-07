package net.minecraft.launcher.ui.popups.login;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.authentication.AuthenticationDatabase;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.GameProfile;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;
import net.minecraft.launcher.authentication.exceptions.InvalidCredentialsException;
import net.minecraft.launcher.authentication.exceptions.UserMigratedException;
import net.minecraft.launcher.authentication.yggdrasil.YggdrasilAuthenticationService;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
@SuppressWarnings("serial")
public class LogInForm extends JPanel implements ActionListener {
    private final LogInPopup popup;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<String> userDropdown = new JComboBox<String>();
    private final JPanel userDropdownPanel = new JPanel();
    private final AuthenticationService authentication = new YggdrasilAuthenticationService();

    public LogInForm(final LogInPopup popup) {
        this.popup = popup;

        usernameField.addActionListener(this);
        passwordField.addActionListener(this);

        createInterface();
    }

    public void actionPerformed(final ActionEvent e) {
        if(e.getSource() == usernameField || e.getSource() == passwordField)
            tryLogIn();
    }

    protected void createInterface() {
        setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 2;
        constraints.gridx = 0;
        constraints.gridy = -1;
        constraints.weightx = 1.0D;

        add(Box.createGlue());

        final JLabel usernameLabel = new JLabel("Identifiant :");
        final Font labelFont = usernameLabel.getFont().deriveFont(1);
        final Font smalltextFont = usernameLabel.getFont().deriveFont(labelFont.getSize() - 2.0F);

        usernameLabel.setFont(labelFont);
        add(usernameLabel, constraints);
        add(usernameField, constraints);

        final JLabel forgotUsernameLabel = new JLabel("(Lequel dois-je utiliser?)");
        forgotUsernameLabel.setFont(smalltextFont);
        forgotUsernameLabel.setHorizontalAlignment(4);
        forgotUsernameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                OperatingSystem.openLink(LauncherConstants.URL_FORGOT_USERNAME);
            }
        });
        add(forgotUsernameLabel, constraints);

        add(Box.createVerticalStrut(10), constraints);

        final JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(labelFont);
        add(passwordLabel, constraints);
        add(passwordField, constraints);

        final JLabel forgotPasswordLabel = new JLabel("(Mot de passe oubli�?)");
        forgotPasswordLabel.setFont(smalltextFont);
        forgotPasswordLabel.setHorizontalAlignment(4);
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                OperatingSystem.openLink(LauncherConstants.URL_FORGOT_PASSWORD_MINECRAFT);
            }
        });
        add(forgotPasswordLabel, constraints);

        createUserDropdownPanel(labelFont);
        add(userDropdownPanel, constraints);

        add(Box.createVerticalStrut(10), constraints);
    }

    protected void createUserDropdownPanel(final Font labelFont) {
        userDropdownPanel.setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 2;
        constraints.gridx = 0;
        constraints.gridy = -1;
        constraints.weightx = 1.0D;

        userDropdownPanel.add(Box.createVerticalStrut(8), constraints);

        final JLabel userDropdownLabel = new JLabel("Character Name:");
        userDropdownLabel.setFont(labelFont);
        userDropdownPanel.add(userDropdownLabel, constraints);
        userDropdownPanel.add(userDropdown, constraints);

        userDropdownPanel.setVisible(false);
    }

    public void tryLogIn() {
        if(authentication.isLoggedIn() && authentication.getSelectedProfile() == null && ArrayUtils.isNotEmpty(authentication.getAvailableProfiles())) {
            popup.setCanLogIn(false);

            GameProfile selectedProfile = null;
            for(final GameProfile profile : authentication.getAvailableProfiles())
                if(profile.getName().equals(userDropdown.getSelectedItem())) {
                    selectedProfile = profile;
                    break;
                }
            if(selectedProfile == null)
                selectedProfile = authentication.getAvailableProfiles()[0];

            final GameProfile finalSelectedProfile = selectedProfile;
            popup.getLauncher().getVersionManager().getExecutorService().execute(new Runnable() {
                public void run() {
                    try {
                        authentication.selectGameProfile(finalSelectedProfile);
                        popup.getLauncher().getProfileManager().getAuthDatabase().register(authentication.getSelectedProfile().getId(), authentication);
                        popup.setLoggedIn(authentication.getSelectedProfile().getId());
                    }
                    catch(final InvalidCredentialsException ex) {
                        popup.getLauncher().println(ex);
                        popup.getErrorForm().displayError(new String[] { "Désolé, mais nous n'avons pu vous identifier dès maintenant.", "S'il vous plait réessayer plus tard." });
                        popup.setCanLogIn(true);
                    }
                    catch(final AuthenticationException ex) {
                        popup.getLauncher().println(ex);
                        popup.getErrorForm().displayError(new String[] { "Désolé, mais nous ne pouvions pas nous connecter à nos serveurs.", "S'il vous plait assurez-vous que vous êtes en ligne et que Minecraft n'est pas bloqué." });
                        popup.setCanLogIn(true);
                    }
                }
            });
        }
        else {
            popup.setCanLogIn(false);
            authentication.logOut();
            authentication.setUsername(usernameField.getText());
            authentication.setPassword(String.valueOf(passwordField.getPassword()));
            final int passwordLength = passwordField.getPassword().length;

            passwordField.setText("");

            popup.getLauncher().getVersionManager().getExecutorService().execute(new Runnable() {
                public void run() {
                    try {
                        authentication.logIn();
                        final AuthenticationDatabase authDatabase = popup.getLauncher().getProfileManager().getAuthDatabase();

                        if(authentication.getSelectedProfile() == null) {
                            if(ArrayUtils.isNotEmpty(authentication.getAvailableProfiles())) {
                                for(final GameProfile profile : authentication.getAvailableProfiles())
                                    userDropdown.addItem(profile.getName());

                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        usernameField.setEditable(false);
                                        passwordField.setEditable(false);
                                        userDropdownPanel.setVisible(true);
                                        popup.repack();
                                        popup.setCanLogIn(true);
                                        passwordField.setText(StringUtils.repeat('*', passwordLength));
                                    }
                                });
                            }
                            else {
                                final String uuid = "demo-" + authentication.getUsername();
                                authDatabase.register(uuid, authentication);
                                popup.setLoggedIn(uuid);
                            }
                        }
                        else {
                            authDatabase.register(authentication.getSelectedProfile().getId(), authentication);
                            popup.setLoggedIn(authentication.getSelectedProfile().getId());
                        }
                    }
                    catch(final UserMigratedException ex) {
                        popup.getLauncher().println(ex);
                        popup.getErrorForm().displayError(new String[] { "Désolé, mais nous ne pouvons pas vous connecter avec votre nom d'utilisateur.", "Vous avez migré votre compte, veuillez utiliser votre adresse e-mail." });
                        popup.setCanLogIn(true);
                    }
                    catch(final InvalidCredentialsException ex) {
                        popup.getLauncher().println(ex);
                        popup.getErrorForm().displayError(new String[] { "Désolé, mais votre nom d'utilisateur ou mot de passe est incorrect !", "S'il vous plait essayer de nouveau. Si vous avez besoin d'aide, essayez le lien 'Mot de passe oublié'." });
                        popup.setCanLogIn(true);
                    }
                    catch(final AuthenticationException ex) {
                        popup.getLauncher().println(ex);
                        popup.getErrorForm().displayError(new String[] { "Désolé, mais les serveurs sont OFF.", "S'il vous plait assurez-vous que vous êtes en ligne et que Minecraft n'est pas OP." });
                        popup.setCanLogIn(true);
                    }
                }
            });
        }
    }
}