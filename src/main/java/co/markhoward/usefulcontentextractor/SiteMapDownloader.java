package co.markhoward.usefulcontentextractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.Files;

public class SiteMapDownloader {
	private final Charset charset;
	private final String baseUri;

	public SiteMapDownloader(Charset charset, String baseUri) {
		this.charset = charset;
		this.baseUri = baseUriChecker(baseUri);
	}

	/**
	 * List all links contained in a site map
	 * 
	 * @param siteMapUri
	 *            The URI of the site map
	 */
	public Set<String> listLinks(String siteMapUri) {
		Set<String> links = new HashSet<>();
		if (Strings.isNullOrEmpty(siteMapUri))
			return links;

		String html = null;
		try {
			if(siteMapUri.startsWith(FILE))
				html = fileToString(siteMapUri);
			else
				html = uriToString(siteMapUri, charset);
			
		} catch (URISyntaxException | IOException exception) {
			exception.printStackTrace();
		}

		if (Strings.isNullOrEmpty(html))
			return links;
		
		Document document = Jsoup.parse(html);
		if(document == null)
			return links;
		
		document.setBaseUri(baseUri);
		for(Element link: document.select("a")){
			String linkUri = link.attr("abs:href");
			if(Strings.isNullOrEmpty(linkUri))
				continue;
			
			if(!linkUri.startsWith(baseUri))
				continue;
			
			links.add(linkUri);
		}
		
		return links;
	}
	
	public Map<String, File> createHierarchy(Set<String> links, File localDirectory) {
		Map<String, File> uriDirectories = new HashMap<>();
		for(String link: links){
			try {
				URL url = new URL(link);
				String path = url.getPath();
				Optional<File> created = createDirectories(path, localDirectory);
				if(created.isPresent())
					uriDirectories.put(link, created.get());
			} catch (MalformedURLException exception) {
				exception.printStackTrace();
			}
		}
		return uriDirectories;
	}
	
	/**
	 * Given a URI this will download as a string
	 * @param uri The URI to download
	 * @return The string value
	 * @throws MalformedURLException If the URI is dodgy
	 * @throws IOException If it cannot connected to
	 */
	public static String uriToString(String uri, Charset charset) throws MalformedURLException, IOException {
		URLConnection conn = new URL(uri).openConnection();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset.name()))) {
			return reader.lines().collect(Collectors.joining("\n"));
		}
	}
	
	/**
	 * Takes a maps of URIs and directories and downloads the HTML and saves in the directory
	 * @param uriDirectories The map of URIs and directories
	 * @throws InterruptedException If the thread is interupted
	 */
	public void downloadUsefulContent(Map<String, File> uriDirectories) throws InterruptedException {
		ExecutorService service = Executors.newCachedThreadPool();
		for(String uri: uriDirectories.keySet())
			service.execute(new UsefulContentExtractor(uri, uriDirectories.get(uri)));
		
		service.shutdown();
		service.awaitTermination(10, TimeUnit.MINUTES);
	}
	
	private String baseUriChecker(String baseUri) {
		if(Strings.isNullOrEmpty(baseUri))
			return "";
		
		if(baseUri.endsWith("/"))
			return baseUri.substring(0, baseUri.length() - 1);
		else
			return baseUri;
	}
	
	private Optional<File> createDirectories(String path, File localDirectory){
		
		if(Strings.isNullOrEmpty(path))
			return Optional.absent();
		
		Iterable<String> splitPath = Splitter.on('/')
	       .trimResults()
	       .omitEmptyStrings()
	       .split(path);
		
		File directory = new File(localDirectory.getAbsolutePath());
		for(String folder: splitPath)
			directory = new File(directory, folder);
		
		if(!directory.exists())
			directory.mkdirs();
		
		return Optional.of(directory);
	}

	private String fileToString(String fileUri) throws URISyntaxException, IOException {
		return Files.toString(new File(new URI(fileUri)), charset);
	}

	private static final String FILE = "file";
}
