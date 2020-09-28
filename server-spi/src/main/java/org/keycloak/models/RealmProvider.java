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

package org.keycloak.models;

import org.keycloak.migration.MigrationModel;
import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.PositionRepresentation;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RealmProvider extends Provider, ClientProvider {

    // Note: The reason there are so many query methods here is for layering a cache on top of an persistent KeycloakSession
    MigrationModel getMigrationModel();
    RealmModel createRealm(String name);
    RealmModel createRealm(String id, String name);
    RealmModel getRealm(String id);
    RealmModel getRealmByName(String name);

    void moveGroup(RealmModel realm, GroupModel group, GroupModel toParent);

    List<GroupModel> getGroups(RealmModel realm);

    Long getGroupsCount(RealmModel realm, Boolean onlyTopGroups);

    Long getClientsCount(RealmModel realm);

    Long getGroupsCountByNameContaining(RealmModel realm, String search);

    List<GroupModel> getGroupsByRole(RealmModel realm, RoleModel role, int firstResult, int maxResults);

    List<GroupModel> getTopLevelGroups(RealmModel realm);
    
    List<GroupModel> getTopLevelGroups(RealmModel realm, Boolean domain);

    List<GroupModel> getTopLevelGroups(RealmModel realm, Integer first, Integer max);
    
    List<GroupModel> getTopLevelGroups(RealmModel realm, Integer first, Integer max, Boolean domain);

    List searchForGroupByName(RealmModel realm, String search, Integer first, Integer max);
    
    List searchForGroupByName(RealmModel realm, String search, Integer first, Integer max, Boolean domain);

    boolean removeGroup(RealmModel realm, GroupModel group);

    GroupModel createGroup(RealmModel realm, String name);
    		
    GroupModel createGroup(RealmModel realm, String name, String code);
    
    GroupModel createGroup(RealmModel realm, String name, String code, Boolean domain);

    GroupModel createGroup(RealmModel realm, String id, String name, String code);

    void addTopLevelGroup(RealmModel realm, GroupModel subGroup);

    RoleModel addRealmRole(RealmModel realm, String name);

    RoleModel addRealmRole(RealmModel realm, String id, String name);

    RoleModel getRealmRole(RealmModel realm, String name);

    Set<RoleModel> getRealmRoles(RealmModel realm);

    Set<RoleModel> getRealmRoles(RealmModel realm, Integer first, Integer max);

    Set<RoleModel> getClientRoles(RealmModel realm, ClientModel client, Integer first, Integer max);

    Set<RoleModel> searchForClientRoles(RealmModel realm, ClientModel client, String search, Integer first,
            Integer max);

    Set<RoleModel> searchForRoles(RealmModel realm, String search, Integer first, Integer max);

    boolean removeRole(RealmModel realm, RoleModel role);

    RoleModel getRoleById(String id, RealmModel realm);

    ClientScopeModel getClientScopeById(String id, RealmModel realm);
 	String getGroupByVhrId(RealmModel realm, Long vhrId);
    
    GroupModel getGroupByCode(RealmModel realm, String code);
    GroupModel getGroupById(String id, RealmModel realm);



    List<RealmModel> getRealms();
    List<RealmModel> getRealmsWithProviderType(Class<?> type);
    boolean removeRealm(String id);
    void close();

    ClientInitialAccessModel createClientInitialAccessModel(RealmModel realm, int expiration, int count);
    ClientInitialAccessModel getClientInitialAccessModel(RealmModel realm, String id);
    void removeClientInitialAccessModel(RealmModel realm, String id);
    List<ClientInitialAccessModel> listClientInitialAccess(RealmModel realm);
    void removeExpiredClientInitialAccess();
    void decreaseRemainingCount(RealmModel realm, ClientInitialAccessModel clientInitialAccess); // Separate provider method to ensure we decrease remainingCount atomically instead of doing classic update

    // SP_POSITION
    PositionModel createPosition(RealmModel realm, PositionRepresentation rep);
    List searchForPositionByName(RealmModel realm, String search, Integer first, Integer max);
    List searchForPositionByNameAndCode(RealmModel realm, String searchName, String searchCode, Integer first, Integer max);
    List<Integer> checkPositionByNameAndCode(RealmModel realm, String searchName, String searchCode);
    PositionModel getPositionById(String id, RealmModel realm);
    boolean removePosition(RealmModel realm, PositionModel position);
    List<PositionModel> getPositions(RealmModel realm);
    PositionModel getPositionByCode(RealmModel realm, String code);
    String getPositionByVhrId(Long vhrId,  RealmModel realm);
    // SP_POSITION
    
    //sync user
    Long getSyncInformationByTaskName(String taskName);
    void addSyncInformation(RealmModel realm, String taskName, Long totalSync, Long totalError,Timestamp startTime, Timestamp endTime);
}
