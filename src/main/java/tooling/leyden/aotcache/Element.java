package tooling.leyden.aotcache;


import java.util.List;

/** Elements that can be found on the AOTCache.
 * A generic interface just in case we add more things to the AOTCache. **/
public interface Element {

	/**
	 * Used to search for this element. For example, on classes this would be the full qualified name of the class.
	 *
	 * @return The key that identifies the element
	 */
	public String getKey();

	/**
	 * Is this a class, a method,...?
	 *
	 * @return The type of element
	 */
	public String getType();

	/**
	 * When describing an element, this is the String we are going to use.
	 *
	 * @return A complete description of this element.
	 */
	public String getDescription();

	/**
	 * Used to understand why this element is added to the cache. There may be more than one source of information
	 * for this element.
	 *
	 * @return Where this element comes from
	 */
	public List<String> getSources();

	public void addSource(String source);
}
