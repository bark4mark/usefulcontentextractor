package co.markhoward.usefulcontentextractor;

import java.io.File;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;

public class MainController {
	@FXML
	private TextField baseURI;

	@FXML
	private TextField siteMapURI;

	@FXML
	private Label chosenDirectory;

	@FXML
	private Button chooseDirectory;

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private Button startButton;

	@FXML
	private Label runningLabel;

	private File chosen;

	@FXML
	private void onDirectoryChoose() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File chosen = directoryChooser.showDialog(anchorPane.getScene().getWindow());
		if (chosen == null)
			return;
		this.chosen = chosen;
		chosenDirectory.setText(chosen.getAbsolutePath());
	}

	@FXML
	private void onStart() {
		if (chosen == null)
			return;

		if (Strings.isNullOrEmpty(siteMapURI.getText()))
			return;

		if (Strings.isNullOrEmpty(baseURI.getText()))
			return;

		final String siteMap = siteMapURI.getText();
		final String base = baseURI.getText();
		startButton.setDisable(true);
		runningLabel.setText("Currently running");
		SiteMapDownloader siteMapDownloader = new SiteMapDownloader(Charsets.UTF_8, base);
		Set<String> links = siteMapDownloader.listLinks(siteMap);
		Map<String, File> hierarchy = siteMapDownloader.createHierarchy(links, chosen);
		try {
			siteMapDownloader.downloadUsefulContent(hierarchy);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			
		}
		startButton.setDisable(false);
		runningLabel.setText("Not running");
	}
}
