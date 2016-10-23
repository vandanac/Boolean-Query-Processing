
public class TermFrequency implements Comparable<TermFrequency> {

	int DocID;
	int TermFeq;
	public TermFrequency(int docID, int tf ) {
		// TODO Auto-generated constructor stub
		DocID =  docID;
		TermFeq = tf;
	}

	@Override
	public int compareTo(TermFrequency o) {
		int comparedSize = o.TermFeq;
		if (this.TermFeq > comparedSize) {
			return 1;
		} else if (this.TermFeq == comparedSize) {
			return 0;
		} else {
			return -1;
		}
	}

}
