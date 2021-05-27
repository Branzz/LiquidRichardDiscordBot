package com.wordpress.brancodes.test;

import java.io.File;
import java.net.URL;
import java.util.stream.Stream;
public class FileTesting {

	private static File[] getResourceFolderFiles(String folder) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource(folder);
		String path = url.getPath();
		File f = new File(path);
		System.out.println(f.isDirectory() + " " + f.isFile() + " " + f.exists() + " " + f.canRead());
		return new File(path).listFiles();
	}

	public static void main(String[] args) {
		// try {
		// 	for (File f: getResourceFolderFiles("")) {
		// 		System.out.println(f);
		// 	}
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }
		Stream.of("java.home", "user.name", "java.runtime.version", "os.name", "user.dir")
			  .map(System::getProperty)
			  .forEach(System.out::println);
	}

}
