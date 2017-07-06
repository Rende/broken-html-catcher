package app;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
		String inputFilePath = "/Users/aydanrende/Documents/gfs/";//
		listFilesForFolder(new File(inputFilePath));///gfs/clueweb09/Disk1/ClueWeb09_German_1/
		HashSet<String> hashSet = getAlreadyProcessedFileSet();
		for (int i = 0; i < fileList.size(); i++) {
			if (!hashSet.contains(fileList.get(i))) {
				try {
					GZIPInputStream gzInputStream;
					System.out.println(fileList.get(i));
					gzInputStream = new GZIPInputStream(new FileInputStream(
							fileList.get(i)));
					WarcRecord warcRecord;
					DataInputStream inStream = new DataInputStream(
							gzInputStream);
					while ((warcRecord = WarcRecord
							.readNextWarcRecord(inStream)) != null) {
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
				} catch (Exception e) {
					e.printStackTrace();
				}
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

	public static HashSet<String> getAlreadyProcessedFileSet() {
//		ClassLoader classLoader = BrokenHtmlCatcher.class.getClassLoader();
//		File fi = new File(classLoader.getResource("already-processed.txt").getFile());
		HashSet<String> set = new HashSet<String>();
		// read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get("already-processed.txt"))) {
			set = (HashSet<String>) stream.collect(Collectors.toSet());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}
}
