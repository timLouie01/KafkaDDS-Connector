#include <jni.h>
#include <string>
#include <cascade_dds/dds.hpp>

using namespace derecho::cascade;


std::string extractTopicName (const std::string& path){

	auto pos = path.find_last_of('/');
	return path.substr(pos +1);	
}
/*
 * Class:     org_apache_kafka_clients_admin_DDSAdminClient
 * Method:    createInternal_native
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_apache_kafka_clients_admin_DDSAdminClient_createInternal_1native
  (JNIEnv* env, jclass cls){
	  auto temp = DDSMetadataClient::create(DDSConfig::get());
          return reinterpret_cast<jlong>(temp.release());
  }
/*
 * Class:     org_apache_kafka_clients_admin_DDSAdminClient
 * Method:    createTopics_native
 * Signature: (JLjava/util/Collection;)V
 */
JNIEXPORT void JNICALL Java_org_apache_kafka_clients_admin_DDSAdminClient_createTopics_1native
  (JNIEnv* env, jobject obj, jlong ptr, jobject newTopics){

  	jclass collectionClass = env->FindClass("java/util/Collection");
	jmethodID iteratorMethod = env-> GetMethodID(collectionClass, "iterator","()Ljava/util/Iterator");
	jobject iterator = env->CallObjectMethod(newTopics, iteratorMethod);
	jclass iteratorClass = env->FindClass("java/util/Iterator");
	jmethodID hasNextMethod = env->GetMethodID(iteratorClass, "hasNext", "()Z");
	jmethodID nextMethod = env->GetMethodID(iteratorClass, "next","()Ljava/lang/Object");

	jclass newTopicClass = env->FindClass("org/apache/kafka/clients/admin/NewTopic");
	jmethodID nameMethod = env->GetMethodID(newTopicClass, "name", "()Ljava/lang/String");

	DDSMetadataClient* pointer = reinterpret_cast<DDSMetadataClient*>(ptr);

	while(env->CallObjectMethod(iterator, hasNextMethod))
	{
		jobject newTopic = env->CallObjectMethod(iterator, nextMethod);
		jstring name = (jstring)env->CallObjectMethod(newTopic, nameMethod);
		const char* nameC = env->GetStringUTFChars(name,nullptr);
		std::string nameStr (nameC);
			
		Topic temp {extractTopicName(nameStr),nameStr};	
		pointer->create_topic(temp);

		env->ReleaseStringUTFChars (name, nameC);
		env->DeleteLocalRef(name);
		env->DeleteLocalRef(newTopic);
	}
	env->DeleteLocalRef(iterator);
	env->DeleteLocalRef(iteratorClass);
	env->DeleteLocalRef(collectionClass);
	env->DeleteLocalRef(newTopicClass);

  }

/*
 * Class:     org_apache_kafka_clients_admin_DDSAdminClient
 * Method:    close
 * Signature: (Ljava/time/Duration;)V
 */
JNIEXPORT void JNICALL Java_org_apache_kafka_clients_admin_DDSAdminClient_close
  (JNIEnv* env, jobject obj, jobject){
		 
  }
	
