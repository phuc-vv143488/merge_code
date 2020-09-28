package org.keycloak.authorization.policy.provider.position;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.attribute.Attributes.Entry;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.evaluation.Evaluation;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.models.PositionModel;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.authorization.PositionPolicyRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author SP_POSITION
 */
public class PositionPolicyProvider implements PolicyProvider {

    private final BiFunction<Policy, AuthorizationProvider, PositionPolicyRepresentation> representationFunction;

    public PositionPolicyProvider(BiFunction<Policy, AuthorizationProvider, PositionPolicyRepresentation> representationFunction) {
        this.representationFunction = representationFunction;
    }

    @Override
    public void evaluate(Evaluation evaluation) {
        AuthorizationProvider authorizationProvider = evaluation.getAuthorizationProvider();
        PositionPolicyRepresentation policy = representationFunction.apply(evaluation.getPolicy(), authorizationProvider);
        RealmModel realm = authorizationProvider.getRealm();
        Entry positionsClaim = evaluation.getContext().getIdentity().getAttributes().getValue(policy.getPositionsClaim());

        if (positionsClaim == null || positionsClaim.isEmpty()) {
            String userPosition = evaluation.getRealm().getUserPosition(evaluation.getContext().getIdentity().getId());
            List<String> lstPosition = new ArrayList<>();
            lstPosition.add(userPosition);
            positionsClaim = new Entry(policy.getPositionsClaim(), lstPosition);
        }

        for (PositionPolicyRepresentation.PositionDefinition definition : policy.getPositions()) {
            PositionModel allowedPosition = realm.getPositionById(definition.getPosId());

            for (int i = 0; i < positionsClaim.size(); i++) {
                String position = positionsClaim.asString(i);

                // in case the position from the claim does not represent a path, we just check an exact name match
                if (position.equals(allowedPosition.getPosName())) {
                    evaluation.grant();
                    return;
                }
            }
        }
    }

    @Override
    public void close() {

    }
}