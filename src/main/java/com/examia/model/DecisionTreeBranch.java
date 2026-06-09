package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;

/**
 * Arista (edge) React Flow del árbol de decisión.
 */
@Data
@Builder
@NoArgsConstructor(onConstructor_ = {@PersistenceCreator})
@AllArgsConstructor
public class DecisionTreeBranch {
    private String id;
    private String source;
    private String target;
    private String label;
}
