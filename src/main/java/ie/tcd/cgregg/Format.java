package ie.tcd.cgregg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Format {
    
    private static final String QREL_FILENAME = "src/main/resources/cranqrel";
    
    public static void formatQRel() throws IOException {
        Path qrelPath = Paths.get(QREL_FILENAME);
        String content = new String(Files.readAllBytes(qrelPath));

        String[] tuples = content.split("\\n");

        for (int i = 0; i < tuples.length; i++) {
            String[] values = tuples[i].split("\\s+");
            if (values.length == 3) {
                tuples[i] = String.join(" ", values[0], "0", values[1], values[2]);
            }
        }

        content = String.join("\n", tuples);
        Files.write(qrelPath, content.getBytes());
    }

    public static void mapQRelVals() throws IOException {
        Path qrelPath = Paths.get(QREL_FILENAME);
        String content = new String(Files.readAllBytes(qrelPath));
        String[] tuples = content.split("\\n");
        for (int i = 0; i < tuples.length; i++) {
            String[] values = tuples[i].split("\\s+");
            String score = values[values.length - 1];
            if (score.equals("-1")) {
                values[values.length - 1] = "5";
            }
            tuples[i] = String.join(" ", values);
        }

        content = String.join("\n", tuples);
        Files.write(qrelPath, content.getBytes());
    }
}
