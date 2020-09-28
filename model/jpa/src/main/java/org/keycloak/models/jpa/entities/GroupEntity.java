/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.jpa.entities;

import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="getGroupIdsByParent", query="select u.id from GroupEntity u where u.parent = :parent"),
        @NamedQuery(name="getGroupIdsByNameContaining", query="select u.id from GroupEntity u where u.realm.id = :realm and u.name like concat('%',:search,'%') and (u.domain = false or u.domain is null) order by u.name ASC"),
        @NamedQuery(name="getDomainIdsByNameContaining", query="select u.id from GroupEntity u where u.realm.id = :realm and u.name like concat('%',:search,'%') and u.domain = true order by u.name ASC"),
        @NamedQuery(name="getTopLevelGroupIds", query="select u.id from GroupEntity u where u.parent is null and u.realm.id = :realm and (u.domain = false or u.domain is null) order by u.name ASC"),
        @NamedQuery(name="getTopLevelDomainIds", query="select u.id from GroupEntity u where u.parent is null and u.domain = true and u.realm.id = :realm order by u.name ASC"),
        @NamedQuery(name="getGroupCount", query="select count(u) from GroupEntity u where u.realm.id = :realm and (u.domain = false or u.domain is null)"),
        @NamedQuery(name="getGroupByVhrId", query="select u.id from GroupEntity u where u.vhrId = :vhrId"),
        @NamedQuery(name="getGroupByDeptCode", query="select u.id from GroupEntity u where u.deptCode = :deptCode"),
	    @NamedQuery(name="getTopLevelGroupCount", query="select count(u) from GroupEntity u where u.realm.id = :realm and u.parent is null and (u.domain = false or u.domain is null)")
})
@Entity
@Table(name="KEYCLOAK_GROUP",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"REALM_ID", "PARENT_GROUP", "NAME"})}
)
public class GroupEntity {
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    @Nationalized
    @Column(name = "NAME")
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_GROUP")
    private GroupEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REALM_ID")
    private RealmEntity realm;
    
    @Column(name = "TELEPHONE")
    protected String telephone;
    
    @Column(name = "IS_ACTIVE")
    private Long isActive;
    
    @Column(name = "ADDRESS")
    private String address;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "DEPT_CODE")
    private String deptCode;

    @Column(name = "CREATE_DATE")
    private Date createDate;

    @Column(name = "TIN")
    private String tin;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "CONTACT_NAME")
    private String contactName;

    @Column(name = "CONTACT_TITLE")
    private String contactTitle;

    @Column(name = "FAX")
    private String fax;

    @Column(name = "TEL")
    private String tel;

    @Column(name = "DEPT_TYPE_ID")
    private Long deptTypeId;

    @Column(name = "LOCATION_ID")
    private Long locationId;

    @Column(name = "DEPT_LEVEL")
    private String deptLevel;

    @Column(name = "IP")
    private String ip;

    @Column(name = "FULL_DEPT_NAME")
    private String fullDeptName;
    
    @Column(name = "VHR_ID")
    private Long vhrId;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            orphanRemoval = true, mappedBy="group")
    protected Collection<GroupAttributeEntity> attributes = new ArrayList<GroupAttributeEntity>();

    public String getId() {
        return id;
    }
    
    @Column(name = "IS_DOMAIN")
    private Boolean domain;
    
    public Boolean getDomain() {
		return domain;
	}

	public void setDomain(Boolean domain) {
		this.domain = domain;
	}

	@Column(name = "CODE")
    private String code;

    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

    public void setId(String id) {
        this.id = id;
    }

    public Collection<GroupAttributeEntity> getAttributes() {
        return attributes;
    }

    public void setAttributes(Collection<GroupAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmEntity getRealm() {
        return realm;
    }

    public void setRealm(RealmEntity realm) {
        this.realm = realm;
    }

    public GroupEntity getParent() {
        return parent;
    }

    public void setParent(GroupEntity parent) {
        this.parent = parent;
    }

    public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public Long getIsActive() {
		return isActive;
	}

	public void setIsActive(Long isActive) {
		this.isActive = isActive;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getTin() {
		return tin;
	}

	public void setTin(String tin) {
		this.tin = tin;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactTitle() {
		return contactTitle;
	}

	public void setContactTitle(String contactTitle) {
		this.contactTitle = contactTitle;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public Long getDeptTypeId() {
		return deptTypeId;
	}

	public void setDeptTypeId(Long deptTypeId) {
		this.deptTypeId = deptTypeId;
	}

	public Long getLocationId() {
		return locationId;
	}

	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	public String getDeptLevel() {
		return deptLevel;
	}

	public void setDeptLevel(String deptLevel) {
		this.deptLevel = deptLevel;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getFullDeptName() {
		return fullDeptName;
	}

	public void setFullDeptName(String fullDeptName) {
		this.fullDeptName = fullDeptName;
	}

	public Long getVhrId() {
		return vhrId;
	}

	public void setVhrId(Long vhrId) {
		this.vhrId = vhrId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof GroupEntity)) return false;

        GroupEntity that = (GroupEntity) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
