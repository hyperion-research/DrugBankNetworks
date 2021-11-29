package drugbank;
/**
 * <copyright>
 *
 * Copyright (c) Alexandru Topirceanu. alex.topirceanu@yahoo.com All rights
 * reserved.
 *
 * File created by Alexandru Jul 20, 2015 7:23:48 PM </copyright>
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import drugbank.Drug.TargetAction;
import sun.java2d.d3d.D3DDrawImage;

public class MainDrugbank {

	private static final String drugAgePath = "c:\\Users\\Alexander\\Documents\\1 Network Medicine\\Drugbank\\drug age\\drug-code-age.csv";

	public static void main(String[] args) {
		MainDrugbank drugbank = new MainDrugbank();

		for (String db : drugbankIDs) {
			try {
				List<Drug> drugs = drugbank.readXML(db, drugAgePath);
//
//				// extract from: "E:\\.data\\drugbank\\drugbank 5.0.11.xml" => "drugbank 5.0.11"
				drugbank.writeGDFInteractions(drugs, "E:\\.data\\drugbank\\cbddin 2021\\"
						+ db.substring(db.lastIndexOf("\\") + 1).replaceAll("xml", "") + "gdf");

//				drugbank.writeGDFInteractions(drugs, /* "C:\\Users\\Alexander\\Downloads\\db41-interactions.gdf" */
//						"E:\\.data\\drugbank\\db42-interactions.gdf");

