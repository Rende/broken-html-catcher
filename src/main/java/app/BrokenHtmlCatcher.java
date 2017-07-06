package app;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.xml.sax.SAXException;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLDocument;
import edu.cmu.lemurproject.WarcRecord;

/**
 *
 */

/**
 * @author Aydan Rende, DFKI
 *
 */
public class BrokenHtmlCatcher {
	private static List<String> fileList = new ArrayList<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inputFilePath = "/gfs/clueweb09/Disk1/ClueWeb09_German_1/";
		listFilesForFolder(new File(inputFilePath));
		for (int i = 0; i < fileList.size(); i++) {
			try {
				GZIPInputStream gzInputStream;
				System.out.println(fileList.get(i));
				gzInputStream = new GZIPInputStream(new FileInputStream(
						fileList.get(i)));
				WarcRecord warcRecord;
				DataInputStream inStream = new DataInputStream(gzInputStream);
				while ((warcRecord = WarcRecord.readNextWarcRecord(inStream)) != null) {
					try {
						String htmlText = warcRecord.getContentUTF8();
						HTMLDocument htmlDoc = new HTMLDocument(htmlText);
						TextDocument doc = new BoilerpipeSAXInput(
								htmlDoc.toInputSource()).getTextDocument();
					} catch (StackOverflowError e) {
						// TODO Auto-generated catch block
						System.out
								.println("Broken warc record id: "
										+ warcRecord
												.getHeaderMetadataItem("WARC-Record-ID"));
						// e.printStackTrace();

					}
				}
				inStream.close();

			} catch (BoilerpipeProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void listFilesForFolder(File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				if (fileEntry.getName().endsWith(".gz")) {
					fileList.add(fileEntry.getAbsolutePath());
				}
			}
		}
	}
}
