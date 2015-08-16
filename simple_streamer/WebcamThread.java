package simple_streamer;

/**
 * @author quangdng
 */

import java.io.UnsupportedEncodingException;

import javax.swing.JFrame;

import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;

/*
 * This class is responsible to receive images from Webcam and encode it as data
 * source for server thread and GUI window.
 */

public class WebcamThread extends Thread {

	public String imgData = null;
	public boolean stopStream = false;

	public WebcamThread() {

	}

	/**
	 * This method is used to provide local stream of images when running in
	 * local mode
	 */
	public void run() {
		// Setup GUI Component
		Viewer myViewer = new Viewer();
		JFrame frame = new JFrame("Simple Stream Viewer");
		frame.setVisible(true);
		frame.setSize(320, 240);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(myViewer);

		OpenIMAJGrabber grabber = new OpenIMAJGrabber();

		Device device = null;
		Pointer<DeviceList> devices = grabber.getVideoDevices();
		for (Device d : devices.get().asArrayList()) {
			device = d;
			break;
		}

		boolean started = grabber.startSession(320, 240, 30,
				Pointer.pointerTo(device));
		if (!started) {
			throw new RuntimeException("Not able to start native grabber!");
		}

		do {
			/* Get a frame from the webcam. */
			grabber.nextFrame();
			/* Get the raw bytes of the frame. */
			byte[] raw_image = grabber.getImage().getBytes(320 * 240 * 3);
			/* Apply a crude kind of image compression. */
			byte[] compressed_image = Compressor.compress(raw_image);
			/* String of compressed image */
			try {
				// Encode image data
				imgData = new String(Base64.encodeBase64(compressed_image),
						"US-ASCII");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/* Give the raw image bytes to the viewer. */
			myViewer.ViewerInput(raw_image);
			frame.repaint();
		} while (!stopStream);

		grabber.stopSession();
	}
}