//			drugbank.writeGDFCommonTargets(drugs, "E:\\.data\\drugbank\\db517-targets.gdf", TargetAction.AGONIST,
//					TargetAction.ACTIVATOR, TargetAction.UNKNOWN, TargetAction.ANTAGONIST, TargetAction.INHIBITOR,
//					TargetAction.NONE);

				System.out.println("DONE");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private List<Drug> readXML(String path, String drugAgePath) throws IOException {
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		List<Drug> drugs = new ArrayList<Drug>();
		List<Drug> drugsWithAge = null;
		String line;

		String drugId, targetId, targetName, drugName, groupType, routeType, atcCode;
		Drug drug = null, drug2 = null;
		int k = 0;

		// read separate drug age file
		if (drugAgePath != null) {
			drugsWithAge = this.scanDrugAgeFile(drugAgePath);
		}

		while ((line = reader.readLine()) != null) {
			line = line.trim();

			if (line.length() == 0) {
				continue;
			}

			if (line.contains(TAG_DRUG_BEGIN)) {
				// save previous drug
				if (drug != null && !drug.isExperimental()
						/*&& getDrugByAge(drugsWithAge, drug.getId()) != null*/ /* && drug.isATC() */) {
					// && !drug.isTopical() ) {
					// 13580 / 4060 / 3410 / 1863
					drugs.add(drug);
				}
				k++;

				// go to next line: drug id
				line = reader.readLine().trim();
				line = line.substring(line.indexOf(">") + 1);
				// set drugId
				drugId = line.substring(0, line.indexOf("<"));

				// go to next line: drug name
				while (!line.contains("<name>")) {
					line = reader.readLine().trim();
				}
				// set drugName
				drugName = line.replace("<name>", "").replace("</name>", "");
				// System.out.println(drugName);

				drug = new Drug(drugId, drugName);
			}

			if (line.contains(TAG_DRUG_GROUP)) {
				// set groupType
				groupType = line.replace("<group>", "").replace("</group>", "");

				if ((groupType.toLowerCase().contains(TAG_DRUG_GROUP_APPR)
						&& groupType.length() == TAG_DRUG_GROUP_APPR.length())
						|| groupType.toLowerCase().contains(TAG_DRUG_GROUP_NUTR)) {
					drug.setExperimental(false);
				}

//				if (groupType.toLowerCase().contains(TAG_DRUG_GROUP_EXPR)
//						|| groupType.toLowerCase().contains(TAG_DRUG_GROUP_INVG)
//						|| groupType.toLowerCase().contains(TAG_DRUG_GROUP_WTHD)) {
//					drug.setExperimental(true);
//				} else {
//					drug.setExperimental(false);
//				}

//				 if(!groupType.toLowerCase().contains(TAG_DRUG_GROUP_APPR)) // debug
//				 System.out.println(groupType);							
			}

			// check for drug group type: approved or not? Include only 'approved' and
			// 'nutraceutical'
			if (line.contains(TAG_DRUG_ROUTE)) {
				// set routeType
				routeType = line.replace("<route>", "").replace("</route>", "");

				if (routeType.contains(TAG_DRUG_ROUTE_TOPICAL)) {
					drug.setTopical(true);
				}

				if (routeType.toLowerCase().contains(TAG_DRUG_ROUTE_TOPICAL)) // debug
					System.out.println(routeType);
			}

			// check if drug contains atc-code
			if (line.contains(TAG_ATC_CODE)) {
				// extract ATC code from current line:
				// <atc-code code="B01AE02"> => 'B'
				atcCode = line.substring(line.indexOf("\"") + 1).substring(0, 1);
				// if (!drug.isATC()) {
				drug.setATCCode(atcCode);
				// } else {
				// drug.mergeATCCode(atcCode);
				// }
				drug.setATC(true);
			}

			// check for drug route type: topical or not? Exclude any 'topical'
			if (line.contains(TAG_DRUG_INTERACTION)) {
				// go to next line: interacting drug id
				line = reader.readLine().trim();
				if (line.contains("<drugbank-id>")) {
					drugId = line.replace("<drugbank-id>", "").replace("</drugbank-id>", "");
				} else {
					drugId = line.replace("<drug>", "").replace("</drug>", "");
				}

				// go to next line: drug name
				while (!line.contains("<name>")) {
					line = reader.readLine().trim();
				}
				drugName = line.replace("<name>", "").replace("</name>", "");

				drug.addInteraction(drugId, drugName);
			}

			if (line.contains(TAG_DRUG_TARGET1) || line.contains(TAG_DRUG_TARGET2)) {
				// go to next line: TARGET drug id
				line = reader.readLine().trim();
				if (line.contains("<id>")) {
					targetId = line.replace("<id>", "").replace("</id>", "");
				} else {
					targetId = "N/A";
				}
				// go to next line: TARGET name
				line = reader.readLine().trim();
				if (line.contains("<name>")) {
					targetName = line.replace("<name>", "").replace("</name>", "");
				} else {
					targetName = "N/A";
				}
				// scan until an action is found
				int qq = 0;
				while (!line.contains(TAG_TARGET_ACTION_START)) {
					line = reader.readLine().trim();
					qq++;
				}

				if (line.contains(TAG_TARGET_ACTION_NONE)) {
					drug.addTarget(targetId, targetName, TargetAction.NONE);
				} else {
					line = reader.readLine().trim();
					line = line.replace("<action>", "").replace("</action>", "");
					if (line.contains(TAG_TARGET_ACTION_ACTIVATOR)) {
						drug.addTarget(targetId, targetName, TargetAction.ACTIVATOR);
					} else if (line.contains(TAG_TARGET_ACTION_INHIBITOR)) {
						drug.addTarget(targetId, targetName, TargetAction.INHIBITOR);
					} else if (line.contains(TAG_TARGET_ACTION_ANTAGONIST)) {
						drug.addTarget(targetId, targetName, TargetAction.ANTAGONIST);
					} else if (line.contains(TAG_TARGET_ACTION_AGONIST)) {
						drug.addTarget(targetId, targetName, TargetAction.AGONIST);
					} else if (line.contains("unknown")) {
						drug.addTarget(targetId, targetName, TargetAction.UNKNOWN);
					} else {
						drug.addTarget(targetId, targetName, TargetAction.OTHER);
						// System.out.println(line);
					}
				}
			}

			// add drug age
//			if (drug != null) {
//				drug2 = getDrugByAge(drugsWithAge, drug.getId());
//				if (drug2 != null)
//					drug.setAge(drug2.getAge());
//			}
		}

		// save last drug
		if (drug != null && !drug.isExperimental()
				/*&& getDrugByAge(drugsWithAge, drug.getId()) != null*/ /* && drug.isATC() */) {// /* &&
			// !drug.isTopical()
			// */
			// {
			drugs.add(drug);
		}

		reader.close();
		System.out.println("Scanned drugs (all types): " + k);
		System.out.println("Filtered drugs (approved, nutraceutical, is ATC): " + drugs.size());
		return drugs;
	}

	private void writeGDFInteractions(List<Drug> drugs, String path) throws FileNotFoundException {
		File file = new File(path);
		PrintWriter pw = new PrintWriter(file);

		pw.println("nodedef>name VARCHAR,label VARCHAR,drug_id VARCHAR,atc VARCHAR,age INTEGER");

		for (Drug drug : drugs) {
			// filter only drugs who's age is known
			pw.println(drug.getId() + ",\"" + drug.getName() + "\"," + drug.getId() + ",\"" + drug.getATCCode() + "\""
					+ (drug.getAge() >= 0 ? ("," + drug.getAge()) : ""));
		}

		pw.println("edgedef>node1 VARCHAR,node2 VARCHAR,sourcename VARCHAR,targetname VARCHAR");
		for (Drug drug : drugs) {
			for (String interaction : drug.getInteractionsMap().keySet()) {
				pw.println(drug.getId() + "," + interaction + ",\"" + drug.getName() + "\",\""
						+ drug.getInteractionsMap().get(interaction) + "\"");
			}
		}
		pw.close();
	}

	private void writeGDFCommonTargets(List<Drug> drugs, String path, TargetAction... actions)
			throws FileNotFoundException {
		File file = new File(path);
		PrintWriter pw = new PrintWriter(file);
		String targets = "";
		int filtered = 0;

		pw.println("nodedef>name VARCHAR,label VARCHAR,targets VARCHAR");

		for (Drug drug : drugs) {
			targets = "";
			for (String target : drug.getTargetsMap().keySet()) {
				// if (drug.getName().equals("Ziprasidone"))
				// {
				// System.out.println(
				// drug.getName() + "\t target:" + target + "\t action:" +
				// drug.getTargetActionsMap().get(target));
				// }

				for (TargetAction action : actions) {
					if (drug.getTargetActionsMap().get(target).equals(action)) {
						targets += drug.getTargetsMap().get(target).toString().replaceAll(",", "") + "***";
					}
				}
			}
			pw.println(drug.getId() + ",\"" + drug.getName() + "\"," + targets);
		}

		pw.println(
				"edgedef>node1 VARCHAR,node2 VARCHAR,sourcename VARCHAR,targetname VARCHAR,weight INTEGER,label VARCHAR");
		int w = 0;
		String label = "", name;
		for (int i = 0; i < drugs.size() - 1; ++i) {
			for (int j = i + 1; j < drugs.size(); ++j) {
				w = 0;
				label = "";
				for (String target1 : drugs.get(i).getTargetsMap().keySet()) {
					for (String target2 : drugs.get(j).getTargetsMap().keySet()) {
						// if two drugs share same target
						if (target1.equals(target2)) {
							// n1 - both drug actions must be either
							// antagonist/inhibitor or
							// agonist/activator
//							if (((drugs.get(i).getTargetActionsMap().get(target1).equals(TargetAction.ANTAGONIST)
////								|| drugs.get(i).getTargetActionsMap().get(target1).equals(TargetAction.INHIBITOR))
////								&& (drugs.get(j).getTargetActionsMap().get(target2).equals(TargetAction.ANTAGONIST)
////									|| drugs.get(j).getTargetActionsMap().get(target2).equals(TargetAction.INHIBITOR)))
////								|| ((drugs.get(i).getTargetActionsMap().get(target1).equals(TargetAction.AGONIST)
////									|| drugs.get(i).getTargetActionsMap().get(target1).equals(TargetAction.ACTIVATOR))
////									&& (drugs.get(j).getTargetActionsMap().get(target2).equals(TargetAction.AGONIST)
////										|| drugs.get(j).getTargetActionsMap().get(target2).equals(
////											TargetAction.ACTIVATOR))))
////							{
////								w++;
////								name = drugs.get(i).getTargetsMap().get(target1).toString().replaceAll(",", "");
////								label += name + "***";
////
////							}

							// n2 - both drug actions must be either
							// antagonist/inhibitor/unknown or
							// agonist/activator/unknown
							if (((drugs.get(i).getTargetActionsMap().get(target1).equals(TargetAction.ANTAGONIST)
									|| drugs.get(i).getTargetActionsMap().get(target1).equals(TargetAction.INHIBITOR)
									|| drugs.get(i).getTargetActionsMap().get(target1).equals(TargetAction.UNKNOWN))
									&& (drugs.get(j).getTargetActionsMap().get(target2).equals(TargetAction.ANTAGONIST)
											|| drugs.get(j).getTargetActionsMap().get(target2)
													.equals(TargetAction.INHIBITOR)
											|| drugs.get(j).getTargetActionsMap().get(target2)
													.equals(TargetAction.UNKNOWN)))
									|| ((drugs.get(i).getTargetActionsMap().get(target1).equals(TargetAction.AGONIST)
											|| drugs.get(i).getTargetActionsMap().get(target1)
													.equals(TargetAction.ACTIVATOR)
											|| drugs.get(i).getTargetActionsMap().get(target1)
													.equals(TargetAction.UNKNOWN))
											&& (drugs.get(j).getTargetActionsMap().get(target2)
													.equals(TargetAction.AGONIST)
													|| drugs.get(j).getTargetActionsMap().get(target2)
															.equals(TargetAction.ACTIVATOR)
													|| drugs.get(j).getTargetActionsMap().get(target2)
															.equals(TargetAction.UNKNOWN)))) {
								w++;
								name = drugs.get(i).getTargetsMap().get(target1).toString().replaceAll(",", "");
								label += name + "***";

							}

							// n2 - both drug actions must be unknown
//							if (drugs.get(i).getTargetActionsMap().get(target1).equals(TargetAction.NONE)
//								&& drugs.get(j).getTargetActionsMap().get(target2).equals(TargetAction.NONE))
//							{
//								w++;
//								name = drugs.get(i).getTargetsMap().get(target1).toString().replaceAll(",", "");
//								label += name + "***";
//
//							}

//							for (TargetAction action: actions)
//							{
//								// the actions must both be of the same type
//								if (drugs.get(i).getTargetActionsMap().get(target1).equals(action)
//									&& drugs.get(j).getTargetActionsMap().get(target2).equals(action))
//								{
//									w++;
//									name = drugs.get(i).getTargetsMap().get(target1).toString().replaceAll(",", "");
//									label += name + "***";
//									break; // ?
//								}
//							}
						}
					}
				}

				if (w > 0) {
					pw.println(drugs.get(i).getId() + "," + drugs.get(j).getId() + ",\"" + drugs.get(i).getName()
							+ "\",\"" + drugs.get(j).getName() + "\"," + w + "," + label);
					label = "";
					filtered++;
				}
			}
		}

		pw.close();
		System.out.println("Filtered and saved " + filtered + " drugs to file");
	}

	private List<Drug> scanDrugAgeFile(String path) throws FileNotFoundException {
		File fin = new File(path);
		Scanner scan = new Scanner(fin);
		List<Drug> drugsWithAge = new ArrayList<Drug>();

		String line, tokens[];

		while (scan.hasNextLine()) {
			line = scan.nextLine().trim();
			if (line.length() == 0)
				continue;

			tokens = line.split(",");
			Drug drug = new Drug(tokens[1].trim(), tokens[0].trim());
			drug.setAge(Integer.parseInt(tokens[2]));
			drugsWithAge.add(drug);
		}

		scan.close();
		return drugsWithAge;
	}

	private Drug getDrugByAge(List<Drug> drugsWithAge, String DBID) {
		for (Drug drug : drugsWithAge) {
			if (drug.getId().equals(DBID))
				return drug;
		}
		return null;
	}

	private static final String TAG_DRUG_BEGIN = "<drug type=";
	private static final String TAG_DRUG_ID = "<drugbank-id>";
	private static final String TAG_DRUG_NAME = "<name>";
	private static final String TAG_DRUG_INTERACTION = "<drug-interaction>";
	private static final String TAG_DRUG_INTERACTION_ID = "<drug>";
	private static final String TAG_DRUG_TARGET1 = "<target>";
	private static final String TAG_DRUG_TARGET2 = "<target ";
	private static final String TAG_DRUG_GROUP = "<group>";
	private static final String TAG_ATC_CODE = "<atc-code code";
	private static final String TAG_DRUG_GROUP_APPR = "approved";
	private static final String TAG_DRUG_GROUP_NUTR = "nutraceutical";
	private static final String TAG_DRUG_GROUP_EXPR = "experimental";
	private static final String TAG_DRUG_GROUP_INVG = "investigational";
	private static final String TAG_DRUG_GROUP_WTHD = "withdrawn";
	private static final String TAG_DRUG_GROUP_ILCT = "illicit";
	private static final String TAG_DRUG_GROUP_VETA = "vet_approved";
	private static final String TAG_DRUG_ROUTE = "<route>";
	private static final String TAG_DRUG_ROUTE_TOPICAL = "Topical";

	private static final String TAG_TARGET_ACTION_START = "<action";
	private static final String TAG_TARGET_ACTION_NONE = "<actions/>";
	private static final String TAG_TARGET_ACTION_INHIBITOR = "inhibitor";
	private static final String TAG_TARGET_ACTION_ACTIVATOR = "activator";
	private static final String TAG_TARGET_ACTION_AGONIST = "agonist";
	private static final String TAG_TARGET_ACTION_ANTAGONIST = "antagonist";

	private static final String drugbank30 = "E:\\.data\\drugbank\\drugbank 3.0.xml";
	private static final String drugbank41 = /* "C:\\Users\\Alexander\\Downloads\\drugbank 4.1.xml"; */ "E:\\.data\\drugbank\\drugbank 4.1.xml";
	private static final String drugbank42 = "E:\\.data\\drugbank\\drugbank 4.2.xml";
	private static final String drugbank43 = "E:\\.data\\drugbank\\drugbank 4.3.xml";
	private static final String drugbank45 = "E:\\.data\\drugbank\\drugbank 4.5.xml";
	private static final String drugbank50 = "E:\\.data\\drugbank\\drugbank 5.0.xml";
	private static final String drugbank501 = "E:\\.data\\drugbank\\drugbank 5.0.1.xml";
	private static final String drugbank502 = "E:\\.data\\drugbank\\drugbank 5.0.2.xml";
	private static final String drugbank503 = "E:\\.data\\drugbank\\drugbank 5.0.3.xml";
	private static final String drugbank504 = "E:\\.data\\drugbank\\drugbank 5.0.4.xml";
	private static final String drugbank505 = "E:\\.data\\drugbank\\drugbank 5.0.5.xml";
	private static final String drugbank506 = "E:\\.data\\drugbank\\drugbank 5.0.6.xml";
	private static final String drugbank507 = "E:\\.data\\drugbank\\drugbank 5.0.7.xml";
	private static final String drugbank508 = "E:\\.data\\drugbank\\drugbank 5.0.8.xml";
	private static final String drugbank509 = "E:\\.data\\drugbank\\drugbank 5.0.9.xml";
	private static final String drugbank5010 = "E:\\.data\\drugbank\\drugbank 5.0.10.xml";
	private static final String drugbank5011 = "E:\\.data\\drugbank\\drugbank 5.0.11.xml";
	private static final String drugbank510 = "E:\\.data\\drugbank\\drugbank 5.1.0.xml";
	private static final String drugbank511 = "E:\\.data\\drugbank\\drugbank 5.1.1.xml";
	private static final String drugbank512 = "E:\\.data\\drugbank\\drugbank 5.1.2.xml";
	private static final String drugbank513 = "E:\\.data\\drugbank\\drugbank 5.1.3.xml";
	private static final String drugbank514 = "E:\\.data\\drugbank\\drugbank 5.1.4.xml";
	private static final String drugbank515 = "E:\\.data\\drugbank\\drugbank 5.1.5.xml";
	private static final String drugbank516 = "E:\\.data\\drugbank\\drugbank 5.1.6.xml";
	private static final String drugbank517 = "E:\\.data\\drugbank\\drugbank 5.1.7.xml";
	private static final String drugbank518 = "E:\\.data\\drugbank\\drugbank 5.1.8.xml";

	private static final String drugbankIDs[] = { drugbank30, drugbank41, drugbank42, drugbank43, drugbank45,
			drugbank50, drugbank501, drugbank502, drugbank503, drugbank504, drugbank505, drugbank506, drugbank507,
			drugbank508, drugbank509, drugbank5010, drugbank5011, drugbank510, drugbank511, drugbank512, drugbank513,
			drugbank514, drugbank515, drugbank516, drugbank517, drugbank518};
}
