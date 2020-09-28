package org.keycloak.models.cache.infinispan.entities;

import java.util.Set;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public interface PositionQuery extends InRealm {
    Set<String> getPositions();
}
