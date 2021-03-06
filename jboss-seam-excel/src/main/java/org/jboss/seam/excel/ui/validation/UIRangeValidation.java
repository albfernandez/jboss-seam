package org.jboss.seam.excel.ui.validation;

import org.jboss.seam.excel.ui.ExcelComponent;

public class UIRangeValidation extends ExcelComponent implements Validation {
	public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.validation.UIRangeValidation";

	private Integer startColumn;
	private Integer startRow;
	private Integer endColumn;
	private Integer endRow;

	public UIRangeValidation() {
		super();
	}
	
	public Integer getStartColumn() {
		return (Integer) valueOf("startColumn", startColumn);
	}

	public void setStartColumn(Integer startColumn) {
		this.startColumn = startColumn;
	}

	public Integer getStartRow() {
		return (Integer) valueOf("startRow", startRow);
	}

	public void setStartRow(Integer startRow) {
		this.startRow = startRow;
	}

	public Integer getEndColumn() {
		return (Integer) valueOf("endColumn", endColumn);
	}

	public void setEndColumn(Integer endColumn) {
		this.endColumn = endColumn;
	}

	public Integer getEndRow() {
		return (Integer) valueOf("endRow", endRow);
	}

	public void setEndRow(Integer endRow) {
		this.endRow = endRow;
	}

	@Override
	public String getFamily() {
		return COMPONENT_TYPE;
	}

	@Override
	public ValidationType getType() {
		return ValidationType.range;
	}
}
