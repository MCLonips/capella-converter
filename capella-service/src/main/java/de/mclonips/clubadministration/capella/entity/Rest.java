package de.mclonips.clubadministration.capella.entity;

import de.mclonips.clubadministration.capella.entity.type.ElementValue;
import lombok.EqualsAndHashCode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;

@EqualsAndHashCode(callSuper = true)
public class Rest extends Element {

    public Rest(final String length) {
        super(Duration.getByLength(length), ElementValue.NONE);
    }

    @Override
    public Cell addToValueCell(final Cell cell, final CellStyle cellStyle) {
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        cell.setCellStyle(cellStyle);

        cell.setCellValue(this.value.getValue());

        return cell;
    }

    @Override
    public Cell addToHeaderCell(final Cell headerCell, final CellStyle cellStyle) {
        headerCell.setCellValue("");
        return headerCell;
    }
}
