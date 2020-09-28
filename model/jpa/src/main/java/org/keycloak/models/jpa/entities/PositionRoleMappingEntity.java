package org.keycloak.models.jpa.entities;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
@NamedQueries({
    @NamedQuery(name="positionsInRole", query="select g from PositionRoleMappingEntity m, PositionEntity g where m.roleId=:roleId and g.posId=m.position"),
        @NamedQuery(name="positionHasRole", query="select m from PositionRoleMappingEntity m where m.position = :position and m.roleId = :roleId"),
        @NamedQuery(name="positionRoleMappings", query="select m from PositionRoleMappingEntity m where m.position = :position"),
        @NamedQuery(name="positionRoleMappingIds", query="select m.roleId from PositionRoleMappingEntity m where m.position = :position"),
        @NamedQuery(name="deletePositionRoleMappingsByRealm", query="delete from  PositionRoleMappingEntity mapping where mapping.position IN (select u from PositionEntity u where u.realm=:realm)"),
        @NamedQuery(name="deletePositionRoleMappingsByRole", query="delete from PositionRoleMappingEntity m where m.roleId = :roleId"),
        @NamedQuery(name="deletePositionRoleMappingsByPosition", query="delete from PositionRoleMappingEntity m where m.position = :position")

})
@Table(name="POSITION_ROLE_MAPPING")
@Entity
@IdClass(PositionRoleMappingEntity.Key.class)
public class PositionRoleMappingEntity {

    @Id
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="POSITION_ID")
    protected PositionEntity position;

    @Id
    @Column(name = "ROLE_ID")
    protected String roleId;

    public PositionEntity getPosition() {
        return position;
    }

    public void setPosition(PositionEntity position) {
        this.position = position;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public static class Key implements Serializable {

        protected PositionEntity position;

        protected String roleId;

        public Key() {
        }

        public Key(PositionEntity position, String roleId) {
            this.position = position;
            this.roleId = roleId;
        }

        public PositionEntity getPosition() {
            return position;
        }

        public String getRoleId() {
            return roleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!roleId.equals(key.roleId)) return false;
            if (!position.equals(key.position)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = position.hashCode();
            result = 31 * result + roleId.hashCode();
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof PositionRoleMappingEntity)) return false;

        PositionRoleMappingEntity key = (PositionRoleMappingEntity) o;

        if (!roleId.equals(key.roleId)) return false;
        if (!position.equals(key.position)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = position.hashCode();
        result = 31 * result + roleId.hashCode();
        return result;
    }

}
