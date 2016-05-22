package co.markhoward.usefulcontentextractor;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLDocument;
import de.l3s.boilerpipe.sax.HTMLFetcher;
import de.l3s.boilerpipe.sax.HTMLHighlighter;

public class UsefulContentExtractor implements Runnable{
	private final String uri;
	private final File directory;
	public UsefulContentExtractor(final String uri, final File directory){
		this.uri = uri;
		this.directory = directory;
	}
	
	@Override
	public void run() {
		try {
			HTMLHighlighter htmlHighlighter = HTMLHighlighter.newExtractingInstance();
			HTMLDocument htmlDoc = HTMLFetcher.fetch(new URL(uri));
			InputSource inputSource = htmlDoc.toInputSource();
			TextDocument textDocument = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
			ArticleExtractor.getInstance().process(textDocument);
			String title = textDocument.getTitle();
			String content = htmlHighlighter.process(textDocument, inputSource);
			Document document = Jsoup.parse("<html></html>");
			document.title(title);
			document.select("html").append(String.format("<div class=\"imported-content\">%s</div>", content));
			final ByteSink sink = Files.asByteSink(new File(directory, DEFAULT_FILENAME));
			sink.write(document.html().getBytes());
		} catch (BoilerpipeProcessingException | IOException | SAXException exception) {
			exception.printStackTrace();
		}
	}
	
	private static final String DEFAULT_FILENAME = "index.html";
}
