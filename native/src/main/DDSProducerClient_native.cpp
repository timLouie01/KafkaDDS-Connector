#include <jni.h>
#include <cascade_dds/dds.hpp>
#include <string>
#include "../headers/org_apache_kafka_clients_producer_DDSProducerClient.h"

using namespace deroche::cascade; 

std::unique_ptr<dds::DDSClient> ddsClient;


std::string convertJStringToString(JNIEnv* env, jstring jStr) {
	 if (!jStr) {
		return "";
	 }

	 const char* chars = env->GetStringUTFChars(jStr, nullptr);
	 std::string str(chars);
	env->ReleaseStringUTFChars(jStr, chars);

	return str;
}

std::string convertJObjectToString(JNIEnv* env, jobject obj) {
	    jclass stringClass = env->FindClass("java/lang/String");
	   if (env->IsInstanceOf(obj, stringClass)) {
			        return convertJStringToString(env, (jstring)obj);
		  }
		   
		        return "";
}

	JNIEXPORT jlongArray JNICALL Java_org_apache_kafka_clients_producer_DDSProducerClient_createInternal_1native
		  (JNIEnv *, jclass cls,jstring topic, jstring message_type_class_name)
{
	std::string message_t = convertJStringToString(env, message_type_class_name);
	std::string topic_c = convertJStringToString(env, topic);
	if (message_t == "java.lang.String"){

		if (!DDSClient){
		
			ddsClient = DDSClient::create(DDSConfig::get());
		auto ddsProducer = ddsClient.template ddsClient.create_publisher<std::string>(topic_c);
		}else
		{
		auto	ddsProducer = ddsClient.template ddsClient.create_publisher<std::string>(topic_c);
		}
		
	jlong pointers[2];
	pointers[0] = reinterpret_cast<jlong>(ddsClient.release());
	pointers[1] = reinterpret_cast<jlong>(ddsProducer.release());
	
	jlongArray result = env->NewLongArray(2);
	env->SetLongArrayRegion(result,0,2,pointers);
	return result;
	} else{
		jclass exceptionCls = env->FindClass("java/lang/IllegalArgumentException");
		 env->ThrowNew(exceptionCls, ("Unsupported message type: " + className).c_str());

	}
}

	JNIEXPORT void JNICALL Java_org_apache_kafka_clients_producer_DDSProducerClient_send_1native (JNIEnv * env, jobject obj, jlong producer_ptr, jstring topic, jobject message)
{
	DDSPublisher* pointer = reinterpret_cast<DDSPublisher*>(producer_ptr);
	
	std::string topic_name = convertJStringToString(env, topic);
	std::string message = convertJObjectToString(env, message);
	pointer->send(topic_name,message);


}
	
	JNIEXPORT void JNICALL Java_org_apache_kafka_clients_producer_DDSProducerClient_close_1native(JNIEnv *, jobject obj, jlong client_ptr)
{
	DDSClient* pointer = reinterpret_cast<DDSClient*>(client_ptr);
	delete pointer;

}

