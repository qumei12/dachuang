package javabean;

import java.util.Date;

public class Mashup {
	private int N_ID;
	private String C_NAME;
	private String C_DESCRIPTION;
	private String C_URL;
	private Date D_DATE;
	
	public Mashup() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Mashup(int n_ID, String c_NAME, String c_DESCRIPTION, String c_URL,
			Date d_DATE) {
		super();
		N_ID = n_ID;
		C_NAME = c_NAME;
		C_DESCRIPTION = c_DESCRIPTION;
		C_URL = c_URL;
		D_DATE = d_DATE;
	}

	public int getN_ID() {
		return N_ID;
	}

	public void setN_ID(int n_ID) {
		N_ID = n_ID;
	}

	public String getC_NAME() {
		return C_NAME;
	}

	public void setC_NAME(String c_NAME) {
		C_NAME = c_NAME;
	}

	public String getC_DESCRIPTION() {
		return C_DESCRIPTION;
	}

	public void setC_DESCRIPTION(String c_DESCRIPTION) {
		C_DESCRIPTION = c_DESCRIPTION;
	}

	public String getC_URL() {
		return C_URL;
	}

	public void setC_URL(String c_URL) {
		C_URL = c_URL;
	}

	public Date getD_DATE() {
		return D_DATE;
	}

	public void setD_DATE(Date d_DATE) {
		D_DATE = d_DATE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((C_DESCRIPTION == null) ? 0 : C_DESCRIPTION.hashCode());
		result = prime * result + ((C_NAME == null) ? 0 : C_NAME.hashCode());
		result = prime * result + ((C_URL == null) ? 0 : C_URL.hashCode());
		result = prime * result + ((D_DATE == null) ? 0 : D_DATE.hashCode());
		result = prime * result + N_ID;
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
		Mashup other = (Mashup) obj;
		if (C_DESCRIPTION == null) {
			if (other.C_DESCRIPTION != null)
				return false;
		} else if (!C_DESCRIPTION.equals(other.C_DESCRIPTION))
			return false;
		if (C_NAME == null) {
			if (other.C_NAME != null)
				return false;
		} else if (!C_NAME.equals(other.C_NAME))
			return false;
		if (C_URL == null) {
			if (other.C_URL != null)
				return false;
		} else if (!C_URL.equals(other.C_URL))
			return false;
		if (D_DATE == null) {
			if (other.D_DATE != null)
				return false;
		} else if (!D_DATE.equals(other.D_DATE))
			return false;
		if (N_ID != other.N_ID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Mashup [N_ID=" + N_ID + ", C_NAME=" + C_NAME
				+ ", C_DESCRIPTION=" + C_DESCRIPTION + ", C_URL=" + C_URL
				+ ", D_DATE=" + D_DATE + "]";
	}
	
	
	
}
