import java.sql.Connection;
import java.sql.SQLException;

import org.freeswitch.esl.ESLconnection;
import org.freeswitch.esl.ESLevent;


public class CallAnswered {
public static void main(String [] args) {
		
		/*
		 * 
		 * Once you get libesljni.so compiled you can either put it in your java library path and
		 * use System.loadlibrary or just use System.load with the absolute path.
		 *
		 */
	///usr/local/freeswitch/mod

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
					if(str.equals("agent-state-change")){
						//CC-Agent: 1000@default
						//CC-Action: agent-state-change
						//CC-Agent-State: Receiving
						str = evt.getHeader("CC-Agent-State",-1);
						if(str.equals("In a queue call")){
		String Query = "Update call_scheduling set status='Ans' where AgentID='"+evt.getHeader("CC-Agent",-1)+"'";;
		update(Query);
						}
					}
				}else{
			//		System.out.println(evt.serialize("plain"));
				}
			}else{
		//		System.out.println(evt.serialize("plain"));
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
