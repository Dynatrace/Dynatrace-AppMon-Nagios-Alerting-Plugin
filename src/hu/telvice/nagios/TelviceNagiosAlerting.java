package hu.telvice.nagios;

import java.io.InputStreamReader;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.pdk.Action;
import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.Status;

public class TelviceNagiosAlerting implements Action {

	private static final Logger log = Logger.getLogger(TelviceNagiosAlerting.class.getName());

	@Override
	public Status execute(ActionEnvironment env) throws Exception {
		log.info("Execute: Telvice Nagios Alerting Plugin");

		String telviceNagiosScriptLocation = env.getConfigString("TelviceNagiosScriptLocation");

		String incident = "";
		for (Incident i : env.getIncidents()) {
			incident = createIncidentParameter(i);
			log.info("incident string: " + incident);
		}

		Process p = Runtime.getRuntime().exec(telviceNagiosScriptLocation + " " + incident);
		p.waitFor();
		log.info("Process exited with code = " + p.exitValue());
		logOutput(p);
		return new Status(Status.StatusCode.Success);
	}

	private void logOutput(Process p) throws Exception {
		String s = null;
		java.io.BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((s = reader.readLine()) != null) {
			log.info(s);
		}
		reader.close();
	}

	private String createIncidentParameter(Incident i) {

		StringBuffer sb = new StringBuffer();

		String serverName = "";
		try {
			serverName = i.getMessage().substring(i.getMessage().lastIndexOf("@") + 1, i.getMessage().lastIndexOf("'"));
		} catch (Exception e) {
			serverName = "UNDETERMINED SERVER NAME";
			log.severe(e.getMessage());
		}

		sb.append(serverName);
		sb.append("#");
		sb.append(i.getIncidentRule().getName());
		sb.append("#");
		sb.append(i.isOpen() ? getNagiosSeverity(i) : 0);
		sb.append("#");
		sb.append(i.getMessage());
		return sb.toString();
	}

	private int getNagiosSeverity(Incident paramIncident) {
		Incident.Severity localSeverity = paramIncident.getSeverity();

		if (localSeverity == Incident.Severity.Error) // NAGIOS CRITICAL
			return 2;
		if (localSeverity == Incident.Severity.Warning) // NAGIOS WARNING
			return 1;
		if (localSeverity == Incident.Severity.Informational) { // NAGIOS OK
			return 0;
		}
		return 3; // NAGIOS UNKNOWN
	}

	@Override
	public Status setup(ActionEnvironment env) throws Exception {
		return new Status(Status.StatusCode.Success);
	}

	@Override
	public void teardown(ActionEnvironment env) throws Exception {
	}
}
