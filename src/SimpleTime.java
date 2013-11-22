

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.freeswitch.esl.ESLconnection;
import org.freeswitch.esl.ESLevent;
import org.freeswitch.swig.API;
import org.freeswitch.swig.freeswitch;
import org.freeswitch.FreeswitchScript;
public class SimpleTime implements Runnable{
	String Calluuid,id;
	String sd="/usr/local/freeswitch/sounds/dwc/announce/";
	public SimpleTime(String Cuid) {
String[] str=Cuid.split(",");
		
		this.Calluuid = str[0];
		this.id=str[1];
	}
	
	public void Announce(){
		System.load("/usr/local/src/freeswitch/libs/esl/java/libesljni.so");
		//ESLconnection con = new ESLconnection("10.128.83.112","8021","ClueCon");
		ESLconnection con = new ESLconnection("127.0.0.1","8021","ClueCon");
		ESLevent evt;
		Date Temp = new Date();
		Long Callstart=Temp.getTime();
		Long waitime = (long) 109;
		while(con.connected()==1){
			int pos =0;
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
					Date dt = new Date();
					Long TimeNow=dt.getTime();
					Long waitremaining = waitime-((TimeNow-Callstart)/1000);
					if(waitremaining<0){callerfound=false;
					break;
					}
					int Tenth = (int) (waitremaining/10);
					String Query = "Insert into Announcement_Details(Announcement_FK,Pvalue,ValueAnnounced,Mtype,Dt) values(";
					Query+=id+",'"+pos+"','"+Tenth+"0','M2',NOW())";
					update(Query);
					System.out.println("Caller position is : "+pos);
					con.send("api uuid_broadcast "+Calluuid+" "+sd+"katar.wav");
					con.send("api uuid_broadcast "+Calluuid+" "+sd+"time.wav");
					if(Tenth<11){
					con.send("api uuid_broadcast "+Calluuid+" "+sd+"digits/"+Tenth+"0.wav");
					}else{
						
						con.send("api uuid_broadcast " + Calluuid + " "
								+ sd + "digits/" + "100.wav");
						
					}
					con.send("api uuid_broadcast "+Calluuid+" "+sd+"seconds.wav");
					//	con.send("api uuid_broadcast "+Calluuid+" "+sd+"");
				}
			}
			}
			if (callerfound==false)break;
		try {
			Thread.sleep(196000);
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

