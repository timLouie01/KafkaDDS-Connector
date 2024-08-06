#include <jni.h>
#include <cascade_dds/dds.hpp>
#include <string>
#include <iostream>
#include "../headers/org_apache_kafka_clients_producer_CascadeDDSProducerClient.h"
#include "KafkaMessage.cpp"

std::string convertJStringToString(JNIEnv* env, jstring jStr) {
		 if (!jStr) {
			 		return "";
						 }

		 	 const char* chars = env->GetStringUTFChars(jStr, nullptr);
			 	 std::string str(chars);
				 	env->ReleaseStringUTFChars(jStr, chars);

						return str;
}

JNIEXPORT jlongArray JNICALL Java_org_apache_kafka_clients_consumer_CascadeDDSConsumerClient_createInternal_1native
  (JNIEnv * env, jclass cls, jstring topic){
	
	  std::string topic_name = convertJStringToString(env, topic);

	jclass myClass = cls;
	jfieldID key_deserializer_ID = env->GetFieldID(myClass, "keyDeserializer", "Lorg/apache/kafka/common/serialization/Deserializer;");

	jfieldID value_deserializer_ID = env->GetFieldID(myClass, "valueDeserializer", "Lorg/apache/kafka/common/serialization/Deserializer;");

	jobject key_deserializer_Instance = env->GetObjectField(obj, key_deserializer_ID);
	jobject value_deserializer_Instance = env->GetObjectField(obj, value_deserializer_ID);

	jclass serializerClass = env->FindClass("org/apache/kafka/common/serialization/Deserializer");

	jmethodID deserializeMethodID = env->GetMethodID(
		    kafkaMessage,
		        "deserialize",
			    "(Ljava/lang/String;[B)LKafkaMessage;"
		);


	  message_handler_t<KafkaMessage> deserializer_handler = [&](const KafkaMessage& msg){
		auto lengthk = msg.key.size;
		auto lengthv = msg.value.size;
		jbyteArray keyByteArray = env->NewByteArray(lengthk);
		jbyteArray valueByteArray = env->NewByteArray(lengthv);

		env->SetByteArrayRegion(keyByteArray, 0, lengthk, reinterpret_cast<jbyte*>(msg.key.bytes));
		env->SetByteArrayRegion(valueByteArray, 0, lengthv, reinterpret_cast<jbyte*>(msg.value.bytes));
		auto key_object = env->CallObjectMethod(key_deserializer_Instance, deserializeMethodID,topic_name,keyByteArray);
		auto value_object = env->CallObjectMethod(value_deserializer_Instance, deserializeMethodID, topic_name, valueByteArray);
		std::cout<< "Read message" <<std::endl;

	 }
	 
	  std::string topic_c = convertJStringToString(env, topic);
	  		auto ddsClient = DDSClient::create(DDSConfig::get());
					auto ddsProducer = ddsClient->create_publisher<KafkaMessage>(topic_c, deserializer_handler);
							
						jlong pointers[2];
							pointers[0] = reinterpret_cast<jlong>(ddsClient.release());
								pointers[1] = reinterpret_cast<jlong>(ddsProducer.release());
									
									jlongArray result = env->NewLongArray(2);
										env->SetLongArrayRegion(result,0,2,pointers);
											return result;


  }


JNIEXPORT void JNICALL Java_org_apache_kafka_clients_consumer_CascadeDDSConsumerClient_close_1native
  (JNIEnv *, jobject, jlong, jlong, jobject){


  }
