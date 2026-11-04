package io.icaco.maven;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies the parameter default values from the generated plugin descriptor
 * (META-INF/maven/plugin.xml) to a mojo, the same way Maven does for parameters
 * that are not configured. Defaults that are expressions, like ${project}, are skipped.
 */
class MojoDefaults {

    static <T> T apply(T mojo, String goal) throws Exception {
        for (Element parameter : configuration(goal)) {
            String defaultValue = parameter.getAttribute("default-value");
            if (!defaultValue.isEmpty() && !defaultValue.contains("${"))
                set(mojo, parameter.getTagName(), defaultValue);
        }
        return mojo;
    }

    static Iterable<Element> configuration(String goal) throws Exception {
        try (InputStream descriptor = MojoDefaults.class.getResourceAsStream("/META-INF/maven/plugin.xml")) {
            NodeList mojos = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(descriptor)
                    .getElementsByTagName("mojo");
            for (int i = 0; i < mojos.getLength(); i++) {
                Element mojo = (Element) mojos.item(i);
                if (goal.equals(mojo.getElementsByTagName("goal").item(0).getTextContent()))
                    return elements(mojo.getElementsByTagName("configuration").item(0).getChildNodes());
            }
        }
        throw new IllegalArgumentException("Goal not found in plugin descriptor: " + goal);
    }

    static Iterable<Element> elements(NodeList nodes) {
        List<Element> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++)
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE)
                result.add((Element) nodes.item(i));
        return result;
    }

    static void set(Object mojo, String name, String value) throws Exception {
        Field field = field(mojo.getClass(), name);
        field.setAccessible(true);
        Class<?> type = field.getType();
        if (type == String.class)
            field.set(mojo, value);
        else if (type == boolean.class)
            field.setBoolean(mojo, Boolean.parseBoolean(value));
        else if (type == File.class)
            field.set(mojo, new File(value));
        else
            throw new IllegalArgumentException("Unsupported parameter type " + type + " for " + name);
    }

    static Field field(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass())
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // Look in the superclass
            }
        throw new NoSuchFieldException(name);
    }

}
