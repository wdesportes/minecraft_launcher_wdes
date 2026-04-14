package fr.wdes;

import java.util.List;
import java.util.Map;

public class JObjectContainer {
	protected String nom;
	protected String version;
	protected String URL_AUTHENTIFICATION_SYSTEM;
	protected String PLACEHOLDER_LOGIN;
	protected String PLACEHOLDER_PASSD;
	protected String URL_FONDS_DOWNLOAD;
	protected String width;
	protected String height;

	/**
	 * Bottom-left text links, rendered left-to-right in declaration order.
	 * Each entry needs at minimum a non-empty {@code url} and {@code name};
	 * everything else is optional. The whole array can be omitted to hide the
	 * footer links entirely.
	 */
	protected List<Link> links;

	/**
	 * Bottom-right social icons, keyed by the well-known network identifier
	 * (facebook, twitter, youtube, steam). Only services with an entry are
	 * rendered; missing entries leave no gap because icons lay out right-to-
	 * left.
	 */
	protected Map<String, Social> socials;

	public JObjectContainer() { }

	/** {@code {"url": "...", "tooltip": "...", "name": "Forum"}} */
	public static class Link {
		protected String url;
		protected String tooltip;
		protected String name;
	}

	/** {@code {"url": "...", "tooltip": "..."}} */
	public static class Social {
		protected String url;
		protected String tooltip;
	}
}
