package fr.jetoile.hadoopunit;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

public class HadoopBootstrapRemoteUtils {

    private MavenProject project;

    private MavenSession session;

    private BuildPluginManager pluginManager;

    public HadoopBootstrapRemoteUtils(MavenProject project, MavenSession session, BuildPluginManager pluginManager) {
        this.project = project;
        this.session = session;
        this.pluginManager = pluginManager;
    }

    public void operateRemoteHadoopUnit(String hadoopUnitPath, String outputFile, String op) throws MojoExecutionException {
        executeMojo(
                plugin(
                        groupId("org.codehaus.mojo"),
                        artifactId("exec-maven-plugin"),
                        version("1.4.0")
                ),
                goal("exec"),
                configuration(
                        element(name("workingDirectory"), hadoopUnitPath + "/bin"),
                        element(name("executable"), "./hadoop-unit-standalone"),
                        element(name("commandlineArgs"), op),
                        element(name("outputFile"), outputFile)
                ),
                executionEnvironment(
                        project,
                        session,
                        pluginManager
                )
        );
    }

    public void tailLogFileUntilFind(Path hadoopLogFilePath, String find, Log log) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(hadoopLogFilePath.toFile()));
            String line;
            boolean keepReading = true;
            while (keepReading) {
                line = reader.readLine();
                if (line == null) {
                    //wait until there is more of the file for us to read
                    Thread.sleep(1000);
                }
                else {
                    keepReading = !StringUtils.containsIgnoreCase(line, find);
                    //do something interesting with the line
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("unable to read wrapper.log file", e);
        }
    }

    public void getHadoopUnitPath(String hadoopUnitPath, Log log) {
        String hadoopUnitHome = System.getenv("HADOOP_UNIT_HOME");
        if (hadoopUnitHome != null) {
            hadoopUnitPath = hadoopUnitHome;
            log.info("overriding property hadoopUnitPath with system environment variable" + hadoopUnitHome);
        }
        log.info("is going to use:" + hadoopUnitPath);

        if (hadoopUnitPath == null) {
            log.error("hadoopUnitPath or HADOOP_UNIT_HOME should be set");
        }
    }
}