package org.keycloak.models.cache.infinispan.stream;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.MarshallUtil;
import org.infinispan.commons.marshall.SerializeWith;
import org.keycloak.models.cache.infinispan.entities.PositionListQuery;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author SP_POSITION
 * @version $Revision: 1 $
 */
@SerializeWith(PositionListPredicate.ExternalizerImpl.class)
public class PositionListPredicate implements Predicate<Map.Entry<String, Revisioned>>, Serializable {
    private String realm;

    public static PositionListPredicate create() {
        return new PositionListPredicate();
    }

    public PositionListPredicate realm(String realm) {
        this.realm = realm;
        return this;
    }

    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        Object value = entry.getValue();
        if (value == null) return false;
        if (value instanceof PositionListQuery) {
            PositionListQuery positionList = (PositionListQuery)value;
            if (positionList.getRealm().equals(realm)) return true;
        }
        return false;
    }

    public static class ExternalizerImpl implements Externalizer<PositionListPredicate> {

        private static final int VERSION_1 = 1;

        @Override
        public void writeObject(ObjectOutput output, PositionListPredicate obj) throws IOException {
            output.writeByte(VERSION_1);

            MarshallUtil.marshallString(obj.realm, output);
        }

        @Override
        public PositionListPredicate readObject(ObjectInput input) throws IOException, ClassNotFoundException {
            switch (input.readByte()) {
                case VERSION_1:
                    return readObjectVersion1(input);
                default:
                    throw new IOException("Unknown version");
            }
        }

        public PositionListPredicate readObjectVersion1(ObjectInput input) throws IOException, ClassNotFoundException {
            PositionListPredicate res = new PositionListPredicate();
            res.realm = MarshallUtil.unmarshallString(input);

            return res;
        }
    }
}
