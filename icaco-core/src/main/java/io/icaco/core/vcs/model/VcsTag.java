package io.icaco.core.vcs.model;

public class VcsTag {

    private final String tagWithOptionalPrefix;
    private final String tagWithoutOptionalPrefix;

    public VcsTag(String tagWithOptionalPrefix, String tagWithoutOptionalPrefix) {
        this.tagWithOptionalPrefix = tagWithOptionalPrefix;
        this.tagWithoutOptionalPrefix = tagWithoutOptionalPrefix;
    }

    public String getName() {
        return tagWithoutOptionalPrefix;
    }

    public boolean hasCommitsOnTag() {
        return !tagWithOptionalPrefix.equals(tagWithoutOptionalPrefix);
    }

}
