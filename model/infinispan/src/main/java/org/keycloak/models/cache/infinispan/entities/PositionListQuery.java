package org.keycloak.models.cache.infinispan.entities;

import org.keycloak.models.RealmModel;

import java.util.Set;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public class PositionListQuery extends AbstractRevisioned implements PositionQuery {
    private final Set<String> positions;
    private final String realm;
    private final String realmName;

    public PositionListQuery(Long revisioned, String id, RealmModel realm, Set<String> positions) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.realmName = realm.getName();
        this.positions = positions;
    }

    @Override
    public Set<String> getPositions() {
        return positions;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public String toString() {
        return "PositionListQuery{" +
                "id='" + getId() + "'" +
                "realmName='" + realmName + '\'' +
                '}';
    }
}
