package com.file_processing.tail;

import static java.nio.file.StandardOpenOption.READ;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.InvalidPathException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Tail {

	private static Logger logger = LoggerFactory.getLogger(Tail.class);

	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final char NEW_LINE = 10;

	@Value("${buffer_size}")
	private int defaultBufferSize = 1;

	/**
	 * Read a number of lines from bottom of file
	 * 
	 * @see Tail https://en.wikipedia.org/wiki/Tail_(Unix)
	 * @param path          a string represents for path to file
	 * @param numberOfLines an integer number indicates number of lines to be
	 *                      retrieved
	 * @return a list of string represents for lines
	 *
	 * @throws InvalidPathException if file does not exist
	 * @throws IOException          If an I/O error occurs
	 */
	public ReadDetails readLines(String path, int numberOfLines, String stringToFind, long startFrom)
			throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			throw new InvalidPathException(path, "File does not exist");
		}

		List<String> lines = new LinkedList<>();
		FileChannel fileChannel = FileChannel.open(file.toPath(), READ);
		long fileSize = fileChannel.size();
		long readBytes = startFrom;
		if (fileSize == 0) {
			return new ReadDetails(lines, readBytes);
		}

		int bufferSize = defaultBufferSize;
		long position;
		ByteBuffer copy;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		do {
			if (bufferSize > fileSize) {
				bufferSize = (int) fileSize;
				position = 0;
			} else {
				position = fileSize - (readBytes + bufferSize);
				if (position < 0) {
					position = 0;
					bufferSize = (int) (fileSize - readBytes);
				}
			}

			copy = ByteBuffer.allocate(bufferSize);
			readBytes += fileChannel.read(copy, position);

			readChunk(copy.array(), out, lines, numberOfLines, stringToFind);
		} while (lines.size() < numberOfLines && position > 0);
		if (out.size() > 0) {
			flushLine(lines, out, stringToFind);
		}
		out.close();
		fileChannel.close();

		return new ReadDetails(lines, readBytes);
	}

	private void readChunk(byte[] bytes, ByteArrayOutputStream out, List<String> lines, int numberOfLines,
			String stringToFind) throws IOException {
		/*
		 * offset in original code is set to -1. if there's a newline at the end of the
		 * file, tmp.write in prependBuffer throws IndexOutOfBoundsException
		 */
		int offset = 0, limit = 0;
		boolean flush = false;
		for (int i = bytes.length - 1; i >= 0 && lines.size() < numberOfLines; i--) {
			byte b = bytes[i];
			if (b == NEW_LINE) {
				logger.info("flush ***" + b + "***");
				flush = true;
			} else {
				offset = i;
				limit++;
			}
			if (flush) {
				prependBuffer(out, bytes, offset, limit);
				flushLine(lines, out, stringToFind);
				limit = 0;
				flush = false;
			}
		}

		if (limit > 0) {
			// in case we have some characters left without in one line
			// we add them to buffer in front of the others
			prependBuffer(out, bytes, offset, limit);
		}
	}

	private void flushLine(List<String> lines, ByteArrayOutputStream out, String stringToFind) {
		try {
			addLines(out.toString(DEFAULT_CHARSET), stringToFind, lines);
		} catch (UnsupportedEncodingException e) {
			addLines(out.toString(), stringToFind, lines);
		}

		out.reset();
	}

	private void addLines(String s, String stringToFind, List<String> lines) {
		/*
		 * This function was modified from the original, to:
		 * 
		 * 1. ignore empty lines
		 * 
		 * 2. handle matching strings
		 */
		s = s.strip();

		boolean addLine = shouldLineBeAdded(s, stringToFind);
		logger.info("add line '" + s + "' ? " + addLine);
		if (addLine) {
			lines.add(s);
		}
	}

	private boolean shouldLineBeAdded(String s, String stringToFind) {
		return !s.isEmpty() && (stringToFind == null || stringToFind.isEmpty()
				|| (!stringToFind.isEmpty() && s.contains(stringToFind)));
	}

	private void prependBuffer(ByteArrayOutputStream out, byte[] bytes, int offset, int limit) throws IOException {
		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		tmp.write(bytes, offset, limit);
		out.writeTo(tmp);
		out.reset();
		tmp.writeTo(out);
	}

}
