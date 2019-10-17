package de.mclonips.clubadministration.capella.entity;

import de.mclonips.clubadministration.capella.entity.type.ElementValue;
import de.mclonips.clubadministration.capella.entity.type.RepetitionTyp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Repetition extends Element {

    private RepetitionTyp type;

    public Repetition(final RepetitionTyp type) {
        super(Duration.FULL, ElementValue.REPETITION);
        this.type = type;
    }

    @Override
    public Cell addToValueCell(final Cell cell, final CellStyle cellStyle) {
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        cellStyle.setBorderTop(BorderStyle.THICK);
        cellStyle.setBorderBottom(BorderStyle.THICK);

        if (this.type == RepetitionTyp.START) {
            cellStyle.setBorderLeft(BorderStyle.THICK);
        } else {
            cellStyle.setBorderRight(BorderStyle.THICK);
        }

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
