# Boolean-Query-Processing
Assignment as part of Information Retrieval course

In this programming assignment, posting lists generated from the RCV1 news corpus were used.
(http://www.daviddlewis.com/resources/testcollections/rcv1/)

Constructed two index with different ordering strategies:
1-The posting of each term ordered by increasing document IDs
2-The postings of each term ordered by decreasing term frequencies. 

Implemented modules that return documents based on term-at-a-time with the postings list ordered by term frequencies,
and document-at-a-time with the postings list ordered by doc IDs for a set of queries.
