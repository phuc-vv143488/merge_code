package org.keycloak.representations.idm.authorization;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author SP_POSOTION
 */
public class PositionPolicyRepresentation extends AbstractPolicyRepresentation {

    private String positionsClaim;
    private Set<PositionDefinition> positions;

    @Override
    public String getType() {
        return "position";
    }

    public String getPositionsClaim() {
        return positionsClaim;
    }

    public void setPositionsClaim(String positionsClaim) {
        this.positionsClaim = positionsClaim;
    }

    public Set<PositionDefinition> getPositions() {
        return positions;
    }

    public void setPositions(Set<PositionDefinition> positions) {
        this.positions = positions;
    }

    public static class PositionDefinition {
        private String posId;
        private String posCode;
        private String posName;
        private String description;
        private Long status;
        private Long type;
        private Long maxUserChiNhanh;
        private Long maxUserTrungTam;
        private Long limitType;
        private Date validDateStart;
        private Date validDateEnd;
        private Date createDate;

        public PositionDefinition() {
        }
        public PositionDefinition(String id, String posName) {
            this.posId = id;
            this.posName = posName;
        }

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
    }
}
