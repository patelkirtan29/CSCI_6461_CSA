import java.nio.file.*;
import java.io.*;
import java.util.*;


public class Assembler {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Input arguments: <input soruce file> <output list file> <output load file>");
            return;
        }
        String sourceFilename = args[0];
        String listFilename = args[1];
        String loadFilename = args[2];

        // Read the source file
        List<String> sourceLines = null;
        try {
            sourceLines = Files.readAllLines(Paths.get(sourceFilename));
        } catch (IOException e) {
            System.out.println("Error reading source file: " + e.getMessage());
            return;
        }
        
        // -- FIRST PASS --
        Map<String, Integer> symbolTable = new HashMap<>();
        int codeLocation = 0;
        for (String line: sourceLines) {
            // 1. Set code location to 0
            // 2. Read a line of the file
            // 3. Use the split command to break the line into its parts
            // 4. Process the line, if it is a label, add the label to a dictionary with the code location. Process
            //    the rest of the line (it could be blank, if so no code is generated). Check for errors in the
            //    code.
            // 5. If code or data was generated increment the code location and go to step 2 until
            //    termination.
        }

        // -- SECOND PASS --
        codeLocation = 0;
        for (String line: sourceLines) {
            // 1. Set code location to 0
            // 2. Read a line of the file
            // 3. Use the split command to break the line into it parts
            // 4. Convert the code according to the second field.
            // 5. Add line to listing file and to load file.
            // 6. If code or data generated, increment the code counter, and go to step2 until termination.
        }
    }
}