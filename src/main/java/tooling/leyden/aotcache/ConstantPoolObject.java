package tooling.leyden.aotcache;

/**
 * This element represents an Object of the ConstantPool(Cache) inside the AOT Cache.
 */
public class ConstantPoolObject extends ReferencingElement {
	private Boolean cache = false;

	public void setCache(Boolean cache) {
		this.cache = cache;
	}

	public String getType() {
		return "ConstantPool" + (this.cache ? "Cache" : "");
	}

}
