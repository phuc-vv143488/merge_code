package org.keycloak.models.cache.infinispan.entities;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.PositionModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.cache.infinispan.DefaultLazyLoader;
import org.keycloak.models.cache.infinispan.LazyLoader;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public class CachedPosition extends AbstractRevisioned implements InRealm {

    private final String realm;

    private final String posCode;
    private final String posName;
    private final String description;
    private final Long status;
    private final Long type;
    private final Long maxUserChiNhanh;
    private final Long maxUserTrungTam;
    private final Long limitType;
    private final Date validDateStart;
    private final Date validDateEnd;
    private final Date createDate;

    private final LazyLoader<PositionModel, MultivaluedHashMap<String, String>> attributes;
    private final LazyLoader<PositionModel, Set<String>> roleMappings;

    public CachedPosition(Long revision, RealmModel realm, PositionModel position) {
        super(revision, position.getPosId());
        this.realm = realm.getId();
        this.posName = position.getPosName();
        this.posCode = position.getPosCode();
        this.description = position.getDescription();
        this.status = position.getStatus();
        this.type = position.getType();
        this.maxUserChiNhanh = position.getMaxUserChiNhanh();
        this.maxUserTrungTam = position.getMaxUserTrungTam();
        this.limitType = position.getLimitType();
        this.validDateStart = position.getValidDateStart();
        this.validDateEnd = position.getValidDateEnd();
        this.createDate = position.getCreateDate();

        this.attributes = new DefaultLazyLoader<>(source -> new MultivaluedHashMap<>(source.getAttributes()), MultivaluedHashMap::new);
        this.roleMappings = new DefaultLazyLoader<>(source -> source.getRoleMappings().stream().map(RoleModel::getId).collect(Collectors.toSet()), Collections::emptySet);
    }

    public String getRealm() {
        return realm;
    }

    public MultivaluedHashMap<String, String> getAttributes(Supplier<PositionModel> position) {
        return attributes.get(position);
    }

    public Set<String> getRoleMappings(Supplier<PositionModel> position) {
        // it may happen that positions were not loaded before so we don't actually need to invalidate entries in the cache
        if (position == null) {
            return Collections.emptySet();
        }
        return roleMappings.get(position);
    }

    public String getPosName() {
        return posName;
    }

    public String getPosCode() {
        return posCode;
    }

    public String getDescription() {
        return description;
    }

    public Long getStatus() {
        return status;
    }

    public Long getType() {
        return type;
    }

    public Long getMaxUserChiNhanh() {
        return maxUserChiNhanh;
    }

    public Long getMaxUserTrungTam() {
        return maxUserTrungTam;
    }

    public Long getLimitType() {
        return limitType;
    }

    public Date getValidDateStart() {
        return validDateStart;
    }

    public Date getValidDateEnd() {
        return validDateEnd;
    }

    public Date getCreateDate() {
        return createDate;
    }
}
