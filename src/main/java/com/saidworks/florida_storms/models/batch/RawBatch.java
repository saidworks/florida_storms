/* (C) Said Zitouni 2025 */
package com.saidworks.florida_storms.models.batch;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Represents a batch of raw file lines before parsing (immutable value object).
 */
@Value
@Builder
public class RawBatch {
    private int batchId;
    private List<String> lines;
    private int startLineNumber;
    private int endLineNumber;
}
