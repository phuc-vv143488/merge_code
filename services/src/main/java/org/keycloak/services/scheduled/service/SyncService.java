package org.keycloak.services.scheduled.service;

import java.util.List;

import org.keycloak.services.scheduled.models.Synchronized;

public interface SyncService {
	List<Object> getDataToSync(Synchronized sync, Long lastSyncTime);
}
