package in.wynk.sms.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
public class DummyTask {
	
	private int numOfThreads = 100;
    private ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
    
    private static final Logger logger          = LoggerFactory.getLogger(DummyTask.class.getCanonicalName());

    public void dummyCall(String msisdn,String  fromShortCode, String message,boolean useDND,long createTimestamp, String priority, String id) {
    	executorService.submit(new Dummy(message, msisdn, createTimestamp, priority, id));
    }
    
    private class Dummy implements Callable<Boolean> {

        private String smsXml;
        private String msisdn;
        private long createTimestamp;
        private String id;
        private String priority;

        Dummy(String smsTxt, String msisdn, long createTimestamp, String priority, String id) {
            this.smsXml = smsTxt;
            this.msisdn = msisdn;
            this.createTimestamp = createTimestamp;
            this.id = id;
            this.priority = priority;
        }

        @Override
        public Boolean call() throws Exception {
        	logger.info("Dummy message sending to : " + msisdn + " : " + smsXml );
        	Random rand = new Random();
        	int random = rand.nextInt(100) + 200;
        	long startTime = System.currentTimeMillis();
        	try {
    			Thread.sleep(random);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
			logger.info("Time by AirtelSMSSender. SMS id:"+  id +   " : SMS Priority: " + priority + " : Time :" + (System.currentTimeMillis() - startTime)   + " ms");
        	logger.info("Dummy Delivered SMS. SMS id:"+  id + " : SMS Priority: " + priority + " : Total time taking in delivery :" + (System.currentTimeMillis() - createTimestamp)   + " ms");
        	return true;
        }
    }    
  
}