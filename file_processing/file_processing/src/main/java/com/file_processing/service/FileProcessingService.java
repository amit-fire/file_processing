package com.file_processing.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.file_processing.FileProcessingConstants;
import com.file_processing.tail.ReadDetails;
import com.file_processing.tail.Tail;

@Service
public class FileProcessingService {

	private Tail tail;
	private String logDir;

	@Autowired
	FileProcessingService(Tail tail) {
		this.tail = tail;
		logDir = FileProcessingConstants.LOG_DIR;
	}

	// for testing
	FileProcessingService(Tail tail, String logDir) {
		this.tail = tail;
		this.logDir = logDir;
	}

	public List<String> getEntries(String fileName, Integer numberOfEntries, String stringToFind) throws IOException {
		System.out.println("collecting entries for file " + fileName);
		Integer remainingNumberOfEntriesToRead = numberOfEntries;
		int numberOfLinesToRead = calculateNumberOfLinesToRead(numberOfEntries);

		String filePath = logDir + fileName;
		ReadDetails details = tail.readLines(filePath, numberOfLinesToRead, stringToFind, 0);

		List<String> results = new ArrayList<>();
		results.addAll(details.getEntries());

		boolean read = true;
		if (details.getEntries().isEmpty()) {
			read = false;
		}

		if (remainingNumberOfEntriesToRead != null) {
			remainingNumberOfEntriesToRead -= details.getEntries().size();
			if (remainingNumberOfEntriesToRead < 1) {
				read = false;
			}
		}

		while (read) {
			details = tail.readLines(filePath, numberOfLinesToRead, stringToFind, details.getBytesRead());

			if (details.getEntries().isEmpty()) {
				read = false;
			} else {
				if (remainingNumberOfEntriesToRead != null) {
					remainingNumberOfEntriesToRead -= details.getEntries().size();
					if (remainingNumberOfEntriesToRead < 1) {
						int entries = numberOfEntries - results.size();
						List<String> subList = details.getEntries().subList(0, entries);
						results.addAll(subList);
						read = false;
					} else {
						results.addAll(details.getEntries());
						read = !details.getEntries().isEmpty();
					}
				} else {
					results.addAll(details.getEntries());
					read = !details.getEntries().isEmpty();
				}
			}
		}

		return results;
	}

	/*
	 * ideally, fetched rows should be sent in batches. this is relevant if
	 * numberOfEntries is large.
	 * 
	 * this can be achieved by pagination.
	 * 
	 * 1. the consumer keeps making requests, each time with the next batch of rows.
	 * note that the Tail class needs to be modified for this case, to return the
	 * line number from which to continue reading. it currently returns number of
	 * bytes read, but in order to make it usable, line number should be returned.
	 * 
	 * 2. consumer provides a callback url (or maybe the same url from which the
	 * request originated) and the service sends the data in batches.
	 */
	private int calculateNumberOfLinesToRead(Integer numberOfEntries) {
		int numberOfLinesToRead = FileProcessingConstants.NUMBER_OF_LINES_TO_READ;
		if (numberOfEntries != null && numberOfEntries > 0) {
			if (numberOfEntries > numberOfLinesToRead) {
				numberOfEntries = numberOfLinesToRead;
				return numberOfEntries;
			} else {
				return numberOfEntries;
			}
		}

		return numberOfLinesToRead;
	}

}
