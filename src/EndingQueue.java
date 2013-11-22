
import java.sql.Connection;
import java.sql.SQLException;

import org.freeswitch.esl.ESLconnection;
import org.freeswitch.esl.ESLevent;


public class EndingQueue {
public static void main(String [] args) {
		
		/*
		 * 
		 * Once you get libesljni.so compiled you can either put it in your java library path and
		 * use System.loadlibrary or just use System.load with the absolute path.
		 *
		 */
//        System.out.println("Starting connection..");
		System.load("/usr/local/src/freeswitch/libs/esl/java/libesljni.so");
	System.load("/usr/local/freeswitch/scripts/Mod_CallCenterScripts/libesljni.so");
		/*
		 *
		 * Trying to keep this simple (and I'm no java expert) I am instantiating the ESLconnection and
		 * ESLevent object in a static reference here so remember if you don't plan on doing everything in main
		 * you will need to instantiate your class first or you will get compile-time errors.
		 *
		 */

		ESLconnection con = new ESLconnection("10.128.83.112","8021","ClueCon");

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
			if(str.equals("CUSTOM")){
				System.out.println("Got a Custom event"+str);
				str = evt.getHeader("Event-Subclass",-1);
				if(str.equals("callcenter::info")){
					System.out.println("Got a CallCenter event"+str);
					str = evt.getHeader("CC-Action",-1);
					if(str.equals("member-queue-end")){
						System.out.println("Someone Leaving the queue "+str);
						//CC-Queue: support@default
						//CC-Action: member-queue-start
						//CC-Member-UUID: 453324f8-3424-4322-4242362fd23d 
						//CC-Member-Session-UUID: b77c49c2-a732-11df-9438-e7d9456f8886
						//CC-Member-CID-Name: CHOUINARD MO
						//CC-Member-CID-Number: 4385551212
						String Caller = evt.getHeader("CC-Member-CID-Number",-1);
						if(Caller.startsWith("11")){
							Caller = Caller.substring(2);
						}
						if(Caller.length()>10){
						Caller = Caller.substring(Caller.length()-10);
						}
						String jointime = evt.getHeader("CC-Member-Joined-Time", -1);
						String leavtime = evt.getHeader("CC-Member-Leaving-Time", -1);
						Long Queuetime = (long) 0;
								Long CallDuration=(long) 0;		
								
						if(evt.getHeader("CC-Cause", -1).trim().indexOf("Terminated")>-1){
							String anstime = evt.getHeader("CC-Agent-Answered-Time", -1);
							//String hookofftime = evt.getHeader("CC-Member-Leaving-Time", -1);
							Queuetime = Long.parseLong(anstime)- Long.parseLong(jointime);
							CallDuration = Long.parseLong(leavtime)- Long.parseLong(anstime);
						}else{
							Queuetime=Long.parseLong(leavtime)- Long.parseLong(jointime);
						}
		//String Query="Insert into calls(StartTime, Number, Bound, TextualDate, CallUUID) values(Now(),'"+Caller+"','IN','"+evt.getHeader("CC-Member-UUID",-1)+"','"+evt.getHeader("CC-Member-Session-UUID",-1)+"')";
	    String Query = "Update calls set QueueTime="+Queuetime +" , Duration="+CallDuration+" where TextualDate='"+evt.getHeader("CC-Member-UUID",-1) +"' and CallUUID='"+evt.getHeader("CC-Member-Session-UUID",-1)+"'";
		System.out.println(Query);
	    update(Query);
					}
		
				}else{
					System.out.println("EVent Subclass is "+ str);
	//				System.out.println(evt.serialize("plain"));
				}
			}else{
//				System.out.println(evt.serialize("plain"));
			}

			// Print the entire event in key : value format. serialize() according to the wiki usually takes no arguments
			// but if you do not put in something you will not get any output so I just stuck plain in.
			

		}
		
	}
public static void update(String Query) {
	Connection con = new DatabaseHandler().getConnection();
	try {
//		Debug(Query);
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
