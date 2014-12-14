package net.redis.distributed.lock;

public class RLock {
	private long validityTime;
	private String resource;
	private String value;
	
	public RLock(long validityTime, String resource, String value) {
		this.validityTime = validityTime;
		this.resource = resource;
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + (int) (validityTime ^ (validityTime >>> 32));
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RLock other = (RLock) obj;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (validityTime != other.validityTime)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	public long getValidityTime() {
		return validityTime;
	}
	public void setValidityTime(long validityTime) {
		this.validityTime = validityTime;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "RLock [validityTime=" + validityTime + ", resource=" + resource
				+ ", value=" + value + "]";
	}
	
	
}
