
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.freeswitch.FreeswitchScript;
import org.freeswitch.HangupHook;
import org.freeswitch.swig.API;
import org.freeswitch.swig.JavaSession;
import org.freeswitch.swig.freeswitch;
import org.freeswitch.swig.session_flag_t;


public class ConferenceService implements FreeswitchScript, HangupHook {
   String cmd="";
	@Override
	public void onHangup() {
		// TODO Auto-generated method stub
		
	}
public void call(){
	//created for dialplan invocation
	run("a","b");
}
	@Override
	public void run(String arg0, String arg1) {
		// TODO Auto-generated method stub
		freeswitch.consoleLog("INFO", "Initializing bootstrap class..." );
		JavaSession session = null;
        try
            {
                session = new JavaSession(arg0);
              session.answer();               
              session.streamFile("/usr/local/freeswitch/sounds/dwc/Welcome Help Line 02.mp3", 0);  
                session.hangup("NORMAL_CLEARING");
            }
        finally
            {
                if (session != null)
                    session.destroy();
            }
        while(true){
        	cmd=read();
        	
        	if(cmd!=null || cmd.toLowerCase().equals("start")){
        		if(cmd.toLowerCase().equals("end")){
        			freeswitch.consoleLog("info", "<ConferenceService.java>: Got Script termination Instruction\n" );
        			break;
        		}
        		freeswitch.consoleLog("info", "Checking for Outbound Call and Conferences Support\n" );
        		StartApplication();
        		try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
	}

	private void StartApplication() {
		// TODO Auto-generated method stub
		Connection con = new DatabaseHandler().getConnection();
		String Query = "Select * from Outbound where status='0' and Schedule <= NOW() and Schedule >= (Now() - Interval 1 MINUTE)";
		Debug(Query);
		ResultSet rs;
		try {
			rs = con.createStatement().executeQuery(Query);
			//ThreadGroup tg = new ThreadGroup("Applications")
			//ThreadPoolExecutor th = new ThreadPoolExecutor();
			while(rs.next()){
				Long id = rs.getLong(1);
				if(rs.getString(2).toLowerCase().equals("out")){
					//freeswitch.
					String confname = rs.getString(3);
					String Number = rs.getString(4);
					String dialstring="";
					String recordstring ="";
					String calls_out="";
					if(rs.getString(5).toLowerCase().equals("gsm") || rs.getString(5).toLowerCase().equals("local") )
					{dialstring = "Originate freetdm/1/A/"+Number+" &conference("+confname+")";
					calls_out = "Insert into calls (CallUUID, Bound, Number, StartTime) values ("+confname+", 'OUT',"+Number+" ,NOW())";
					}else if(rs.getString(5).toLowerCase().equals("agent")){
						dialstring = "conference "+confname+" dial user/"+Number;
						String Filepath = "/usr/local/freeswitch/recordings/" +"";
						recordstring = "conference "+ confname+ " record "+Filepath;
					}
					org.freeswitch.swig.API a = new API();
					a.executeString(dialstring);
					if(recordstring.length()>3){
					//	a.executeString(recordstring);
					}
					//JavaSession ss = new JavaSession("freetdm/1/A/9968707080");
                    //ss.answer();				   
					//JavaSession sg = new JavaSession("freetdm/1/A/9654661039");
				    //sg.answer();
					//freeswitch.bridge(ss, sg);
				    Query = "Update Outbound set status='1' where id="+id;
				    con.createStatement().executeUpdate(Query);
                    if (calls_out.length()>2){
                    	con.createStatement().executeUpdate(calls_out);
                    	
                    }
				}
				if(rs.getString(2).toLowerCase().equals("conf")){
					//freeswitch.
					String confname = rs.getString(3);
					String Number = rs.getString(4);
					String dialstring="";
					if(rs.getString(5).toLowerCase().equals("gsm") || rs.getString(5).toLowerCase().equals("local") )
					{dialstring = "Originate freetdm/1/A/"+Number+" &conference("+confname+")";
					}else if(rs.getString(5).toLowerCase().equals("uuid")){
					 dialstring = "uuid_transfer "+Number+" -both conference:"+confname+"@default inline";	
					}
					org.freeswitch.swig.API a = new API();
					a.executeString(dialstring);
					
					//JavaSession ss = new JavaSession("freetdm/1/A/"+rs.getString(4)+" &conference("+confname+")");
//                    ss.answer();				   
	//				JavaSession sg = new JavaSession("freetdm/1/A/9654661039");
		//		    sg.answer();
			//		freeswitch.bridge(ss, sg);
				    Query = "Update Outbound set status='1' where id="+id;
				    con.createStatement().executeUpdate(Query);
				}
//				Connection cn = new DatabaseHandler().getConnection();
	//			ResultSet rst = con.createStatement().executeQuery("Select * from Apps_registration where id="+rs.getString("App_id"));
		//		Debug("Select * from Apps_registration where id="+rs.getString("App_id"));
//				if (rst.next()){
//		
//			Debug("Starting Application " + rst.getString("Name"));
		TimeUnit.SECONDS.sleep(1);
//			}
//				cn.close();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
		
	}

	private void Debug(String str) {
		// TODO Auto-generated method stub
		freeswitch.consoleLog("Debug", "<BootStrap>"+str+"\n");
	}

	private String read() {
		// TODO Auto-generated method stub
		cmd="";
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader("/usr/local/freeswitch/scripts/cmd.txt"));
 
			if ((sCurrentLine = br.readLine()) != null) {
				cmd= sCurrentLine;
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return cmd;
	}

}

//
//CREATE TABLE `Outbound` (
//		  `id` bigint(20) NOT NULL AUTO_INCREMENT,
//		  `OutType` bigint(20) DEFAULT NULL, CONF|OUT
//		  `OutName` datetime DEFAULT NULL,
//		  `Number` varchar(90) DEFAULT NULL,
//		  `NumberType` varchar(30) DEFAULT NULL, |UUID|LOCAL|GSM|Internal
//		  `Status` varchar(1) DEFAULT NULL,
//		  `Schedule` datetime DEFAULT NULL,
//		  PRIMARY KEY (`id`)
//		  ) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8
