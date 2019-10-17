package de.mclonips.clubadministration.capella.entity;

import de.mclonips.clubadministration.capella.entity.type.NoteBindingType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Note extends Element {

    private boolean hasDot;
    private NoteBindingType noteBindingType;
    private CellStyle cellStyle;


    public Note(final String duration, final String pitch) {
        this(duration, pitch, false);
    }

    public Note(final String duration, final String pitch, final boolean hasDot) {
        this(duration, pitch, hasDot, NoteBindingType.NO_BINDING);
    }

    public Note(final String duration, final String pitch, final boolean hasDot, final NoteBindingType typ) {
        super(Duration.getByLength(duration), pitch);

        this.hasDot = hasDot;
        this.noteBindingType = typ;

    }

    @Override
    public Cell addToValueCell(final Cell cell, final CellStyle cellStyle) {
        //Cell-Style for value-cells
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);

        cell.setCellStyle(cellStyle);

        cell.setCellValue(this.value.getValue());

        return cell;
    }

    @Override
    public Cell addToHeaderCell(final Cell headerCell, final CellStyle cellStyle) {
        headerCell.setCellValue(this.length.getValue());

        return headerCell;
    }
}
