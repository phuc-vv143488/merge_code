package org.keycloak.models;

import org.keycloak.provider.ProviderEvent;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
public interface PositionModel extends RoleMapperModel {
    interface PositionRemovedEvent extends ProviderEvent {
        RealmModel getRealm();
        PositionModel getPosition();
        KeycloakSession getKeycloakSession();
    }
    String getPosId();

    String getPosName();
    void setPosName(String name);

    String getPosCode();
    void setPosCode(String code);

    String getDescription();
    void setDescription(String description);

    Long getStatus();
    void setStatus(Long status);

    Long getType();
    void setType(Long type);

    Long getMaxUserChiNhanh();
    void setMaxUserChiNhanh(Long maxUserChiNhanh);

    Long getMaxUserTrungTam();
    void setMaxUserTrungTam(Long maxUserTrungTam);

    Long getLimitType();
    void setLimitType(Long limitType);

    Date getValidDateStart();
    void setValidDateStart(Date validDateStart);

    Date getValidDateEnd();
    void setValidDateEnd(Date validDateEnd);

    Date getCreateDate();
    void setCreateDate(Date createDate);

    /**
     * Set single value of specified attribute. Remove all other existing values
     *
     * @param name
     * @param value
     */
    void setSingleAttribute(String name, String value);

    void setAttribute(String name, List<String> values);

    void removeAttribute(String name);

    /**
     * @param name
     * @return null if there is not any value of specified attribute or first value otherwise. Don't throw exception if there are more values of the attribute
     */
    String getFirstAttribute(String name);

    /**
     * @param name
     * @return list of all attribute values or empty list if there are not any values. Never return null
     */
    List<String> getAttribute(String name);

    Map<String, List<String>> getAttributes();
    
    Long getVhrId();
    
    void setVhrId(Long vhrId);
}
