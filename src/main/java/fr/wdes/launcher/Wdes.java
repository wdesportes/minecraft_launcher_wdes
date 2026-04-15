package fr.wdes.launcher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import fr.wdes.Launcher;


import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

@SuppressWarnings("serial")
public class Wdes extends JFrame {

    public static double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version"));
    public static void main(final String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {



        System.setProperty("java.net.preferIPv4Stack", "true");

        final OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();

        optionParser.accepts("help", "Show help").forHelp();
        optionParser.accepts("force", "Force updating");

        final OptionSpec<String> proxyHostOption = optionParser.accepts("proxyHost", "Optional").withRequiredArg();
        final OptionSpec<Integer> proxyPortOption = optionParser.accepts("proxyPort", "Optional").withRequiredArg().defaultsTo("8080", new String[0]).ofType(Integer.class);
        final OptionSpec<String> proxyUserOption = optionParser.accepts("proxyUser", "Optional").withRequiredArg();
        final OptionSpec<String> proxyPassOption = optionParser.accepts("proxyPass", "Optional").withRequiredArg();

        final OptionSpec<String> nonOptions = optionParser.nonOptions();
        OptionSet optionSet;
        try {
            optionSet = optionParser.parse(args);
        }
        catch(final OptionException e) {
            optionParser.printHelpOn(System.out);
            System.out.println("(Pour ajouter un argument : '--' suivi de chaque nom d'argument ");
            return;
        }

        if(optionSet.has("help")) {
            optionParser.printHelpOn(System.out);
            return;
        }

        final String hostName = optionSet.valueOf(proxyHostOption);
        Proxy proxy = Proxy.NO_PROXY;
        if(hostName != null)
            try {
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(hostName, optionSet.valueOf(proxyPortOption).intValue()));
            }
            catch(final Exception ignored) {
            }
        final String proxyUser = optionSet.valueOf(proxyUserOption);
        final String proxyPass = optionSet.valueOf(proxyPassOption);
        PasswordAuthentication passwordAuthentication = null;
        if(!proxy.equals(Proxy.NO_PROXY) && stringHasValue(proxyUser) && stringHasValue(proxyPass)) {
            passwordAuthentication = new PasswordAuthentication(proxyUser, proxyPass.toCharArray());

            final PasswordAuthentication auth = passwordAuthentication;
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return auth;
                }

            });
        }



        final List<String> strings = optionSet.valuesOf(nonOptions);
        final String[] remainderArgs = strings.toArray(new String[strings.size()]);

       new Wdes( proxy, passwordAuthentication, remainderArgs);


    }

    public static boolean stringHasValue(final String string) {
        return string != null && !string.isEmpty();
    }

    private final SplashPanel splash;

    public Wdes(final Proxy proxy, final PasswordAuthentication proxyAuth, final String[] remainderArgs) {
        super("[WdesLaunchers] Lancement de votre launcher en cours...");
        this.setUndecorated(true);


        if (JAVA_VERSION <= 1.6) {
            JOptionPane.showMessageDialog(this,
                "Version de java détéctée : "+JAVA_VERSION+", Version 1.7 minimum !!.\nMerci de mettre a jour java : java.com.",
                "[WdesLaunchers] Impossible de lancer java",
                JOptionPane.ERROR_MESSAGE);
            System.out.println("Version : "+JAVA_VERSION);
            System.exit(0);
        }
        // Match LauncherPanel.FRAME_WIDTH / FRAME_HEIGHT (880 / 520) so
        // frame.pack() in Launcher.initializeFrame() doesn't resize or
        // shift the window when the splash is swapped out for the real
        // launcher panel - visually, the window stays put.
        setSize(880, 520);
        setDefaultCloseOperation(3);

        // Replace the legacy white JTextArea boot screen with a glassy
        // splash showing the bundled blurred background + a single
        // rolling status line. Older log lines still go to stdout for
        // debugging so nothing is actually lost.
        splash = new SplashPanel(880, 520);
        add(splash);
        setLocationRelativeTo(null);
        setVisible(true);

        // Boot diagnostics: dumped to stdout (kept for debugging) but no
        // longer painted into a scrolling text area on the splash. The
        // splash shows a single rolling status line driven by the latest
        // print() / println() call.
        println("WdesLauncher v(1.0)");
        println("Current time is " + DateFormat.getDateTimeInstance(2, 2, Locale.FRANCE).format(new Date()));
        println("os.name="     + System.getProperty("os.name"));
        println("os.version="  + System.getProperty("os.version"));
        println("os.arch="     + System.getProperty("os.arch"));
        println("java.version=" + System.getProperty("java.version"));
        println("java.vendor="  + System.getProperty("java.vendor"));
        println("sun.arch.data.model=" + System.getProperty("sun.arch.data.model"));

        splash.setStatus("Préparation du launcher...");
        new Launcher(this, "33a86c10-1e71-4e51-83b5-bdf218f29b97", "wdeslaunchers", proxy, proxyAuth, remainderArgs);
    }


    public void print(final String string) {
        System.out.print(string);
        if (splash != null) {
            splash.setStatus(string);
        }
    }

    public void println(final String string) {
        print(string + "\n");
    }
}
