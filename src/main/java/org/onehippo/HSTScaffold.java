package org.onehippo;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HSTScaffold {

    final static Logger log = Logger.getLogger(HSTScaffold.class);

    final public static String SCAFFOLD_DIR_NAME = ".scaffold";

    public static final String PROJECT_NAME = "projectName";

    public static final String PROJECT_DIR = "projectDir";
    public static final String DEFAULT_PROJECT_DIR = ".";
    public static final String PROJECT_PACKAGE_NAME = "projectPackageName";

    public static final String TEMPLATE_PATH = "templatePath";
    public static final String DEFAULT_TEMPLATE_PATH = "bootstrap/webfiles/src/main/resources";

    public static final String JAVA_COMPONENT_PATH = "componentPath";
    public static final String DEFAULT_COMPONENT_PATH = "site/src/main/java";

    public static final Pattern COMMENT = Pattern.compile("^(\\s*)#.*");
    public static final Pattern URL = Pattern.compile("^(\\s*)(/[^\\s]*/?)*");
    public static final Pattern CONTENT = Pattern.compile("^(\\s*)(/[^\\s]*/?)*");
    public static final Pattern PAGE = Pattern.compile("([\\w\\(\\),\\s]+)");

    private static HSTScaffold scaffold;
    private ScaffoldBuilder builder;

    private List<Route> routes = new ArrayList<Route>();

    public static Properties properties;

    HSTScaffold(String projectDirPath) throws IOException {
        File scaffoldDir = createHiddenScaffold(projectDirPath);

        loadProperties(scaffoldDir);

        read(new InputStreamReader(this.getClass().getResourceAsStream("/scaffold.hst")));
    }

    private void loadProperties(File scaffoldDir) {
        properties = new Properties();
        try {
            File propertiesFile = new File(scaffoldDir, "conf.properties");

            if (!propertiesFile.exists()) {
                FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/scaffold.properties"), propertiesFile);
            }

            properties.load(new BufferedInputStream(new FileInputStream(propertiesFile)));
            properties.put(PROJECT_DIR, scaffoldDir.getParent());
            // todo can we determine project name and project package name from the existing sources
            // and what about the hst site conf name / multi site setups?
        } catch (IOException e) {
            log.error("Error loading properties");
        }
    }

    private File createHiddenScaffold(String projectDirPath) throws IOException {
        File projectDir = new File(projectDirPath);
        if (!projectDir.exists()) {
            throw new IOException(String.format("Project directory doesn't exist %s.", HSTScaffold.properties.getProperty(HSTScaffold.PROJECT_DIR)));
        }

        File scaffoldDir = new File(projectDir, ".scaffold");
        if (!scaffoldDir.exists()) {
            scaffoldDir.mkdirs();
        }
        return scaffoldDir;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void read(Reader configReader) {
        try {
            StringBuilder configBuilder = new StringBuilder();

            BufferedReader reader = new BufferedReader(configReader);
            char [] buffer = new char[1024];
            while (reader.read(buffer) > 0) {
                configBuilder.append(buffer);
            }

            read(configBuilder.toString());
        } catch (FileNotFoundException e) {
            log.error("Error reading configuration file, file not found.", e);
        } catch (IOException e) {
            log.error("Error reading configuration file.", e);
        }
    }

    private boolean isComment(String line) {
        Matcher matcher = COMMENT.matcher(line);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public void read(String config) {
        // *name, :id
        // /text/*path       /contact/path:String    text(header,main(banner, text),footer)
        Scanner scanner = new Scanner(config);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (isComment(line)) {
                continue;
            }

            Matcher matcher = URL.matcher(line);
            if (!matcher.find() || StringUtils.isEmpty(matcher.group(2))) {
                log.warn("Invalid route, url: "+line);
                continue;
            }
            String url = matcher.group(2);
            line = line.substring(matcher.group(1).length()+matcher.group(2).length());

            matcher = CONTENT.matcher(line);
            if (!matcher.find() || StringUtils.isEmpty(matcher.group(2))) {
                log.warn("Invalid route, content: "+line);
                continue;
            }
            String content = matcher.group(2);
            line = line.substring(matcher.group(1).length()+matcher.group(2).length());

            matcher = PAGE.matcher(line);
            if (!matcher.find()) {
                log.warn("Invalid route, page: "+line);
                continue;
            }

            String page = matcher.group(1);
            routes.add(new Route(url, content, page));
        }

    }

    public void setBuilder(ScaffoldBuilder builder) {
        this.builder = builder;
    }


    public void build(boolean dryRun) {
        if (this.builder != null) {
            try {
                this.builder.build(dryRun);
            } catch (Exception e) {
                log.error("Error building scaffold, rolling back.", e);
            }
        }
    }

    public void rollback(boolean dryRun) {
        if (this.builder != null) {
            try {
                this.builder.rollback(dryRun);
            } catch (Exception e) {
                log.error("Error rolling back to previous project state.");
            }
        }
    }

    public static HSTScaffold instance(String path) throws IOException {
        if (scaffold == null) {
            scaffold = new HSTScaffold(path);
        }

        return scaffold;
    }

}
