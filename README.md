# Lucene Cranfield Collection
orginal scripts by Dr. Gary Munnelly
adapted for 2020 by Colin Daly

Completed by Claire Gregg for Information Retrieval and Web Search module - CS7IS3.

## Instructions to run

### Build
this will have the effect of installing all prerequisite packages e.g. lucene 8.6.2

```mvn package```

### Run

```java -jar target/cranfieldcollection-1.0.jar```

Optional arguments for configuration of the indexing/querying

- ``--english-analyzer`` - enable the more effective EnglishAnalyzer
- ``--extra-stop-words`` - add extra stop words to the English Analyzer
- ``--auto-stop-word`` - enable the QueryAutoStopWordAnalyzer when querying the index
- ``--scoring-approach`` - select scoring approach for ranking when querying - options are vsm (default - Vector Space Model) or bm25
- ``-i`` or ``--interactive`` - start an interactive search shell to query the index, quit using q

The ideal configuration found in this assignment is 
```
java -jar target/cranfieldcollection-1.0.jar --scoring-approach=bm25 --english-analyzer --extra-stop-words --auto-stop-word
```

### Test
Test using trec_eval, built from https://github.com/usnistgov/trec_eval.

```
 ./trec_eval src/main/resources/cranqrel src/main/resources/results
```


