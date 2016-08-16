package withdrawndoc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WithdrawnDoC {

    public static String productContent = "//172.16.55.197/design/Smartwares - Product Content/PRODUCTS";
    public static String certificates = "G:/QC/Certificates";
    static Connection con = null;
    static Statement st = null;
    static ResultSet rs = null;
    static String[][] Table = null;

    public static void main(String[] args) throws IOException {

        con = Utils.getConnection();

        try {
            st = con.createStatement();
            String SQL = "SELECT sap,item from elro.items;";
            rs = st.executeQuery(SQL);
            rs.last();
            int rowNumb = rs.getRow();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnS = rsmd.getColumnCount();
            rs.beforeFirst();
            Table = new String[rowNumb][columnS];
            int i = 0;
            int j = 0;
            while (rs.next() && i < rowNumb) {

                for (j = 0; j < columnS; j++) {
                    Table[i][j] = (rs.getString(j + 1));
                }
                i++;
            }
            i = j = 0;

        } catch (SQLException ex) {
            Logger.getLogger(WithdrawnDoC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Utils.closeDB(rs, st, con);
        }

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
            File[] docFiles = subDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && (pathname.getName().startsWith("DoC_" + subDir.getName()));
                }
            });
            if (docFiles.length > 0) {
                FileWriter fw = new FileWriter("H:/Logs/WithdrawnDoC.log", true);
                BufferedWriter bw = new BufferedWriter(fw);
                DateFormat dateFormater = new SimpleDateFormat("dd.MM.yyyy");
                String modDate = dateFormater.format(new Date());
                bw.newLine();
                bw.write(modDate);
                for (int i = 0; i < docFiles.length; i += 1) {
                    try {
                        int dateDoc = Integer.parseInt(docFiles[i].toString().substring(docFiles[i].toString().length() - 12, docFiles[i].toString().length() - 4));
                        datesList1.put(dateDoc, docFiles[i]);
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

                        String prodContFolder = datesList1.get(i).getAbsoluteFile().getParent();
                        String sap = datesList1.get(i).getParentFile().getName().substring(0, 2) + "." + datesList1.get(i).getParentFile().getName().substring(2, 5) + "." + datesList1.get(i).getParentFile().getName().substring(5, 7);
                        String fileName = datesList1.get(i).getName();
                        String itemNo = null;
                        for (int j = 0; j < Table.length; j++) {
                            if (Table[j][0].equals(sap)) {
                                itemNo = Table[j][1];
                                itemNo = itemNo.replace("/", "_");
                            }
                        }
                        File certDir = new File(certificates + "/" + itemNo);
                        if (!certDir.exists()) {
                            certDir.mkdirs();
                        }
                        File[] supplierDirs = certDir.listFiles(new FileFilter() {

                            @Override
                            public boolean accept(File pathname) {
                                return pathname.isDirectory();
                            }
                        });
                        String certFolder = null;
                        if (supplierDirs.length > 0) {
                            certFolder = supplierDirs[0].toString();
                        } else {
                            certFolder = certDir.toString();
                        }

                        if (!datesList1.get(i).equals(datesList1.get(max))) {
                            String doneFolder = certFolder + "/_Done";
                            boolean doneExists = new File(doneFolder).exists();
                            if (doneExists) {
                                FileRename(bw, prodContFolder, doneFolder, fileName);
                            } else {
                                System.out.println(doneFolder + " not exists !!!");
                                bw.newLine();
                                bw.write(doneFolder + " not exists !!!");
                                boolean doneCreate = new File(doneFolder).mkdirs();
                                if (doneCreate) {
                                    System.out.println(doneFolder + " cteated");
                                    bw.newLine();
                                    bw.write(doneFolder + " created");
                                    FileRename(bw, prodContFolder, doneFolder, fileName);
                                } else {
                                    System.out.println(doneFolder + " not cteated !!!");
                                    bw.newLine();
                                    bw.write(doneFolder + " not created !!!");
                                }
                            }
                        } else {
                            boolean destFileExists = new File(certFolder + "/" + fileName).exists();
                            if (!destFileExists) {
                                copyFile(bw, prodContFolder, certFolder, fileName);
                            }
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

    private static void FileRename(BufferedWriter bw, String oldFolderName, String newFolderName, String fileName) throws IOException {
        boolean rename = new File(oldFolderName + "/" + fileName).renameTo(new File(newFolderName + "/repealed_" + fileName));
        if (rename == true) {
            System.out.println("\t" + new File(oldFolderName + "/" + fileName) + " renamed into: " + new File(newFolderName + "/repealed_" + fileName));
            bw.newLine();
            bw.write("\t" + new File(oldFolderName + "/" + fileName) + " renamed into: " + new File(newFolderName + "/repealed_" + fileName));
        } else {
            System.out.println("\t" + fileName + " not renamed !!!");
            bw.newLine();
            bw.write("\t" + fileName + " not renamed !!!");
        }
    }

    private static void copyFile(BufferedWriter bw, String oldFolderName, String newFolderName, String fileName) throws IOException {
        File src = new File(oldFolderName + "/" + fileName);
        File dstFile = new File(newFolderName + "/" + fileName);

        InputStream in = null;
        try {
            in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dstFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            System.out.println("\t" + src + " copied into: " + dstFile);
            bw.newLine();
            bw.write("\t" + src + " copied into: " + dstFile);
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WithdrawnDoC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            in.close();
        }
    }
}
