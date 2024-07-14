package Kafka_API.clients.admin;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collection;
import java.time.Duration;
import java.util.Map;

public class DDSAdminClient implements Admin{

	// private String metadata_pathname;

	// private ConcurrentHashMap<String,Topic> topics;
 	private long DDSMetadataClient_ptr;

	static{
		System.loadLibrary("dds_Kafka_API_jni"); 
	}

	public DDSAdminClient(long input){
		this.DDSMetadataClient_ptr = input;
	}

	public static native long createInternal_native();
	public static Admin createInternal(){return new DDSAdminClient(createInternal_native());}

	public native void createTopics_native(long pointer, Collection<NewTopic> newTopics);

	public CreateTopicsResult createTopics(Collection<NewTopic> newTopics){
		this.createTopics_native(this.DDSMetadataClient_ptr, newTopics);
		return new CreateTopicsResult();
	}
	public native void close(Duration timeout);
}
