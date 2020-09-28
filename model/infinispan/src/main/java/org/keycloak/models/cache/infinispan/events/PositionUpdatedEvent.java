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

package org.keycloak.models.cache.infinispan.events;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.MarshallUtil;
import org.infinispan.commons.marshall.SerializeWith;
import org.keycloak.models.cache.infinispan.RealmCacheManager;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

/**
 * @author SP_POSITION
 */
@SerializeWith(PositionUpdatedEvent.ExternalizerImpl.class)
public class PositionUpdatedEvent extends InvalidationEvent implements RealmCacheInvalidationEvent {

    private String posId;

    public static PositionUpdatedEvent create(String posId) {
        PositionUpdatedEvent event = new PositionUpdatedEvent();
        event.posId = posId;
        return event;
    }

    @Override
    public String getId() {
        return posId;
    }


    @Override
    public String toString() {
        return "GroupUpdatedEvent [ " + posId + " ]";
    }

    @Override
    public void addInvalidations(RealmCacheManager realmCache, Set<String> invalidations) {
        // Nothing. ID already invalidated
    }

    public static class ExternalizerImpl implements Externalizer<PositionUpdatedEvent> {
        private static final int VERSION_1 = 1;

        @Override
        public void writeObject(ObjectOutput output, PositionUpdatedEvent obj) throws IOException {
            output.writeByte(VERSION_1);

            MarshallUtil.marshallString(obj.posId, output);
            // ...
        }

        @Override
        public PositionUpdatedEvent readObject(ObjectInput input) throws IOException, ClassNotFoundException {
            switch (input.readByte()) {
                case VERSION_1:
                    return readObjectVersion1(input);
                default:
                    throw new IOException("Unknown version");
            }
        }

        public PositionUpdatedEvent readObjectVersion1(ObjectInput input) throws IOException, ClassNotFoundException {
            PositionUpdatedEvent res = new PositionUpdatedEvent();
            res.posId = MarshallUtil.unmarshallString(input);
            // ...

            return res;
        }
    }
}
