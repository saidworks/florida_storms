/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cyclone {
    private HeaderLine header;
    private List<DataLine> dataLines;

    public Cyclone(HeaderLine header) {
        this.header = header;
        this.dataLines = new ArrayList<>();
    }

    /**
     * Verifies if the number of data lines matches the count in the header
     */
    public boolean isDataCompletePerHeader() {
        return dataLines.size() == header.getEntriesCount();
    }
}
