module.controller('PositionListCtrl', function($scope, $route, $q, realm, Position, PositionSearchState, Notifications, $location, Dialog) {
    $scope.init = function() {
        $scope.realm = realm;

        PositionSearchState.query.realm = realm.realm;
        $scope.query = PositionSearchState.query;
        $scope.searchQuery();
    };
    $scope.searchLoaded = false;
    $scope.lastSearch = false;
    $scope.isDisabled = function() {
        return false;
    };
    $scope.firstPage = function () {
        $scope.query.first = 0;
        $scope.searchQuery();
    };
    $scope.previousPage = function () {
        $scope.query.first -= parseInt($scope.query.max);
        if ($scope.query.first < 0) {
            $scope.query.first = 0;
        }
        $scope.searchQuery();
    };
    $scope.nextPage = function () {
        $scope.query.first += parseInt($scope.query.max);
        $scope.searchQuery();
    };

    $scope.searchQuery = function () {
        $scope.searchLoaded = false;

        $scope.positions = Position.query($scope.query, function() {
            $scope.searchLoaded = true;
            $scope.lastSearch = $scope.query.search;
            PositionSearchState.isFirstSearch = false;
        });
    };

    $scope.removePosition = function (position) {
        Dialog.confirmDelete(position.posName, 'position', function() {
            position.$remove({
                realm : realm.realm,
                posId : position.posId
            }, function() {
                $route.reload();

                if ($scope.positions.length === 1 && $scope.query.first > 0) {
                    $scope.previousPage();
                }

                Notifications.success("The position has been deleted.");
            }, function() {
                Notifications.error("Position couldn't be deleted");
            });
        });
    };
    $scope.clearSearch = function () {
        console.log('clearSearch')
    };
    $scope.createPosition = function () {
        $location.url("/create/position/" + realm.realm);
    };

});

module.controller('PositionTabCtrl', function(Dialog, $scope, Current, Position, Notifications, $location) {
    $scope.removePosition = function() {
        Dialog.confirmDelete($scope.position.name, 'position', function() {
            Position.remove({
                realm : Current.realm.realm,
                posId : $scope.position.posId
            }, function() {
                $location.url("/realms/" + Current.realm.realm + "/positions");
                Notifications.success("The position has been deleted.");
            });
        });
    };
});

module.controller('PositionDetailCtrl', function($scope, realm, position, Position, Components, $location, $http, Dialog, Notifications) {
    $scope.realm = realm;
    $scope.create = !position.posId;
    // $scope.editPositionname = $scope.create; // || $scope.realm.editPositionnameAllowed;

    if ($scope.create) {
        $scope.position = { status: true, attributes: {} };
        $scope.isFirstLoadForUpdate = false;
    } else {
        if (!position.attributes) {
            position.attributes = {}
        }
        $scope.position = position;
        $scope.position = convertPositionForShow();
        $scope.isFirstLoadForUpdate = true;
    }

    $scope.changed = false; // $scope.create;
    $scope.$watch('position', function() {
        if (!angular.equals($scope.position, position)) {
            if (!$scope.isFirstLoadForUpdate)
                $scope.changed = true;
            else
                $scope.isFirstLoadForUpdate = false;
        }
    }, true);

    $scope.isValidate = true;
    $scope.checkErr = function (startDate, endDate) {
        $scope.errMessage = '';
        if (startDate && endDate) {
            if (startDate > endDate) {
                $scope.errMessage = 'End Date should be greater than Start Date';
                $scope.isValidate = false;
            } else $scope.isValidate = true;
        } else $scope.isValidate = true;
    };

    function convertPositionForSave() {
        var obj = {};
        obj = angular.copy($scope.position);
        if (obj.status) {
            obj.status = 1;
        } else {
            obj.status = 0;
        }
        if (obj.type) {
            obj.type = 1;
        } else {
            obj.type = 0;
        }
        convertAttributeValuesToLists(obj);
        return obj
    }
    function convertPositionForShow() {
        var obj = {};
        obj = angular.copy($scope.position);
        if (obj.status === 1) {
            obj.status = true;
        } else {
            obj.status = false;
        }
        if (obj.type === 1) {
            obj.type = true;
        } else {
            obj.type = false;
        }
        // DATE
        if (obj.validDateStart) {
            obj.validDateStart = new Date(obj.validDateStart);
        }
        if (obj.validDateEnd) {
            obj.validDateEnd = new Date(obj.validDateEnd);
        }
        // CONVERT ATTRIBUTE
        convertAttributeValuesToString(obj);
        return obj
    }

    $scope.save = function() {
        //convertAttributeValuesToLists();
        if ($scope.create) {
            Position.save({
                realm: realm.realm
            }, convertPositionForSave(), function (data, headers) {
                $scope.changed = false;
                //convertAttributeValuesToString($scope.position);
                position = convertPositionForShow();
                var l = headers().location;

                console.debug("Location == " + l);

                var id = l.substring(l.lastIndexOf("/") + 1);


                $location.url("/realms/" + realm.realm + "/positions/" + id);
                Notifications.success("The position has been created.");
            });
        } else {
            Position.update({
                realm: realm.realm,
                posId: $scope.position.posId
            }, convertPositionForSave(), function () {
                $scope.changed = false;
                //convertAttributeValuesToString($scope.position);
                position = convertPositionForShow();
                Notifications.success("Your changes have been saved to the position.");
            });
        }
    };

    function convertAttributeValuesToLists(position) {
        var attrs = position.attributes;
        for (var attribute in attrs) {
            if (typeof attrs[attribute] === "string") {
                var attrVals = attrs[attribute].split("##");
                attrs[attribute] = attrVals;
            }
        }
    }

    function convertAttributeValuesToString(position) {
        var attrs = position.attributes;
        for (var attribute in attrs) {
            if (typeof attrs[attribute] === "object") {
                var attrVals = attrs[attribute].join("##");
                attrs[attribute] = attrVals;
            }
        }
    }

    $scope.reset = function() {
        $scope.position = angular.copy(position);
        $scope.changed = false;
    };

    $scope.cancel = function() {
        $location.url("/realms/" + realm.realm + "/positions");
    };

    $scope.addAttribute = function() {
        $scope.position.attributes[$scope.newAttribute.key] = $scope.newAttribute.value;
        delete $scope.newAttribute;
    }

    $scope.removeAttribute = function(key) {
        delete $scope.position.attributes[key];
    }

    $scope.onChangeName = function () {
        if ($scope.position.posName) {
            $scope.position.posName.trim();
        }
    }
    $scope.onChangeCode = function () {
        if ($scope.position.posCode) {
            $scope.position.posCode.trim();
        }
    }
});

