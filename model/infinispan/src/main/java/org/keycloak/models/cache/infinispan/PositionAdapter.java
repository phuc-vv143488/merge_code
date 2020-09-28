package org.keycloak.models.cache.infinispan;

import org.keycloak.models.*;
import org.keycloak.models.cache.infinispan.entities.CachedPosition;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public class PositionAdapter implements PositionModel {

    protected final CachedPosition cached;
    protected final RealmCacheSession cacheSession;
    protected final KeycloakSession keycloakSession;
    protected final RealmModel realm;
    private final Supplier<PositionModel> modelSupplier;
    protected volatile PositionModel updated;

    public PositionAdapter(CachedPosition cached, RealmCacheSession cacheSession, KeycloakSession keycloakSession, RealmModel realm) {
        this.cached = cached;
        this.cacheSession = cacheSession;
        this.keycloakSession = keycloakSession;
        this.realm = realm;
        modelSupplier = this::getPositionModel;
    }

    protected void getDelegateForUpdate() {
        if (updated == null) {
            cacheSession.registerPositionInvalidation(cached.getId());
            updated = modelSupplier.get();
            if (updated == null) throw new IllegalStateException("Not found in database");
        }
    }

    protected volatile boolean invalidated;
    public void invalidate() {
        invalidated = true;
    }

    protected boolean isUpdated() {
        if (updated != null) return true;
        if (!invalidated) return false;
        updated = cacheSession.getRealmDelegate().getPositionById(cached.getId(), realm);
        if (updated == null) throw new IllegalStateException("Not found in database");
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PositionModel)) return false;

        PositionModel that = (PositionModel) o;

        if (!cached.getId().equals(that.getPosId().toString())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return cached.getId().hashCode();
    }

    @Override
    public String getPosId() {
        if (isUpdated()) return updated.getPosId();
        return cached.getId();
    }

    @Override
    public String getPosName() {
        if (isUpdated()) return updated.getPosName();
        return cached.getPosName();
    }

    @Override
    public void setPosName(String name) {
        getDelegateForUpdate();
        updated.setPosName(name);

    }

    @Override
    public String getPosCode() {
        if (isUpdated()) return updated.getPosCode();
        return cached.getPosCode();
    }

    @Override
    public void setPosCode(String code) {
        getDelegateForUpdate();
        updated.setPosCode(code);
    }

    @Override
    public String getDescription() {
        if (isUpdated()) return updated.getDescription();
        return cached.getDescription();
    }

    @Override
    public void setDescription(String description) {
        getDelegateForUpdate();
        updated.setDescription(description);
    }

    @Override
    public Long getStatus() {
        if (isUpdated()) return updated.getStatus();
        return cached.getStatus();
    }

    @Override
    public void setStatus(Long status) {
        getDelegateForUpdate();
        updated.setStatus(status);
    }

    @Override
    public Long getType() {
        if (isUpdated()) return updated.getType();
        return cached.getType();
    }

    @Override
    public void setType(Long type) {
        getDelegateForUpdate();
        updated.setType(type);
    }

    @Override
    public Long getMaxUserChiNhanh() {
        if (isUpdated()) return updated.getMaxUserChiNhanh();
        return cached.getMaxUserChiNhanh();
    }

    @Override
    public void setMaxUserChiNhanh(Long maxUserChiNhanh) {
        getDelegateForUpdate();
        updated.setMaxUserChiNhanh(maxUserChiNhanh);
    }

    @Override
    public Long getMaxUserTrungTam() {
        if (isUpdated()) return updated.getMaxUserTrungTam();
        return cached.getMaxUserTrungTam();
    }

    @Override
    public void setMaxUserTrungTam(Long maxUserTrungTam) {
        getDelegateForUpdate();
        updated.setMaxUserTrungTam(maxUserTrungTam);
    }

    @Override
    public Long getLimitType() {
        if (isUpdated()) return updated.getLimitType();
        return cached.getLimitType();
    }

    @Override
    public void setLimitType(Long limitType) {
        getDelegateForUpdate();
        updated.setLimitType(limitType);
    }

    @Override
    public Date getValidDateStart() {
        if (isUpdated()) return updated.getValidDateStart();
        return cached.getValidDateStart();
    }

    @Override
    public void setValidDateStart(Date validDateStart) {
        getDelegateForUpdate();
        updated.setValidDateStart(validDateStart);
    }

    @Override
    public Date getValidDateEnd() {
        if (isUpdated()) return updated.getValidDateEnd();
        return cached.getValidDateEnd();
    }

    @Override
    public void setValidDateEnd(Date validDateEnd) {
        getDelegateForUpdate();
        updated.setValidDateEnd(validDateEnd);
    }

    @Override
    public Date getCreateDate() {
        if (isUpdated()) return updated.getCreateDate();
        return cached.getCreateDate();
    }

    @Override
    public void setCreateDate(Date createDate) {
        getDelegateForUpdate();
        updated.setCreateDate(createDate);
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        getDelegateForUpdate();
        updated.setSingleAttribute(name, value);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        getDelegateForUpdate();
        updated.setAttribute(name, values);
    }

    @Override
    public void removeAttribute(String name) {
        getDelegateForUpdate();
        updated.removeAttribute(name);

    }

    @Override
    public String getFirstAttribute(String name) {
        if (isUpdated()) return updated.getFirstAttribute(name);
        return cached.getAttributes(modelSupplier).getFirst(name);
    }

    @Override
    public List<String> getAttribute(String name) {
        List<String> values = cached.getAttributes(modelSupplier).get(name);
        if (values == null) return null;
        return values;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return cached.getAttributes(modelSupplier);
    }

    @Override
    public Set<RoleModel> getRealmRoleMappings() {
        if (isUpdated()) return updated.getRealmRoleMappings();
        Set<RoleModel> roleMappings = getRoleMappings();
        Set<RoleModel> realmMappings = new HashSet<>();
        for (RoleModel role : roleMappings) {
            RoleContainerModel container = role.getContainer();
            if (container instanceof RealmModel) {
                if (((RealmModel) container).getId().equals(realm.getId())) {
                    realmMappings.add(role);
                }
            }
        }
        return realmMappings;
    }

    @Override
    public Set<RoleModel> getClientRoleMappings(ClientModel app) {
        if (isUpdated()) return updated.getClientRoleMappings(app);
        Set<RoleModel> roleMappings = getRoleMappings();
        Set<RoleModel> appMappings = new HashSet<>();
        for (RoleModel role : roleMappings) {
            RoleContainerModel container = role.getContainer();
            if (container instanceof ClientModel) {
                if (((ClientModel) container).getId().equals(app.getId())) {
                    appMappings.add(role);
                }
            }
        }
        return appMappings;
    }

    @Override
    public boolean hasRole(RoleModel role) {
        if (isUpdated()) return updated.hasRole(role);
        if (cached.getRoleMappings(modelSupplier).contains(role.getId())) return true;

        Set<RoleModel> mappings = getRoleMappings();
        for (RoleModel mapping: mappings) {
            if (mapping.hasRole(role)) return true;
        }
        return false;
    }

    @Override
    public void grantRole(RoleModel role) {
        getDelegateForUpdate();
        updated.grantRole(role);
    }

    @Override
    public Set<RoleModel> getRoleMappings() {
        if (isUpdated()) return updated.getRoleMappings();
        Set<RoleModel> roles = new HashSet<>();
        for (String id : cached.getRoleMappings(modelSupplier)) {
            RoleModel roleById = keycloakSession.realms().getRoleById(id, realm);
            if (roleById == null) {
                // chance that role was removed, so just delegate to persistence and get user invalidated
                getDelegateForUpdate();
                return updated.getRoleMappings();
            }
            roles.add(roleById);

        }
        return roles;
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        getDelegateForUpdate();
        updated.deleteRoleMapping(role);
    }

    private PositionModel getPositionModel() {
        return cacheSession.getRealmDelegate().getPositionById(cached.getId(), realm);
    }
}
