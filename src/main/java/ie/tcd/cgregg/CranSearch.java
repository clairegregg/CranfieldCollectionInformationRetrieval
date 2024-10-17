package ie.tcd.cgregg;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;


@Command(name = "cran-search", description = "Indexes and queries the cranfield collection")
public class CranSearch implements Runnable {
    @Option(names = "--english-analyzer", description = "Use the EnglishAnalyzer. By default will use StandardAnalyzer")
    boolean englishAnalyzer;

    @Option(names = "--extra-stop-words", description = "Use the predefined extra stopwords.")
    boolean extraStopwords;


    @Option(names = "--auto-stop-word", description = "Enable QueryAutoStopWordAnalyzer when querying index.")
    boolean autoStopWord;

    @Option(names = "--scoring-approach", defaultValue = "vsm", description = "One of 'vsm' and 'bm25'(Vector Space Model and BM25).")
    String scoringApproach;

    @Option(names = {"--interactive", "-i"}, description = "Start an interactive query session.")
    boolean interactive;

    @Override
    public void run() {
        try {
            CreateIndex.createIndex(englishAnalyzer, extraStopwords, scoringApproach);
            QueryIndex.queryIndex(englishAnalyzer, extraStopwords, autoStopWord, scoringApproach);
            if (interactive) {
                InteractiveSearch.interactiveSearch(englishAnalyzer, extraStopwords, autoStopWord, scoringApproach);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CranSearch()).execute(args); // |5|
        System.exit(exitCode); // |6|
    }
}
