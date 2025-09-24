package tooling.leyden.aotcache;

/**
 * This element represents an Object of the ConstantPool(Cache) inside the AOT Cache.
 */
public class ConstantPoolObject extends ReferencingElement {
	private Boolean cache = false;

	public ConstantPoolObject(Boolean cache) {
		this.cache = cache;
	}

	public Boolean getCache() {
		return cache;
	}

	public String getType() {
		return "ConstantPool" + (this.cache ? "Cache" : "");
	}

}
