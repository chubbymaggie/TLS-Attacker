package tlsattacker.fuzzer.config;

import tlsattacker.fuzzer.config.mutator.ActionExecutorTypeConfig;
import tlsattacker.fuzzer.config.mutator.SimpleMutatorConfig;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.validators.PositiveInteger;
import de.rub.nds.tlsattacker.tls.config.ClientCommandConfig;
import de.rub.nds.tlsattacker.tls.config.converters.FileConverter;
import de.rub.nds.tlsattacker.tls.config.validators.PercentageValidator;
import java.io.File;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;

/**
 * A Config File which controls the EvolutionaryFuzzer.
 * 
 * @author Robert Merget - robert.merget@rub.de
 * @author Juraj Somorovsky <juraj.somorovsky@rub.de>
 */
@Parameters(commandDescription = "Starts the Fuzzer")
public class EvolutionaryFuzzerConfig extends FuzzerGeneralConfig {

    /**
     *
     */
    public static final String ATTACK_COMMAND = "fuzzer";
    private static final Logger LOG = Logger.getLogger(EvolutionaryFuzzerConfig.class.getName());

    @Parameter(names = "-output_folder", description = "Output folder for the fuzzing results.", converter = FileConverter.class)
    private String outputFolder = "./data/";
    @Parameter(names = "-archive_folder", description = "Archive Folder that contains TestVectors that should seed the Fuzzer", converter = FileConverter.class)
    private String archiveFolder = "./archive/";
    @Parameter(names = "-threads", description = "Number of Threads running Simultaniously, (Default:Number of Server in Config)", validateWith = PositiveInteger.class)
    private Integer threads = -1;
    @Parameter(names = "-mutator", description = "The Mutator the Fuzzer uses to generate new TestVectors (Default: simple). Possible: simple")
    private String mutator = "simple";
    @Parameter(names = "-certificate_mutator", description = "The Mutator the Fuzzer uses to generate new Certificates (Default: fixed). Possible: fixed")
    private String certMutator = "fixed";
    @Parameter(names = "-no_old", description = "The mutator wont run WorkflowTraces he finds in the Good WorkflowTrace Folder, before he starts generating new Mutations")
    private boolean noOld = false;
    @Parameter(names = "-start_stopped", description = "Starts the Fuzzer in a stopped state.")
    private boolean startStopped = false;
    @Parameter(names = "-clean_start", description = "Deletes previous good Workflows on startup")
    private boolean cleanStart = false;

    @Parameter(names = "-inject_pin_child", description = "If the PIN Agent should instrument into the Childprocess")
    private boolean injectPinChild = true;
    @Parameter(names = "-certificate_mutator_selftest", description = "Test that the CertificateMutator is properly configured at start")
    private boolean certMutatorSelftest = false;
    @Parameter(names = "-use_kill", description = "Uses the kill command specified in the server configuration files.")
    private boolean useKill = false;

    private File tracesFolder; // Temporary Folder which contains currently
			       // executed traces
    private ActionExecutorTypeConfig actionExecutorConfig;

    public boolean isUseKill() {
	return useKill;
    }

    public void setUseKill(boolean useKill) {
	this.useKill = useKill;
    }

    public ActionExecutorTypeConfig getActionExecutorConfig() {
	return actionExecutorConfig;
    }

    public void setActionExecutorConfig(ActionExecutorTypeConfig actionExecutorConfig) {
	this.actionExecutorConfig = actionExecutorConfig;
    }

    public String getArchiveFolder() {
	return archiveFolder;
    }

    public void setArchiveFolder(String archiveFolder) {
	this.archiveFolder = archiveFolder;
    }

    public File getTracesFolder() {
	return tracesFolder;
    }

    public String getCertMutator() {
	return certMutator;
    }

    public void setCertMutator(String certMutator) {
	this.certMutator = certMutator;
    }

    public String getMutator() {
	return mutator;
    }

    public void setMutator(String mutator) {
	this.mutator = mutator;
    }

    public boolean isCleanStart() {
	return cleanStart;
    }

    public void setCleanStart(boolean cleanStart) {
	this.cleanStart = cleanStart;
    }

    public boolean isStartStopped() {
	return startStopped;
    }

    public void setStartStopped(boolean startStopped) {
	this.startStopped = startStopped;
    }

    public boolean isNoOld() {
	return noOld;
    }

    public void setNoOld(boolean noOld) {
	this.noOld = noOld;
    }

    // Are we currently in serialization mode?
    private boolean serialize = false;

    /**
     * Constructor for EvolutionaryFuzzerConfig, defaults output Folder to "."
     * and serverCommandFromFile to server/server.config
     */
    public EvolutionaryFuzzerConfig() {
	outputFolder = "data/";
	setFuzzingMode(true);
	setKeystore("../resources/rsa1024.jks");
	setPassword("password");
	setAlias("alias");
	this.tracesFolder = new File(outputFolder + "traces/");
	tracesFolder.mkdirs();
	new File(getOutputCertificateFolder()).mkdirs();
	new File(getOutputClientCertificateFolder()).mkdirs();
	new File(getOutputFolder()).mkdirs();
	new File(getOutputServerCertificateFolder()).mkdirs();
	new File(getCertificateMutatorConfigFolder()).mkdirs();
	new File(getAnalyzerConfigFolder()).mkdirs();
	new File(getOutputFaultyFolder()).mkdirs();
	new File(getArchiveFolder()).mkdirs();
	File f = new File(getMutatorConfigFolder() + "action_executor.conf");
	if (f.exists()) {
	    actionExecutorConfig = JAXB.unmarshal(f, ActionExecutorTypeConfig.class);
	} else {
	    actionExecutorConfig = new ActionExecutorTypeConfig();
	    JAXB.marshal(actionExecutorConfig, f);
	}
    }

    public boolean isSerialize() {
	return serialize;
    }

    public void setSerialize(boolean serialize) {
	this.serialize = serialize;
    }

    public Integer getThreads() {
	return threads;
    }

    public void setThreads(Integer threads) {
	this.threads = threads;
    }

    /**
     * Returns the Path to the Folder in which the Fuzzer will save its output
     * to. The Server will genereate several Folder in the Output Folder.
     * 
     * @return Path to the Folder in which the Fuzzer will save its output to
     */
    public String getOutputFolder() {
	return outputFolder;
    }

    public String getOutputCertificateFolder() {
	return outputFolder + "certificates/";
    }

    public String getOutputClientCertificateFolder() {
	return outputFolder + "certificates/client/";
    }

    public String getOutputServerCertificateFolder() {
	return outputFolder + "certificates/server/";
    }

    public String getOutputFaultyFolder() {
	return outputFolder + "faulty/";
    }

    /**
     * Sets the Path to the Folder in which the Fuzzer will save its output to.
     * The Server will genereate several Folder in the Output Folder.
     * 
     * @param outputFolder
     */
    public void setOutputFolder(String outputFolder) {
	this.outputFolder = outputFolder;
	File f = new File(outputFolder);
	f.mkdirs();
	this.tracesFolder = new File(outputFolder + "traces/");

    }

    public boolean getInjectPinChild() {
	return injectPinChild;
    }

    public void setCertMutatorSelftest(boolean certMutatorSelftest) {
	this.certMutatorSelftest = certMutatorSelftest;
    }

    public boolean isCertMutatorSelfTest() {
	return certMutatorSelftest;
    }

}
