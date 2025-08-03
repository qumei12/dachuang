package javabean;

public class Mashup_API {
	private int N_ID;
	private int N_MASHUP_ID;
	
	public Mashup_API() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Mashup_API(int n_ID, int n_MASHUP_ID) {
		super();
		N_ID = n_ID;
		N_MASHUP_ID = n_MASHUP_ID;
	}

	public int getN_ID() {
		return N_ID;
	}

	public void setN_ID(int n_ID) {
		N_ID = n_ID;
	}

	public int getN_MASHUP_ID() {
		return N_MASHUP_ID;
	}

	public void setN_MASHUP_ID(int n_MASHUP_ID) {
		N_MASHUP_ID = n_MASHUP_ID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + N_ID;
		result = prime * result + N_MASHUP_ID;
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
		Mashup_API other = (Mashup_API) obj;
		if (N_ID != other.N_ID)
			return false;
		if (N_MASHUP_ID != other.N_MASHUP_ID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Mashup_API [N_ID=" + N_ID + ", N_MASHUP_ID=" + N_MASHUP_ID
				+ "]";
	}

	
	
}
