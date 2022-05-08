package com.wordpress.brancodes.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableBuilder {

	int height;
	int width;
	private List<String> rowTitles;
	private List<String> columnTitles;
	private List<List<String>> data;
	private String columnBuffer;
	private String rowBuffer;
	private String intersectBuffer;
	private boolean rightAligned;

	public TableBuilder(Object[] rowTitles, Object[] columnTitles, Object[][] data, Object columnBuffer, Object rowBuffer, Object intersectBuffer, boolean rightAligned) {
		height = rowTitles.length;
		width = columnTitles.length;
		this.rowTitles = Arrays.stream(rowTitles).map(o -> o == null ? "" : o.toString()).collect(Collectors.toList());
		this.columnTitles = Arrays.stream(columnTitles).map(o -> o == null ? "" : o.toString()).collect(Collectors.toList());
		this.data = Arrays.stream(data)
						  .map(col -> Arrays.stream(col)
											.map(Object::toString)
											.collect(Collectors.toList()))
						  .collect(Collectors.toList());
		this.columnBuffer = columnBuffer == null ? "" : columnBuffer.toString();
		this.rowBuffer = rowBuffer == null ? "" : rowBuffer.toString();
		this.intersectBuffer = intersectBuffer == null ? "" : intersectBuffer.toString();
		this.rightAligned = rightAligned;

		if (data.length != height || (!(data.length > 0) || data[0].length != width))
			throw new IllegalArgumentException();

	}

	public TableBuilder(Object[] rowTitles, Object[] columnTitles, Object[][] data) {
		this(rowTitles, columnTitles, data, null, null, null, false);
	}

	public String create() {
		StringBuilder str = new StringBuilder();
		int rowTitlesLength = rowTitles.stream().map(String::length).max(Integer::compareTo).orElse(0);
		int[] colLengths = data.stream()
							   .mapToInt(column -> column.stream()
														 .map(String::length)
														 .max(Integer::compareTo)
														 .orElse(0))
							   .toArray();

		// columnTitles.stream().map(title -> " ".repeat(columnTitlestitle.length()))
		return str.toString();
	}

}
