package com.file_processing.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.file_processing.service.FileProcessingService;
import com.file_processing.validation.RequestParamValidator;

@RestController
@RequestMapping("/app")
public class FileProcessingController {

	private FileProcessingService fileProcessingService;
	private RequestParamValidator validator;

	@Autowired
	FileProcessingController(FileProcessingService fileProcessingService, RequestParamValidator validator) {
		this.fileProcessingService = fileProcessingService;
		this.validator = validator;
	}

	@GetMapping("/entries")
	/*
	 * in case files in a different folder (not var/log) need to be read, fileName
	 * can be changed to filePath or filePath can be added as an optional query
	 * parameter and the default will be var/log
	 * 
	 */
	ResponseEntity<?> getEntries(@RequestParam(required = true) String fileName,
			@RequestParam(required = false) Integer numberOfEntries,
			@RequestParam(required = false) String stringToFind) {
		System.out.println("request received for fileName=" + fileName + " numberOfEntries=" + numberOfEntries
				+ " stringToFind=" + stringToFind);
		try {
			System.out.println("validating query parameters");
			validator.validateFileName(fileName);
			validator.validateNumberOfEntries(numberOfEntries);
		} catch (Exception e) {
			System.out.println("query parameters are not valid. exception " + e.getMessage());
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		try {
			List<String> entries = fileProcessingService.getEntries(fileName, numberOfEntries, stringToFind);
			return new ResponseEntity<List<String>>(entries, HttpStatus.OK);
		} catch (IOException e) {
			System.out.println("error collecting entries");
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
