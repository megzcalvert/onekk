package org.wheatgenetics.onekk;

public class SeedRecord {
	
	private int id;
	private String sampleId;
	private String length;
	private String width;
	private String diameter;
	private String circularity;
	private String color;
	private String area;
	private String weight;

	public SeedRecord() {
	}

	public SeedRecord(String sampleId, String length, String width, String diameter, String circularity, String color, String area, String weight) {
		super();
		this.sampleId = sampleId;
		this.length = length;
		this.width = width;
		this.diameter = diameter;
		this.circularity = circularity;
		this.color = color;
		this.area = area;
		this.weight = weight;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(int id) {
		this.sampleId = sampleId;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getDiameter() {
		return diameter;
	}

	public void setDiameter(String diameter) {
		this.diameter = diameter;
	}

	public void setCircularity(String circularity) {
		this.circularity = circularity;
	}

	public String getCircularity() {
		return circularity;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}
	
	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return sampleId + "," + length + "," + width + "," + diameter + "," + circularity + "," + color  + "," + area  + "," + weight;
	}
}
