package resources.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * 
 * @author ispace
 */
public enum PrefixesMap {

	INSTANCE;

	private HashMap<String, String> prefixes;

	public HashMap<String, String> load() {

		// load if null only
		if (prefixes == null) {

			prefixes = new HashMap<String, String>();

			// set up the input stream
			//InputStream is = PrefixesMap.class
			//		.getResourceAsStream("/resources/data/prefixes.csv");
			
			try {

				FileInputStream fis = new FileInputStream("data/prefixes.csv");

				InputStreamReader reader = new InputStreamReader(fis);
				
				BufferedReader in = new BufferedReader(reader);

				Iterable<CSVRecord> records = CSVFormat.DEFAULT
						.withHeader("Prefix", "URI").withSkipHeaderRecord(true)
						.withQuote('"').withDelimiter(',').parse(in);

				for (CSVRecord record : records) {
					
					if (record.size() > 1) {
						prefixes.put(record.get("Prefix"), record.get("URI"));
					}
					
				}
				
			} catch (FileNotFoundException ex) {
				Logger.getLogger(PrefixesMap.class.getName()).log(Level.SEVERE,
						null, ex);
			} catch (IOException ex) {
				Logger.getLogger(PrefixesMap.class.getName()).log(Level.SEVERE,
						null, ex);
			}
			
		}

		return prefixes;
	}

}
