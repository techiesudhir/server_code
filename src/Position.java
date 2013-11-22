

import java.sql.Connection;
import java.sql.SQLException;

import org.freeswitch.esl.ESLconnection;
import org.freeswitch.esl.ESLevent;
import org.freeswitch.swig.API;
import org.freeswitch.swig.freeswitch;
import org.freeswitch.FreeswitchScript;
public class Position implements Runnable{
	String Calluuid,id;
	String sd="/usr/local/freeswitch/sounds/dwc/announce/";
	public Position(String Cuid) {
String[] str=Cuid.split(",");
		
		this.Calluuid = str[0];
		this.id=str[1];
	}
	
	public void Announce(){
//		System.load("/usr/local/src/freeswitch/libs/esl/java/libesljni.so");
		System.load("/usr/local/freeswitch/scripts/Mod_CallCenterScripts/libesljni.so");
		//ESLconnection con = new ESLconnection("10.128.83.112","8021","ClueCon");
		ESLconnection con = new ESLconnection("127.0.0.1","8021","ClueCon");
		ESLevent evt;

		while(con.connected()==1){
			int pos =0;
			int i=0;
			while(i<2000){
			con.recvEventTimed(1);
			i++;
			}
			i=0;
			System.out.println("System going to say position");
			//evt= con.executeAsync("callcenter_config", "queue list members support@default",Calluuid);
			evt = con.sendRecv("api callcenter_config queue list members dwc@default");
			//evt = con.sendRecv("callcenter_config queue list members support@default");
			String Mesg=evt.serialize("plain");
			System.out.println(Mesg);
			String[] lines = Mesg.split(System.getProperty("line.separator"));
			boolean callerfound=false;
			for(String ln: lines){
			if((ln.indexOf("Trying")>-1)||(ln.indexOf("Waiting")>-1)){
				pos++;
				if(ln.indexOf(Calluuid)>-1){
					callerfound=true;
					System.out.println("Caller position is : "+pos);
					String Query = "Insert into Announcement_Details(Announcement_FK,Pvalue,ValueAnnounced,Mtype,Dt) values(";
					Query+=id+",'"+pos+"','"+pos+"','M1',NOW())";
					update(Query);
					System.out.println("Caller position is : "+pos);
					con.send("api uuid_broadcast "+Calluuid+" "+sd+"katar.wav");
					con.send("api uuid_broadcast "+Calluuid+" "+sd+"position.wav");
					con.send("api uuid_broadcast "+Calluuid+" "+sd+"digits/"+pos+".wav");
				}
			}
			}
			if (callerfound==false)break;
		try {
			System.out.println("System going to sleep");
			Thread.sleep(19600);
			System.out.println("System waked up!");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
//		freeswitch.consoleLog("INFO", "Announcing");
	//	API ap = new org.freeswitch.swig.API();
		//String reply = ap.execute("callcenter_config", "queue list members dwc@default");
		//System.out.println(reply);
		
	}

	

	@Override
	public void run() {
		// TODO Auto-generated method stub
	Announce();	
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
