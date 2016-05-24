package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A assignment marking assistant designed to make marking student code easier
 * Compares two jars based on required submission guidelines and compares
 * student version with original version
 * 
 * Also detects additional java files submitted and prints them out
 * 
 * @author Kai Yang
 *
 */
public class Marker {

	public final String dir = "marking/"; // folder name
	private List<String> files;
	int addFiles = 0;
	int missingFiles = 0;

	private List<String> missing;
	private List<String> additional;
	public Marker() {
		files = new ArrayList<String>();
		missing = new ArrayList<String>();
		additional = new ArrayList<String>();
	}

	/**
	 * Unzips the master jar to working directory and adds all files to filelist
	 * 
	 * @param filename
	 */
	private void unzipMasterJar(String filename) {
		try {
			// Open the zip file
			ZipFile zipFile = new ZipFile(filename);
			Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();

				String name = zipEntry.getName();

				// Do we need to create a directory ?
				File file = new File(name);
				if (name.endsWith("/")) {
					file.mkdirs();
					continue;
				}

				File parent = file.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}

				// Extract the file
				InputStream is = zipFile.getInputStream(zipEntry);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = is.read(bytes)) >= 0) {
					fos.write(bytes, 0, length);
				}
				if (getFileExtension(file).equals("java")) {
					files.add(name);
				}
				is.close();
				fos.close();

			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Master unzip done");

	}

	/**
	 * Unzips a zip(or jar) file to a specified directory affix
	 * 
	 * @param filename
	 *            zip file to be unzipped
	 * @param affix
	 *            folder to unzip it to
	 */
	public void unzipJar(String filename, String affix) {

		try {
			// Open the zip file
			ZipFile zipFile = new ZipFile(filename);
			Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();

				String name = affix + zipEntry.getName();

				// Do we need to create a directory ?
				File file = new File(name);
				if (name.endsWith("/")) {
					file.mkdirs();
					continue;
				}

				File parent = file.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}

				// Extract the file
				InputStream is = zipFile.getInputStream(zipEntry);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = is.read(bytes)) >= 0) {
					fos.write(bytes, 0, length);
				}
				is.close();
				fos.close();

			}
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Student unzip done");
	}

	/**
	 * Runs diff on each file to find differences
	 */
	private void diffFiles() {
		for (String s : files) {
			System.out.println(s);
			File studentCurrent = new File(dir+s);
			if(studentCurrent.exists())	Diff.diff(new String[] { s, dir + s });
			else {
				System.out.println("ERROR: This file was not present in student submission");
				missing.add(s);
				missingFiles++;
			}
			
			System.out.println("================END OF FILE================");
			System.out.println("");
		}
	}

	/**
	 * Recursively goes through a directory to find all java files not present
	 * in mark scheme
	 * 
	 * @param folder
	 * @throws FileNotFoundException
	 */
	private void findExtras(File folder) throws FileNotFoundException {
		File[] fileList = folder.listFiles();
		for (File f : fileList) {
			String filePath = f.getPath();
			String[] filePathSplit = filePath.split("\\\\|/");
			ArrayList<String> filePathSplitList = new ArrayList(Arrays.asList(filePathSplit));
			filePathSplitList.remove(0);
			filePath = String.join("/", filePathSplitList);

			if (filePath.endsWith("java") && !files.contains(filePath)) {
				System.out.println(filePath);
				additional.add(filePath);
				Scanner sc = new Scanner(f);
				addFiles++;
				while (sc.hasNextLine()) {
					System.out.println(sc.nextLine());
				}
				System.out.println("===========END OF FILE===========");
				System.out.println("");
				sc.close();
			}
			if (f.isDirectory()) {
				findExtras(f);
			}
		}
	}

	/**
	 * Gets a file extension
	 * 
	 * @param file
	 * @return
	 */
	private static String getFileExtension(File file) {
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		else
			return "";
	}

	/**
	 * Recursively deletes files
	 * @param f
	 * @throws IOException
	 */
	private void delete(File f) throws IOException {
		System.out.println(f);
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if(!f.delete()){
			System.out.println("failed to delete file " +f.getPath());
		}
	}
	
	private void printResults(){
		System.out.println("Parsing complete. \nNumber of files compared : " + files.size());
		System.out.println("Number of additional files: " + addFiles);
		if(addFiles!=0){
			System.out.println("Additional filenames: ");
			for(String s:additional){
				System.out.println(s);
			}
		}
		System.out.println("Number of missing files : "+missingFiles);
		if(missingFiles!=0){
			System.out.println("Missing filenames: ");
			for(String s:missing){
				System.out.println(s);
			}
		}
	}

	/**
	 * Diffs two jars based on the filestructure, supply it with a file
	 * containing target filestructure(one provided as example without
	 * whitespace), name of master jar(dave's copy), name of student jar
	 *
	 * @param args
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		// default debugging case
		if (args.length == 0) {
			System.out.println("Warning no arguments supplied, defaulting to debug arguments");
			args = new String[] { "master.jar", "student.jar" };
		}
		// create marker
		Marker marker = new Marker();
		// create marking directory
		File directory = new File(marker.dir);
		

		// attempt to cleanup previous students data
		try {
			System.out.println("Deleting previous files:");
			marker.delete(directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//create new marking directory
		directory.mkdir();
		// processing arguments
		marker.unzipMasterJar(args[0]);
		marker.unzipJar(args[1], marker.dir);
		// marker.generateList();
		// diffing files
		marker.diffFiles();
		// marker.cleanup();
		System.out.println("Detecting additional files...:");
		marker.findExtras(directory);
		System.out.println("Finished detecting additional files");

		System.out.println("====================");
		marker.printResults();

	}

}
