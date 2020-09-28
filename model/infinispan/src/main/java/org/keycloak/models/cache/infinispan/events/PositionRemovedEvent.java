package org.keycloak.models.cache.infinispan.events;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.MarshallUtil;
import org.infinispan.commons.marshall.SerializeWith;
import org.keycloak.models.PositionModel;
import org.keycloak.models.cache.infinispan.RealmCacheManager;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

/**
 * @author SP_POSITION
 */
@SerializeWith(PositionRemovedEvent.ExternalizerImpl.class)
public class PositionRemovedEvent extends InvalidationEvent implements RealmCacheInvalidationEvent {

    private String posId;
    private String realmId;

    public static PositionRemovedEvent create(PositionModel position, String realmId) {
        PositionRemovedEvent event = new PositionRemovedEvent();
        event.realmId = realmId;
        event.posId = position.getPosId();
        return event;
    }

    @Override
    public String getId() {
        return posId;
    }

    @Override
    public String toString() {
        return String.format("PositionRemovedEvent [ realmId=%s, posId=%s ]", realmId, posId);
    }

    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        realmCache.positionQueriesInvalidations(realmId, invalidations);
    }

    public static class ExternalizerImpl implements Externalizer<PositionRemovedEvent> {

        private static final int VERSION_1 = 1;

        @Override
        public void writeObject(ObjectOutput output, PositionRemovedEvent obj) throws IOException {
            output.writeByte(VERSION_1);

            MarshallUtil.marshallString(obj.realmId, output);
            MarshallUtil.marshallString(obj.posId, output);
        }

        @Override
        public PositionRemovedEvent readObject(ObjectInput input) throws IOException, ClassNotFoundException {
            switch (input.readByte()) {
                case VERSION_1:
                    return readObjectVersion1(input);
                default:
                    throw new IOException("Unknown version");
            }
        }

        public PositionRemovedEvent readObjectVersion1(ObjectInput input) throws IOException, ClassNotFoundException {
            PositionRemovedEvent res = new PositionRemovedEvent();
            res.realmId = MarshallUtil.unmarshallString(input);
            res.posId = MarshallUtil.unmarshallString(input);

            return res;
        }
    }
}
