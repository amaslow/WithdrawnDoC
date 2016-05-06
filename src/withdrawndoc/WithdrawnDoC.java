package withdrawndoc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WithdrawnDoC {

    static String productContent = "G:/Product Content/PRODUCTS";

    public static void main(String[] args) throws IOException {

        File dir = new File(productContent);
        File[] subDirs = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        List<Integer> datesList = new ArrayList<Integer>();
        Map<Integer, File> datesList1 = new HashMap<Integer, File>();

        for (File subDir : subDirs) {
            File[] subFiles = subDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && (pathname.getName().startsWith("DoC_") || pathname.getName().startsWith("testDoC_"));
                }
            });
            if (subFiles.length > 1) {
                FileWriter fw = new FileWriter("H:/Logs/WithdrawnDoC.log", true);
                BufferedWriter bw = new BufferedWriter(fw);
                DateFormat dateFormater = new SimpleDateFormat("dd.MM.yyyy");
                String modDate = dateFormater.format(new Date());
                bw.newLine();
                bw.write(modDate);

                for (int i = 0; i < subFiles.length; i += 1) {
                    try {
                        int dateDoc = Integer.parseInt(subFiles[i].toString().substring(subFiles[i].toString().length() - 12, subFiles[i].toString().length() - 4));
                        datesList1.put(dateDoc, subFiles[i]);
                        datesList.add(dateDoc);
                    } catch (NumberFormatException e) {
                    }
                }
                if (datesList.size() > 0) {
                    int max = datesList.get(0);
                    for (int counter = 1; counter < datesList.size(); counter++) {
                        if (datesList.get(counter) > max) {
                            max = datesList.get(counter);
                        }
                    }
                    Set<Integer> keys = datesList1.keySet();
                    for (Integer i : keys) {
                        if (!datesList1.get(i).equals(datesList1.get(max))) {
                            String folder = datesList1.get(i).getAbsoluteFile().getParent();
                            String fileName = datesList1.get(i).getName();
                            FileRename(bw, folder, fileName);
                        }
                    }
                    datesList.clear();
                    datesList1.clear();
                }
                bw.newLine();
                bw.write("----------------------------------------------------");
                bw.flush();
                bw.close();
            }
        }
    }

    private static void FileRename(BufferedWriter bw, String folderName, String fileName) throws IOException {
        boolean existed = new File(folderName + "/" + fileName).exists();
        if (existed) {
            boolean rename = new File(folderName + "/" + fileName).renameTo(new File(folderName + "/repealed_" + fileName));
            if (rename == true) {
                System.out.println("\t" + new File(folderName + "/" + fileName) + " renamed into: " + new File(folderName + "/repealed_" + fileName));
                bw.newLine();
                bw.write("\t" + new File(folderName + "/" + fileName) + " renamed into: " + new File(folderName + "/repealed_" + fileName));
            } else {
                System.out.println("\t" + fileName + " not renamed !!!");
                bw.newLine();
                bw.write("\t" + fileName + " not renamed !!!");
            }
        } else {
            System.out.println(fileName + " not exists !!!");
            bw.newLine();
            bw.write(fileName + " not exists !!!");
        }
    }
}
