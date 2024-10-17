package ie.tcd.cgregg;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.query.QueryAutoStopWordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.misc.SweetSpotSimilarity;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class InteractiveSearch {
    private static final String INDEX_DIRECTORY = "index";
    private static final int MAX_RESULTS = 10;

    public static void interactiveSearch(boolean englishAnalyzer, boolean extraStopwords, boolean autoStopWord, String scoringApproach) throws IOException, ParseException {
        Analyzer initAnalyzer = CreateIndex.cranfieldAnalyzer(englishAnalyzer, extraStopwords);

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
        }

        Analyzer analyzer = autoStopWord?
                new QueryAutoStopWordAnalyzer(initAnalyzer, ireader)
                : initAnalyzer;

        QueryParser parser = new QueryParser("content", analyzer);

        String queryString = "";
        Scanner scanner = new Scanner(System.in);
        do {
            queryString =  CreateIndex.preprocessing(queryString.trim());

            if (!queryString.isEmpty()) {
                Query query = parser.parse(queryString);

                ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;

                System.out.println("Top " + hits.length + " relevant documents:");
                for (int i = 0; i < hits.length; i++) {
                    Document doc = isearcher.doc(hits[i].doc);
                    System.out.println((i+1) + ") Title: [" + doc.get("title") + "] Score: " + hits[i].score);
                }
                System.out.println();
            }
            System.out.print(">>> ");
            queryString = scanner.nextLine();
        } while (!queryString.equals("q"));
    }
}
