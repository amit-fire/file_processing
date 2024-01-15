package com.file_processing.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.file_processing.FileProcessingConstants;
import com.file_processing.tail.ReadDetails;
import com.file_processing.tail.Tail;

@Service
public class FileProcessingService {

	private static Logger logger = LoggerFactory.getLogger(FileProcessingService.class);

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

		logger.info("file: " + fileName + " numberOfEntries: " + numberOfEntries
				+ " numberOfLinesToRead: " + numberOfLinesToRead);

		String filePath = logDir + fileName;
		ReadDetails details = tail.readLines(filePath, numberOfLinesToRead, stringToFind, 0);

		logger.info("details 1: " + getMsg(details));
		System.out.println("details1: " + getMsg(details));
		List<String> results = new ArrayList<>();
		results.addAll(details.getEntries());

		boolean read = true;
		if (details.getEntries().isEmpty()) {
			read = false;
			logger.info("read 1: " + read);
		}

		if (remainingNumberOfEntriesToRead != null) {
			remainingNumberOfEntriesToRead -= details.getEntries().size();
			logger.info("remainingNumberOfEntriesToRead 1: " + remainingNumberOfEntriesToRead);
			if (remainingNumberOfEntriesToRead < 1) {
				read = false;
				logger.info("read 2: " + read);
			}
		}

		while (read) {
			details = tail.readLines(filePath, numberOfLinesToRead, stringToFind, details.getBytesRead());
			logger.info("details 2: " + getMsg(details));
			if (details.getEntries().isEmpty()) {
				read = false;
				logger.info("read 3: " + read);
			} else {
				if (remainingNumberOfEntriesToRead != null) {
					remainingNumberOfEntriesToRead -= details.getEntries().size();
					logger.info("remainingNumberOfEntriesToRead 2: " + remainingNumberOfEntriesToRead);
					if (remainingNumberOfEntriesToRead < 1) {
						int entries = numberOfEntries - results.size();
						logger.info("sub list entries: " + entries);
						List<String> subList = details.getEntries().subList(0, entries);
						results.addAll(subList);
						read = false;
						logger.info("read 4: " + read);
					} else {
						results.addAll(details.getEntries());
						read = !details.getEntries().isEmpty();
						logger.info("read 5: " + read);
					}
				} else {
					results.addAll(details.getEntries());
					read = !details.getEntries().isEmpty();
					logger.info("read 6: " + read);
				}
			}
		}

		return results;
	}

	private String getMsg(ReadDetails details) {
		return details == null ? "details null"
				: details.getEntries() == null ? "entries null" : "entries = " + details.getEntries().size();

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
