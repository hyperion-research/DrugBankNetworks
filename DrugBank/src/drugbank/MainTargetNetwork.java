package drugbank;
/**
 * <copyright>
 *
 * Copyright (c) Alexandru Topirceanu. alex.topirceanu@yahoo.com All rights
 * reserved.
 *
 * File created by Alexander Mar 27, 2020 11:14:46 AM </copyright>
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainTargetNetwork
{
	private static final String drugIDs515 = "c:\\Users\\Alexander\\Documents\\1 Network Medicine\\Drugbank\\drugIDs515.csv";
	private static final String targets515 = "c:\\Users\\Alexander\\Documents\\1 Network Medicine\\Drugbank\\pharmacologically_active515.csv";
	private static final String SEP_CSV = ",";
	private static final String SEP_DRUGS = ";";

	public static void main(String[] args)
	{
		MainTargetNetwork targetNetwork = new MainTargetNetwork();

		try
		{
			List<TargetType> targets = targetNetwork.readTargets(targets515);
			targetNetwork.writeGDFTargetNetwork(
				"c:\\Users\\Alexander\\Documents\\1 Network Medicine\\Drugbank\\targets515.gdf", targets);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		System.out.println("DONE");
	}

	private List<TargetType> readTargets(String path) throws FileNotFoundException
	{
		List<TargetType> targets = new ArrayList<TargetType>();

		File file = new File(path);
		Scanner scan = new Scanner(file);
		String line, words[];

		while (scan.hasNextLine())
		{
			line = scan.nextLine().trim();

			if (line.length() == 0)
			{
				continue;
			}

			if (line.startsWith("#"))
			{
				continue;
			}

			words = line.split(SEP_CSV);

			// ID,Name,Gene Name,GenBank Protein ID,GenBank Gene ID,UniProt
			// ID,Uniprot Title,PDB ID,GeneCard ID,GenAtlas ID,HGNC
			// ID,Species,Drug IDs
			// 4,Coagulation factor XIII A
			// chain,F13A1,182309,M22001,P00488,F13A_HUMAN,1EVU; 1EX0; 1F13;
			// 1FIE; 1GGT; 1GGU; 1GGY; 1QRK;
			// 4KTY,,F13A1,HGNC:3531,Humans,DB11300; DB11311; DB11571;
			// DB11572; DB13151

			TargetType target = new TargetType(words[0].trim(), words[1].trim());
			target.setPropertyValue("Type", "Target");
			target.setPropertyValue("Gene Name", words[2]);
			target.setPropertyValue("UniProt ID", words[5]);
			target.setPropertyValue("UniProt Title", words[6]);

			line = words[12].trim(); // list of drugs delimited by ';'
			if (line.toLowerCase().equals("humans"))
			{
				line = words[13].trim();
			}
			if (line.length() > 0)
			{
				words = line.split(SEP_DRUGS);
				List<TargetType> drugs = new ArrayList<TargetType>();
				for (String word: words)
				{
					TargetType drug = new TargetType(word.trim(), "");
					drug.setPropertyValue("Type", "Drug");
					drugs.add(drug);
				}
				target.setPropertyValue("drugs", drugs);
			}

			targets.add(target);
		}

		System.out.println("Read " + targets.size() + " targets");

		return targets;
	}

	private void writeGDFTargetNetwork(String outpath, List<TargetType> targets) throws FileNotFoundException
	{
		File file = new File(outpath);
		PrintWriter pw = new PrintWriter(file);
		String drugs;

		pw.println("nodedef>name VARCHAR,label VARCHAR,drugs VARCHAR");

		// write nodes
		for (TargetType target: targets)
		{
			drugs = "";
			List<TargetType> drugList = (List<TargetType>) target.getPropertyValue("drugs");
			for (TargetType drug: drugList)
			{
				drugs += drug.getID() + "***";
			}

			pw.println(target.getID() + ",\"" + target.getName() + "\"," + drugs);
		}

		// write edges
		pw.println(
			"edgedef>node1 VARCHAR,node2 VARCHAR,sourcename VARCHAR,targetname VARCHAR,weight INTEGER,label VARCHAR");
		String label = "";
		int w = 0;
		for (int i = 0; i < targets.size() - 1; ++i)
		{
			for (int j = i + 1; j < targets.size(); ++j)
			{
				// if two targets have the same durg then w++
				w = 0;
				label = "";

				for (TargetType drug1: (List<TargetType>) targets.get(i).getPropertyValue("drugs"))
				{
					for (TargetType drug2: (List<TargetType>) targets.get(j).getPropertyValue("drugs"))
					{
						if (drug1.getID().equals(drug2.getID()))
						{
							w++;
							label += drug1.getID() + "***";
						}
					}
				}

				if (w > 0)
				{
					pw.println(targets.get(i).getID() + "," + targets.get(j).getID() + ",\"" + targets.get(i).getName()
						+ "\",\"" + targets.get(j).getName() + "\"," + w + "," + label);
				}
			}
		}

		pw.close();
	}
}
