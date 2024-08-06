package org.apache.kafka.clients.producer;

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.Uuid;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import java.util.Properties;
import java.util.Collections;
public class CascadeDDSProducerClient<K, V> implements Producer<K, V> {
	
	private static final String NOT_IMPLEMENTED_MESSAGE = "Not Implemented for Cascade DDS";
	private long DDSClient_ptr;
	private long DDSProducer_ptr;
	private final Serializer<K> keySerializer;
	private final Serializer<V> valueSerializer;
	private final String clientId;

	static{
									    	
	System.loadLibrary("producer_consumer_DDS");
	}
								    	   				   @SuppressWarnings({"unchecked", "this-escape"})	
       	public CascadeDDSProducerClient(Properties properties){
										    					long[] input = createInternal_native();
															this.DDSClient_ptr = input[0];
															this.DDSProducer_ptr = input[1];
															ProducerConfig config = new ProducerConfig(properties);
															this.clientId = config.getString(ProducerConfig.CLIENT_ID_CONFIG);
															this.keySerializer = (Serializer<K>)config.getConfiguredInstance(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Serializer.class);
															this.keySerializer.configure(config.originals(Collections.singletonMap(ProducerConfig.CLIENT_ID_CONFIG, clientId)), true);

															this.valueSerializer = (Serializer<V>) config.getConfiguredInstance(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,Serializer.class);
																												this.valueSerializer.configure(config.originals(Collections.singletonMap(ProducerConfig.CLIENT_ID_CONFIG, clientId)), false);
															 }

									    	   					public static native long[] createInternal_native();

										    					@Override													 public void initTransactions() {
												 	         throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
														 		     }

											    @Override
											            public void beginTransaction() throws ProducerFencedException {
													    		        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																			    }
											    @SuppressWarnings("deprecation")
											        @Override
												        public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String consumerGroupId) throws ProducerFencedException {
																        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																				    }
											    @SuppressWarnings("deprecation")
											        @Override
												        public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, ConsumerGroupMetadata groupMetadata) throws ProducerFencedException {
																        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																				    }
											    			  

											        @Override
												        public void commitTransaction() throws ProducerFencedException {
																        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																				    }

												    @Override
												            public void abortTransaction() throws ProducerFencedException {
														    		        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																				    }
												    	public native void send_native(long publisher,String topic, K key, V message);
													    @Override
													            public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
															    		CompletableFuture<RecordMetadata> return_val = new CompletableFuture<>();
																			Future<RecordMetadata> return_val1 = return_val;
																					
																					this.send_native(this.DDSProducer_ptr,record.topic(), record.key(), record.value());
																							
																							return return_val1;

																										    }

													        @Override
														        public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
																		        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																						    }

														    @Override
														            public void flush() {
																    		        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																						    }

														        @Override
															        public List<PartitionInfo> partitionsFor(String topic) {
																			        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																							    }

															    @Override
															            public Map<MetricName, ? extends Metric> metrics() {
																	    		        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																							    }

															        @Override
																        public Uuid clientInstanceId(Duration timeout) {
																				        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																								    }

																    @Override
																            public void close() {
																		    		close(Duration.ofMillis(Long.MAX_VALUE));
																							    }

																    	public native void close_native(long DDSClient, long DDSProducer, Duration timeout);
																	    @Override
																	            public void close(Duration timeout) {
																			    		        this.close_native(this.DDSClient_ptr, this.DDSProducer_ptr, timeout);
																									    }

}
