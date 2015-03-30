/**
 * dynaTrace Nagios Alerting Plugin 
 * Created By Balazs Bakai
 * www.bakaibalazs.hu
 **/

package hu.bakaibalazs.dynatrace.nagios;

import java.util.logging.Logger;

import com.dynatrace.diagnostics.pdk.Action;
import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Violation;
import com.googlecode.jsendnsca.MessagePayload;
import com.googlecode.jsendnsca.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.NagiosSettings;
import com.googlecode.jsendnsca.builders.MessagePayloadBuilder;
import com.googlecode.jsendnsca.builders.NagiosSettingsBuilder;
import com.googlecode.jsendnsca.encryption.Encryption;
import com.googlecode.jsendnsca.Level;

public class NagiosAction implements Action {

	private static final Logger log = Logger.getLogger(NagiosAction.class.getName());

	private static final String DYNATRACE_PREFIX = "dynaTrace_";

	private NagiosSettings settings;
	private NagiosPassiveCheckSender sender;

	/**
	 * Initializes the Action Plugin. This method is always called before execute.
	 */
	@Override
	public Status setup(ActionEnvironment env) throws Exception {
		log.fine("Nagios Alerting Setup");

		String nagiosHostNameOrIPAddress = env.getConfigString("nagiosHostNameOrIPAddress");
		String nscaPassword = env.getConfigPassword("nscaPassword");				
		String nscaEncryption = env.getConfigString("nscaEncryption");
		int nscaListeningPort = Integer.parseInt(env.getConfigString("nscaListeningPort"));
		int connectionTimeout = Integer.parseInt(env.getConfigString("connectionTimeout"));
		int socketTimeout = Integer.parseInt(env.getConfigString("socketTimeout"));
		
		logConfigurationProperties(nagiosHostNameOrIPAddress, nscaListeningPort, nscaPassword, nscaEncryption, connectionTimeout, socketTimeout);

		settings = new NagiosSettingsBuilder().withNagiosHost(nagiosHostNameOrIPAddress).withPassword(nscaPassword).withPort(nscaListeningPort)
				.withEncryption(Encryption.valueOf(nscaEncryption)).withConnectionTimeout(connectionTimeout).withResponseTimeout(socketTimeout).create();

		sender = new NagiosPassiveCheckSender(settings);

		return new Status(Status.StatusCode.Success);
	}

	/**
	 * This method is called at the scheduled intervals, but only if incidents occurred in the meantime.
	 */
	@Override
	public Status execute(ActionEnvironment env) throws Exception {
		log.info("Nagios Alerting Execute");
		for (Incident i : env.getIncidents()) {
			logIncidents(i);

			MessagePayload payload = new MessagePayloadBuilder().withHostname(DYNATRACE_PREFIX + i.getKey().getSystemProfile())
					.withLevel(i.isOpen() ? getNagiosSeverity(i) : Level.OK).withServiceName(i.getIncidentRule().getName()).withMessage(i.getMessage())
					.create();

			try {
				sender.send(payload);
				log.info("Incident is sent successfully to Nagios");
			} catch (Exception e) {
				log.severe("Exception message: " + e.getMessage());
				log.severe("Exception trace: " + stackTraceToString(e));
			}
		}
		return new Status(Status.StatusCode.Success);
	}

	public String stackTraceToString(Throwable e) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	private Level getNagiosSeverity(Incident paramIncident) {
		Incident.Severity localSeverity = paramIncident.getSeverity();

		if (localSeverity == Incident.Severity.Error) // NAGIOS CRITICAL
			return Level.CRITICAL;
		if (localSeverity == Incident.Severity.Warning) // NAGIOS WARNING
			return Level.WARNING;
		if (localSeverity == Incident.Severity.Informational) { // NAGIOS OK
			return Level.OK;
		}
		return Level.UNKNOWN; // NAGIOS UNKNOWN
	}

	private void logConfigurationProperties(String nagiosHostNameOrIPAddress, Integer nscaListeningPort, String password, String nscaEncryption,
			Integer connectionTimeout, Integer socketTimeout) {

		log.fine("nagiosHostNameOrIPAddress: " + nagiosHostNameOrIPAddress);
		log.fine("nscaListeningPort: " + nscaListeningPort);
		log.fine("nscaEncryption: " + nscaEncryption);
		log.fine("connectionTimeout: " + connectionTimeout);
		log.fine("socketTimeout: " + socketTimeout);
	}

	private void logIncidents(Incident incident) {		
		log.fine("LEVEL:" + (incident.isOpen() ? getNagiosSeverity(incident) : Level.OK));
		log.fine("RULE:" + incident.getIncidentRule().getName());
		log.fine("MSG:" + incident.getMessage());
		log.fine("SERVER:" + DYNATRACE_PREFIX + incident.getKey().getSystemProfile());

		String message = incident.getMessage();
		log.info("Incident " + message + " triggered.");
		for (Violation violation : incident.getViolations()) {
			log.info("Measure " + violation.getViolatedMeasure().getName() + " violoated threshold.");
		}
	}

	/**
	 * Shuts the Plugin down and frees resources. This method is called either way if the Action setup/execution has failed or was successful.
	 */
	@Override
	public void teardown(ActionEnvironment env) throws Exception {
	}

}
