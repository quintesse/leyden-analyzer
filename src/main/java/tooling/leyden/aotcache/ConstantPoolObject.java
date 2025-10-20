package tooling.leyden.aotcache;

/**
 * This element represents an Object of the ConstantPool(Cache) inside the AOT Cache.
 */
public class ConstantPoolObject extends ReferencingElement {
	private String constantPoolCacheAddress;

	public ConstantPoolObject() {
	}

	public String getConstantPoolCacheAddress() {
		return constantPoolCacheAddress;
	}

	public void setConstantPoolCacheAddress(String constantPoolCacheAddress) {
		this.constantPoolCacheAddress = constantPoolCacheAddress;
	}
}
