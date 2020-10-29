package in.wynk.sms.constants;

public class SMSConstants {

	public static final int INPUT_WORKER_THREADS = 1000;
	public static final int INPUT_WORKER_MAX_QUEUE = 10000000;
	public static final double INPUT_WORKER_THREADS_UTILIZATION = 0.6;
	
	public static final int OUTPUT_WORKER_THREADS = 200;
	public static final int OUTPUT_WORKER_MAX_QUEUE = 10000000;
	public static final double OUTPUT_WORKER_THREADS_UTILIZATION = 0.6;
	
	public static final String REDIS_QUEUE_KEY = "sms_queue";
	public static final String REDIS_SCHEDULED_QUEUE_KEY = "sms_scheduled_queue";
	
	public static final String REDIS_CHANNEL  =  "sms_pubsub";
	
	public static final String REDIS_SMS_INPUT_WORKERS  = "sms_input_workers_status";
	public static final String REDIS_SMS_OUTPUT_WORKERS  = "sms_output_workers_status";

	
	public static final String REDIS_PUB_MESSAGE = "Receicing Messages";
	
    public static final String       SMS_MESSAGE_SHORTCODE                   = "A$-WYNKED";

    public static final String HIGH = "HIGH";
    public static final String MEDIUM = "MEDIUM";
    public static final String LOW = "LOW";
	
	
}
