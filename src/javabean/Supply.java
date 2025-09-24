package javabean;

public class Supply {
	private int ID;
	private String NAME;
	private String DESCRIPTION;
	private String URL;
	private int DISEASE_ID;
	private int SUPPLY_ID;

	public Supply() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Supply(int id, String name, String description, String url, int diseaseId, int supplyId) {
		super();
		ID = id;
		NAME = name;
		DESCRIPTION = description;
		URL = url;
		DISEASE_ID = diseaseId;
		SUPPLY_ID = supplyId;
	}

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
	}

	public String getNAME() {
		return NAME;
	}

	public void setNAME(String name) {
		NAME = name;
	}

	public String getDESCRIPTION() {
		return DESCRIPTION;
	}

	public void setDESCRIPTION(String description) {
		DESCRIPTION = description;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String url) {
		URL = url;
	}

	public int getDISEASE_ID() {
		return DISEASE_ID;
	}

	public void setDISEASE_ID(int diseaseId) {
		DISEASE_ID = diseaseId;
	}

	public int getSUPPLY_ID() {
		return SUPPLY_ID;
	}

	public void setSUPPLY_ID(int supplyId) {
		SUPPLY_ID = supplyId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + DISEASE_ID;
		result = prime * result + ((DESCRIPTION == null) ? 0 : DESCRIPTION.hashCode());
		result = prime * result + ID;
		result = prime * result + ((NAME == null) ? 0 : NAME.hashCode());
		result = prime * result + SUPPLY_ID;
		result = prime * result + ((URL == null) ? 0 : URL.hashCode());
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
		Supply other = (Supply) obj;
		if (DISEASE_ID != other.DISEASE_ID)
			return false;
		if (DESCRIPTION == null) {
			if (other.DESCRIPTION != null)
				return false;
		} else if (!DESCRIPTION.equals(other.DESCRIPTION))
			return false;
		if (ID != other.ID)
			return false;
		if (NAME == null) {
			if (other.NAME != null)
				return false;
		} else if (!NAME.equals(other.NAME))
			return false;
		if (SUPPLY_ID != other.SUPPLY_ID)
			return false;
		if (URL == null) {
			if (other.URL != null)
				return false;
		} else if (!URL.equals(other.URL))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Supply [ID=" + ID + ", NAME=" + NAME + ", DESCRIPTION=" + DESCRIPTION + ", URL=" + URL + ", DISEASE_ID="
				+ DISEASE_ID + ", SUPPLY_ID=" + SUPPLY_ID + "]";
	}

}