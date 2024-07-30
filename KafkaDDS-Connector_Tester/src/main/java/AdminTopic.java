import org.apache.kafka.clients.admin.Admin;
// import org.apache.kafka.clients.admin.AdminClientConfig;
 import org.apache.kafka.clients.admin.NewTopic;
 import org.apache.kafka.clients.admin.ListTopicsOptions;
 import org.apache.kafka.clients.admin.CascadeDDSAdminClient;
 import org.apache.kafka.clients.admin.ListTopicsResult;
 import org.apache.kafka.clients.admin.TopicListing;
 import org.apache.kafka.clients.admin.DescribeTopicsResult;
 import org.apache.kafka.clients.admin.TopicDescription;
 import org.apache.kafka.common.TopicCollection;

// import org.apache.kafka.clients.admin.DescribeClusterResult;
// import org.apache.kafka.clients.admin.TopicDescription; 
// import org.apache.kafka.common.Node;
// import org.apache.kafka.common.TopicPartitionInfo;

import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;
import java.util.Optional;
import java.util.Map;

public class AdminTopic
{
    private Admin adminClient = null;

    public static void main( String[] args )
    {
	Scanner keyboard = new Scanner(System.in);
        Properties properties = new Properties();
        properties.setProperty("useCascadeDDSImpl", "true");
	properties.setProperty("DDSConfigPath",".");
       	 
        // Using the new admin calss
        Admin adminClient = Admin.create(properties);
	
	Optional<Integer> numPartitions = Optional.empty();
	Optional<Short> replicationFactor = Optional.empty();
	int cont = 0;
	while (cont ==0){
	System.out.println("What should the path be for the topic?");
	String path = keyboard.nextLine();
	NewTopic topic = new NewTopic(path,numPartitions,replicationFactor);
	try{       
       	adminClient.createTopics(Collections.singleton(topic)).all().get();
	System.out.println("Successfully created Topic");
	}
	catch(Exception e){

	System.out.println("Failure to CreateTopic");
	}
	ListTopicsResult list_out  = adminClient.listTopics(new ListTopicsOptions());
	try{
	Map<String, TopicListing> stringToTopicList = list_out.namesToListings().get();

	for (Map.Entry<String, TopicListing> entry: stringToTopicList.entrySet()){
		System.out.println(entry.getKey() + " : " + entry.getValue());
	}
	}catch (Exception e){

		System.out.println("Failure to Get List of Topics");
	}
	System.out.println("Want to keep making topics?(yes/no");
		if (keyboard.nextLine().equalsIgnoreCase("yes")){
			cont = 0;
		}else{
			cont = 1;
		}
	}
	System.out.println("Enter path name of topic to delete?");
	String path2 = keyboard.nextLine();
	TopicCollection temp2 = TopicCollection.ofTopicNames(Collections.singleton(path2));

	adminClient.deleteTopics(temp2, null);

	System.out.println("Enter path name of topic you want me to describe?");
	String path3 = keyboard.nextLine();
	TopicCollection temp3 = TopicCollection.ofTopicNames(Collections.singleton(path3));

	DescribeTopicsResult output = adminClient.describeTopics(temp3,null);
	System.out.println("The name and topic id of the topic you asked me to describe is:");
	try{
       	for (Map.Entry<String,TopicDescription> entry: output.allTopicNames().get().entrySet()){

		System.out.println(entry.getKey() +  " : " + entry.getValue());
	}
	}
	catch(Exception e){
		System.out.println("Failure to printout topic description");
	}
    }
}
