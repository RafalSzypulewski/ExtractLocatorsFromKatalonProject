import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class locatorsExtractor {
    public static void listFilesForFolder(final File folder) {
        List<File> files = new ArrayList<>();
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                files.add(fileEntry);
            } else {
                if (fileEntry.getName().contains(".rs")) {
                    listOfFiles.add(fileEntry.getAbsolutePath());
                }
            }
        }
        for (File x : files) {
            listFilesForFolder(x);
        }
    }


    static List<String> listOfFiles;

    public static void main(String[] args) {
        listOfFiles = new ArrayList<String>();
        String filePath = "C:\\katalon_project_path\\Object Repository";
        final File folder = new File(filePath);
        listFilesForFolder(folder);
        String finalOutput = "";
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        String[] checkList = new String[10];
        int[] count = new int[10];
        boolean isNewCategory = false;
        for (String s : listOfFiles) {
            try {
                String trimmedDirectory = s.replace(filePath, "");
                StringBuilder category = new StringBuilder();
                String[] split = trimmedDirectory.split("\\\\");

                for (int i = 1; i < split.length; i++) {

                    StringBuilder newCategory = new StringBuilder("\n\n#");
                    if (i < split.length - 1) {

                        if (checkList[i] != split[i].intern()) {
                            checkList[i] = split[i];
                            count[i]++;
                            for (int z = i + 1; z < checkList.length; z++) {
                                checkList[z] = "";
                            }
                            for (int x = 1; x < i; x++) {
                                newCategory.append("#");
                            }

                            category.append(newCategory).append(" ").append(checkList[i]);
                            isNewCategory = true;

                        }

                    }
                    if (i == split.length - 1) {
                        if (isNewCategory) {
                            category.append("\n|Test Object|Method|Type|Locator|\n" + "|--|--|--|--|");
                            isNewCategory = false;
                        }
                        split[i] = split[i].replace(".rs", "");
                        category.append("\n|").append(split[i]);
                    }
                }
                DocumentBuilder builder = domFactory.newDocumentBuilder();
                Document dDoc = builder.parse(s);

                XPath xPath = XPathFactory.newInstance().newXPath();
                Node node = (Node) xPath.evaluate("/WebElementEntity/selectorMethod", dDoc, XPathConstants.NODE);
                if (node != null) {
                    String nodeLocator = node.getTextContent();
                    var node1 = (NodeList) xPath.evaluate("/WebElementEntity/selectorCollection/entry", dDoc, XPathConstants.NODESET);

                    for (int i = 0; i < node1.getLength(); i++) {
                        String temp = node1.item(i).getTextContent();
                        if (temp.contains(nodeLocator)) {
                            //System.out.println(temp);
                            temp = temp.replace(nodeLocator + "\n", "");
                            category.append("|").append(nodeLocator).append("|");
                            temp = temp.replace("\n", "");
                            if (temp.contains("${")) {
                                category.append("Dynamic|");
                            } else {
                                category.append("Static|");
                            }
                            category.append("`").append(temp).append("`|");
                        }

                    }
                    finalOutput += category;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finalOutput = finalOutput.replaceAll("  +", "");
        //System.out.println(finalOutput);
        String now = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        try {
            PrintWriter out = new PrintWriter("result\\" + now + ".txt");
            out.println(finalOutput);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}