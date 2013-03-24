package com.hartveld.stream.reactive.examples.mousedb;

import com.hartveld.stream.reactive.Observable;
import com.hartveld.stream.reactive.concurrency.Schedulers;
import com.hartveld.stream.reactive.swing.ReactiveFrame;
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
		final ReactiveFrame frame = new ReactiveFrame("Mouse-as-a-Database");
		frame.setMinimumSize(new Dimension(640, 480));

//		final AutoCloseable movedSubscription = frame.component.mouseEvents.moved
//				.throttle(500, TimeUnit.MILLISECONDS)
//				.observeOn(Schedulers.DEFAULT)
//				.map(event -> event.getPoint())
//				.subscribe(p -> LOG.info("Mouse moved: {}", p));

//		final AutoCloseable draggedSubscription = frame.component.mouseEvents.dragged
//				.throttle(200, TimeUnit.MILLISECONDS)
//				.observeOn(Schedulers.DEFAULT)
//				.map(event -> event.getPoint())
//				.subscribe(point -> LOG.info("Mouse dragged: {}", point));

		final Observable<MouseEvent> throttled = frame.component.mouseEvents.dragged
				.throttle(10, TimeUnit.MILLISECONDS);

		final Observable<MouseEvent> merged = frame.component.mouseEvents.moved
				.throttle(10, TimeUnit.MILLISECONDS)
				.merge(throttled)
				.throttle(200, TimeUnit.MILLISECONDS);

		final AutoCloseable mergedSubscription = merged
				.observeOn(Schedulers.DEFAULT)
				.subscribe(event -> LOG.info("Event: {}", event));

		frame.window.closing
				.subscribe(event -> {
					LOG.info("Window closed. Shutting down...");

					try {
//						movedSubscription.close();
//						draggedSubscription.close();
						mergedSubscription.close();
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
