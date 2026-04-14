package fr.wdes.ui.popups.profile;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import fr.wdes.events.RefreshedVersionsListener;
import fr.wdes.ui.SettingsTheme;
import fr.wdes.ui.lite.LiteComboBox;
import fr.wdes.updater.VersionManager;
import fr.wdes.updater.VersionSyncInfo;
import fr.wdes.versions.Version;

@SuppressWarnings("serial")
public class ProfileVersionPanel extends JPanel implements RefreshedVersionsListener {

    private static class VersionListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            if(value instanceof VersionSyncInfo) {
                final VersionSyncInfo syncInfo = (VersionSyncInfo) value;
                final Version version = syncInfo.getLatestVersion();
                value = String.format("%s %s", version.getType().getName(), version.getId());
            }
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setOpaque(true);
            setForeground(SettingsTheme.FG);
            setBackground(isSelected ? new java.awt.Color(255, 255, 255, 35) : new java.awt.Color(28, 28, 30, 240));
            setFont(SettingsTheme.font(12f));
            setBorder(new javax.swing.border.EmptyBorder(4, 8, 4, 8));
            return this;
        }
    }

    private final ProfileEditorPopup editor;
    private final LiteComboBox<Object> versionList = new LiteComboBox<Object>();

    public ProfileVersionPanel(final ProfileEditorPopup editor) {
        this.editor = editor;

        setLayout(new BorderLayout(0, 6));
        setOpaque(false);
        SettingsTheme.styleSection(this);

        add(SettingsTheme.header("Version de Minecraft"), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        addEventHandlers();

        final List<VersionSyncInfo> versions = editor.getLauncher().getVersionManager().getVersions(editor.getProfile().getVersionFilter());
        if(versions.isEmpty())
            editor.getLauncher().getVersionManager().addRefreshedVersionsListener(this);
        else
            populateVersions(versions);
    }

    protected void addEventHandlers() {
        versionList.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) { updateVersionSelection(); }
        });
    }

    private JPanel buildBody() {
        final JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        final GridBagConstraints lc = SettingsTheme.labelConstraints(0);
        body.add(new JLabel("") {
            // empty placeholder for the label slot - the section header
            // already says what this is, so we just want the field to
            // span the full width.
            { setPreferredSize(new java.awt.Dimension(0, 0)); }
        }, lc);

        final GridBagConstraints fc = SettingsTheme.fieldConstraints(0);
        fc.gridx = 0;
        fc.gridwidth = 2;
        body.add(versionList, fc);

        versionList.setRenderer(new VersionListRenderer());
        // Disabled by default; populateVersions() flips it back on once the
        // remote version list has actually been loaded so users can pick a
        // version. Without this re-enable the dropdown was permanently
        // greyed out and there was no way to switch versions.
        versionList.setEnabled(false);
        return body;
    }

    public void onVersionsRefreshed(final VersionManager manager) {
        final List<VersionSyncInfo> versions = manager.getVersions(editor.getProfile().getVersionFilter());
        populateVersions(versions);
        editor.getLauncher().getVersionManager().removeRefreshedVersionsListener(this);
    }

    private void populateVersions(final List<VersionSyncInfo> versions) {
        final String previous = editor.getProfile().getLastVersionId();
        VersionSyncInfo selected = null;

        versionList.removeAllItems();
        versionList.addItem("Garder la dernière version");

        for(final VersionSyncInfo version : versions) {
            if(version.getLatestVersion().getId().equals(previous))
                selected = version;

            versionList.addItem(version);
        }

        if(selected == null && !versions.isEmpty())
            versionList.setSelectedIndex(0);
        else
            versionList.setSelectedItem(selected);

        versionList.setEnabled(!versions.isEmpty());
    }

    public boolean shouldReceiveEventsInUIThread() {
        return true;
    }

    private void updateVersionSelection() {
        final Object selection = versionList.getSelectedItem();

        if(selection instanceof VersionSyncInfo) {
            final Version version = ((VersionSyncInfo) selection).getLatestVersion();
            editor.getProfile().setLastVersionId(version.getId());
        }
        else
            editor.getProfile().setLastVersionId(null);
    }
}
