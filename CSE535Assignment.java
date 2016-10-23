import java.awt.ItemSelectable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

public class CSE535Assignment {

	static LinkedList<TermPostingSize> pSizeList = new LinkedList<>();
	static Map<String, LinkedList<Integer>> hmapbyDocID = new HashMap<String, LinkedList<Integer>>();
	static Map<String, LinkedList<TermFrequency>> hmapbyTF = new HashMap<String, LinkedList<TermFrequency>>();
	static StringBuilder outputString = new StringBuilder();
	static ArrayList<Integer> finallistDAATAnd = new ArrayList<>();

	public CSE535Assignment() {
		// TODO Auto-generated constructor stub
	}

	public static void getTopKTerms(int n) {
		outputString.append(" FUNCTION: getTopK " + n + " Result: ");
		if (n <= pSizeList.size())
			for (int i = 0; i < n; i++) {
				outputString.append(pSizeList.get(i).Term + ",");
			}
		outputString.deleteCharAt(outputString.length() - 1);

	}

	public static void getPostings(String query_terms) {

		String[] query_term_list = query_terms.split("\\s+");

		for (String query_term : query_term_list) {
			LinkedList<Integer> postingList1 = new LinkedList<>();
			LinkedList<TermFrequency> postingList2 = new LinkedList<>();
			postingList1 = hmapbyDocID.get(query_term);
			postingList2 = hmapbyTF.get(query_term);
			outputString.append(" FUNCTION: getPostings " + query_term);

			if (postingList1 != null && postingList1.size() != 0) {
				outputString.append(" Ordered by doc IDs: ");
				for (int i = 0; i < postingList1.size(); i++)
					outputString.append(postingList1.get(i) + ",");

				outputString.deleteCharAt(outputString.length() - 1); // Remove
																		// trailing
																		// comma
			} else {
				outputString.append("Term not found");
				continue;
			}

			if (postingList2.size() != 0)
				outputString.append(" Ordered by TF: ");
			for (int i = 0; i < postingList2.size(); i++)
				outputString.append(postingList2.get(i).DocID + ",");

			outputString.deleteCharAt(outputString.length() - 1);// Remove
																	// trailing
																	// comma
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		createIndex(args[0]);
		getTopKTerms(Integer.parseInt(args[2]));
		String queryLine;
		File queryFile = new File(args[3]);
		File outputFile = new File(args[1]);

		try {
			outputFile.createNewFile();
			FileWriter fileWriter = new FileWriter(outputFile);
			BufferedReader input = new BufferedReader(new FileReader(queryFile));
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			while ((queryLine = input.readLine()) != null) {
				queryLine = queryLine.trim();
				getPostings(queryLine);
				bufferedWriter.write(outputString.toString());
				outputString.setLength(0);

				termAtATimeQueryAnd(queryLine);
				bufferedWriter.write(outputString.toString());
				outputString.setLength(0);

				termAtATimeQueryOr(queryLine);
				bufferedWriter.write(outputString.toString());
				outputString.setLength(0);

				docAtATimeQueryAnd(queryLine);
				bufferedWriter.write(outputString.toString());
				outputString.setLength(0);

				docAtATimeQueryOr(queryLine);
				bufferedWriter.write(outputString.toString());
				outputString.setLength(0);
			}
			input.close();
			bufferedWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void createIndex(String indexFilePath) {
		String indexline;
		File indexfile = new File(indexFilePath);

		try {
			BufferedReader input = new BufferedReader(new FileReader(indexfile));
			while ((indexline = input.readLine()) != null) {
				LinkedList<TermFrequency> sortedByTF = new LinkedList<>();
				LinkedList<Integer> sortedByDocID = new LinkedList<>();
				String[] id = indexline.split("\\\\");
				String[] docids = id[2].substring(2).replace("]", "").split(",");

				for (int i = 0; i < docids.length; i++) {
					String[] document = docids[i].split("/");
					TermFrequency docObj = new TermFrequency(Integer.parseInt(document[0].trim()),
							Integer.parseInt(document[1].trim()));

					sortedByTF.add(docObj);
					sortedByDocID.add(Integer.parseInt(document[0].trim()));

				}
				Collections.sort(sortedByDocID); // Sort before inserting in the
													// Hash Map
				Collections.sort(sortedByTF, Collections.reverseOrder());
				TermPostingSize tp = new TermPostingSize(id[0], Integer.parseInt(id[1].replace("c", "").trim()));
				pSizeList.add(tp);
				hmapbyDocID.put(id[0], sortedByDocID);
				hmapbyTF.put(id[0], sortedByTF);
			}
			Collections.sort(pSizeList, Collections.reverseOrder());
			input.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public static void termAtATimeQueryAnd(String query_terms) {
		long startTime = System.nanoTime();
		String[] query_term_list = query_terms.split("\\s+");
		int comparisons = 0;

		outputString.append(" FUNCTION: termAtATimeQueryAnd ");
		for (String t : query_term_list)
			outputString.append(t + ",");
		outputString.deleteCharAt(outputString.length() - 1);

		/* Intermediate intersection list which stores the final result */
		LinkedList<TermFrequency> intlist = new LinkedList<>();
		/*
		 * Temporary list stores the intersection of intermediate list and
		 * current posting list
		 */
		LinkedList<TermFrequency> templist = new LinkedList<>();
		/* Posting list of the first term is the base */
		if (hmapbyTF.get(query_term_list[0]) == null) {
			outputString.append(" Term not found");
			return;
		}
		intlist.addAll(hmapbyTF.get(query_term_list[0]));

		for (int i = 1; i < query_term_list.length; i++) {
			// Posting list of every other term
			LinkedList<TermFrequency> queryResult = new LinkedList<>();
			if (hmapbyTF.get(query_term_list[i]) == null) {
				outputString.append(" Term not found");
				return;
			}
			queryResult.addAll(hmapbyTF.get(query_term_list[i]));

			// Iterator on the intermediate list reset
			ListIterator<TermFrequency> intIterator = intlist.listIterator();
			// Iterator on the posting list in consideration
			ListIterator<TermFrequency> queryIterator = queryResult.listIterator();

			/*
			 * Every document of the intermediate list is compared with every
			 * document of the current posting list
			 */
			while (intIterator.hasNext()) {
				int a = intIterator.next().DocID;
				while (queryIterator.hasNext()) {
					TermFrequency b = queryIterator.next();
					comparisons++;
					/* If there is a match, store it in a temporary list */
					if (b.DocID == a) {
						templist.add(b);
						break;
					}

				}
				queryIterator = queryResult.listIterator();
			}
			/*
			 * If no matches found between current posting list and intermediate
			 * list
			 */
			if (templist.size() == 0) {
				intlist.clear();
				break;
			}
			/* Temporary list becomes the new intermediate list */
			intlist.clear();
			intlist.addAll(templist);
			intIterator = intlist.listIterator();
			templist.clear();
		}
		long endTime = System.nanoTime();

		outputString.append((intlist != null ? intlist.size() : 0) + " documents are found ");
		outputString.append(comparisons + " comparisons are made ");
		outputString.append(TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) + " seconds are used ");
		OptimizeTAATAnd(query_terms);
		outputString.append(" Result: ");
		if (intlist != null) {
			ArrayList<Integer> printList = new ArrayList<Integer>();
			for (TermFrequency f : intlist)
				printList.add(f.DocID);
			Collections.sort(printList);
			for (Integer f : printList)
				outputString.append(f + ", ");

			outputString.deleteCharAt(outputString.length() - 2);
		}

	}

	public static void termAtATimeQueryOr(String query_terms) {
		long startTime = System.currentTimeMillis();
		String[] query_term_list = query_terms.split("\\s+");
		int comparisons = 0;

		outputString.append(" FUNCTION: termAtATimeQueryOr ");
		for (String t : query_term_list)
			outputString.append(t + ",");
		outputString.deleteCharAt(outputString.length() - 1);

		LinkedList<TermFrequency> intList = new LinkedList<>();
		/* Use x to skip the term thats not found in the index */
		int x = 0;
		while (hmapbyTF.get(query_term_list[x]) == null)
			x++;
		intList.addAll(hmapbyTF.get(query_term_list[x]));

		for (int i = 1; i < query_term_list.length; i++) {
			LinkedList<TermFrequency> queryResult = new LinkedList<>();
			/* skip the term thats not found in the index */
			if (hmapbyTF.get(query_term_list[i]) == null)
				continue;
			queryResult.addAll(hmapbyTF.get(query_term_list[i]));

			int intListSize = intList.size();
			for (TermFrequency tf : queryResult) {
				boolean found = false;
				for (int j = 0; j < intListSize; j++) {
					comparisons++;
					if (tf.DocID == intList.get(j).DocID) {
						found = true;
						break;
					}

				}
				if (!found)
					intList.add(tf);
			}

		}
		long endTime = System.currentTimeMillis();
		outputString.append((intList != null ? intList.size() : 0) + " documents are found ");
		outputString.append(comparisons + " comparisons are made ");
		outputString.append(endTime - startTime + " seconds are used ");
		OptimizeTAATOr(query_terms);
		outputString.append(" Result: ");
		if (intList != null) {
			ArrayList<Integer> printList = new ArrayList<Integer>();
			for (TermFrequency f : intList)
				printList.add(f.DocID);
			Collections.sort(printList);
			for (Integer f : printList)
				outputString.append(f + ", ");

			outputString.deleteCharAt(outputString.length() - 2);
		}

	}

	public static void docAtATimeQueryAnd(String query_terms) {

		long startTime = System.currentTimeMillis();
		String[] query_term_list = query_terms.split("\\s+");
		int comparisons = 0;
		finallistDAATAnd.clear();/* Clear global variable */

		outputString.append(" FUNCTION: docAtATimeQueryAnd ");
		for (String t : query_term_list)
			outputString.append(t + ",");
		outputString.deleteCharAt(outputString.length() - 1);

		/*
		 * iteratorList -Stores current pointer/iterator values for the posting
		 * lists of all the query terms
		 */
		ArrayList<Integer> iteratorList = new ArrayList<Integer>();

		/* resultSet - List of the postings list for all the query terms */
		LinkedList<LinkedList<Integer>> resultSet = new LinkedList<LinkedList<Integer>>();

		for (int i = 0; i < query_term_list.length; i++) {
			if (hmapbyDocID.get(query_term_list[i]) == null) {
				outputString.append(" Term not found");
				return;
			}
			resultSet.add(hmapbyDocID.get(query_term_list[i]));
			int intlistIterator = 0;
			iteratorList.add(intlistIterator);
		}
		int highest = 0;
		while (!OrComplete(iteratorList)) {
			int last = 0;
			/*
			 * itrPosition is used to find the index of the current iterator in
			 * the loop
			 */
			int itrPosition = -1;
			highest = resultSet.get(0).get(iteratorList.get(0));

			boolean check = true;
			itrPosition = -1;
			for (Integer itr : iteratorList) {
				/*
				 * This iteration finds the highest DocID from the current
				 * position of the iterators in every posting list
				 */
				itrPosition++;
				if (itr == -1) {
					long timeTaken = System.currentTimeMillis() - startTime;
					printDAATAndAnalysis(timeTaken, comparisons);
					return;
				}

				int b = resultSet.get(itrPosition).get(itr);
				comparisons++;
				if (b > highest) {
					highest = b;
					check = false;
				} else if (b < highest)
					check = false;
			}
			/* if all current pointers point to the same document */
			if (check) {
				/*
				 * Add the document to the final list and increment all
				 * pointers/iterators
				 */
				finallistDAATAnd.add(highest);
				itrPosition = -1;
				for (Integer itr : iteratorList) {
					itrPosition++;
					if (itr == resultSet.get(itrPosition).size() - 1) {
						long timeTaken = System.currentTimeMillis() - startTime;
						printDAATAndAnalysis(timeTaken, comparisons);
						return;
					} else
						iteratorList.set(itrPosition, ++itr);

				}
			} else {/*
					 * Increment the iterator on each posting list until its
					 * value is equal to or greater than the highest DocID of
					 * current iteration
					 */
				itrPosition = -1;
				for (Integer itr : iteratorList) {
					itrPosition++;
					int b = resultSet.get(itrPosition).get(itr);
					comparisons++;
					while (b < highest) {
						if (itr != resultSet.get(itrPosition).size() - 1)
							iteratorList.set(itrPosition, ++itr);
						else {
							long timeTaken = System.currentTimeMillis() - startTime;
							printDAATAndAnalysis(timeTaken, comparisons);
							return;
						}

						b = resultSet.get(itrPosition).get(itr);
						comparisons++;
					}

				}
			}
		}
	}

	public static void docAtATimeQueryOr(String query_terms) {
		long startTime = System.currentTimeMillis();
		String[] query_term_list = query_terms.split("\\s+");
		int comparisons = 0;

		outputString.append(" FUNCTION: docAtATimeQueryOr ");
		for (String t : query_term_list)
			outputString.append(t + ",");
		outputString.deleteCharAt(outputString.length() - 1);

		/* finallistDAATOR - Final result list */
		LinkedList<Integer> finallistDAATOR = new LinkedList<>();
		/*
		 * iteratorList -Stores current pointer/iterator values for the posting
		 * lists of all the query terms
		 */
		ArrayList<Integer> iteratorList = new ArrayList<Integer>();
		/*
		 * priorityqueue - To collect all the values less than the highest DocID
		 * in the current iteration
		 */
		PriorityQueue<Integer> priorityqueue = new PriorityQueue<Integer>();

		/* resultSet - List of the postings list for all the query terms */
		LinkedList<LinkedList<Integer>> resultSet = new LinkedList<LinkedList<Integer>>();

		for (int i = 0; i < query_term_list.length; i++) {
			if (hmapbyDocID.get(query_term_list[i]) == null)
				continue;
			resultSet.add(hmapbyDocID.get(query_term_list[i]));
			int intlistIterator = 0;
			iteratorList.add(intlistIterator);
		}
		int highest = 0;
		while (!OrComplete(iteratorList)) {
			/*
			 * itrPosition is used to find the index of the current iterator in
			 * the loop
			 */
			int itrPosition = -1;
			for (Integer itr : iteratorList) {
				itrPosition++;
				if (itr != -1) {
					highest = resultSet.get(itrPosition).get(itr);
					break;
				}

			}
			boolean check = true;
			itrPosition = -1;
			for (Integer itr : iteratorList) {
				/*
				 * This iteration finds the highest DocID from the current
				 * position of the iterators in every posting list
				 */
				itrPosition++;
				if (itr == -1)
					continue;
				int b = resultSet.get(itrPosition).get(itr);
				comparisons++;
				if (b > highest) {
					highest = b;
					check = false;
				} else if (b < highest)
					check = false;
			}
			/* if all current pointers point to the same document */
			if (check) {

				/*
				 * Add the document to the final list and increment all
				 * pointers/iterators
				 */
				/*
				 * If iterator has reached the end of the posting list. Set its
				 * value to -1
				 */
				finallistDAATOR.add(highest);
				itrPosition = -1;
				for (Integer itr : iteratorList) {
					itrPosition++;
					if (itr == resultSet.get(itrPosition).size() - 1)
						iteratorList.set(itrPosition, -1);
					else if (itr != -1)
						iteratorList.set(itrPosition, ++itr);

				}
			} else {/*
					 * Increment the iterator on each posting list until its
					 * value is equal to or greater than the highest DocID of
					 * current iteration
					 */
				itrPosition = -1;
				for (Integer itr : iteratorList) {
					itrPosition++;
					if (itr != -1) {
						int b = resultSet.get(itrPosition).get(itr);
						comparisons++;
						while (b < highest) {
							/*
							 * Using a priority queue to collect all the values
							 * less than the highest DocID in current iteration
							 */
							priorityqueue.add(b);
							if (itr != resultSet.get(itrPosition).size() - 1)
								iteratorList.set(itrPosition, ++itr);
							else {
								iteratorList.set(itrPosition, -1);
								break;
							}

							b = resultSet.get(itrPosition).get(itr);
							comparisons++;
						}

					}

				}
			}
			/* Add values from the priority queue to the final DAAT OR list */
			/*
			 * If the last added value is equal to the head value of the queue
			 * then skip it - avoid duplicates
			 */
			while (priorityqueue.size() > 0) {
				if (finallistDAATOR.size() == 0 || !finallistDAATOR.getLast().equals(priorityqueue.peek()))
					finallistDAATOR.add(priorityqueue.poll());
				else if (finallistDAATOR.getLast().equals(priorityqueue.peek())) {
					priorityqueue.poll();
				}

			}

		}

		long endTime = System.currentTimeMillis();
		outputString.append((finallistDAATOR != null ? finallistDAATOR.size() : 0) + " documents are found ");
		outputString.append(comparisons + " comparisons are made ");
		outputString.append(endTime - startTime + " seconds are used ");
		outputString.append(" Result: ");
		if (finallistDAATOR != null)
			for (Integer f : finallistDAATOR)
				outputString.append(f + ", ");
		outputString.deleteCharAt(outputString.length() - 2);

	}

	public static void printDAATAndAnalysis(long timeTaken, int comparisonsMade) {
		outputString.append((finallistDAATAnd != null ? finallistDAATAnd.size() : 0) + " documents are found ");
		outputString.append(comparisonsMade + " comparisons are made ");
		outputString.append(timeTaken + " seconds are used ");
		outputString.append(" Result: ");
		for (Integer f : finallistDAATAnd)
			outputString.append(f + ", ");
		outputString.deleteCharAt(outputString.length() - 2);
	}

	public static boolean OrComplete(ArrayList<Integer> itrList) {

		for (Integer itr : itrList) {
			if (itr != -1)
				return false;
		}
		return true;
	}

	public static void OptimizeTAATAnd(String query_terms) {
		LinkedList<TermPostingSize> dfList = new LinkedList<>();
		String[] query_term_list = query_terms.split("\\s+");
		int comparisons = 0;
		for (String q : query_term_list) {

			// int index = pSizeList.indexOf(q);
			int df = hmapbyTF.get(q).size();
			TermPostingSize dfObj = new TermPostingSize(q, df);
			dfList.add(dfObj);

		}
		Collections.sort(dfList);
		int count = 0;
		for (TermPostingSize df : dfList) {
			query_term_list[count++] = df.Term;
		}
		/* Intermediate intersection list which stores the final result */
		LinkedList<TermFrequency> intlist = new LinkedList<>();
		/*
		 * Temporary list stores the intersection of intermediate list and
		 * current posting list
		 */
		LinkedList<TermFrequency> templist = new LinkedList<>();

		intlist.addAll(hmapbyTF.get(query_term_list[0]));

		for (int i = 1; i < query_term_list.length; i++) {
			// Posting list of every other term
			LinkedList<TermFrequency> queryResult = new LinkedList<>();
			queryResult.addAll(hmapbyTF.get(query_term_list[i]));

			// Iterator on the intermediate list reset
			ListIterator<TermFrequency> intIterator = intlist.listIterator();
			// Iterator on the posting list in consideration
			ListIterator<TermFrequency> queryIterator = queryResult.listIterator();

			/*
			 * Every document of the intermediate list is compared with every
			 * document of the current posting list
			 */
			while (intIterator.hasNext()) {
				int a = intIterator.next().DocID;
				while (queryIterator.hasNext()) {
					TermFrequency b = queryIterator.next();
					comparisons++;
					/* If there is a match, store it in a temporary list */
					if (b.DocID == a) {
						templist.add(b);
						break;
					}

				}
				queryIterator = queryResult.listIterator();
			}
			/*
			 * If no matches found between current posting list and intermediate
			 * list
			 */
			if (templist.size() == 0) {
				intlist.clear();
				break;
			}
			/* Temporary list becomes the new intermediate list */
			intlist.clear();
			intlist.addAll(templist);
			intIterator = intlist.listIterator();
			templist.clear();
		}

		outputString.append(comparisons + " comparisons are made with optimization ");
		
	}

	public static void OptimizeTAATOr(String query_terms) {
		LinkedList<TermPostingSize> dfList = new LinkedList<>();
		String[] query_term_list = query_terms.split("\\s+");
		int comparisons = 0;
		for (String q : query_term_list) {
			int df = 0;
			if (hmapbyTF.get(q) == null)
				df = 0;
			else
				df = hmapbyTF.get(q).size();
			TermPostingSize dfObj = new TermPostingSize(q, df);
			dfList.add(dfObj);

		}
		Collections.sort(dfList);
		int count = 0;
		for (TermPostingSize df : dfList) {
			query_term_list[count++] = df.Term;
		}
		LinkedList<TermFrequency> intList = new LinkedList<>();
		/* Use x to skip the term thats not found in the index */
		int x = 0;
		while (hmapbyTF.get(query_term_list[x]) == null)
			x++;
		intList.addAll(hmapbyTF.get(query_term_list[x]));

		for (int i = 1; i < query_term_list.length; i++) {
			LinkedList<TermFrequency> queryResult = new LinkedList<>();
			/* skip the term thats not found in the index */
			if (hmapbyTF.get(query_term_list[i]) == null)
				continue;
			queryResult.addAll(hmapbyTF.get(query_term_list[i]));

			int intListSize = intList.size();
			for (TermFrequency tf : queryResult) {
				boolean found = false;
				for (int j = 0; j < intListSize; j++) {
					comparisons++;
					if (tf.DocID == intList.get(j).DocID) {
						found = true;
						break;
					}

				}
				if (!found)
					intList.add(tf);
			}

		}
		outputString.append(comparisons + " comparisons are made with optimization ");

	}

}
