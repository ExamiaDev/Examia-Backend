package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.Map;

/**
 * Nodo React Flow del árbol de decisión.
 * type: "decision" | "process" | "terminal"
 * position: {"x": double, "y": double}
 * data: {"label": string}
 */
@Data
@Builder
@NoArgsConstructor(onConstructor_ = {@PersistenceCreator})
@AllArgsConstructor
public class DecisionTreeNode {
    private String id;
    private String type;
    private Map<String, Double> position;
    private Map<String, String> data;
}
