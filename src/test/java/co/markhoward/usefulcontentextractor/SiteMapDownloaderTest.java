package co.markhoward.usefulcontentextractor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class SiteMapDownloaderTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private SiteMapDownloader siteMapDownloader;
	
	@Before
	public void setup(){
		siteMapDownloader = new SiteMapDownloader(Charsets.UTF_8, "http://samplesite.terminalfour.com/");
	}
	
	@Test
	public void shouldListAllLinksInASiteMap(){
		Set<String> links = listLinks();
		Assert.assertTrue(links.size() == 30);
	}
	
	@Test
	public void shouldCreateHierarchyFromLinks() throws IOException{
		
		Map<String, File> uriDirectories = createHierarchy();
		Assert.assertTrue(uriDirectories.size() > 0);
	}
	
	@Test
	public void shouldDownloadHtmlFiles() throws IOException, InterruptedException{
		Map<String, File> uriDirectories = createHierarchy();
		siteMapDownloader.downloadUsefulContent(uriDirectories);
		for(File directory: uriDirectories.values()){
			Assert.assertTrue(new File(directory, "index.html").exists());
		}
	}
	
	private Map<String, File> createHierarchy() throws IOException{
		Set<String> links = listLinks();
		File localDirectory = tempFolder.newFolder();
		return siteMapDownloader.createHierarchy(links, localDirectory);
	}
	
	private Set<String> listLinks(){
		return siteMapDownloader.listLinks(Resources.getResource("sitemap.html").toString());
	}
}
