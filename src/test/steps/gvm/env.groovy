package gvm

import static cucumber.api.groovy.Hooks.*
import static gvm.VertxUtils.*

SERVICE_DOWN_URL = "http://localhost:0"
SERVICE_UP_URL = "http://localhost:8080"
FAKE_JDK_PATH = "/path/to/my/openjdk"

forceOffline = false
buildScriptDir = new File("build/testScripts")

counter = "${(Math.random() * 10000).toInteger()}".padLeft(4, "0")

localGroovyCandidate = "/tmp/groovy-core" as File

gvmVersion = "x.y.z"
gvmVersionOutdated = "x.y.y"

gvmBaseEnv = "/tmp/gvm-$counter"
gvmBaseDir = gvmBaseEnv as File

gvmDirEnv = "$gvmBaseEnv/.gvm"
gvmDir = gvmDirEnv as File
binDir = "${gvmDirEnv}/bin" as File
srcDir = "${gvmDirEnv}/src" as File
varDir = "${gvmDirEnv}/var" as File
etcDir = "${gvmDirEnv}/etc" as File
extDir = "${gvmDirEnv}/ext" as File
archiveDir = "${gvmDirEnv}/archives" as File
tmpDir = "${gvmDir}/tmp" as File

broadcastFile = new File(varDir, "broadcast")
candidatesFile = new File(varDir, "candidates")
initScript = new File(binDir, "gvm-init.sh")

server = null
bash = null

Before(){
	cleanUp()
	server = startServer(gvmVersion)

	binDir.mkdirs()
    srcDir.mkdirs()
	varDir.mkdirs()
    etcDir.mkdirs()
    extDir.mkdirs()
    archiveDir.mkdirs()
    tmpDir.mkdirs()

    // Initialise candidate directories
    ['groovy', 'grails'].each { candidate ->
        def candidateDir = "${gvmDirEnv}/${candidate}" as File
        candidateDir.mkdirs()
    }

    // Initialise broadcast file
    broadcastFile << 'This is a LIVE Broadcast!'

    // Initialise candidates file
    candidatesFile << 'groovy,grails'

    // Copy the init script into the gvm bin folder
    initScript << new File(buildScriptDir, 'gvm-init.sh').text

	// Copy all modular scripts into the gvm src folder
    for (f in buildScriptDir.listFiles()){
        if(!(f.name in ['selfupdate.sh', 'install.sh', 'gvm-init.sh'])){
            new File(srcDir, f.name) << f.text
        }
    }
}

After(){ scenario ->
    def output = bash?.output
    if (output) {
        scenario.write("\nOutput: \n${output}")
    }
	bash?.stop()
    cleanUp()
}

private cleanUp(){
    gvmBaseDir.deleteDir()
    localGroovyCandidate.deleteDir()
}
