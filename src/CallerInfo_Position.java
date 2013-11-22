

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.freeswitch.esl.ESLconnection;
import org.freeswitch.esl.ESLevent;
public class CallerInfo_Position {
	
	/* M0 NUll 
	 * M1 Position
	 * M2 Simple time
	 * M3 Dynamic Time
	 * M4 Simple Manipulated Time
	 * M5 Simple Message
	 */
	
	/* Result Expected
	 * Call Drop V/s Call Connected
	 */
	public static void main(String [] args) {
		//System.load("/usr/local/src/freeswitch/libs/esl/java/libesljni.so");
		System.load("/usr/local/freeswitch/scripts/Mod_CallCenterScripts/libesljni.so");
		ESLconnection con = new ESLconnection("127.0.0.1","8021","ClueCon");
	//	ESLconnection con = new ESLconnection("10.128.83.112","8021","ClueCon");
		ESLevent evt;
		if (con.connected() == 1) System.out.println("connected");
		con.events("plain","all");
		
		int mode =0;
		while (con.connected() == 1) {						
			// Get an event - recvEvent will block if nothing is queued
			evt = con.recvEvent();
			//System.out.println(evt.serialize("plain"));
			String str = "";
			//System.out.println("Got the body as :"+str);
			str = evt.getHeader("Event-Name",-1);
			if(str==null){
				System.out.println("Got nul event skipping");
				continue;
			}
			if(str.equals("CUSTOM")){
				//System.out.println("Got a Custom event"+str);
				str = evt.getHeader("Event-Subclass",-1);
				if(str.equals("callcenter::info")){
					System.out.println("Got a CallCenter event"+str);
					str = evt.getHeader("CC-Action",-1);
					//System.out.println(evt.serialize("plain"));
					if(str.equals("member-queue-start")){
						System.out.println("Someone entering the queue "+str);
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
						
						String Query="Insert into Announcement(Number, Date, Mtype,Textualdate,CallUUID) value('"+Caller+"',NOW(),'M"+mode+"','"+evt.getHeader("CC-Member-UUID",-1)+"','"+evt.getHeader("CC-Member-Session-UUID",-1)+"')";
						String Mesg= evt.getHeader("CC-Member-Session-UUID",-1)+",";
								Mesg+=updateandReturn(Query);
						if(mode<5){
							if(mode==0){								
								//SimpleMessage p = new SimpleMessage(Mesg);
								//Thread th = new Thread(p);
								//th.start();
							}else if(mode==1){
								Position p = new Position(Mesg);
								Thread th = new Thread(p);
								th.start();
							}
							else if(mode==2){
								SimpleTime p = new SimpleTime(Mesg);
								Thread th = new Thread(p);
								th.start();
							}
							else if(mode==3){
								DynamicTime p = new DynamicTime(Mesg);
								Thread th = new Thread(p);
								th.start();
							}
							else if(mode==4){
								ManipulatedTime p = new ManipulatedTime(Mesg);
								Thread th = new Thread(p);
								th.start();
							}
							
					//	message_announce(evt.getHeader("CC-Member-Session-UUID",-1));
						mode++;
						}else{
							 if(mode==5){
									SimpleMessage p = new SimpleMessage(Mesg);
									Thread th = new Thread(p);
									th.start();			
									
								}
							mode=0;
						}
//						System.out.println(evt.serialize("plain"));
						//String Query="Insert into calls(StartTime, Number, Bound, TextualDate, CallUUID) values(Now(),'"+Caller+"','IN','"+evt.getHeader("CC-Member-UUID",-1)+"','"+evt.getHeader("CC-Member-Session-UUID",-1)+"')";
	//update(Query);
									
	}
		
				}else{
				//	System.out.println("EVent Subclass is "+ str);
	//				System.out.println(evt.serialize("plain"));
				}
			}else{
//				System.out.println(evt.serialize("plain"));
			}

			// Print the entire event in key : value format. serialize() according to the wiki usually takes no arguments
			// but if you do not put in something you will not get any output so I just stuck plain in.			

		}
	}
	private static void message_announce(String str) {
		// TODO Auto-generated method stub
		Class IVRApp;
		Position p = new Position(str);
		Thread th = new Thread(p);
		th.start();
//		try {
//			//IVRApp = Class.forName("CallInfo.Position");
//			//Class parameter[]={String.class};
//			//Object param = str;
//			//Object obj = IVRApp.getConstructor(parameter).newInstance(param);
//			//IVRApp.getDeclaredMethod("Announce").invoke(obj);
//		
//		
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	}
	public static String updateandReturn(String Query) {
		Connection con = new DatabaseHandler().getConnection();
	    String id ="";
		try {
			con.createStatement().executeUpdate(Query);
			ResultSet rs = con.createStatement().executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
					id = rs.getString(1);
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
		return id;

	}
	public static void update(String Query) {
		Connection con = new DatabaseHandler().getConnection();
		try {
			//Debug(Query);
			con.createStatement().executeUpdate(Query);
			
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
