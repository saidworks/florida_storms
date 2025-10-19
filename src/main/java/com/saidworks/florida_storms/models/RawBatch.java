/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a batch of raw file lines before parsing
 */
@Data
@Builder
public class RawBatch {
    private int batchId;
    private List<String> lines;
    private int startLineNumber;
    private int endLineNumber;
}