module.controller('PositionRoleMappingCtrl', function($scope, $http, $route, realm, position, clients, client, Client, Notifications, PositionRealmRoleMapping,
                                                   PositionClientRoleMapping, PositionAvailableRealmRoleMapping, PositionAvailableClientRoleMapping,
                                                   PositionCompositeRealmRoleMapping, PositionCompositeClientRoleMapping) {
    $scope.realm = realm;
    $scope.position = position;
    $scope.selectedRealmRoles = [];
    $scope.selectedRealmMappings = [];
    $scope.realmMappings = [];
    $scope.clients = clients;
    $scope.client = client;
    $scope.clientRoles = [];
    $scope.clientComposite = [];
    $scope.selectedClientRoles = [];
    $scope.selectedClientMappings = [];
    $scope.clientMappings = [];
    $scope.dummymodel = [];

    $scope.realmMappings = PositionRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
    $scope.realmRoles = PositionAvailableRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
    $scope.realmComposite = PositionCompositeRealmRoleMapping.query({realm : realm.realm, posId : position.posId});

    $scope.addRealmRole = function() {
        $scope.selectedRealmRolesToAdd = JSON.parse('[' + $scope.selectedRealmRoles + ']');
        $scope.selectedRealmRoles = [];
        $http.post(authUrl + '/admin/realms/' + realm.realm + '/positions/' + position.posId + '/role-mappings/realm',
            $scope.selectedRealmRolesToAdd).then(function() {
            $scope.realmMappings = PositionRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.realmRoles = PositionAvailableRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.realmComposite = PositionCompositeRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.selectedRealmMappings = [];
            $scope.selectedRealmRoles = [];
            if ($scope.selectedClient) {
                console.log('load available');
                $scope.clientComposite = PositionCompositeClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
                $scope.clientRoles = PositionAvailableClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
                $scope.clientMappings = PositionClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
                $scope.selectedClientRoles = [];
                $scope.selectedClientMappings = [];
            }
            $scope.selectedRealmRolesToAdd = [];
            Notifications.success("Role mappings updated.");

        });
    };

    $scope.deleteRealmRole = function() {
        $scope.selectedRealmMappingsToRemove = JSON.parse('[' + $scope.selectedRealmMappings + ']');
        $http.delete(authUrl + '/admin/realms/' + realm.realm + '/positions/' + position.posId + '/role-mappings/realm',
            {data : $scope.selectedRealmMappingsToRemove, headers : {"content-type" : "application/json"}}).then(function() {
            $scope.realmMappings = PositionRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.realmRoles = PositionAvailableRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.realmComposite = PositionCompositeRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.selectedRealmMappings = [];
            $scope.selectRealmRoles = [];
            if ($scope.selectedClient) {
                console.log('load available');
                $scope.clientComposite = PositionCompositeClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
                $scope.clientRoles = PositionAvailableClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
                $scope.clientMappings = PositionClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
                $scope.selectedClientRoles = [];
                $scope.selectedClientMappings = [];
            }
            $scope.selectedRealmMappingsToRemove = [];
            Notifications.success("Role mappings updated.");
        });
    };

    $scope.addClientRole = function() {
        $scope.selectedClientRolesToAdd = JSON.parse('[' + $scope.selectedClientRoles + ']');
        $http.post(authUrl + '/admin/realms/' + realm.realm + '/positions/' + position.posId + '/role-mappings/clients/' + $scope.selectedClient.id,
            $scope.selectedClientRolesToAdd).then(function() {
            $scope.clientComposite = PositionCompositeClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
            $scope.clientRoles = PositionAvailableClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
            $scope.clientMappings = PositionClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
            $scope.selectedClientRoles = [];
            $scope.selectedClientMappings = [];
            $scope.realmComposite = PositionCompositeRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.realmRoles = PositionAvailableRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.selectedClientRolesToAdd = [];
            Notifications.success("Role mappings updated.");
        });
    };

    $scope.deleteClientRole = function() {
        $scope.selectedClientMappingsToRemove = JSON.parse('[' + $scope.selectedClientMappings + ']');
        $http.delete(authUrl + '/admin/realms/' + realm.realm + '/positions/' + position.posId + '/role-mappings/clients/' + $scope.selectedClient.id,
            {data : $scope.selectedClientMappingsToRemove, headers : {"content-type" : "application/json"}}).then(function() {
            $scope.clientComposite = PositionCompositeClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
            $scope.clientRoles = PositionAvailableClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
            $scope.clientMappings = PositionClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
            $scope.selectedClientRoles = [];
            $scope.selectedClientMappings = [];
            $scope.realmComposite = PositionCompositeRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.realmRoles = PositionAvailableRealmRoleMapping.query({realm : realm.realm, posId : position.posId});
            $scope.selectedClientMappingsToRemove = [];
            Notifications.success("Role mappings updated.");
        });
    };

    $scope.changeClient = function(client) {
        $scope.selectedClient = client;
        if (!client || !client.id) {
            $scope.selectedClient = null;
            $scope.clientRoles = null;
            $scope.clientMappings = null;
            $scope.clientComposite = null;
            return;
        }
        if ($scope.selectedClient) {
            $scope.clientComposite = PositionCompositeClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
            $scope.clientRoles = PositionAvailableClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
            $scope.clientMappings = PositionClientRoleMapping.query({realm : realm.realm, posId : position.posId, client : $scope.selectedClient.id});
        }
        $scope.selectedClientRoles = [];
        $scope.selectedClientMappings = [];
    };

    clientSelectControl($scope, $route.current.params.realm, Client);

});

