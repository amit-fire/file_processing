package com.file_processing.tail;

import java.util.List;

public class ReadDetails {

	private List<String> entries;
	private long bytesRead;

	public ReadDetails(List<String> entries, long bytesRead) {
		this.entries = entries;
		this.bytesRead = bytesRead;
	}

	public List<String> getEntries() {
		return entries;
	}

	public long getBytesRead() {
		return bytesRead;
	}

}
