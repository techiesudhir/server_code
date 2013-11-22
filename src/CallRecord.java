import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.freeswitch.esl.ESLconnection;
import org.freeswitch.esl.ESLevent;


public class CallRecord {
public static void main(String [] args) {
		
		/*
		 * 
		 * Once you get libesljni.so compiled you can either put it in your java library path and
		 * use System.loadlibrary or just use System.load with the absolute path.
		 *
		 */

	//	System.load("/usr/local/src/freeswitch/libs/esl/java/libesljni.so");
	System.load("/usr/local/freeswitch/scripts/Mod_CallCenterScripts/libesljni.so");
		/*
		 *
		 * Trying to keep this simple (and I'm no java expert) I am instantiating the ESLconnection and
		 * ESLevent object in a static reference here so remember if you don't plan on doing everything in main
		 * you will need to instantiate your class first or you will get compile-time errors.
		 *
		 */

		ESLconnection con = new ESLconnection("127.0.0.1","8021","ClueCon");

		ESLevent evt;

		if (con.connected() == 1) System.out.println("connected");
		con.events("plain","all");

		// Loop while connected to the socket -- not sure if this method is constantly updated so may not be a good test
		while (con.connected() == 1) {			
			// Get an event - recvEvent will block if nothing is queued
			evt = con.recvEvent();
			//System.out.println(evt.serialize("plain"));
			String str = evt.getBody();
			System.out.println("Got the body as :"+str);
			str = evt.getHeader("Event-Name",-1);
			if(str.equals("RECORD_STOP")){
				System.out.println("Got a event"+str);
				str = evt.getHeader("variable_current_application",-1);
				if(str.equals("callcenter")){
					System.out.println("Got a CallCenter event"+str);
					str = evt.getHeader("variable_cc_queue",-1);
					if(str.equals("dwc@default")){
						//CC-Agent: 1000@default
						//CC-Action: agent-state-change
						//CC-Agent-State: Receiving
						String Audiofile = evt.getHeader("variable_cc_record_filename",-1);
						String TextualDate = evt.getHeader("variable_cc_member_uuid",-1);
						String CallUUID = evt.getHeader("variable_call_uuid",-1);
						String Caller = evt.getHeader("Caller-Caller-ID-Number",-1);
						if(Caller.startsWith("11")){
							Caller = Caller.substring(2);
						}
						if(Caller.length()>10){
						Caller = Caller.substring(Caller.length()-10);
						}
						
		String Query = "Update call_scheduling set status='Free', Serving='' where TextualDate='"+TextualDate+"' and ";
		       Query += "CallUUID='"+CallUUID+"'";
		update(Query);
		Query = "Select idCall from calls where TextualDate='"+TextualDate+"' and ";
		       Query += "CallUUID='"+CallUUID+"' and Number='"+Caller+"'";				
		          Connection cnn = new DatabaseHandler().getConnection();
		          try {
		           		 ResultSet rs = cnn.createStatement().executeQuery(Query);
		 				if (rs.next()) {
		 				String Callid= rs.getString(1);
		 				String updtQry = "Insert into audio(Filepath, Number, Callid) values('"+Audiofile+"','"+Caller+"','"+Callid+"')";
		 				update(updtQry);
		 				}
		 			} catch (SQLException e) {
		 					// TODO Auto-generated catch block
		 					e.printStackTrace();
		 				} finally {
		 				try {
							cnn.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		 				}		
					}
				}else{
		//			System.out.println(evt.serialize("plain"));
				}
			}else{
			//	System.out.println(evt.serialize("plain"));
			}

			// Print the entire event in key : value format. serialize() according to the wiki usually takes no arguments
			// but if you do not put in something you will not get any output so I just stuck plain in.
			

		}
		
		
	}
public static void update(String Query) {
	Connection con = new DatabaseHandler().getConnection();
	try {
		//Debug(Query);
		con.createStatement().executeUpdate(Query);
		con.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} finally {
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
}
