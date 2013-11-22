import java.sql.Connection;
import java.sql.SQLException;

import org.freeswitch.esl.ESLconnection;
import org.freeswitch.esl.ESLevent;


public class CallOffered {
public static void main(String [] args) {
		
		/*
		 * 
		 * Once you get libesljni.so compiled you can either put it in your java library path and
		 * use System.loadlibrary or just use System.load with the absolute path.
		 *
		 */

//		System.load("/usr/local/src/freeswitch/libs/esl/java/libesljni.so");
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
			System.out.println(evt.serialize("plain"));
			String str = evt.getBody();
			System.out.println("Got the body as :"+str);
			str = evt.getHeader("Event-Name",-1);
			if(str.equals("CUSTOM")){
				System.out.println("Got a Custom event"+str);
				str = evt.getHeader("Event-Subclass",-1);
				if(str.equals("callcenter::info")){
					System.out.println("Got a CallCenter event"+str);
					str = evt.getHeader("CC-Action",-1);
					if(str.equals("agent-offering")){
						System.out.println("Ringing the agent "+str);
						//CC-Queue: support@default
						//CC-Agent: AgentNameHere
						//CC-Action: agent-offering 
						//CC-Agent-System: single_box 
						//CC-Member-UUID: 453324f8-3424-4322-4242362fd23d 
						//CC-Member-Session-UUID: 600165a4-f748-11df-afdd-b386769690cd 
						//CC-Member-CID-Name: CHOUINARD MO 
						//CC-Member-CID-Number: 4385551212
						String Caller = evt.getHeader("CC-Member-CID-Number",-1);
						if(Caller.startsWith("11")){
							Caller = Caller.substring(2);
						}
						if(Caller.length()>10){
						Caller = Caller.substring(Caller.length()-10);
						}
						String Query = "Update call_scheduling Set Serving='"+Caller+"', TextualDate='"+evt.getHeader("CC-Member-UUID",-1)+"', CallUUID='"+evt.getHeader("CC-Member-Session-UUID",-1)+"' ";
						 Query+= " where AgentID='"+evt.getHeader("CC-Agent",-1)+"'";
						 System.out.println("Query:"+Query);
						 update(Query);		 
					}
				}else{
				//	System.out.println(evt.serialize("plain"));
				}
			}else{
				//System.out.println(evt.serialize("plain"));
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
