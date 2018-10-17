package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
public class IndexSetupProcess implements Job{
public void execute(JobExecutionContext context) throws JobExecutionException {

	try {
		
	String command = "curl -XPUT \"localhost:9200/test\" -H \"Content-Type: application/json\" -d'\r\n" + 
			"{\r\n" + 
			"\"mappings\": {\r\n" + 
			"    \"data\": {\r\n" + 
			"\"properties\": {\r\n" + 
			"          \"barrierFree\": {\r\n" + 
			"            \"type\": \"boolean\"\r\n" + 
			"          },\r\n" + 
			"          \"dayOfTheWeek\": {\r\n" + 
			"            \"type\": \"long\"\r\n" + 
			"          },\r\n" + 
			"          \"delay\": {\r\n" + 
			"            \"type\": \"long\"\r\n" + 
			"          },\r\n" + 
			"          \"lineId\": {\r\n" + 
			"            \"type\": \"text\",\r\n" + 
			"            \"fields\": {\r\n" + 
			"              \"keyword\": {\r\n" + 
			"                \"type\": \"keyword\",\r\n" + 
			"                \"ignore_above\": 256\r\n" + 
			"              }\r\n" + 
			"            }\r\n" + 
			"          },\r\n" + 
			"          \"location\": {\r\n" + 
			"            \"type\": \"geo_point\"\r\n" + 
			"          },\r\n" + 
			"          \"name\": {\r\n" + 
			"            \"type\": \"text\",\r\n" + 
			"            \"fields\": {\r\n" + 
			"              \"keyword\": {\r\n" + 
			"                \"type\": \"keyword\",\r\n" + 
			"                \"ignore_above\": 256\r\n" + 
			"              }\r\n" + 
			"            }\r\n" + 
			"          },\r\n" + 
			"          \"rbl\": {\r\n" + 
			"            \"type\": \"text\",\r\n" + 
			"            \"fields\": {\r\n" + 
			"              \"keyword\": {\r\n" + 
			"                \"type\": \"keyword\",\r\n" + 
			"                \"ignore_above\": 256\r\n" + 
			"              }\r\n" + 
			"            }\r\n" + 
			"          },\r\n" + 
			"          \"richtungsId\": {\r\n" + 
			"            \"type\": \"text\",\r\n" + 
			"            \"fields\": {\r\n" + 
			"              \"keyword\": {\r\n" + 
			"                \"type\": \"keyword\",\r\n" + 
			"                \"ignore_above\": 256\r\n" + 
			"              }\r\n" + 
			"            }\r\n" + 
			"          },\r\n" + 
			"          \"serverTime\": {\r\n" + 
			"            \"type\": \"date\"\r\n" + 
			"          },\r\n" + 
			"          \"stationNumber\": {\r\n" + 
			"            \"type\": \"text\",\r\n" + 
			"            \"fields\": {\r\n" + 
			"              \"keyword\": {\r\n" + 
			"                \"type\": \"keyword\",\r\n" + 
			"                \"ignore_above\": 256\r\n" + 
			"              }\r\n" + 
			"            }\r\n" + 
			"          },\r\n" + 
			"          \"timePlanned\": {\r\n" + 
			"            \"type\": \"date\"\r\n" + 
			"          },\r\n" + 
			"          \"timeReal\": {\r\n" + 
			"            \"type\": \"date\"\r\n" + 
			"          },\r\n" + 
			"          \"title\": {\r\n" + 
			"            \"type\": \"text\",\r\n" + 
			"            \"fields\": {\r\n" + 
			"              \"keyword\": {\r\n" + 
			"                \"type\": \"keyword\",\r\n" + 
			"                \"ignore_above\": 256\r\n" + 
			"              }\r\n" + 
			"            }\r\n" + 
			"          },\r\n" + 
			"          \"trafficjam\": {\r\n" + 
			"            \"type\": \"boolean\"\r\n" + 
			"          },\r\n" + 
			"          \"type\": {\r\n" + 
			"            \"type\": \"text\",\r\n" + 
			"            \"fields\": {\r\n" + 
			"              \"keyword\": {\r\n" + 
			"                \"type\": \"keyword\",\r\n" + 
			"                \"ignore_above\": 256\r\n" + 
			"              }\r\n" + 
			"            }\r\n" + 
			"          }\r\n" + 
			"        }\r\n" + 
			"      }\r\n" + 
			"}}";	
		
	String command2 = "curl -H \"Content-Type:application/x-ndjson\" -XPOST \"http://localhost:9200/_bulk?pretty\" --data-binary @C:/Users/admin/Desktop/WL/export/realTimeTempFile.json";
	ProcessBuilder builder = new ProcessBuilder(
            "cmd.exe", "/c", command);
        Process p;
		
			p = builder.start();
		
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String inputLine;
		String test="";
		while ((inputLine = r.readLine()) != null) {
			test = inputLine;
			System.out.println(test);
		}
		r.close();
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
}
}