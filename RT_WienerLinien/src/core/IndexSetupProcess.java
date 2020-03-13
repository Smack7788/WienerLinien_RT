package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
public class IndexSetupProcess implements Job{
DateFormat indexDateFormat = new SimpleDateFormat("yyyy-MM-dd");
private static String indexTimestamp = null;

public void execute(JobExecutionContext context) throws JobExecutionException {

	try {
		
		indexTimestamp = indexDateFormat.format(new Date());
		Calendar indexCalendar = Calendar.getInstance();
		indexCalendar.setTime(indexDateFormat.parse(indexTimestamp));
		indexCalendar.add(Calendar.DATE, 1);
		indexTimestamp = indexDateFormat.format(indexCalendar.getTime());
		
		System.out.println(indexTimestamp);
				
		String command = "curl -k -XPUT \"https://localhost:9200/rt_wienerlinien_" + indexTimestamp + "\" -H \"Content-Type:application/json\" -u \"elastic:85f599nljxs5qvlfmdkr8475\" -d \"@../RT_WienerLinien/conf/dailyIndex.mapping.txt\"";
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
	        Process p = builder.start();
	        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String inputLine;
			String test="";
			while ((inputLine = r.readLine()) != null) {
				test = inputLine;
				System.out.println(test);
			}
			r.close();
		
	} catch (IOException | ParseException e) {
		e.printStackTrace();
	}
	
}
}