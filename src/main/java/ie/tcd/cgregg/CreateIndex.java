package ie.tcd.cgregg;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.misc.SweetSpotSimilarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 
public class CreateIndex
{
    
    // Directory where the search index will be saved
    private static final String INDEX_DIRECTORY = "index";
    private static final String CORPUS_FILENAME = "src/main/resources/cran.all.1400";
    private static final String ID_ID = "I";
    private static final String AUTHOR_ID = "A";
    private static final String TITLE_ID = "T";
    private static final String TEXT_ID = "W";

    public static void createIndex(boolean englishAnalyzer, boolean extraStopwords, String scoringApproach) throws IOException
    {
        // Analyzer that is used to process TextField
        Analyzer analyzer = cranfieldAnalyzer(englishAnalyzer, extraStopwords);

        // ArrayList of documents in the corpus
        ArrayList<Document> documents = new ArrayList<>();

        // Open the directory that contains the search index
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // Set up an index writer to add process and save documents to the index
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        switch (scoringApproach) {
            case "vsm":
                config.setSimilarity(new ClassicSimilarity());
                break;
            case "bm25":
                config.setSimilarity(new BM25Similarity());
        }
        IndexWriter iwriter = new IndexWriter(directory, config);

        String content = new String(Files.readAllBytes(Paths.get(CORPUS_FILENAME)));
        String[] docs = content.split("(?=\\.I)");

        for (String doc : docs) {
            documents.add(retrieveDocument(doc));
        }
        
        // Save the document to the index
        iwriter.addDocuments(documents);

        // Commit changes and close everything
        iwriter.close();
        directory.close();
    }

    private static Document retrieveDocument(String unprocessed) {
        Pattern pattern = Pattern.compile("\\.(\\w)\\s((.*\\s)*?)(?=(\\.\\w)|(\\Z))");
        Matcher matcher = pattern.matcher(unprocessed);
        Document doc = new Document();

        while (matcher.find()) {
            String id = matcher.group(1);
            String value = matcher.group(2).trim();
            addField(doc, id, preprocessing(value));
        }
        return doc;
    }

    private static void addField(Document doc, String id, String value) {
        switch (id) {
            case ID_ID:
                doc.add(new StringField("id", value, Field.Store.YES));
                break;
            case AUTHOR_ID:
                doc.add(new StringField("author", value, Field.Store.YES));
                break;
            case TITLE_ID:
                doc.add(new StringField("title", value, Field.Store.YES));
                break;
            case TEXT_ID:
                doc.add(new TextField("content", value, Field.Store.YES));
                break;
            default:
                break;
        }
    }

    public static Analyzer cranfieldAnalyzer(boolean useEnglishAnalyzer, boolean useExtraStopwords) throws IOException {
        if (!useEnglishAnalyzer) {
            return new StandardAnalyzer();
        }
        CharArraySet stopwords = CharArraySet.copy(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        if (useExtraStopwords) {
            stopwords.addAll(Set.of("which", "us", "been", "were", "from"));
        }
        return new EnglishAnalyzer(stopwords);
    }

    public static String preprocessing(String input) {
        return input.replaceAll("\\p{Punct}", "");
    }
}
