package com.contentful.vault;

import org.apache.commons.lang3.StringUtils;

public final class Resource implements ResourceInterface {

    private final String remoteId;

    private final String createdAt;

    private final String updatedAt;

    private final String contentType;

    public Resource(String remoteId, String createdAt, String updatedAt, String contentType) {
        this.remoteId = remoteId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.contentType = contentType;
    }

    @Override
    public String remoteId() {
        return remoteId;
    }

    @Override
    public String createdAt() {
        return createdAt;
    }

    @Override
    public String updatedAt() {
        return updatedAt;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    String getIdPrefix() {
        return null;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;

        Resource resource = (Resource) o;
        String prefix = StringUtils.defaultString(getIdPrefix(), "");
        if (!prefix.equals(StringUtils.defaultString(resource.getIdPrefix(), ""))) return false;
        return (prefix + remoteId()).equals(prefix + resource.remoteId());
    }

    @Override public int hashCode() {
        return (StringUtils.defaultString(getIdPrefix(), "") + remoteId).hashCode();
    }
}
