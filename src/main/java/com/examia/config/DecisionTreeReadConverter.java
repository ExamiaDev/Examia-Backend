package com.examia.config;

import com.examia.model.DecisionTreeBranch;
import com.examia.model.DecisionTreeDefinition;
import com.examia.model.DecisionTreeNode;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReadingConverter
public class DecisionTreeReadConverter implements Converter<Document, DecisionTreeDefinition> {

    @Override
    public DecisionTreeDefinition convert(Document source) {
        List<DecisionTreeNode> nodes = new ArrayList<>();
        List<DecisionTreeBranch> edges = new ArrayList<>();

        Object nodesValue = source.get("nodes");
        if (nodesValue instanceof List<?> nodesList) {
            for (Object nodeObj : nodesList) {
                if (nodeObj instanceof Document nodeDoc) {
                    nodes.add(readNode(nodeDoc));
                }
            }
        }
        // Si nodes es un Document (formato viejo tipo mapa), lo ignoramos y dejamos lista vacía.

        Object edgesValue = source.get("edges");
        if (edgesValue instanceof List<?> edgesList) {
            for (Object edgeObj : edgesList) {
                if (edgeObj instanceof Document edgeDoc) {
                    edges.add(readEdge(edgeDoc));
                }
            }
        }

        return DecisionTreeDefinition.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    private DecisionTreeNode readNode(Document doc) {
        String id = doc.getString("_id") != null ? doc.getString("_id") : doc.getString("id");
        String type = doc.getString("type");

        Map<String, Double> position = new HashMap<>();
        Object posObj = doc.get("position");
        if (posObj instanceof Document posDoc) {
            posDoc.forEach((k, v) -> {
                if (v instanceof Number n) position.put(k, n.doubleValue());
            });
        }

        Map<String, String> data = new HashMap<>();
        Object dataObj = doc.get("data");
        if (dataObj instanceof Document dataDoc) {
            dataDoc.forEach((k, v) -> data.put(k, v != null ? v.toString() : null));
        }

        return DecisionTreeNode.builder()
                .id(id)
                .type(type)
                .position(position)
                .data(data)
                .build();
    }

    private DecisionTreeBranch readEdge(Document doc) {
        String id = doc.getString("_id") != null ? doc.getString("_id") : doc.getString("id");
        return DecisionTreeBranch.builder()
                .id(id)
                .source(doc.getString("source"))
                .target(doc.getString("target"))
                .label(doc.getString("label"))
                .build();
    }
}
