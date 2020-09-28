package org.keycloak.models.jpa.entities;

import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="userPositionMemberOf", query="select u from PositionEntity p join UserEntity u on p.posId = u.positionId where u.realmId = :realmId and u.id = :userId and p.posId = :positionId"),

        @NamedQuery(name="getPositionIdsByNameContaining", query="select u.posId from PositionEntity u where u.realm.id = :realm and u.posName like concat('%',:search,'%') order by u.posName ASC"),
        @NamedQuery(name="getPositionIdsByNameAndPosCodeContaining", query="select u.posId from PositionEntity u where u.realm.id = :realm and u.posName like concat('%',:searchName,'%') and u.posCode like concat('%',:searchCode,'%') order by u.posName ASC"),
        @NamedQuery(name="checkByName", query="select 1 from PositionEntity u where u.realm.id = :realm and u.posName = :searchName ") ,
        @NamedQuery(name="checkByCode", query="select 1 from PositionEntity u where u.realm.id = :realm and u.posCode = :searchCode "),
        @NamedQuery(name="getPositionCount", query="select count(u) from PositionEntity u where u.realm.id = :realm"),
        @NamedQuery(name="getPositionByUser", query="select p.posId from PositionEntity p join UserEntity u on p.posId = u.positionId where u.realmId = :realmId and u.id = :userId and p.posName like concat('%',:posName,'%')"),
        @NamedQuery(name="getPositionByVhrId", query="select p.posId from PositionEntity p where p.vhrId = :vhrId"),
        @NamedQuery(name="getPositionByPosCode", query="select p.posId from PositionEntity p where p.posCode = :posCode")
})
@Entity
@Table(name="KEYCLOAK_POSITION",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"REALM_ID", "POS_NAME"})}
)
public class PositionEntity {

    @Id
    @Column(name="POS_ID", length = 36)
    @Access(AccessType.PROPERTY)
    protected String posId;

    @Nationalized
    @Column(name = "POS_CODE")
    protected String posCode;

    @Nationalized
    @Column(name = "POS_NAME")
    protected String posName;

    @Nationalized
    @Column(name = "DESCRIPTION")
    protected String description;

    @Column(name = "STATUS")
    private Long status;

    @Column(name = "TYPE")
    private Long type;

    @Column(name = "MAX_USER_CHINHANH")
    private Long maxUserChiNhanh;

    @Column(name = "MAX_USER_TRUNGTAM")
    private Long maxUserTrungTam;

    @Column(name = "LIMIT_TYPE")
    private Long limitType;

    @Column(name = "VALID_DATE_START")
    private Date validDateStart;

    @Column(name = "VALID_DATE_END")
    private Date validDateEnd;

    @Column(name = "CREATE_DATE")
    private Date createDate;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "REALM_ID")
    private RealmEntity realm;

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy="position")
    protected Collection<PositionAttributeEntity> attributes = new ArrayList<PositionAttributeEntity>();
    
    
    @Column(name = "VHR_ID")
    private Long vhrId;

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public String getPosCode() {
        return posCode;
    }

    public void setPosCode(String posCode) {
        this.posCode = posCode;
    }

    public Long getVhrId() {
		return vhrId;
	}

	public void setVhrId(Long vhrId) {
		this.vhrId = vhrId;
	}

    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Long getMaxUserChiNhanh() {
        return maxUserChiNhanh;
    }

    public void setMaxUserChiNhanh(Long maxUserChiNhanh) {
        this.maxUserChiNhanh = maxUserChiNhanh;
    }

    public Long getMaxUserTrungTam() {
        return maxUserTrungTam;
    }

    public void setMaxUserTrungTam(Long maxUserTrungTam) {
        this.maxUserTrungTam = maxUserTrungTam;
    }

    public Long getLimitType() {
        return limitType;
    }

    public void setLimitType(Long limitType) {
        this.limitType = limitType;
    }

    public Date getValidDateStart() {
        return validDateStart;
    }

    public void setValidDateStart(Date validDateStart) {
        this.validDateStart = validDateStart;
    }

    public Date getValidDateEnd() {
        return validDateEnd;
    }

    public void setValidDateEnd(Date validDateEnd) {
        this.validDateEnd = validDateEnd;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public RealmEntity getRealm() {
        return realm;
    }

    public void setRealm(RealmEntity realm) {
        this.realm = realm;
    }

    public Collection<PositionAttributeEntity> getAttributes() {
        return attributes;
    }

    public void setAttributes(Collection<PositionAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof PositionEntity)) return false;

        PositionEntity that = (PositionEntity) o;

        if (!posId.equals(that.posId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return posId.hashCode();
    }
}
