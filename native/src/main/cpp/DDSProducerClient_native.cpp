#include <algorithm>
#include <jni.h>
#include <cascade_dds/dds.hpp>
#include <string>
#include "../headers/org_apache_kafka_clients_producer_CascadeDDSProducerClient.h"
#include "KafkaMessage.cpp"

using namespace derecho::cascade; 


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
		  (JNIEnv * env, jclass cls,jstring topic)
{
	std::string topic_c = convertJStringToString(env, topic);
		auto ddsClient = DDSClient::create(DDSConfig::get());
		auto ddsProducer = ddsClient->create_publisher<KafkaMessage>(topic_c);
		
	jlong pointers[2];
	pointers[0] = reinterpret_cast<jlong>(ddsClient.release());
	pointers[1] = reinterpret_cast<jlong>(ddsProducer.release());
	
	jlongArray result = env->NewLongArray(2);
	env->SetLongArrayRegion(result,0,2,pointers);
	return result;
	}

	JNIEXPORT void JNICALL Java_org_apache_kafka_clients_producer_DDSProducerClient_send_1native (JNIEnv * env, jobject obj, jlong producer_ptr, jstring topic, jobject key, jobject value)
{
	DDSPublisher<KafkaMessage>* pointer = reinterpret_cast<DDSPublisher<KafkaMessage>*>(producer_ptr);
	std::string topic_name = convertJStringToString(env, topic);

	jclass myClass = env->GetObjectClass(obj);	
	jfieldID key_serializer_ID = env->GetFieldID(myClass, "keySerializer", "Lorg/apache/kafka/common/serialization/Serializer;");

	jfieldID value_serializer_ID = env->GetFieldID(myClass, "valueSerializer", "Lorg/apache/kafka/common/serialization/Serializer;");

	jobject key_serializer_Instance = env->GetObjectField(obj, key_serializer_ID);
	jobject value_serializer_Instance = env->GetObjectField(obj, value_serializer_ID);

	jclass serializerClass = env->FindClass("org/apache/kafka/common/serialization/Serializer");

	jmethodID serializeMethodID = env->GetMethodID(serializerClass, "serialize", "(Ljava/lang/String;Ljava/lang/Object;)[B");

	jbyteArray keyserializedBytes = (jbyteArray)env->CallObjectMethod(key_serializer_Instance, serializeMethodID, topic_name, key);
	jsize key_length = env->GetArrayLength(keyserializedBytes);
	jbyte *keybyte = env->GetByteArrayElements(keyserializedBytes, nullptr);

	jbyteArray valueserializedBytes = (jbyteArray)env->CallObjectMethod(value_serializer_Instance, serializeMethodID, topic_name,value);
	jsize value_length = env->GetArrayLength(valueserializedBytes);
	jbyte *valuebyte = env->GetByteArrayElements(valueserializedBytes, nullptr);
	

	Blob key_blob (reinterpret_cast<uint8_t*>(keybyte),key_length);
	Blob value_blob (reinterpret_cast<uint8_t*>(valuebyte), value_length);
	KafkaMessage message_to_send (key_blob,value_blob);
	pointer->send(message_to_send);

	env->ReleaseByteArrayElements(keyserializedBytes, keybyte, JNI_ABORT);
	env->ReleaseByteArrayElements(valueserializedBytes, valuebyte, JNI_ABORT);
}
	
	JNIEXPORT void JNICALL Java_org_apache_kafka_clients_producer_DDSProducerClient_close_1native(JNIEnv *, jobject obj, jlong client_ptr, jlong producer_ptr)
{
	std::unique_ptr<DDSClient> pointer (reinterpret_cast<DDSClient*>(client_ptr));
	std::unique_ptr<DDSPublisher<KafkaMessage>> pointer2 (reinterpret_cast<DDSPublisher<KafkaMessage>*>(producer_ptr));

}

