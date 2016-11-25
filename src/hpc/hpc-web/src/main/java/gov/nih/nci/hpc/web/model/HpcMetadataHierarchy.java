package gov.nih.nci.hpc.web.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HpcMetadataHierarchy {
	private Map<String, String> levels;
	private Map<String, String> collectionAttributes;
	private Map<String, String> dataobjectAttributes;
	private List<String> allAttributes;
	
	public List<String> getAllAttributes() {
		return allAttributes;
	}

	public void setAllAttributes(List<String> allAttributes) {
		this.allAttributes = allAttributes;
	}

	public Map<String, String> getDataobjectAttributes() {
		return dataobjectAttributes;
	}

	public Map<String, String> getLevels() {
		return levels;
	}

	public void setLevels(Map<String, String> levels) {
		this.levels = levels;
	}
	
	public void addLevel(String level, String name)
	{
		if(levels == null)
			levels = new HashMap<String, String>();
		levels.put(level, name);
	}
	
	public Map<String, String> getCollectionAttributes() {
		return collectionAttributes;
	}
	public void setCollectionAttributes(Map<String, String> collectionAttributes) {
		this.collectionAttributes = collectionAttributes;
	}
	
	public void addCollectionAttributes(String name, String value)
	{
		if(collectionAttributes == null)
			collectionAttributes = new HashMap<String, String>();
		collectionAttributes.put(name, value);
	}
	
	public void setDataobjectAttributes(Map<String, String> dataobjectAttributes) {
		this.dataobjectAttributes = dataobjectAttributes;
	}
	
	public void addDataobjectAttributes(String name, String value)
	{
		if(dataobjectAttributes == null)
			dataobjectAttributes = new HashMap<String, String>();
		dataobjectAttributes.put(name, value);
	}
	
}