module.controller('PositionMembersCtrl', function($scope, realm, position, PositionMembership, PositionDeleteUser, Dialog, Notifications) {
    $scope.realm = realm;
    $scope.page = 0;
    $scope.position = position;

    $scope.query = {
        realm: realm.realm,
        posId: position.posId,
        max : 5,
        first : 0
    };


    $scope.firstPage = function() {
        $scope.query.first = 0;
        $scope.searchQuery();
    };

    $scope.previousPage = function() {
        $scope.query.first -= parseInt($scope.query.max);
        if ($scope.query.first < 0) {
            $scope.query.first = 0;
        }
        $scope.searchQuery();
    };

    $scope.nextPage = function() {
        $scope.query.first += parseInt($scope.query.max);
        $scope.searchQuery();
    };

    $scope.searchQuery = function() {
        $scope.searchLoaded = false;

        $scope.users = PositionMembership.query($scope.query, function() {
            $scope.searchLoaded = true;
            $scope.lastSearch = $scope.query.search;
        });
    };

    $scope.searchQuery();

    // delete position of user
    $scope.removeUser = function(userId, userName) {
        if (userId) {
            Dialog.confirmDelete(userName, 'member', function() {
                PositionDeleteUser.query({
                    realm : realm.realm,
                    posId : position.posId,
                    userId : userId
                }, function() {
                    $scope.searchQuery();

                    Notifications.success("The position of user has been deleted.");
                }, function() {
                    Notifications.error("Position of user couldn't be deleted");
                });
            });
        }
    }
});
