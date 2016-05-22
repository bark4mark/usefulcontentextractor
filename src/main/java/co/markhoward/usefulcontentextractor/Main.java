package co.markhoward.usefulcontentextractor;


import java.io.IOException;

import com.google.common.io.Resources;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(Resources.getResource("main.fxml"));
			primaryStage.setTitle(APP_NAME);
			primaryStage.show();
			primaryStage.setScene(new Scene(root));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() throws Exception {
		System.exit(0);
	}
	
	private static final String APP_NAME = "Useful content extractor";
}
