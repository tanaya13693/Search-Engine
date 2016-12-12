package search;

public class Rank {
	private String file_id=null;
	private double rank=0.0;
	public String getFile_id() {
		return file_id;
	}
	public void setFile_id(String file_id) {
		this.file_id = file_id;
	}
	public double getRank() {
		return rank;
	}
	public void setRank(double rank) {
		this.rank = rank;
	}
	public Rank(double rank,String file_id)
	{
		this.rank=rank;
		this.file_id=file_id;
	}
}
