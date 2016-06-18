package org.onehippo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.security.provider.MD5;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Unit test for simple HSTScaffold.
 */
// TODO pseudo codes
public class HSTScaffoldTest extends TestCase {

    // todo logging

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HSTScaffoldTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(HSTScaffoldTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    // Test Parsing
    public void testScaffoldRoutes() {
        Scaffold scaffold = Scaffold.instance();
        List<Route> routes = scaffold.getRoutes();
        assertEquals(routes.length(), 5);
    }

    public void testUrlParameter() {
        Scaffold scaffold = Scaffold.instance();
        List<Route> routes = scaffold.getRoutes();
        Map<String,String> urlParameters = routes.get(3).getUrlMatcher().getParameters();
        String type = urlParameters.get("id");
        assertEquals(type, "String");
    }

    public void testWildcardUrlParameter() {
        Scaffold scaffold = Scaffold.instance();
        List<Route> routes = scaffold.getRoutes();
        Map<String,String> urlParameters = routes.get(4).getUrlMatcher().getParameters();
        String type = urlParameters.get("path");
        assertEquals(type, "String");
    }

    public void testPages() {
        Scaffold scaffold = Scaffold.instance();
        List<Route> routes = scaffold.getRoutes();
        Page page = routes.get(0).getPage();
        List<Component> components = page.getComponents();

        assertEquals(components.length(), 3);

        Component main = components.get(1);
        assertEquals(main.getName(), "main");

        components = main.getComponents();

        assertEquals(components.length, 2);

        Component component = components.get(1);
        assertEquals(component.getTemplate(), "/some/path/text.ftl");
        assertEquals(component.getJavaClass(), "/some/path/TextComponent.java");
    }


    private Document loadXml(String fileName) {
        File componentsFile = new File(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(componentsFile);
        return doc;
    }


    private boolean validateComponentXml(Component component) throws XPathExpressionException {
        Document doc = loadXml("/path/to/components/containers.xml");

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile(xpathString); // e. g.: "//sv:node[@name='carousel']"
        NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        if (nodeList.getLength() == 1) {
            Node node = nodeList.item(0);
            NodeList childNodes = node.getChildNodes();

            // todo order
            // validate
            /*
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
            <sv:value>hst:containeritemcomponent</sv:value>
            </sv:property>
            <sv:property sv:name="hst:componentclassname" sv:type="String">
            <sv:value>org.onehippo.cms7.essentials.components.EssentialsCarouselComponent</sv:value>
            </sv:property>
            <sv:property sv:name="hst:template" sv:type="String">
            <sv:value>essentials-carousel</sv:value>
            </sv:property>
            <sv:property sv:name="hst:xtype" sv:type="String">
            <sv:value>HST.Item</sv:value>
            </sv:property>
            */

            String javaClass = component.getJavaClass();
            String template = component.getTemplate();

            Node child = childNodes.item(0);
            assertTrue("jcr:primaryType".equals(child.getAttributes().getNamedItem("sv:name"))
                    && "hst:containeritemcomponent".equals(child.getFirstChild().getNodeValue()));
            if (javaClass != null) {
                child = childNodes.item(1);
                assertTrue("hst:componentclassname".equals(child.getAttributes().getNamedItem("sv:name"))
                        && javaClass.equals(child.getFirstChild().getNodeValue()));
            }
            if (template != null) {
                child = childNodes.item(2);
                assertTrue("hst:template".equals(child.getAttributes().getNamedItem("sv:name"))
                        && template.equals(child.getFirstChild().getNodeValue()));
            }
            // ?
            child = childNodes.item(3);
            assertTrue("hst:xtype".equals(child.getAttributes().getNamedItem("sv:name"))
                    && "HST.Item".equals(child.getFirstChild().getNodeValue()));

        }

        return true;
    }


    // TODO test actual scaffolding, xml, ftl, files
    public void testComponents() {
        Scaffold scaffold = Scaffold.instance();

        try {
            // todo create a backup of files which are changed
            scaffold.build();

            for (Component component : component.getComponents()) {
                validateComponentXml(component);
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } finally {
            scaffold.rollback();
        }

    }

    public void testTemplates() {
        final Map<String, String> before = TestUtils.dirHash(new File("."));
        Scaffold scaffold = Scaffold.instance();

        try {
            scaffold.build();

            File componentsFile = new File("components.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(componentsFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            try {
                Document doc = loadXml("/file/to/templates.xml");
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();

                for (Template template : scaffold.getTemplates()) {

                    XPathExpression expr = xpath.compile(xpathString); // e. g.: "//sv:node[@name='carousel']"
                    NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                    if (nodeList.getLength() == 1) {
                        /*
                        <sv:node sv:name="base-top-menu">
                        <sv:property sv:name="jcr:primaryType" sv:type="Name">
                        <sv:value>hst:template</sv:value>
                        </sv:property>
                        <sv:property sv:name="hst:renderpath" sv:type="String">
                        <sv:value>webfile:/freemarker/gogreen/base-top-menu.ftl</sv:value>
                        </sv:property>
                        </sv:node>
                        */

                        Node node = nodeList.item(0);
                        NodeList childNodes = node.getChildNodes();

                        Node child = childNodes.item(0);
                        assertTrue("jcr:primaryType".equals(child.getAttributes().getNamedItem("sv:name"))
                                && "hst:template".equals(child.getFirstChild().getNodeValue()));

                        child = childNodes.item(1);
                        assertTrue("hst:renderpath".equals(child.getAttributes().getNamedItem("sv:name"))
                                && template.getRenderPath().equals(child.getFirstChild().getNodeValue()));

                        assertTrue((new File(template.getFile()).exists()));
                    }

                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        } finally {
            scaffold.rollback();
        }

        final Map<String, String> after = TestUtils.dirHash(new File("."));
        assertFalse(TestUtils.dirChanged(before, after));
    }

    private void testTemplateInclude(Component component) {
        Reader reader = new BufferedReader(new FileReader(new File((component.getTemplate()))));
        Scanner scanner = new Scanner(reader);

        assertTrue(scanner.hasNext(Pattern.compile("<@hst.include ref=\""+component.getName()+"\">")));

        for (Component component : component.getComponents()) {
            testTemplateInclude(component);
        }
    }

    public void testTemplateIncludes() {
        Scaffold scaffold = Scaffold.instance();

        try {
            scaffold.build();

            File componentsFile = new File("components.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(componentsFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // home(header,main(banner, text),footer)
            for (Page page : scaffold.getPages()) {
                List<Component> components = page.getComponents();
                for (Component component : components) {
                    // home template should contain header include, main include, footer include
                    testTemplateInclude(component);
                }
            }
        } catch(Exception e) {
            scaffold.rollback();
        }
    }

    // TODO project dir
    public void testDryRun() {
        final Map<String, String> before = TestUtils.dirHash(new File("."));
        Scaffold scaffold = Scaffold.instance();

        // persist data to dry run .scaffold/dryrun directory instead of project
        scaffold.dryRun();

        final Map<String, String> after = TestUtils.dirHash(new File("."));

        assertFalse(TestUtils.dirChanged(before, after));
    }

    // TODO project dir
    public void testRollback() {
        final Map<String, String> before = TestUtils.dirHash(new File("."));
        Scaffold scaffold = Scaffold.instance();

        scaffold.build();

        assertTrue(new File(".scaffold").exists());

        scaffold.rollback();

        final Map<String, String> after = TestUtils.dirHash(new File("."));

        assertFalse(TestUtils.dirChanged(before, after));
    }

    // TODO project dir
    public void testUpdateDryRun() {
        final Map<String, String> before = TestUtils.dirHash(new File("."));
        Scaffold scaffold = Scaffold.instance();

        scaffold.build();

        assertTrue(new File(".scaffold").exists());

        scaffold.addRoute(new Route("/dryrun", "dryrun", "dryrun(header,main(banner,text),footer)"));

        scaffold.dryRun();

        assertTrue(dryRunUpdated); // TODO

        scaffold.rollback();

        final Map<String, String> after = TestUtils.dirHash(new File("."));
        assertFalse(TestUtils.dirChanged(before, after));
    }

    // TODO project dir
    public void testUpdate() {
        final Map<String, String> before = TestUtils.dirHash(new File("."));
        Scaffold scaffold = Scaffold.instance();

        scaffold.build();
        assertTrue(new File(".scaffold").exists());

        assertTrue(filesBuild);

        scaffold.update();

        assertTrue(filesUpdated);

        scaffold.rollback();

        final Map<String, String> after = TestUtils.dirHash(new File("."));
        assertFalse(TestUtils.dirChanged(before, after));
    }

}
