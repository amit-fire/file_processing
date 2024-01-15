package com.file_processing.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.file_processing.tail.Tail;

public class FileProcessingServiceTest {

	File filesDir = new File("src/test/resources/files");
	String filesDirPath = filesDir.getAbsolutePath() + File.separator;
	String fileName = "file.txt";

	FileProcessingService fileProcessing = new FileProcessingService(new Tail(), filesDirPath);

	@Test
	public void testReadEntireFile() throws IOException {
		List<String> entries = fileProcessing.getEntries(fileName, null, "");
		Assertions.assertEquals(28, entries.size());
	}

	@Test
	public void testEmptyFile() throws IOException {
		List<String> entries = fileProcessing.getEntries("empty.txt", null, "");
		Assertions.assertTrue(entries.isEmpty());
	}

	@Test
	public void testNumberOfLinesToReadIsLargerThanLChunkSize() throws IOException {
		List<String> entries = fileProcessing.getEntries(fileName, 123, "");
		Assertions.assertEquals(28, entries.size());
	}

	@Test
	public void testNumberOfLinesToReadIsSmallerThanChunkSize() throws IOException {
		List<String> entries = fileProcessing.getEntries(fileName, 3, "");
		Assertions.assertEquals(3, entries.size());
	}

	@Test
	public void testSearchText() throws IOException {
		List<String> entries = fileProcessing.getEntries(fileName, 5, "a");
		Assertions.assertEquals(3, entries.size());
	}

	@Test
	public void testNumberOfLinesToReadIsLargerThanLChunkSizeButSmallerThanLinesInFile() throws IOException {
		List<String> entries = fileProcessing.getEntries(fileName, 23, "");
		Assertions.assertEquals(23, entries.size());
	}
}
