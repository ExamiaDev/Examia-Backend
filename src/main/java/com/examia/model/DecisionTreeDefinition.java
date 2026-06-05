package com.examia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * Árbol de decisión en formato React Flow:
 * nodes: lista de DecisionTreeNode
 * edges: lista de DecisionTreeBranch (aristas)
 */
@Data
@Builder
@NoArgsConstructor(onConstructor_ = {@PersistenceCreator})
@AllArgsConstructor
public class DecisionTreeDefinition {
    @Builder.Default
    private List<DecisionTreeNode> nodes = new ArrayList<>();
    @Builder.Default
    private List<DecisionTreeBranch> edges = new ArrayList<>();
}
