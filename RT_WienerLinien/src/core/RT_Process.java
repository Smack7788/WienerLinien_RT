package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class RT_Process implements Job{
	long startTime = System.currentTimeMillis();
	
	private static final int MAX_RBL = 9500;
	private static final int MAGIC_LOOP_NUMBER = 500;
	private String REQUEST_URL_All = "http://www.wienerlinien.at/ogd_realtime/monitor?%s&sender=nFTMbBjYEHbCMKSv";
	private static Logger logger = Logger.getLogger(RT_Process.class);
	private static final String REAL_TIME_TEMP_FILE = "../RT_WienerLinien/tmpstore/realTimeTempFile";
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	DateFormat indexDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static String ES_INDEX = "";
	private static String indexTimestamp = null;
	private static final String ES_INDEX_TYPE = "_doc";
	Integer dayOfTheWeek = new Date().getDay();
	Integer hourOfTheDay = new Date().getHours();
	private static Writer log = null;
	//TODO Reorganize the constants
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
			    
		
		Date date= new Date();
		long time = date.getTime();
//		try {
//				log = new BufferedWriter(new FileWriter("../RT_WienerLinien/logs/"+time+".txt"));
//				log.append("*****LOG START*****");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}  //clears file every time
		
		
		
	    //Define the Index name including date
	    indexTimestamp = indexDateFormat.format(new Date());
	    ES_INDEX = "rt_wienerlinien_"+indexTimestamp;
	    
	    // Returns a String List of all monitor JSON Objects
	    List<String> jsonMonitorList = runAll(0, MAX_RBL);
	    
	    int counterSuccess = 0;
		int counterFailed = 0;
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(REAL_TIME_TEMP_FILE+time+".json"));//clears file every time
			
			for (int i = 0; i < jsonMonitorList.size(); i++) {
				String[] jsonArray = convertDataFromInput(jsonMonitorList.get(i));
				if (jsonArray != null){
					output.append(jsonArray[0]+ "\n");
					output.append(jsonArray[1]+ "\n");
					counterSuccess++;
				} else {
					counterFailed++;
				}
			}
			output.close();
			logger.info(counterSuccess + " have successfully been processed" +"\n"+ counterFailed + " have had an error");
			
			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println(estimatedTime);
			
			
			String command = "curl -k -H \"Content-Type:application/x-ndjson\" -u \"elastic:t6gqvhc9w52snpq8st6s4www\" -XPOST \"https://localhost:9200/_bulk?pretty\" --data-binary @../RT_WienerLinien/tmpstore/realTimeTempFile"+time+".json";
			ProcessBuilder builder = new ProcessBuilder(
		            "cmd.exe", "/c", command);
		        Process p = builder.start();
		        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String inputLine;
				String test="";
				while ((inputLine = r.readLine()) != null) {
					test = inputLine;
					System.out.println(test);
				}
				r.close();
				
				Files.delete(Paths.get(REAL_TIME_TEMP_FILE+time+".json"));
		} catch (IOException | java.text.ParseException e) {
			logger.debug(e.getMessage());
			
		}
	}	
	    
	private List<String> runAll(int start, int end) {
		logger.info("runALL");
//		try {
//			log.append("runALL");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		List<String> JSONmonitorlist = new ArrayList<String>();
		try {
			List<String> responseJsonMessagelist = loadRealtimeDataAll(start, end);
			for (String responseJsonMessage :responseJsonMessagelist) {
				JSONObject responseJsonObject = new JSONObject(responseJsonMessage);
				JSONObject message = responseJsonObject.getJSONObject("message");
				// MetaData of the request
				String messageValue = (String) message.get("value");
				Integer messageCode = (Integer) message.get("messageCode");
				String messageServerTime = (String) message.get("serverTime");
				logger.debug("meta data of the request value=" + messageValue + "; messageCode=" + messageCode+ ", messageServerTime=" + messageServerTime);
				JSONObject data = responseJsonObject.getJSONObject("data");
				JSONArray monitorsDetails = (JSONArray) data.get("monitors");
				for (int j = 0; j < monitorsDetails.length(); ++j) {
					JSONObject monitorSingle = monitorsDetails.getJSONObject(j);
					monitorSingle.put("serverTime", messageServerTime);
					JSONmonitorlist.add(monitorSingle.toString());
				}
			}
		} catch (MalformedURLException e) {
			logger.debug(e.getMessage());
		} catch (IOException e) {
			logger.debug(e.getMessage());
		} catch (JSONException e) {
			logger.debug(e.getMessage());
		}
		return JSONmonitorlist;
	}
	
	private List<String> loadRealtimeDataAll(int start, int end) throws MalformedURLException, IOException, ProtocolException {
		logger.info("loadRealtimeDataAll");
//		try {
//			log.append("loadRealtimeDataAll");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		List<String> finalUrllist = buildURLAll(start, end);
		logger.info("Url built");
		List<String> JSONresponselist = new ArrayList<String>();
		
		Date date= new Date();
		long time = date. getTime();
		
		for (String finalUrl : finalUrllist) {
			URL url = new URL(finalUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			int responseCode = con.getResponseCode();

			logger.debug("Sending 'GET' request to URL: " + finalUrl);
			logger.debug("Response Code : " + responseCode);

			System.out.println("Sending 'GET' request to URL: " + finalUrl);
			System.out.println("Response Code : " + responseCode + "     Timestamp: " + time);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			JSONresponselist.add(response.toString());
		}
		return JSONresponselist;
	}
	
	private List<String> buildURLAll(int start, int end) throws FileNotFoundException {
		logger.info("Build URL all");
//		try {
//			log.append("Build URL all");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		Scanner s = new Scanner(new File("../RT_WienerLinien/tmpstore/RBL_List.txt"));
		ArrayList<Integer> list = new ArrayList<Integer>();
		while (s.hasNextInt()){
		    list.add(s.nextInt());
		}
		s.close();
		
		List<String> URLarray = new ArrayList<String>();
		logger.info("Requesting rbl numbers " + start + " through " + end);

		
		for (int j = 0; j < list.size()-1;) {
			String rbltext = String.format("rbl=%d", list.get(j));
			for (int i = 1; j < list.size()-1 && i < MAGIC_LOOP_NUMBER; i++) {
				j++;
				rbltext += "&rbl=" + list.get(j);
			}
			String finalURL = String.format(REQUEST_URL_All, rbltext);
			URLarray.add(finalURL);
			j++;
		}
		System.out.println(URLarray);
		
		return URLarray;
	}
	
	public String[] convertDataFromInput(String inputData) throws java.text.ParseException {
		if(inputData == null){
			return null;
		}		
		logger.debug(inputData);		
		JSONObject oldJsonObject = null;
		try {
			oldJsonObject = new JSONObject(inputData);
		} catch (JSONException e1) {
			logger.debug("Problem creating json object");
		}
		
		JSONObject linesObject = null;
		String name = "";
		String richtungsId = "";
		try {
			JSONArray linesArray = (JSONArray) oldJsonObject.get("lines");
			linesObject = linesArray.getJSONObject(0);
			name = (String) linesObject.get("name");
			richtungsId = (String) linesObject.get("richtungsId");
		} catch (JSONException e1) {
			logger.debug("Problem reading lines object");
		}
	
		String timePlanned = null;
		String timeReal = null;
		JSONObject departureTime = null;
		try {
			JSONObject departures = linesObject.getJSONObject("departures");
			JSONArray departure = (JSONArray) departures.get("departure");
			JSONObject departureObject = departure.getJSONObject(0);
			departureTime = departureObject.getJSONObject("departureTime");
		} catch (JSONException e1) {
			logger.debug("Problem reading departureTime");
		}
		Integer lineId=null;
		try {
			lineId = (Integer) linesObject.get("lineId");
		} catch (JSONException e1) {
			logger.debug("Cannot read lineId");
		}
		String type="";
		try {
			type = (String) linesObject.get("type");
		} catch (JSONException e1) {
			logger.debug("Cannot read lines[0].type");
		}
		Boolean barrierFree=null;
		try {
			barrierFree = (Boolean) linesObject.get("barrierFree");
		} catch (JSONException e1) {
			logger.debug("Cannot read lines[0].barrierFree");
		}
		Boolean trafficjam = null;
		try {
			trafficjam = (Boolean) linesObject.get("trafficjam");
		} catch (JSONException e1) {
			logger.debug("Cannot read lines[0].trafficjam");
		}
		Date timeP = null;
		try {
			timePlanned = (String) departureTime.get("timePlanned");
			timeP = dateFormat.parse(timePlanned);
		} catch (JSONException e1) {
			logger.debug("Cannot read timePlanned "+e1.getMessage());
		}

		Date timeR = null;
		try {
			timeReal = (String) departureTime.get("timeReal");
			timeR = dateFormat.parse(timeReal);
		} catch (JSONException e1) {
			logger.debug("Cannot read timeReal "+e1.getMessage());
		}
		
		String serverTime = null;
		try {
			serverTime = (String) oldJsonObject.get("serverTime");
		} catch (JSONException e1) {
			logger.debug("Cannot read serverTime");
		}
		
		Integer delay = calcDelay(timeR,timeP);
		
		String title = "";
		String stationNumber ="";
		JSONObject locationStop = null;
		JSONObject properties = null;
		try {
			locationStop = oldJsonObject.getJSONObject("locationStop");
			properties = locationStop.getJSONObject("properties");
			title = (String) properties.get("title");
		} catch (JSONException e1) {
			logger.debug("Cannot read title ");
		}
		try {
			stationNumber = (String) properties.get("name");
		} catch (JSONException e1) {
			logger.debug("Cannot read stationNumber (properties.name) ");
		}
		JSONArray location = null;
		String location2 = null;
		try {
			JSONObject geometry = locationStop.getJSONObject("geometry");
			location = (JSONArray) geometry.get("coordinates");
			location2 = "{\"lat\":"+ location.getDouble(1) + ",\"lon\":" + location.getDouble(0) + "}";
			
		} catch (JSONException e1) {
			logger.debug("Cannot read coordinates");
		}
		Integer rbl = null;
		try {
			JSONObject attributes = properties.getJSONObject("attributes");
			rbl = (Integer) attributes.get("rbl");
		} catch (JSONException e1) {
			logger.debug("Cannot read attributes");
		}
		
		StringBuilder header = new StringBuilder();
		header.append("{\"index\":{\"_index\":\"");
		header.append(ES_INDEX);
		header.append("\",\"_type\":\"");
		header.append(ES_INDEX_TYPE);
		header.append("\"}}");
		
		StringBuilder dataString = new StringBuilder();
		String POST_FIX = "\":\"";
		String POST_FIX2 = "\":";
		String PRE_FIX = "\"";
		String DELIMITER = "\",";
		
		String KEY_VALUE_PAIR = PRE_FIX+"%s"+POST_FIX;
		String KEY_VALUE_PAIR2 = PRE_FIX+"%s"+POST_FIX2;
		
		dataString.append("{");
		if(name != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "name"));
			dataString.append(name);
			dataString.append(DELIMITER);
		}
		if(stationNumber != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "stationNumber"));
			dataString.append(stationNumber);
			dataString.append(DELIMITER);
		}
		if(title != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "title"));
			dataString.append(title);
			dataString.append(DELIMITER);
		}
		if(lineId != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "lineId"));
			dataString.append(lineId);
			dataString.append(DELIMITER);
		}
		if(rbl != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "rbl"));
			dataString.append(rbl);
			dataString.append(DELIMITER);
		}
		if(richtungsId != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "richtungsId"));
			dataString.append(richtungsId);
			dataString.append(DELIMITER);
		}
		if(type != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "type"));
			dataString.append(type);
			dataString.append(DELIMITER);
		}
		if(barrierFree != null){
			dataString.append(String.format(KEY_VALUE_PAIR2, "barrierFree"));
			dataString.append(barrierFree);
			dataString.append(",");
		}
		if(delay != null){
			dataString.append(String.format(KEY_VALUE_PAIR2, "delay"));
			dataString.append(delay);
			dataString.append(",");
		}
		if(timePlanned != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "timePlanned"));
			dataString.append(timePlanned);
			dataString.append(DELIMITER);
		}
		if(timeReal != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "timeReal"));
			dataString.append(timeReal);
			dataString.append(DELIMITER);
		}
		if(trafficjam != null){
			dataString.append(String.format(KEY_VALUE_PAIR2, "trafficjam"));
			dataString.append(trafficjam);
			dataString.append(",");
		}
		if(serverTime != null){
			dataString.append(String.format(KEY_VALUE_PAIR, "serverTime"));
			dataString.append(serverTime);
			dataString.append(DELIMITER);
		}
		if(location2 != null){
			dataString.append(String.format(KEY_VALUE_PAIR2, "location"));
			dataString.append(location2);
			dataString.append(",");
		}
		if(dayOfTheWeek != null){
			dataString.append(String.format(KEY_VALUE_PAIR2, "dayOfTheWeek"));
			dataString.append(dayOfTheWeek);
			dataString.append(",");
		}
		if(hourOfTheDay != null){
			dataString.append(String.format(KEY_VALUE_PAIR2, "hourOfTheDay"));
			dataString.append(hourOfTheDay);
			dataString.append(",");
		}
		
		boolean lastCharCheck = dataString.toString().endsWith(",");
		if(lastCharCheck) {
			dataString.setLength(dataString.length() - 1);
		}
		dataString.append("}");	
		
		String[] returnValue = new String[2];
		returnValue[0] = header.toString();
		returnValue[1] = dataString.toString();
		
		return returnValue;
	}
	
	private Integer calcDelay(Date timeR, Date timeP) {
		Integer delay = null;
		if(timeR != null && timeP != null) {
			delay = (int) ((timeR.getTime()-timeP.getTime())/1000);
		}
		return delay;
	}	
}