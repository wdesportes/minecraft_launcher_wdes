package fr.wdes;

public class JObjectContainer {
	protected String nom;
	protected String version;
	protected String URL_AUTHENTIFICATION_SYSTEM;
	protected String PLACEHOLDER_LOGIN;
	protected String PLACEHOLDER_PASSD;
	protected String URL_FONDS_DOWNLOAD;
	protected String width;
	protected String height;
	// Optional bottom-left text links. Any field left null is simply not
	// rendered, so the launcher only shows links the operator opted into.
	protected String URL_HOME;
	protected String URL_FORUM;
	protected String URL_DONATE;
	// Optional bottom-right social icons. Same opt-in rule.
	protected String URL_TWITTER;
	protected String URL_FACEBOOK;
	protected String URL_YOUTUBE;
	protected String URL_STEAM;
	public JObjectContainer() { }
}
