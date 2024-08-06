#include <derecho/mutils-serialization/SerializationSupport.hpp>


using namespace derecho::cascade;

class KafkaMessage:public mutils::ByteRepresentable{

	public:

		Blob key;
		Blob value;

	DEFAULT_SERIALIZATION_SUPPORT(KafkaMessage, key, value);


	KafkaMessage(const Blob& key, const Blob& value){
		this->key = key;
		this->value = value;

	}

};
