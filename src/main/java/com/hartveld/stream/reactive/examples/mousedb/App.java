package com.hartveld.stream.reactive.examples.mousedb;

import static com.hartveld.stream.reactive.concurrency.Schedulers.defaultScheduler;

import com.hartveld.stream.reactive.Observable;
import com.hartveld.stream.reactive.swing.ReactiveSwingFrame;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		LOG.info("Hello, world!");

		SwingUtilities.invokeLater(() -> createGUI());
	}

	private static void createGUI() {
		final ReactiveSwingFrame frame = new ReactiveSwingFrame("Mouse-as-a-Database");
		frame.setMinimumSize(new Dimension(640, 480));

		final Observable<MouseEvent> events = frame.component().mouse().moved()
				.throttle(10, TimeUnit.MILLISECONDS)
				.merge(
					frame.component().mouse().dragged()
							.throttle(10, TimeUnit.MILLISECONDS)
				)
				.throttle(200, TimeUnit.MILLISECONDS);

		final AutoCloseable subscription = events
				.observeOn(defaultScheduler())
				.subscribe(event -> LOG.info("Event: {}", event));

		frame.window().closing()
				.subscribe(event -> {
					LOG.info("Window closing. Shutting down...");

					try {
						subscription.close();
					} catch (Exception e) {
						LOG.error("Something went wrong: {}", e.getMessage(), e);
						System.exit(1);
					}
				});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.pack();
		frame.setVisible(true);
	}

}
