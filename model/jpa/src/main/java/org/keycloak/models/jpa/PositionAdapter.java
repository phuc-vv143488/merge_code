package org.keycloak.models.jpa;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.*;
import org.keycloak.models.jpa.entities.PositionAttributeEntity;
import org.keycloak.models.jpa.entities.PositionEntity;
import org.keycloak.models.jpa.entities.PositionRoleMappingEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.RoleUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public class PositionAdapter implements PositionModel, JpaModel<PositionEntity> {

    protected PositionEntity position;
    protected EntityManager em;
    protected RealmModel realm;

    public PositionAdapter(RealmModel realm, EntityManager em, PositionEntity position) {
        this.em = em;
        this.position = position;
        this.realm = realm;
    }

    public PositionEntity getEntity() {
        return position;
    }


    public static PositionEntity toEntity(PositionModel model, EntityManager em) {
        if (model instanceof PositionAdapter) {
            return ((PositionAdapter)model).getEntity();
        }
        return em.getReference(PositionEntity.class, model.getPosId());
    }

    @Override
    public boolean hasRole(RoleModel role) {
        Set<RoleModel> roles = getRoleMappings();
        return RoleUtils.hasRole(roles, role);
    }

    protected TypedQuery<PositionRoleMappingEntity> getPositionRoleMappingEntityTypedQuery(RoleModel role) {
        TypedQuery<PositionRoleMappingEntity> query = em.createNamedQuery("positionHasRole", PositionRoleMappingEntity.class);
        query.setParameter("position", getEntity());
        query.setParameter("roleId", role.getId());
        return query;
    }

    @Override
    public void grantRole(RoleModel role) {
        if (hasRole(role)) return;
        PositionRoleMappingEntity entity = new PositionRoleMappingEntity();
        entity.setPosition(getEntity());
        entity.setRoleId(role.getId());
        em.persist(entity);
        em.flush();
        em.detach(entity);
    }

    @Override
    public Set<RoleModel> getRealmRoleMappings() {
        Set<RoleModel> roleMappings = getRoleMappings();

        Set<RoleModel> realmRoles = new HashSet<RoleModel>();
        for (RoleModel role : roleMappings) {
            RoleContainerModel container = role.getContainer();
            if (container instanceof RealmModel) {
                realmRoles.add(role);
            }
        }
        return realmRoles;
    }


    @Override
    public Set<RoleModel> getRoleMappings() {
        // we query ids only as the role might be cached and following the @ManyToOne will result in a load
        // even if we're getting just the id.
        TypedQuery<String> query = em.createNamedQuery("positionRoleMappingIds", String.class);
        query.setParameter("position", getEntity());
        List<String> ids = query.getResultList();
        Set<RoleModel> roles = new HashSet<RoleModel>();
        for (String roleId : ids) {
            RoleModel roleById = realm.getRoleById(roleId);
            if (roleById == null) continue;
            roles.add(roleById);
        }
        return roles;
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        if (position == null || role == null) return;

        TypedQuery<PositionRoleMappingEntity> query = getPositionRoleMappingEntityTypedQuery(role);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        List<PositionRoleMappingEntity> results = query.getResultList();
        if (results.size() == 0) return;
        for (PositionRoleMappingEntity entity : results) {
            em.remove(entity);
        }
        em.flush();
    }

    @Override
    public Set<RoleModel> getClientRoleMappings(ClientModel app) {
        Set<RoleModel> roleMappings = getRoleMappings();

        Set<RoleModel> roles = new HashSet<RoleModel>();
        for (RoleModel role : roleMappings) {
            RoleContainerModel container = role.getContainer();
            if (container instanceof ClientModel) {
                ClientModel appModel = (ClientModel)container;
                if (appModel.getId().equals(app.getId())) {
                   roles.add(role);
                }
            }
        }
        return roles;
    }

    @Override
    public String getPosId() {
        if (this.position != null) {
            return this.position.getPosId();
        }
        return null;
    }

    @Override
    public String getPosName() {
        if (this.position != null) {
            return this.position.getPosName();
        }
        return null;
    }

    @Override
    public void setPosName(String name) {
        if (this.position != null)
            this.position.setPosName(name);
    }

    @Override
    public String getPosCode() {
        if (this.position != null) {
            return this.position.getPosCode();
        }
        return null;
    }

    @Override
    public void setPosCode(String code) {
        if (this.position != null)
            this.position.setPosCode(code);
    }

    @Override
    public String getDescription() {
        if (this.position != null) {
            return this.position.getDescription();
        }
        return null;
    }

    @Override
    public void setDescription(String description) {
        if (this.position != null)
            this.position.setDescription(description);
    }

    @Override
    public Long getStatus() {
        if (this.position != null) {
            return this.position.getStatus();
        }
        return null;
    }

    @Override
    public void setStatus(Long status) {
        if (this.position != null)
            this.position.setStatus(status);
    }

    @Override
    public Long getType() {
        if (this.position != null) {
            return this.position.getType();
        }
        return null;
    }

    @Override
    public void setType(Long type) {
        if (this.position != null)
            this.position.setType(type);
    }

    @Override
    public Long getMaxUserChiNhanh() {
        if (this.position != null) {
            return this.position.getMaxUserChiNhanh();
        }
        return null;
    }

    @Override
    public void setMaxUserChiNhanh(Long maxUserChiNhanh) {
        if (this.position != null)
            this.position.setMaxUserChiNhanh(maxUserChiNhanh);
    }

    @Override
    public Long getMaxUserTrungTam() {
        if (this.position != null) {
            return this.position.getMaxUserTrungTam();
        }
        return null;
    }

    @Override
    public void setMaxUserTrungTam(Long maxUserTrungTam) {
        if (this.position != null)
            this.position.setMaxUserTrungTam(maxUserTrungTam);
    }

    @Override
    public Long getLimitType() {
        if (this.position != null) {
            return this.position.getLimitType();
        }
        return null;
    }

    @Override
    public void setLimitType(Long limitType) {
        if (this.position != null)
            this.position.setLimitType(limitType);
    }

    @Override
    public Date getValidDateStart() {
        if (this.position != null) {
            return this.position.getValidDateStart();
        }
        return null;
    }

    @Override
    public void setValidDateStart(Date validDateStart) {
        if (this.position != null)
            this.position.setValidDateStart(validDateStart);
    }

    @Override
    public Date getValidDateEnd() {
        if (this.position != null) {
            return this.position.getValidDateEnd();
        }
        return null;
    }

    @Override
    public void setValidDateEnd(Date validDateEnd) {
        if (this.position != null)
            this.position.setValidDateEnd(validDateEnd);
    }

    @Override
    public Date getCreateDate() {
        if (this.position != null) {
            return this.position.getCreateDate();
        }
        return null;
    }

    @Override
    public void setCreateDate(Date createDate) {
        if (this.position != null)
            this.position.setCreateDate(createDate);
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        boolean found = false;
        List<PositionAttributeEntity> toRemove = new ArrayList<>();
        for (PositionAttributeEntity attr : position.getAttributes()) {
            if (attr.getName().equals(name)) {
                if (!found) {
                    attr.setValue(value);
                    found = true;
                } else {
                    toRemove.add(attr);
                }
            }
        }

        for (PositionAttributeEntity attr : toRemove) {
            em.remove(attr);
            position.getAttributes().remove(attr);
        }

        if (found) {
            return;
        }

        persistAttributeValue(name, value);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        // Remove all existing
        removeAttribute(name);

        // Put all new
        for (String value : values) {
            persistAttributeValue(name, value);
        }
    }

    private void persistAttributeValue(String name, String value) {
        PositionAttributeEntity attr = new PositionAttributeEntity();
        attr.setId(KeycloakModelUtils.generateId());
        attr.setName(name);
        attr.setValue(value);
        attr.setPosition(position);
        em.persist(attr);
        position.getAttributes().add(attr);
    }

    @Override
    public void removeAttribute(String name) {
        Iterator<PositionAttributeEntity> it = position.getAttributes().iterator();
        while (it.hasNext()) {
            PositionAttributeEntity attr = it.next();
            if (attr.getName().equals(name)) {
                it.remove();
                em.remove(attr);
            }
        }
    }

    @Override
    public String getFirstAttribute(String name) {
        for (PositionAttributeEntity attr : position.getAttributes()) {
            if (attr.getName().equals(name)) {
                return attr.getValue();
            }
        }
        return null;
    }

    @Override
    public List<String> getAttribute(String name) {
        List<String> result = new ArrayList<>();
        for (PositionAttributeEntity attr : position.getAttributes()) {
            if (attr.getName().equals(name)) {
                result.add(attr.getValue());
            }
        }
        return result;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> result = new MultivaluedHashMap<>();
        for (PositionAttributeEntity attr : position.getAttributes()) {
            result.add(attr.getName(), attr.getValue());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PositionModel)) return false;

        PositionModel that = (PositionModel) o;
        return that.getPosId().equals(getPosId());
    }

    @Override
    public int hashCode() {
        return getPosId().hashCode();
    }

}
