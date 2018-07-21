package com.kavi.user.dataobjects;

import java.util.List;


import com.google.gson.annotations.Expose;


public class CompanyDO{
	
	@Expose
    private String id;     
	
	@Expose
    private String name;
	
	@Expose
    private String category = "company";
	
	@Expose
    private String display_name;
    
	@Expose
	private List<PlantDO> plant;
	
	public List<PlantDO> getPlant() {
		return plant;
	}

	public void setPlantDO(List<PlantDO> plant) {
		this.plant = plant;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

    
}