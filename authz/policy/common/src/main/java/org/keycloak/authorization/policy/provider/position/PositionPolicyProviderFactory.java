package org.keycloak.authorization.policy.provider.position;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.PositionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.authorization.PositionPolicyRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.*;

/**
 * @author SP_POSITION
 */
public class PositionPolicyProviderFactory implements PolicyProviderFactory<PositionPolicyRepresentation> {

    private PositionPolicyProvider provider = new PositionPolicyProvider(this::toRepresentation);

    @Override
    public String getId() {
        return "position";
    }

    @Override
    public String getName() {
        return "Position";
    }

    // >>>
    @Override
    public String getGroup() {
        return "Identity Based";
    }

    @Override
    public String getPosition() {
        return "Position Based";
    }

    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return provider;
    }

    @Override
    public PolicyProvider create(KeycloakSession session) {
        return provider;
    }

    @Override
    public PositionPolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        PositionPolicyRepresentation representation = new PositionPolicyRepresentation();

        representation.setPositionsClaim(policy.getConfig().get("positionsClaim"));

        try {
            representation.setPositions(getPositionsDefinition(policy.getConfig()));
        } catch (IOException cause) {
            throw new RuntimeException("Failed to deserialize positions", cause);
        }
        return representation;
    }

    @Override
    public Class<PositionPolicyRepresentation> getRepresentationType() {
        return PositionPolicyRepresentation.class;
    }

    @Override
    public void onCreate(Policy policy, PositionPolicyRepresentation representation, AuthorizationProvider authorization) {
        updatePolicy(policy, representation.getPositionsClaim(), representation.getPositions(), authorization);
    }

    @Override
    public void onUpdate(Policy policy, PositionPolicyRepresentation representation, AuthorizationProvider authorization) {
        updatePolicy(policy, representation.getPositionsClaim(), representation.getPositions(), authorization);
    }

    @Override
    public void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        try {
            updatePolicy(policy, representation.getConfig().get("positionsClaim"), getPositionsDefinition(representation.getConfig()), authorization);
        } catch (IOException cause) {
            throw new RuntimeException("Failed to deserialize positions", cause);
        }
    }

    @Override
    public void onExport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        Map<String, String> config = new HashMap<>();
        PositionPolicyRepresentation positionPolicy = toRepresentation(policy, authorization);
        Set<PositionPolicyRepresentation.PositionDefinition> positions = positionPolicy.getPositions();

        for (PositionPolicyRepresentation.PositionDefinition definition: positions) {
            PositionModel position = authorization.getRealm().getPositionById(definition.getPosId());

            definition.setPosId(null);
            definition.setPosName(position.getPosName());
            definition.setPosCode(position.getPosCode());
            definition.setDescription(position.getDescription());
            definition.setStatus(position.getStatus());
            definition.setType(position.getType());
            definition.setMaxUserChiNhanh(position.getMaxUserChiNhanh());
            definition.setMaxUserTrungTam(position.getMaxUserTrungTam());
            definition.setLimitType(position.getLimitType());
            definition.setValidDateStart(position.getValidDateStart());
            definition.setValidDateEnd(position.getValidDateEnd());
            definition.setCreateDate(position.getCreateDate());
        }

        try {
            String positionsClaim = positionPolicy.getPositionsClaim();

            if (positionsClaim != null) {
                config.put("positionsClaim", positionsClaim);
            }

            config.put("positions", JsonSerialization.writeValueAsString(positions));
        } catch (IOException cause) {
            throw new RuntimeException("Failed to export position policy [" + policy.getName() + "]", cause);
        }

        representation.setConfig(config);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(event -> {
        });
    }

    @Override
    public void close() {

    }

    private void updatePolicy(Policy policy, String positionsClaim, Set<PositionPolicyRepresentation.PositionDefinition> positions, AuthorizationProvider authorization) {
        if (positions == null || positions.isEmpty()) {
            throw new RuntimeException("You must provide at least one position");
        }

        Map<String, String> config = new HashMap<>(policy.getConfig());

        if (positionsClaim != null) {
            config.put("positionsClaim", positionsClaim);
        }

        List<PositionModel> positionsModel = authorization.getRealm().getPositions();

        for (PositionPolicyRepresentation.PositionDefinition definition : positions) {
            PositionModel position = null;

            if (definition.getPosId() != null) {
                position = authorization.getRealm().getPositionById(definition.getPosId());
            }

            String name = definition.getPosName();
            
            if (position == null && name != null) {
                    PositionModel pos = positionsModel.stream().filter(positionModel -> positionModel.getPosName().equals(name)).findFirst().orElseThrow(() -> new RuntimeException("Position with name [" + name + "] not found"));

                    if (pos != null) {
                        position = pos;
                    }
            }

            if (position == null) {
                throw new RuntimeException("Position with id [" + definition.getPosId() + "] not found");
            }

            definition.setPosId(position.getPosId());
            definition.setPosName(null);
            // AHIHI
        }

        try {
            config.put("positions", JsonSerialization.writeValueAsString(positions));
        } catch (IOException cause) {
            throw new RuntimeException("Failed to serialize positions", cause);
        }

        policy.setConfig(config);
    }

    private HashSet<PositionPolicyRepresentation.PositionDefinition> getPositionsDefinition(Map<String, String> config) throws IOException {
        return new HashSet<>(Arrays.asList(JsonSerialization.readValue(config.get("positions"), PositionPolicyRepresentation.PositionDefinition[].class)));
    }
}
