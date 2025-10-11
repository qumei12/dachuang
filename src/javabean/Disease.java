package javabean;

import java.util.Date;

public class Disease {
	private int ID;
	private String NAME;
	private String DESCRIPTION;
	private String URL;
	private Date DATE;
	
	public Disease() {
		super();
		// TODO Auto-generated constructor stub
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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((DESCRIPTION == null) ? 0 : DESCRIPTION.hashCode());
		result = prime * result + ((NAME == null) ? 0 : NAME.hashCode());
		result = prime * result + ((URL == null) ? 0 : URL.hashCode());
		result = prime * result + ((DATE == null) ? 0 : DATE.hashCode());
		result = prime * result + ID;
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
		Disease other = (Disease) obj;
		if (DESCRIPTION == null) {
			if (other.DESCRIPTION != null)
				return false;
		} else if (!DESCRIPTION.equals(other.DESCRIPTION))
			return false;
		if (NAME == null) {
			if (other.NAME != null)
				return false;
		} else if (!NAME.equals(other.NAME))
			return false;
		if (URL == null) {
			if (other.URL != null)
				return false;
		} else if (!URL.equals(other.URL))
			return false;
		if (DATE == null) {
			if (other.DATE != null)
				return false;
		} else if (!DATE.equals(other.DATE))
			return false;
		if (ID != other.ID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Disease [ID=" + ID + ", NAME=" + NAME
				+ ", DESCRIPTION=" + DESCRIPTION + ", URL=" + URL
				+ ", DATE=" + DATE + "]";
	}
	
	
	
}