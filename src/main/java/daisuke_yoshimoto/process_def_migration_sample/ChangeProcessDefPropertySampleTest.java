package daisuke_yoshimoto.process_def_migration_sample;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * DynamicBpmnServiceによるプロセス定義の書き換え処理のサンプル
 */
public class ChangeProcessDefPropertySampleTest extends ChangeProcessDefVersionSampleTest {

	@Test
	public void testChangeProcessDefProperty() {
		// プロセスの開始
		ProcessInstance processInstance = startProcessInstance();
		
		// プロセスインスタンスが参照するプロセス定義を変更する
		ObjectNode changedNode = processEngine.getDynamicBpmnService().changeSequenceFlowCondition("flow4", "${input == 'aaa'}");
		processEngine.getDynamicBpmnService().saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), changedNode);
		
		// ユーザタスクの処理
		complete(processInstance);
		
		Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		// プロセスインスタンスが参照するプロセス定義の内容が変更され、到達するユーザタスクがUser Task3となった
		assertEquals("usertask3", task.getTaskDefinitionKey());
	}
}
