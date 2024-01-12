package com.file_processing.generate_file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateFile {

	public static void main(String[] args) {
		String f = "output file path";
		long fileSize = 0;
		// fileSize=2000000000 to generate 3GB file
		write(f, fileSize);
	}

	static void write(String fileName, long fileSize) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			generate(writer, fileSize);
			System.out.println("file created successfully");
		} catch (IOException e) {
			System.err.println("error creating file: " + e.getMessage());
		}
	}

	static void generate(BufferedWriter writer, long fileSize) throws IOException {
		Random random = new Random();
		long currentSize = 0;
		int v = 0;
		while (currentSize < fileSize) {
			String randomData = Long.toHexString(random.nextLong());
			writer.write(String.valueOf(v) + " " + randomData);
			writer.newLine();
			currentSize += randomData.length();
			v++;
		}
		System.out.println(v);
	}

}
