
public class TermPostingSize implements Comparable<TermPostingSize>{
	String Term;
	int PostingSize;

	public TermPostingSize(String term, int postingSize) {
		// TODO Auto-generated constructor stub
		Term = term;
		PostingSize = postingSize;
		
	}	
	@Override
	public int compareTo(TermPostingSize o) {
		int comparedSize = o.PostingSize;
		if (this.PostingSize > comparedSize) {
			return 1;
		} else if (this.PostingSize == comparedSize) {
			return 0;
		} else {
			return -1;
		}
	}
}
