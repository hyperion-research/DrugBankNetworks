package drugbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class MainDDI {

	private static final String VERSION = "21.06.23"; // "21.03.19"; // 21.03.01
	private static final String DDIFolderPath = /* "C:\\Users\\Alexander\\Documents\\My Tresor\\tmp\\ddi\\"; */ "e:\\.data\\drugbank\\ddi\\";
	private static final String DDIFile = "DDis - drugs.com " + VERSION + ".csv";
	private static final String CLUSTER_STATS = "clusters stats.csv";

	public static void main(String[] args) {
		MainDDI ddi = new MainDDI();

		try {
			ddi.convertCSVToGDF(DDIFolderPath + DDIFile, DDIFolderPath + "ddi_" + VERSION + ".gdf",
					DDIFolderPath + CLUSTER_STATS);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("DONE");
	}

	private void convertCSVToGDF(String inPath, String outPath, String clusterPath) throws IOException {
		File file = new File(inPath);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		File fout = new File(outPath);
		PrintWriter pw = new PrintWriter(fout);

		Map<String, Drug> drugMap = new HashMap<String, Drug>();

		String line, tokens[];
		int k = 0, linecounter = 0;

		// if a cluster file is given, then load data into list of @DrugAgeObjects
		List<DrugAgeObject> drugAgeList = loadDrugAgeList(clusterPath);

		reader.readLine(); // skip csv header

		// read once to map all drugs
		while ((line = reader.readLine()) != null) {
			line = line.trim();

			if (line.length() == 0) {
				continue;
			}
			tokens = line.split(",");

			if (tokens[1].length() > 0)
				drugMap.put(tokens[1], new Drug(tokens[1], tokens[0]));
			if (tokens[3].length() > 0)
				drugMap.put(tokens[3], new Drug(tokens[3], tokens[2]));
			linecounter++;
		}

		// write all drugs from map to gdf
		if (drugAgeList == null) {
			pw.println("nodedef>name VARCHAR,label VARCHAR");
			for (String drugId : drugMap.keySet()) {
				pw.println(drugId + "," + drugMap.get(drugId).getName());
			}
		} else {
			// add info on drug age for those drugs found in the drugage list
			pw.println("nodedef>name VARCHAR,label VARCHAR,age_m INTEGER,age_y INTEGER,"
					+ "interactions INTEGER,cluster INTEGER");
			DrugAgeObject drugAge = null;
			for (String drugId : drugMap.keySet()) {
				drugAge = findDrugByName(drugAgeList, drugMap.get(drugId).getName());
				// print drug age data
				if (drugAge != null)
					pw.println(drugId + "," + drugAge.drugName + "," + drugAge.ageM + "," + drugAge.ageY + ","
							+ drugAge.degree + "," + drugAge.cluster);
				else // do not print additional data
					pw.println(drugId + "," + drugMap.get(drugId).getName());
			}
		}
		reader.close();

		// reopen file
		reader = new BufferedReader(new FileReader(file));
		reader.readLine(); // skip csv header

		pw.println(
				"edgedef>node1 VARCHAR,node2 VARCHAR,weight DOUBLE,sourcename VARCHAR,targetname VARCHAR,btw DOUBLE,bpd DOUBLE,interaction VARCHAR");

		// read second time to map edges
		while ((line = reader.readLine()) != null) {
			line = line.trim();

			if (line.length() == 0) {
				continue;
			}
			tokens = line.split(",");

			// Sodium chloride,DB09153,Tolvaptan,DB06212,0.955979,0.002697,moderate
			if (interactionToNumber(tokens[6]) > 0 && tokens[1].length() > 0 && tokens[3].length() > 0) {
				pw.println(tokens[1] + "," + tokens[3] + "," + interactionToNumber(tokens[6]) + "," + tokens[0] + ","
						+ tokens[2] + "," + tokens[4] + "," + tokens[5] + "," + tokens[6].toLowerCase());
			}
			k++;
		}

		reader.close();
		pw.close();
		System.out.println("Scanned interactions: " + k);
	}

//	private List<Drug> readCSV(String inPath) throws IOException {
//		File file = new File(inPath);
//		BufferedReader reader = new BufferedReader(new FileReader(file));
//		List<Drug> drugs = new ArrayList<Drug>();
//		Map<String, Drug> drugMap = new HashMap<String, Drug>();
//		String line, tokens[];
//		
//		Drug drug1 = null, drug2 = null;
//		
//		int k = 0;
//
//		reader.readLine(); // skip csv header
//		
//		while ((line = reader.readLine()) != null) {
//			line = line.trim();
//
//			if (line.length() == 0) {
//				continue;
//			}		
//			tokens = line.split(",");
//			
//			// Sodium chloride,DB09153,Tolvaptan,DB06212,0.955979,0.002697,moderate
//			drug1 = new Drug(tokens[1], tokens[0]);
//			drug2 = new Drug(tokens[3], tokens[2]);
//			
//			drug1.addInteraction(tokens[3], tokens[2]);			
//			
//			drugMap.put(tokens[1], drug1);
//			drugMap.put(tokens[3], drug2);
//		}
//
//		reader.close();
//		System.out.println("Scanned drugs: " + k);		
//		return drugs;
//	}

	private int interactionToNumber(String interaction) {
		interaction = interaction.toLowerCase();

		if (interaction.equals("minor"))
			return 1;
		else if (interaction.equals("moderate"))
			return 2;
		else if (interaction.equals("major"))
			return 3;
		else
			return -1;
	}

	private List<DrugAgeObject> loadDrugAgeList(String clusterPath) throws IOException {
		// if a cluster file is given, then load data into list of @DrugAgeObjects
		if (clusterPath != null) {
			String line, tokens[];
			File fCluster = new File(clusterPath);
			BufferedReader rCluster = new BufferedReader(new FileReader(fCluster));
			List<DrugAgeObject> drugAgeList = new ArrayList<DrugAgeObject>();

			rCluster.readLine(); // skip header line
			while ((line = rCluster.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				tokens = line.split(",");

				drugAgeList.add(new DrugAgeObject(tokens[0].trim(), Integer.parseInt(tokens[1].trim()),
						Integer.parseInt(tokens[2].trim()), Integer.parseInt(tokens[3].trim()),
						Integer.parseInt(tokens[4].trim())));
			}
			rCluster.close();
			return drugAgeList;
		}
		return null;
	}

	// finds a drug given by name in the provided list
	private DrugAgeObject findDrugByName(List<DrugAgeObject> drugAgeList, String drugName) {
		for (DrugAgeObject obj : drugAgeList) {
			if (obj.drugName.equals(drugName)) {
				return obj;
			}
		}
		return null;
	}

	class DrugAgeObject {
		String drugName;
		int ageM, ageY;
		int degree;
		int cluster;

		public DrugAgeObject(String drugName, int ageM, int ageY, int degree, int cluster) {
			super();
			this.drugName = drugName;
			this.ageM = ageM;
			this.ageY = ageY;
			this.degree = degree;
			this.cluster = cluster;
		}
	}

}
