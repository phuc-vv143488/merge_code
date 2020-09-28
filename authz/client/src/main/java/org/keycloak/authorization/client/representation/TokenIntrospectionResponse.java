/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.client.representation;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.idm.authorization.Permission;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class TokenIntrospectionResponse extends JsonWebToken {

    @JsonProperty
    private Boolean active;

    private List<Permission> permissions;
    
    private String userName;
    
    private String preferredUsername;
    
    private String email;
    
    private Boolean enable;

    private String phoneNumber;
    
    public Map<String, String> getDomainData() {
		return domainData;
	}

	private Map<String,String> domainData;
    
    public String getPhoneNumber() {
		return phoneNumber;
	}

	public Boolean getActive() {
        return this.active;
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }
    
    public String getUserName() {
    	return this.userName;
    }

	public String getPreferredUsername() {
		return preferredUsername;
	}

	public String getEmail() {
		return email;
	}

	public Boolean getEnable() {
		return enable;
	}
}
