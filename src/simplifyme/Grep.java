package simplifyme;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class Grep {

  public void execute(String[] args, InputStream inputStream, OutputStream outputStream) throws GrepException {
    if (args.length == 0) {
      throw new GrepException("grep: too few arguments");
    } else {

      try {
        Options options = new Options()
            .addOption(new Option("i", false, "case-insensitive"))
            .addOption(new Option("w", false, "match words only"))
            .addOption(Option.builder("A").hasArg().build())
            .addOption(Option.builder("pattern").hasArg().build())
            .addOption(Option.builder("files").hasArgs().build());
        final CommandLine commandLine = new DefaultParser().parse(
            options, args);

        boolean ignoreCase = commandLine.hasOption("i");
        boolean wordOnly = commandLine.hasOption("w");
        int numberAfterMatching = Integer.valueOf(commandLine.getOptionValue("A", "0"));
        String patternStr = commandLine.getOptionValue("pattern");
        String[] files = commandLine.getOptionValues("files");
        List<String> fileNames = files != null ? asList(files) : Collections.<String>emptyList();

        if (inputStream != null) {
          grep(inputStream, outputStream, patternStr, wordOnly, ignoreCase, numberAfterMatching, false, null);
        } else {
          if (fileNames.isEmpty()) {
            throw new GrepException("too few filenames");
          }
          boolean printFileNames = fileNames.size() > 1;
          for (String fileName : fileNames) {
            try (InputStream inputStream1 = new FileInputStream(fileName)) {
              grep(inputStream1, outputStream, patternStr, wordOnly, ignoreCase, numberAfterMatching, printFileNames, fileName);
            } catch (IOException e) {
              throw new GrepException(String.format("grep :%s : %s\n", fileName, e.getMessage()));
            }
          }
        }

      } catch (ParseException e) {
        throw new GrepException("grep:" + e.getMessage(), e);
      }
    }
  }

  private void grep(InputStream inputStream, OutputStream outputStream, String patternStr, boolean wordOnly,
                    boolean ignoreCase, int numberAfterMatching, boolean printName, String fileName) {

    String findInStart = patternStr.startsWith("^") ? "^" : "";
    String findInEnd = patternStr.endsWith("$") ? "$" : "";

    String regex = patternStr.substring(findInStart.isEmpty() ? 0 : 1,
        findInEnd.isEmpty() ? patternStr.length(): patternStr.length() - 1);
    if (wordOnly) {
      regex = "\\b" + regex + "\\b";
    }
    regex = findInStart + regex + findInEnd;

    int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
    Pattern pattern = Pattern.compile(regex, flags);

    int numberAfterMatchingCounter = numberAfterMatching + 1;

    try {
      PrintStream out = new PrintStream(outputStream); //don't close
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
      ) {
        String line = reader.readLine();
        while (line != null) {
          if (pattern.matcher(line).find()) {
            numberAfterMatchingCounter = 0;
            if (printName) {
              out.append(fileName).append(":").println(line);
            } else {
              out.println(line);
            }
          } else {
            if (numberAfterMatchingCounter < numberAfterMatching) {
              if (printName) {
                out.append(fileName).append("-").println(line);
              } else {
                out.append(line).println();
              }
            } else if (numberAfterMatchingCounter == numberAfterMatching) {
              if (printName) {
                out.append(fileName).append("-").println(line);
              } else {
                out.println(line);
              }
              out.println("--");
            }
          }
          numberAfterMatchingCounter++;
          line = reader.readLine();
        }
      } catch (IOException e) {
        out.println(String.format("grep :%s : %s\n", fileName, e.getMessage()));
      }
    } catch (Exception e) {
      try {
        outputStream.write(String.format("grep :%s : %s\n", fileName, e.getMessage()).getBytes());
      } catch (IOException e1) {
        System.err.println(String.format("grep :%s : %s\n", fileName, e.getMessage()));
      }
    }
  }
}
