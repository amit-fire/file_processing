package com.file_processing.validation;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RequestParamValidator {

	@Value("${log_dir}")
	private String logDir;

	// TODO: externalize messages
	public void validateFileName(String fileName) throws Exception {
		File f = new File(logDir + fileName);
		if (!f.isFile()) {
			throw new Exception("fileName " + fileName + " does not exist");
		}
	}

	public void validateNumberOfEntries(Integer numberOfEntries) throws Exception {
		if (numberOfEntries != null && numberOfEntries < 1) {
			throw new Exception("numberOfEntries (received: " + numberOfEntries + ") should be a positive number");
		}
	}

}
