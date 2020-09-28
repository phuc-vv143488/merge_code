package org.keycloak.services.scheduled.models;

import java.util.Date;

public class OrganizationBean {
	private Long organizationId;
    private String code;
    private String name;
    private String effectiveStartDate;
    private String effectiveEndDate;
    private Long orgParentId;
    private Long orgLevelManage;
    private String path;
    private Long orgTypeId;
    private Long orgLevel;
    private Long orderNumber;
    private String orgCodePath;
    private String abbreviation;
    private Long hasEmployee;
    private Long isActive;
    private String address;
    private String phoneNumber;
    private String description;
    private String createdTime;
    public String getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getEffectiveStartDate() {
        if (effectiveStartDate != null && effectiveStartDate != "") {
            return new Date(Long.valueOf(effectiveStartDate));
        }
        return null;
    }

    public void setEffectiveStartDate(String effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public Date getEffectiveEndDate() {
        if(effectiveEndDate != null && effectiveEndDate !="") {
            return new Date(Long.valueOf(effectiveEndDate));
        }
        return null;
    }

    public void setEffectiveEndDate(String effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public Long getParentId() {
        return orgParentId;
    }

    public void setParentId(Long parentId) {
        this.orgParentId = parentId;
    }

    public Long getOrgLevelManage() {
        return orgLevelManage;
    }

    public void setOrgLevelManage(Long orgLevelManage) {
        this.orgLevelManage = orgLevelManage;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getOrgTypeId() {
        return orgTypeId;
    }

    public void setOrgTypeId(Long orgTypeId) {
        this.orgTypeId = orgTypeId;
    }

    public Long getOrgLevel() {
        return orgLevel;
    }

    public void setOrgLevel(Long orgLevel) {
        this.orgLevel = orgLevel;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrgCodePath() {
        return orgCodePath;
    }

    public void setOrgCodePath(String orgCodePath) {
        this.orgCodePath = orgCodePath;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Long getHasEmployee() {
        return hasEmployee;
    }

    public void setHasEmployee(Long hasEmployee) {
        this.hasEmployee = hasEmployee;
    }

    public Long getIsActive() {
        return isActive;
    }

    public void setIsActive(Long isActive) {
        this.isActive = isActive;
    }
}
