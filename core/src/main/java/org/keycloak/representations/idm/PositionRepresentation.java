package org.keycloak.representations.idm;

import java.util.*;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public class PositionRepresentation {
    protected String posId;
    protected String posCode;
    protected String posName;
    protected String description;
    private Long status;
    private Long type;
    private Long maxUserChiNhanh;
    private Long maxUserTrungTam;
    private Long limitType;
    private Date validDateStart;
    private Date validDateEnd;
    private Date createDate;

    protected Map<String, List<String>>  attributes;
    protected List<String> realmRoles;
    protected Map<String, List<String>> clientRoles;
    private Map<String, Boolean> access;

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

    public List<String> getRealmRoles() {
        return realmRoles;
    }

    public void setRealmRoles(List<String> realmRoles) {
        this.realmRoles = realmRoles;
    }

    public Map<String, List<String>> getClientRoles() {
        return clientRoles;
    }

    public void setClientRoles(Map<String, List<String>> clientRoles) {
        this.clientRoles = clientRoles;
    }


    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>>  attributes) {
        this.attributes = attributes;
    }

    public PositionRepresentation singleAttribute(String name, String value) {
        if (this.attributes == null) attributes = new HashMap<>();
        attributes.put(name, Arrays.asList(value));
        return this;
    }

    public Map<String, Boolean> getAccess() {
        return access;
    }

    public void setAccess(Map<String, Boolean> access) {
        this.access = access;
    }

}
