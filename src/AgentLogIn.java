import org.freeswitch.DTMFCallback;
import org.freeswitch.FreeswitchScript;
import org.freeswitch.HangupHook;
import org.freeswitch.swig.API;
import org.freeswitch.swig.JavaSession;
import org.freeswitch.swig.freeswitch;
import org.freeswitch.swig.session_flag_t;


public class AgentLogIn implements FreeswitchScript, DTMFCallback, HangupHook {

	@Override
	public void onHangup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String onDTMF(Object arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run(String arg0, String arg1) {
		// TODO Auto-generated method stub
		String File = "/usr/local/freeswitch/sounds/dwc/AgentLogin.mp3";
		JavaSession ss = new JavaSession(arg0);
		ss.answer();
		  String Telephone = ss.getVariable("caller_id_number");
		String AgentID = "";
		while(ss.ready()){
		AgentID=ss.read(1, 4, File, 2500, "#", 0);
		if(AgentID.length()==4){
			break;
		}else{
			freeswitch.console_log("ERR", "Got Agent ID :" +AgentID+"\n");
		}
		}
		API ap = new API();
		String apicall = "callcenter_config agent set contact "+AgentID+ " [call_timeout=60]user/"+Telephone;
		ap.executeString(apicall);
		apicall = "callcenter_config agent set status "+AgentID+ "Available";
		ap.executeString(apicall);
		ss.hangup("");
	}

}
