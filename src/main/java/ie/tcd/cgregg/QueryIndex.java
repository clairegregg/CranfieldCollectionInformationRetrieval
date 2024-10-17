package ie.tcd.cgregg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.query.QueryAutoStopWordAnalyzer;
import org.apache.lucene.document.Document;

import org.apache.lucene.misc.SweetSpotSimilarity;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.QueryParser;

public class QueryIndex
{

    // the location of the search index
    private static final String INDEX_DIRECTORY = "index";
    private static final String QUERY_FILENAME = "src/main/resources/cran.qry";
    private static final String RESULTS_FILENAME = "src/main/resources/results";

    // Limit the number of search results we get
    private static final int MAX_RESULTS = 50;

    public static void queryIndex(boolean englishAnalyzer, boolean extraStopwords, boolean autoStopWord, String scoringApproach) throws IOException, ParseException {
        Analyzer initAnalyzer = CreateIndex.cranfieldAnalyzer(englishAnalyzer, extraStopwords);

        BufferedWriter writer = new BufferedWriter(new FileWriter(RESULTS_FILENAME));

        // Open the folder that contains our search index
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // create objects to read and search across the index
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        switch (scoringApproach) {
            case "vsm":
                isearcher.setSimilarity(new ClassicSimilarity());
                break;
            case "bm25":
                isearcher.setSimilarity(new BM25Similarity());
                break;
        }

        Analyzer analyzer = autoStopWord?
                new QueryAutoStopWordAnalyzer(initAnalyzer, ireader)
                : initAnalyzer;

        String content = new String(Files.readAllBytes(Paths.get(QUERY_FILENAME)));
        String[] queries = content.split("(?=\\.I)");
        for (int i=0; i< queries.length; i++) {
            queries[i] = retrieveQuery(queries[i]);
        }

        QueryParser parser = new QueryParser("content", analyzer);

        for (int queryNumber = 1; queryNumber < queries.length; queryNumber++) {
            String queryString = CreateIndex.preprocessing(queries[queryNumber-1].trim());
            Query query = parser.parse(queryString);

            ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;

            for (int rank = 1; rank <= hits.length; rank++)
            {
                Document hitDoc = isearcher.doc(hits[rank-1].doc);
                String docNumber = hitDoc.get("id");
                String[] results = {String.valueOf(queryNumber), "Q0", docNumber, String.valueOf(rank), String.valueOf(hits[rank-1].score), "STANDARD\n"};
                writer.write(String.join("\t", results));
            }
        }

        // close everything we used
        writer.close();
        ireader.close();
        directory.close();
    }

    private static String retrieveQuery(String unprocessed) {
        return unprocessed.split("\\.W")[1];
    }
}
