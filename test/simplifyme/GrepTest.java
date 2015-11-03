package simplifyme;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static java.util.Arrays.asList;

public class GrepTest {
  private String testString = "first test string\nsecond test string\nthird test string";
  private File tempFile;
  private Grep grep = new Grep();
  private ByteArrayOutputStream outputStream;

  @Before
  public void createTempFile() throws Exception {
    tempFile = File.createTempFile("test.grep", ".tmp");
    Files.write(tempFile.toPath(), testString.getBytes());
    outputStream = new ByteArrayOutputStream();
  }

  @Test
  public void testExecuteWithSimplePattern() throws Exception {
    List<String> args = asList("-pattern", "first", "-files", tempFile.getAbsolutePath());
    grep.execute(toArray(args), null, outputStream);
    Assert.assertEquals("first test string\n", outputStream.toString());
  }

  @Test
  public void testExecuteWithIgnoreCase() throws Exception {
    List<String> args = asList("-pattern", "First", "-files", tempFile.getAbsolutePath(), "-i");
    grep.execute(toArray(args), null, outputStream);
    Assert.assertEquals("first test string\n", outputStream.toString());
  }

  @Test
  public void testExecuteWithOnlyWorldWithoutMatching() throws Exception {
    List<String> args = asList("-pattern", "fi", "-files", tempFile.getAbsolutePath(), "-w");
    grep.execute(toArray(args), null, outputStream);
    Assert.assertEquals("", outputStream.toString());
  }


  @Test
  public void testExecuteWithOnlyWorldWithMatching() throws Exception {
    List<String> args = asList("-pattern", "first", "-files", tempFile.getAbsolutePath(), "-w");
    grep.execute(toArray(args), null, outputStream);
    Assert.assertEquals("first test string\n", outputStream.toString());
  }

  @Test
  public void testExecuteWithAfterLine() throws Exception {
    List<String> args = asList("-pattern", "first", "-files", tempFile.getAbsolutePath(), "-A", "1");
    grep.execute(toArray(args), null, outputStream);
    Assert.assertEquals("first test string\nsecond test string\n--\n", outputStream.toString());
  }

  @Test
  public void testExecuteWithStream() throws Exception {
    List<String> args = asList("-pattern", "^First", "-A", "1", "-i", "-w");
    ByteArrayInputStream inputStream = new ByteArrayInputStream(testString.getBytes());
    grep.execute(toArray(args), inputStream, outputStream);
    Assert.assertEquals("first test string\nsecond test string\n--\n", outputStream.toString());
  }

  private String[] toArray(List<String> args) {
    return args.toArray(new String[args.size()]);
  }
}