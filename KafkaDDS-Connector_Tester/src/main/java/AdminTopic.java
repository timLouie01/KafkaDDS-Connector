import org.apache.kafka.clients.admin.Admin;
// import org.apache.kafka.clients.admin.AdminClientConfig;
 import org.apache.kafka.clients.admin.NewTopic;
 import org.apache.kafka.clients.admin.ListTopicsOptions;

// import org.apache.kafka.clients.admin.DescribeClusterResult;
// import org.apache.kafka.clients.admin.TopicDescription; 
// import org.apache.kafka.common.Node;
// import org.apache.kafka.common.TopicPartitionInfo;

import java.util.Collections;
import java.util.Properties;

import java.util.Optional;

public class AdminTopic
{
    private Admin adminClient = null;

    public static void main( String[] args )
    {
        Properties properties = new Properties();
        properties.setProperty("useCascadeDDSImpl", "true");
	properties.setProperty("DDSConfigPath",".");
       	 
        // Using the new admin calss
        Admin adminClient = Admin.create(properties);
	
	Optional<Integer> numPartitions = Optional.empty();
	Optional<Short> replicationFactor = Optional.empty();

	NewTopic topic = new NewTopic("/dds/tiny_text/Topic_1",numPartitions,replicationFactor);
	try{       
       	adminClient.createTopics(Collections.singleton(topic)).all().get();
	}
	catch(Exception e){

	System.out.println("Failure to CreateTopic");
	}
	adminClient.listTopics(new ListTopicsOptions());

    }
}
