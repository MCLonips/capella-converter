package de.mclonips.clubadministration.capella.entity;

import com.google.common.base.Strings;
import de.mclonips.clubadministration.capella.entity.type.ElementValue;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

@Data
public abstract class Element {

    protected final Duration length;
    protected final ElementValue value;

    public Element() {
        this(Duration.FULL, ElementValue.NONE);
    }

    public Element(final Duration duration, final String valueString) {
        this(duration, Strings.isNullOrEmpty(valueString) ? ElementValue.NONE : ElementValue.valueOf(valueString.replace('#', 'C')));
    }

    public Element(final Duration length, final ElementValue value) {
        this.length = length;
        this.value = value;
    }

    public abstract Cell addToValueCell(final Cell cell, final CellStyle cellStyle);

    public abstract Cell addToHeaderCell(final Cell headerCell, final CellStyle cellStyle);
}
