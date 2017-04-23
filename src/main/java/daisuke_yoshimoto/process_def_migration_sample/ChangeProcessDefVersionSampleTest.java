package daisuke_yoshimoto.process_def_migration_sample;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Before;
import org.junit.Test;

/*
 * SetProcessDefinitionVersionCmdによるプロセスインスタンスが参照するプロセス定義のバージョンを変更するサンプル
 */
public class ChangeProcessDefVersionSampleTest {
	
	ProcessEngine processEngine;
	
	@Before
	public void setup() {
		processEngine = createProcessEngine();
	}

	@Test
	public void testNonChangeProcessDefVersion() {
		// プロセスの開始
		ProcessInstance processInstance = startProcessInstance();
		// ユーザタスクの処理
		complete(processInstance);
		
		Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		// 到達するユーザタスクがUser Task2となる
		assertEquals("usertask2", task.getTaskDefinitionKey());
	}
	
	@Test
	public void testChangeProcessDefVersion() {
		ProcessInstance processInstance = startProcessInstance();
		
		// 修正後のプロセス定義をデプロイ
		processEngine.getRepositoryService().createDeployment()
		.name("process_def_migration_sample.bpmn")
		.addClasspathResource("diagrams/process_def_migration_sample_v2.bpmn")
		.deploy();
		
		// プロセスインスタンスが参照するプロセス定義を変更する
		((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getCommandExecutor().execute(new SetProcessDefinitionVersionCmd(processInstance.getId(), 2));
		
		complete(processInstance);
		
		Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		// プロセスインスタンスが参照するプロセス定義のバージョンが変更され、到達するユーザタスクがUser Task3となった
		assertEquals("usertask3", task.getTaskDefinitionKey());
	}

	protected ProcessEngine createProcessEngine() {
		return ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
			.setJdbcDriver("org.h2.Driver")
			.setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
			.setJdbcUsername("sa")
			.setJdbcPassword("")
			.setDatabaseSchemaUpdate("drop-create")
			.setEnableProcessDefinitionInfoCache(true)
			.buildProcessEngine();
	}
	
	protected ProcessInstance startProcessInstance() {
		processEngine.getRepositoryService().createDeployment()
		.name("process_def_migration_sample.bpmn")
		.addClasspathResource("diagrams/process_def_migration_sample_v1.bpmn")
		.deploy();
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("input", "aaa");
		return processEngine.getRuntimeService().startProcessInstanceByKey("process_def_migration_sample", variables);
	}
	
	protected void complete(ProcessInstance processInstance) {
		Task task1 = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		processEngine.getTaskService().complete(task1.getId());
	}
}
