package fr.wdes.versions;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.lang3.text.StrSubstitutor;

import fr.wdes.LauncherConstants;
import fr.wdes.OperatingSystem;
@SuppressWarnings("serial")
public class Library {
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private static final StrSubstitutor SUBSTITUTOR = new StrSubstitutor(new HashMap() {
    });
    protected String name;
    protected List<Rule> rules;
    protected Map<OperatingSystem, String> natives;
    protected ExtractRules extract;
    protected String url;

    public Library() {
    }

    public Library(final String name) {
        if(name == null || name.length() == 0)
            throw new IllegalArgumentException("Library name cannot be null or empty");
        this.name = name;
    }

    public Library addNative(final OperatingSystem operatingSystem, final String name) {
        if(operatingSystem == null || !operatingSystem.isSupported())
            throw new IllegalArgumentException("Cannot add native for unsupported OS");
        if(name == null || name.length() == 0)
            throw new IllegalArgumentException("Cannot add native for null or empty name");
        if(natives == null)
            natives = new EnumMap<OperatingSystem, String>(OperatingSystem.class);
        natives.put(operatingSystem, name);
        return this;
    }

    public boolean appliesToCurrentEnvironment() {
        if(rules == null)
            return true;
        Rule.Action lastAction = Rule.Action.DISALLOW;

        for(final Rule rule : rules) {
            final Rule.Action action = rule.getAppliedAction();
            if(action != null)
                lastAction = action;
        }

        return lastAction == Rule.Action.ALLOW;
    }

    public String getArtifactBaseDir() {
        if(name == null)
            throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
        // Full unbounded split: Mojang 1.13+ libraries carry the classifier
        // baked into the name ("org.lwjgl:lwjgl:3.3.1:natives-linux"), and
        // the older ":", 3 split would glue "3.3.1:natives-linux" into one
        // version slot and produce paths libraries.minecraft.net 404s on.
        final String[] parts = name.split(":");
        return String.format("%s/%s/%s", new Object[] { parts[0].replaceAll("\\.", "/"), parts[1], parts[2] });
    }

    public String getArtifactFilename(final String classifier) {
        if(name == null)
            throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");

        final String[] parts = name.split(":");
        // Classifier priority:
        //   1. explicit argument (legacy natives-map layout: the
        //      natives.linux value IS the classifier);
        //   2. parts[3] if the name itself is four-part
        //      (modern Mojang layout for native artefacts);
        //   3. no classifier.
        String effectiveClassifier = classifier;
        if ((effectiveClassifier == null || effectiveClassifier.isEmpty()) && parts.length > 3) {
            effectiveClassifier = parts[3];
        }
        final String suffix = effectiveClassifier != null && !effectiveClassifier.isEmpty()
                ? "-" + effectiveClassifier
                : "";
        final String result = String.format("%s-%s%s.jar", new Object[] { parts[1], parts[2], suffix });

        return SUBSTITUTOR.replace(result);
    }

    public String getArtifactPath() {
        return getArtifactPath(null);
    }

    public String getArtifactPath(final String classifier) {
        if(name == null)
            throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
        return String.format("%s/%s", new Object[] { getArtifactBaseDir(), getArtifactFilename(classifier) });
    }

    public String getDownloadUrl() {
        if(url != null)
            return url;
        return LauncherConstants.LIBRARY_DOWNLOAD_BASE;
    }

    public ExtractRules getExtractRules() {
        return extract;
    }

    public String getName() {
        return name;
    }

    public Map<OperatingSystem, String> getNatives() {
        return natives;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public boolean hasCustomUrl() {
        return url != null;
    }

    public Library setExtractRules(final ExtractRules rules) {
        extract = rules;
        return this;
    }

    @Override
    public String toString() {
        return "Library{name='" + name + '\'' + ", rules=" + rules + ", natives=" + natives + ", extract=" + extract + '}';
    }
}