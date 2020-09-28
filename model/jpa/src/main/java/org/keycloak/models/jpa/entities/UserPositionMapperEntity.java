package org.keycloak.models.jpa.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name="USER_POSITION")
@Entity
@IdClass(UserPositionMapperEntity.Key.class)
public class UserPositionMapperEntity {
	 	@Id
	    @ManyToOne(fetch= FetchType.LAZY)
	    @JoinColumn(name="USER_ID")
	    protected UserEntity user;

	    @Id
	    @ManyToOne(fetch= FetchType.LAZY)
	    @JoinColumn(name="POSITION", insertable=false, updatable=false)
	    protected PositionEntity position;

	    @Id
	    @Column(name = "POSITION_ID")
	    protected String positionId;

		public UserEntity getUser() {
			return user;
		}

		public void setUser(UserEntity user) {
			this.user = user;
		}

		public PositionEntity getPosition() {
			return position;
		}

		public void setPosition(PositionEntity position) {
			this.position = position;
		}

		public String getPositionId() {
			return positionId;
		}

		public void setPositionId(String positionId) {
			this.positionId = positionId;
		}
		
		 public static class Key implements Serializable {

		        protected UserEntity user;

		        protected String positionId;

		        public Key() {
		        }

		        public Key(UserEntity user, String positionId) {
		            this.user = user;
		            this.positionId = positionId;
		        }

		       

		        public UserEntity getUser() {
					return user;
				}

				public void setUser(UserEntity user) {
					this.user = user;
				}

				public String getPositionId() {
					return positionId;
				}

				public void setPositionId(String positionId) {
					this.positionId = positionId;
				}

				@Override
		        public boolean equals(Object o) {
		            if (this == o) return true;
		            if (o == null || getClass() != o.getClass()) return false;

		            Key key = (Key) o;

		            if (!positionId.equals(key.positionId)) return false;
		            if (!user.equals(key.user)) return false;

		            return true;
		        }

		        @Override
		        public int hashCode() {
		            int result = user.hashCode();
		            result = 31 * result + positionId.hashCode();
		            return result;
		        }
		    }

		    @Override
		    public boolean equals(Object o) {
		        if (this == o) return true;
		        if (o == null) return false;
		        if (!(o instanceof UserPositionMapperEntity)) return false;

		        UserPositionMapperEntity key = (UserPositionMapperEntity) o;

		        if (!positionId.equals(key.positionId)) return false;
		        if (!user.equals(key.user)) return false;

		        return true;
		    }

		    @Override
		    public int hashCode() {
		        int result = user.hashCode();
		        result = 31 * result + positionId.hashCode();
		        return result;
		    }
}
