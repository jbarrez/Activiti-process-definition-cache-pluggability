package org.activiti;

import java.io.InputStream;
import java.net.BindException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.h2.jdbc.JdbcSQLException;
import org.h2.tools.Server;

/**
 * @author Joram Barrez
 */
public class DemoUtil {
  
  private static final Logger logger = Logger.getLogger(DemoUtil.class.getName());
  
  public static Server startH2Server() throws SQLException {
    try {
      Server h2Server = Server.createTcpServer("-tcpAllowOthers");
      h2Server.start();
      logger.info("H2 Database started");
      return h2Server;
    } catch (JdbcSQLException e) {
      if (e.getCause() instanceof BindException) {
        logger.info("H2 Database is already running!");
      } else {
        throw e;
      }
    }
    return null;
  }
  
  public static boolean areDemoProcessesDeployed() {
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();
    return repositoryService.createProcessDefinitionQuery().count() > 0;
  }
  
  public static void deployProcessDefinitions(int nrOfProcessDefinitions) {
    logger.info("Deploying " + nrOfProcessDefinitions + " processes to the database");
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();
    String processDefinitionTemplate = readTemplateFile("/processes/myProcess.bpmn20.xml");
    for (int i=1; i<=nrOfProcessDefinitions; i++) {
      String index = String.valueOf(i).replace(",", ".");
      repositoryService.createDeployment()
        .addString("Process " + index + ".bpmn20.xml", MessageFormat.format(processDefinitionTemplate, index))
        .deploy();
      logger.info("Deployed process definition " + i);
    }
    
    logger.info("Deployed " + nrOfProcessDefinitions + " processes");
    logger.info("Closing down the process engine.");
    ProcessEngines.destroy();
  }
  
  @SuppressWarnings("resource")
  public static String askForInput(String text) {
    logger.info(text);
    
    Scanner scanner = new Scanner(System.in);
    String input = scanner.nextLine();
    return input;
  }
  
  public static void startProcessInstances(ProcessEngine processEngine, int numberOfInstances) {
    RepositoryService repositoryService = processEngine.getRepositoryService();
    RuntimeService runtimeService = processEngine.getRuntimeService();

    int numberOfProcessDefinitions = (int) repositoryService.createProcessDefinitionQuery().count();
    
    for (int i=0; i<numberOfInstances; i++) {
      // Randomly select a process definition 
      int random = new Random().nextInt(numberOfProcessDefinitions) + 1;
      runtimeService.startProcessInstanceByKey("myProcess" + random);
    }
  }

  public static String readTemplateFile(String templateFile) {
    logger.info("Reading template file '" + templateFile + "'");
    
    InputStream inputStream = DemoUtil.class.getResourceAsStream(templateFile);
    if (inputStream == null) {
      logger.log(Level.WARNING, "Could not read template file '" + templateFile + "'!");
    } else {

      Scanner scanner = null;
      try {
        scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
        if (scanner.hasNext()) {
          return scanner.next();
        }
      } finally {
        if (scanner != null) {
          scanner.close();
        }
      }
    }
    return null;
  }

}
