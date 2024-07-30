#include <cstdio>
#include <jni.h>
#include <memory_resource>
#include <string>
#include <queue>
#include <cascade_dds/dds.hpp>
#include <unordered_map>
#include "../headers/org_apache_kafka_clients_admin_CascadeDDSAdminClient.h"

using namespace derecho::cascade;


std::string extractTopicName (const std::string& path){

	auto pos = path.find_last_of('/');
	return path.substr(pos +1);	
}

JNIEXPORT jlong JNICALL Java_org_apache_kafka_clients_admin_CascadeDDSAdminClient_createInternal_1native
  (JNIEnv* env, jclass cls, jstring path){
	  auto temp = DDSMetadataClient::create(DDSConfig::get());
          return reinterpret_cast<jlong>(temp.release());
  }

JNIEXPORT jobject JNICALL Java_org_apache_kafka_clients_admin_CascadeDDSAdminClient_createTopics_1native
  (JNIEnv* env, jobject obj, jlong ptr, jobject newTopics){



	std::vector<std::string> topics_added;

  	jclass collectionClass = env->FindClass("java/util/Collection");
	jmethodID iteratorMethod = env-> GetMethodID(collectionClass, "iterator","()Ljava/util/Iterator;");
	jobject iterator = env->CallObjectMethod(newTopics, iteratorMethod);
	jclass iteratorClass = env->FindClass("java/util/Iterator");
	jmethodID hasNextMethod = env->GetMethodID(iteratorClass, "hasNext", "()Z");
	jmethodID nextMethod = env->GetMethodID(iteratorClass, "next","()Ljava/lang/Object;");

	jclass newTopicClass = env->FindClass("org/apache/kafka/clients/admin/NewTopic");
	jmethodID nameMethod = env->GetMethodID(newTopicClass, "name", "()Ljava/lang/String;");

	DDSMetadataClient* pointer = reinterpret_cast<DDSMetadataClient*>(ptr);

	while(env->CallBooleanMethod(iterator, hasNextMethod))
	{
		jobject newTopic = env->CallObjectMethod(iterator, nextMethod);
		jstring name = (jstring)env->CallObjectMethod(newTopic, nameMethod);
		const char* nameC = env->GetStringUTFChars(name,nullptr);
		std::string nameStr (nameC);
			
		Topic temp {extractTopicName(nameStr),nameStr};	
		pointer->create_topic(temp);
		topics_added.emplace_back(nameStr);

		env->ReleaseStringUTFChars (name, nameC);
		env->DeleteLocalRef(name);
		env->DeleteLocalRef(newTopic);
	}
	env->DeleteLocalRef(iterator);
	env->DeleteLocalRef(iteratorClass);
	env->DeleteLocalRef(collectionClass);
	env->DeleteLocalRef(newTopicClass);

	jclass stringClass = env->FindClass("java/lang/String");
	jobjectArray stringArray = env->NewObjectArray(topics_added.size(), stringClass, nullptr);

	for (size_t i = 0; i < topics_added.size(); ++i) {
	        env->SetObjectArrayElement(stringArray, i, env->NewStringUTF(topics_added[i].c_str()));
	}
	return stringArray;

  }
JNIEXPORT void JNICALL Java_org_apache_kafka_clients_admin_CascadeDDSAdminClient_close_1native
  (JNIEnv* env, jobject obj, jlong ptr,jobject time){


	  std::unique_ptr<DDSMetadataClient> pointer (reinterpret_cast<DDSMetadataClient*>(ptr));
	

  }

/*
JNIEXPORT jobjectArray JNICALL Java_org_apache_kafka_clients_admin_CascadeDDSAdminClient_get_1Topics_1native
  (JNIEnv* env, jobject obj){
	
	pointer->list_topics<void>([](const std::unordered_map<std::string,Topic>& topics)->void{
	for(const auto& topic :topics){
	//	std::cout<< "Topic Name: " << topic.second.name << std::endl;

	}
  
});
	jclass stringClass = env->FindClass("java/lang/String");
	jobjectArray stringArray = env->NewObjectArray(topics.size(), stringClass, nullptr);

	for (size_t i = 0; i < topics.size(); ++i) {
	        env->SetObjectArrayElement(stringArray, i, env->NewStringUTF(topics[i].c_str()));
	}
	return stringArray;
}
*/
JNIEXPORT void JNICALL Java_org_apache_kafka_clients_admin_CascadeDDSAdminClient_deleteTopics_1native(JNIEnv* env, jobject obj, jlong ptr, jobject topics){

	DDSMetadataClient* pointer = reinterpret_cast<DDSMetadataClient*>(ptr);

	jclass collectionClass = env->FindClass("java/util/Collection");
	jmethodID iteratorMethod = env-> GetMethodID(collectionClass, "iterator","()Ljava/util/Iterator;");
	jobject iterator = env->CallObjectMethod(topics, iteratorMethod);
	jclass iteratorClass = env->FindClass("java/util/Iterator");
	jmethodID hasNextMethod = env->GetMethodID(iteratorClass, "hasNext", "()Z");
	jmethodID nextMethod = env->GetMethodID(iteratorClass, "next","()Ljava/lang/Object;");


	while(env->CallBooleanMethod(iterator, hasNextMethod))
	{
		jobject topic_del = env->CallObjectMethod(iterator, nextMethod);
		jstring topicjs = static_cast<jstring>(topic_del);
		const char* topicC = env->GetStringUTFChars(topicjs,nullptr);
		std::string topicName (topicC);
		pointer->remove_topic(topicName);

		env->ReleaseStringUTFChars (topicjs, topicC);
		env->DeleteLocalRef(topicjs);


	}
	env->DeleteLocalRef(iterator);
	env->DeleteLocalRef(iteratorClass);
	env->DeleteLocalRef(collectionClass);

}


