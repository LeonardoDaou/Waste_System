import java.io.File;

import core.LarsFederate;
import core.LarsFederateAmbassador;
import skf.config.ConfigurationFactory;
import model.Lars;
import model.Position;
import model.Quaternion;

public class LarsMain {
    
    private static final File conf = new File("config/conf.json");
    
    public static void main(String[] args) throws Exception {
        
    	// Placeholder starting position. Will update dynamically later
    	Lars lars = new Lars(
                "Lars", 
                "Lars", 
                "AitkenBasinLocalFixed",
                new Position(600, -7100, -2000),  // Adjustable start position
                new Quaternion(0, 0, 0, 1));
        
        LarsFederateAmbassador ambassador = new LarsFederateAmbassador();
        LarsFederate federate = new LarsFederate(ambassador, lars);
        
        // start execution
        federate.configureAndStart(new ConfigurationFactory().importConfiguration(conf));
    }
}
