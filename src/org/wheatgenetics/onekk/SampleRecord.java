package org.wheatgenetics.onekk;

public class SampleRecord {

	private int id;
	private int position;
	private String sampleId;
	private String photo;
	private String personId;
	private String timestamp;
	private String weight;
	
	public SampleRecord() {
	}
	
	public SampleRecord(String sampleId, int position, String photo, String personId, String timestamp, String weight) {
		super();
		this.sampleId = sampleId;
		this.photo = photo;
		this.personId = personId;
		this.timestamp = timestamp;
		this.weight = weight;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(String sample) {
		this.sampleId = sample;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String person) {
		this.personId = person;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimestamp() {
		return timestamp;
	}
	
	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return sampleId + "," + photo + "," + personId + "," + timestamp + "," + weight;
	}
}
