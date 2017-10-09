package de.upb.crc901.mascot.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import de.upb.crc901.configurationsetting.serialization.SerializationWrapper;
import de.upb.crc901.mascot.structure.Template;

public class TemplateReaderImpl {

	public static List<Template> readInFile(SerializationWrapper parsedData, String fileName) {
		return readInFile(parsedData, new File(fileName));
	}

	public static List<Template> readInFile(SerializationWrapper parsedData, File file) {
		/* read template file */
		List<Template> templates = new LinkedList<Template>();
		TemplateParser templateParser = new TemplateParser();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			StringBuilder templateText = new StringBuilder();
			String strLine;
			boolean hasBlankLine = false;
			while ((strLine = in.readLine()) != null) {
				if (strLine.trim().startsWith("#"))
					continue;
				if (hasBlankLine && strLine.trim().length() == 0) {
					templates.add(
							templateParser.parseTemplate(templateText.toString(), parsedData.getCompositionDomain()));
					templateText = new StringBuilder();
					hasBlankLine = false;
				}

				if (strLine.trim().length() > 0)
					templateText.append(strLine).append("\n");
				else if (templateText.length() > 0)
					hasBlankLine = true;
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return templates;
	}

}
