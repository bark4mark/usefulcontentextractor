package co.markhoward.usefulcontentextractor;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UsefulContentExtractorTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Test
	public void shouldExtractTitleAndBodyFromHTML() throws IOException{
		File directory = tempFolder.newFolder();
		UsefulContentExtractor usefulContentExtractor = new UsefulContentExtractor("http://bark4mark.blogspot.ie/", directory);
		usefulContentExtractor.run();
		Assert.assertTrue(new File(directory, "index.html").exists());
	}
}
