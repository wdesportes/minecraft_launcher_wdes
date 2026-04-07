package fr.wdes.versions;

import java.util.HashMap;
import java.util.Map;

public enum ReleaseType {
    SNAPSHOT("snapshot", "Enable experimental development versions (\"snapshots\")"), RELEASE("release", null), OLD_BETA("old-beta", "Allow use of old \"Beta\" minecraft versions (From 2010-2011)"), OLD_ALPHA("old-alpha", "Allow use of old \"Alpha\" minecraft versions (From 2010)");

    protected static final String POPUP_DEV_VERSIONS = "Etes vous sûr de vouloir activer les versions de développement ?\nAucune stabilitée n'est garantie, ils peuvent corrompre vos mondes\nVous êtes conseillés de changer le répertoire par défaut ou de faire des sauvegardes régulières.";
    protected static final String POPUP_OLD_VERSIONS = "Ces versions sont très anciennes et instables. Aucuns bugs ou autres problèmes auront une correction prévue.\nIl est fortement conseillé de changer de répertoire de démarrage.\nNous ne sommes pas responsables pour la perte de votre nostalgie ou de vos sauvegardes !";
    protected static final Map<String, ReleaseType> lookup;

    public static ReleaseType getByName(final String name) {
        return lookup.get(name);
    }

    protected final String name;

    protected final String description;

    static {
        lookup = new HashMap<String, ReleaseType>();

        for(final ReleaseType type : values())
            lookup.put(type.getName(), type);
    }

    private ReleaseType(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getPopupWarning() {
        if(description == null)
            return null;
        if(this == SNAPSHOT)
            return POPUP_DEV_VERSIONS;
        if(this == OLD_BETA)
            return POPUP_OLD_VERSIONS;
        if(this == OLD_ALPHA)
            return POPUP_OLD_VERSIONS;
        return null;
    }
}