package tooling.leyden.aotcache;


/** Elements that can be found on the AOTCache.
 * A generic interface just in case we add more things to the AOTCache. **/
public interface Element {

	public String getKey();

	public String getType();

	public String getDescription();
}
