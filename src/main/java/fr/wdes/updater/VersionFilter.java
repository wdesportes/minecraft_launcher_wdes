package fr.wdes.updater;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import fr.wdes.versions.ReleaseType;


public class VersionFilter {
	protected final Set<ReleaseType> types = new HashSet<ReleaseType>();
	protected int maxCount = 5;

    public VersionFilter() {
        Collections.addAll(types, ReleaseType.values());
    }

    public VersionFilter excludeTypes(final ReleaseType[] types) {
        if(types != null)
            for(final ReleaseType type : types)
                this.types.remove(type);
        return this;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public Set<ReleaseType> getTypes() {
        return types;
    }

    public VersionFilter includeTypes(final ReleaseType[] types) {
        if(types != null)
            Collections.addAll(this.types, types);
        return this;
    }

    public VersionFilter onlyForTypes(final ReleaseType[] types) {
        this.types.clear();
        includeTypes(types);
        return this;
    }

    public VersionFilter setMaxCount(final int maxCount) {
        this.maxCount = maxCount;
        return this;
    }
}