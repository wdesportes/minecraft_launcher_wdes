package fr.wdes.ui.tabs;

import java.awt.Component;

import javax.swing.JTabbedPane;

import fr.wdes.Launcher;
@SuppressWarnings("serial")
public class LauncherTabPanel extends JTabbedPane {
    private final Launcher launcher;
    //private final HomeTab home;
    //private final WebsiteTab blog;
    private final ConsoleTab console;

    public LauncherTabPanel(final Launcher launcher) {
        super(1);

        this.launcher = launcher;
        //home = new HomeTab(launcher);
        //blog = new WebsiteTab(launcher);
        console = new ConsoleTab(launcher);

        createInterface();
    }

    protected void createInterface() {
    	//addTab("Main", home);
    	//setTabPlacement(JTabbedPane.SOUTH_EAST);
        //addTab("Web", blog);
        addTab("Console", console);


    }


    public ConsoleTab getConsole() {
        return console;
    }

    public Launcher getLauncher() {
        return launcher;
    }

    protected void removeTab(final Component tab) {
        for(int i = 0; i < getTabCount(); i++)
            if(getTabComponentAt(i) == tab) {
                removeTabAt(i);
                break;
            }
    }

    public void showConsole() {
        setSelectedComponent(console);
    }
}