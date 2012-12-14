package org.activiti;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.util.LogUtil;
import org.h2.tools.Server;

import static org.activiti.DemoUtil.*;

/**
 * @author Joram Barrez
 */
public class RunProcessDefinitionCacheDemo {

  private static final Logger logger = Logger.getLogger(RunProcessDefinitionCacheDemo.class.getName());

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }

  public static void main(String[] args) throws Exception {

    boolean runDefault = validateArguments(args);
    boolean distributed = !runDefault;
    
    if(distributed) {
      // Infinispan spits out A LOT logging.
      // So in the distributed case, we set it a bit higher
      Logger rootLogger = logger.getLogger("");
      rootLogger.setLevel(Level.INFO);
    }
    
    // Setup database
    Server h2Server = startH2Server();
    
    if (!areDemoProcessesDeployed()) { // extra cluster nodes beyond the first one should not deploy new processes
      // Deploy test processes
      int numberOfProcessDefinitions = Integer.valueOf(askForInput("How many process definitions do you want?"));
      deployProcessDefinitions(numberOfProcessDefinitions);
    }
    
    // Create Process Engine based
    ProcessEngine processEngine = null;
    if (runDefault) {
      processEngine = ProcessEngines.getDefaultProcessEngine();
    } else {
      ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
              .createProcessEngineConfigurationFromResource("activiti-with-distributed-deployment-cache.cfg.xml");
      processEngine = processEngineConfiguration.buildProcessEngine();
    }
    
    // Start instances
    int numberOfInstances = Integer.valueOf(askForInput("How many process instance do I need to start?"));
    startProcessInstances(processEngine, numberOfInstances);
    
    if(distributed) {
      // Just wait a very long time so we can see some cache messages
      Thread.sleep(Long.MAX_VALUE);
    }

    // Shutdown
    if (h2Server != null) {
      h2Server.stop();
    }
    logger.info("All Done!");
  }

  private static boolean validateArguments(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("I can't guess which configuration you want to run. You should provide 'default' or 'distributed' as argument");
      throw new RuntimeException();
    }
    
    if (args[0].toLowerCase().equals("default")) {
      return true;
    } else if (args[0].toLowerCase().equals("distributed")) {
      return false;
    } else {
      System.err.println("I tried hard, really! But I cant decipher which configuration you need. Use 'default' or 'distributed'.");
      throw new RuntimeException();
    }
  }
  

}
