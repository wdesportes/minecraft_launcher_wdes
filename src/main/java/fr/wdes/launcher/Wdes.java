package fr.wdes.launcher;

import java.awt.Font;
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

import fr.wdes.Launcher;


import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

@SuppressWarnings("serial")
public class Wdes extends JFrame {

	private static final Font MONOSPACED = new Font("Monospaced", 0, 12);
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

    private final JTextArea textArea;


    private final JScrollPane scrollPane;

    private final StringBuilder outputBuffer = new StringBuilder();

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
        setSize(854, 480);
        setDefaultCloseOperation(3);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setFont(MONOSPACED);
        ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(1);

        scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(22);

        add(scrollPane);
        setLocationRelativeTo(null);
        setVisible(true);
        println("WdesLauncher v(1.0)");
        println(new StringBuilder().append("Current time is ").append(DateFormat.getDateTimeInstance(2, 2, Locale.FRANCE).format(new Date())).toString());
        println(new StringBuilder().append("System.getProperty('os.name') == '").append(System.getProperty("os.name")).append("'").toString());
        println(new StringBuilder().append("System.getProperty('os.version') == '").append(System.getProperty("os.version")).append("'").toString());
        println(new StringBuilder().append("System.getProperty('os.arch') == '").append(System.getProperty("os.arch")).append("'").toString());
        println(new StringBuilder().append("System.getProperty('java.version') == '").append(System.getProperty("java.version")).append("'").toString());
        println(new StringBuilder().append("System.getProperty('java.vendor') == '").append(System.getProperty("java.vendor")).append("'").toString());
        println(new StringBuilder().append("System.getProperty('sun.arch.data.model') == '").append(System.getProperty("sun.arch.data.model")).append("'").toString());
        println("");

        new Launcher(this,"33a86c10-1e71-4e51-83b5-bdf218f29b97","wdeslaunchers", proxy, proxyAuth, remainderArgs);


    }


    public void print(final String string) {
        System.out.print(string);

        outputBuffer.append(string);

        final Document document = textArea.getDocument();
        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();

        final boolean shouldScroll = scrollBar.getValue() + scrollBar.getSize().getHeight() + MONOSPACED.getSize() * 2 > scrollBar.getMaximum();
        try {
            document.insertString(document.getLength(), string, null);
        }
        catch(final BadLocationException ignored) {
        }
        if(shouldScroll)
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    scrollBar.setValue(2147483647);
                }
            });
    }

    public void println(final String string) {
        print(new StringBuilder().append(string).append("\n").toString());
    }





	}

