package com.imooc.activiti.helloworld;

import com.google.common.collect.Maps;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * �����࣬����activiti
 * @author: Mifanxiaobai
 * @create: 2018-12-06 23:22
 **/
public class DemoMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoMain.class);

    public static void main(String[] args) throws ParseException {
        LOGGER.info("���������ɹ�");
        //������������
        ProcessEngine processEngine = getProcessEngine();

        //�������̶����ļ�
        String processDefinitionId = deployment(processEngine);

        //������������
        //��ȡ����ʱ����
        ProcessInstance processInstance = getProcessInstance(processEngine, processDefinitionId);

        //������������
        Scanner scanner = new Scanner(System.in);
        while (processInstance != null && !processInstance.isEnded()){
            TaskService taskService = processEngine.getTaskService();
            List<Task> list = taskService.createTaskQuery().list();
            LOGGER.info("������������Ϊ��{}��",list.size());
            for (Task task:list) {
                LOGGER.info("�����������{}��",task.getName());
                FormService formService = processEngine.getFormService();
                TaskFormData taskFormData = formService.getTaskFormData(task.getId());
                List<FormProperty> formProperties = taskFormData.getFormProperties();
                String line = null;
                Map<String,Object> variables = Maps.newHashMap();
                for (FormProperty property : formProperties){
                    //�ж��û���������
                    if(StringFormType.class.isInstance(property.getType())){
                        LOGGER.info("�����롾{}��?",property.getName());
                        line = scanner.nextLine();
                        LOGGER.info("������������ǡ�{}��",line);
                        variables.put(property.getId(),line);
                    }else{
                        LOGGER.info("������ʱ�䣬��ʽΪyyyy-MM-dd��{}��?",property.getName());
                        line = scanner.nextLine();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date parse = sdf.parse(line);
                        LOGGER.info("������������ǡ�{}��",line);
                        variables.put(property.getId(),parse);
                    }
                }
                //�ύ��
                taskService.complete(task.getId(),variables);
                //�ύ���������ʵ��
                processInstance = processEngine.getRuntimeService()
                        .createProcessInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .singleResult();
            }
        }
        scanner.close();
        LOGGER.info("�������");
    }

    private static ProcessInstance getProcessInstance(ProcessEngine processEngine, String processDefinitionId) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId);
        LOGGER.info("�������̡�{}��",processInstance.getProcessDefinitionKey());
        return processInstance;
    }

    private static String deployment(ProcessEngine processEngine) {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addClasspathResource("activiti-status.bpmn20.xml");
        Deployment deployment = deploymentBuilder.deploy();
        String id = deployment.getId();

        //��ȡ���̶������
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(id)
                .singleResult();
        String processDefinitionId = processDefinition.getId();
        String processDefinitionName = processDefinition.getName();
        LOGGER.info("���̶�������Ϊ��{}��,idΪ��{}��",processDefinitionName,processDefinitionId);
        return processDefinitionId;
    }

    private static ProcessEngine getProcessEngine() {
        ProcessEngineConfiguration cfg = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        ProcessEngine processEngine = cfg.buildProcessEngine();
        String name = processEngine.getName();
        String version = processEngine.VERSION;
        LOGGER.info("�����������ơ�{}��,�汾Ϊ��{}��",name,version);
        return processEngine;
    }
}
