/**
 * Jul 25, 2012 10:59:39 AM
 * @author nhnghia
 */
package fr.lri.proptester.global;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Filtre element &lt;vdict name="p" time="1234"&gt;pass&lt;/vdict&gt; from a
 * stream
 */

public class ResultLocalFilter {
	String data;
	BufferedReader bufferedReader;

	String VERDICT_ELEMENT = "message";
	int VERDICT_ELEMENT_LENGTH = 7;
	String verdict; // curret verdict which is get from stream
	boolean isThreadRunning; // being geting from stream
	boolean isClosed; //

	/**
	 * @param streamAddress
	 *            URL address
	 */
	public ResultLocalFilter(final String streamAddress) {
		bufferedReader = null;
		data = "";
		verdict = null;
		isEndOfStream = false;
		isThreadRunning = false;
		isClosed = false;

		// open stream
		Thread openStream = new Thread() {
			public void run() {
				while (!isClosed) {
					try {
						URL u = new URL(streamAddress);
						bufferedReader = new BufferedReader(
								new InputStreamReader(u.openStream()));
						break;
					} catch (IOException ex) {
						fr.lri.schora.util.Debug.print(ex);
						fr.lri.schora.util.Print
								.error("Cannot open stream at ["
										+ streamAddress
										+ "],we try again in 2 seconds");
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		openStream.start();

	}

	// Using to close loops of openStream and readVerdict
	public void close() {
		isClosed = true;
	}

	public String getMessage() {

		// not yet open stream
		if (bufferedReader == null)
			return null;

		if (isEndOfStream)
			return null;

		if (verdict != null) {
			String s = verdict;
			verdict = null;
			return s;
		}

		if (!isThreadRunning) {
			try {
				isThreadRunning = true;
				// read data to verdict
				Thread readVerdict = new Thread() {
					public void run() {
						try {
							if (!isClosed && !isEndOfStream) {
								String str = bufferedReader.readLine();
								if (str == null) {
									fr.lri.schora.util.Debug.println("End of Stream");
									isEndOfStream = true;
									return;
								}
								data = data + str;

								int d = data.indexOf("</" + VERDICT_ELEMENT
										+ ">");
								// Extract un <vdict> element
								if (d > 0) {
									int d1 = data
											.indexOf("<" + VERDICT_ELEMENT);
									if (d1 > -1) {
										d = d + VERDICT_ELEMENT_LENGTH + 3;
										setVerdict(data.substring(d1, d));
										// tools.Debug.println(verdict);
										data = data.substring(d);
									}
								}

							}
						} catch (Exception ex) {
							fr.lri.schora.util.Debug.print(ex);
						} finally {
							isThreadRunning = false;
						}
					}
				};
				readVerdict.setName("readVerdict");
				readVerdict.start();
			} catch (Exception ex) {
				fr.lri.schora.util.Debug.print(ex);
			}
		}
		return null;
	}

	synchronized void setVerdict(String s) {
		verdict = s;
	}

	boolean isEndOfStream;

	public boolean isEndOfStream() {
		return isEndOfStream;
	}
}
