package org.apache.kafka.clients.consumer;

import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.Uuid;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.Properties;

public class CascadeDDSConsumerClient<K, V> implements Consumer<K, V> {

	private static final String NOT_IMPLEMENTED_MESSAGE = "Not Implemented for Cascade DDS";
	public long DDSClient_ptr;
	public long DDSConsumer_ptr;
	public final Deserializer<K> keyDeserializer;
	public final Deserializer<V> valueDeserializer;
	
	static{
		System.loadLibrary("consumer_DDS");
	}
	public static native long[] createInternal_native(String topic);

	public CascadeDDSConsumerClient(Properties properties, Deserializer<K> keyDeserializer,
			                         Deserializer<V> valueDeserializer){

		String topic = properties.getProperty("topic");
		if (topic == null){
		// Error handling?
		}
		long[] input = createInternal_native(topic);
		this.DDSClient_ptr = input[0];
		this.DDSConsumer_ptr = input[1];
		this.keyDeserializer = keyDeserializer;
		this.valueDeserializer = valueDeserializer;
	}
	 @Override
	public Set<TopicPartition> assignment() {
			            
		throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
				        }

	@Override
		        
	public Set<String> subscription() {
				       
	       	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
					    }

		   
       	@Override
			  
      	public void subscribe(Collection<String> topics) {
				           
	       	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
					        }

			 
	@Override
			       
       	public void subscribe(Collection<String> topics, ConsumerRebalanceListener callback) {
					        
		throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
						    }

			     
     	@Override
				  
      	public void assign(Collection<TopicPartition> partitions) {
					          
	      	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
						        }

				
    	@Override
				   
   	public void subscribe(Pattern pattern, ConsumerRebalanceListener callback) {
						     
	     	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
							    }

				    
    	@Override
					   
       	public void subscribe(Pattern pattern) {
						         
	     	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
							        }

					    
	@Override
					       
       	public void unsubscribe() {
							      
	      	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
								    }

@SuppressWarnings("deprecation")					      
      	@Override
						   
       	public ConsumerRecords<K, V> poll(long timeout) {
							        
	    	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
								        }

						   
       	@Override
						       
       	public ConsumerRecords<K, V> poll(Duration timeout) {
								     
	     	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
									    }

						       
       	@Override
							  
      	public void commitSync() {
								        
	    	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
									        }

							  
      	@Override
							     
     	public void commitSync(Duration timeout) {
									 
	 	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
										    }

							       
       	@Override
								 
     	public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets) {
									       
	   	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
										        }

								   
       	@Override
								       
       	public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets, Duration timeout) {
										       
	       	throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
											    }

								     
     	@Override
									   
       	public void commitAsync() {
										            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
											        }

									  
      	@Override
									      
      	public void commitAsync(OffsetCommitCallback callback) {
											        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
												    }

									   
   	@Override
										    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
											            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
												        }

										    		@Override
										        public void seek(TopicPartition partition, long offset) {
												        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
													    }

										        @Override
											    public void seek(TopicPartition partition, OffsetAndMetadata offsetAndMetadata) {
												            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
													        }

											    @Override
											        public void seekToBeginning(Collection<TopicPartition> partitions) {
													        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
														    }

											        @Override
												    public void seekToEnd(Collection<TopicPartition> partitions) {
													            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
														        }

												    @Override
												        public long position(TopicPartition partition) {
														        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
															    }

												        @Override
													    public long position(TopicPartition partition, Duration timeout) {
														            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
															        }

													    @Override
													        @Deprecated
														    public OffsetAndMetadata committed(TopicPartition partition) {
															            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																        }

													        @Override
														    @Deprecated
														        public OffsetAndMetadata committed(TopicPartition partition, Duration timeout) {
																        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																	    }

														    @Override
														        public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> partitions) {
																        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																	    }

														        @Override
															    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> partitions, Duration timeout) {
																            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																	        }

															    @Override
															        public Uuid clientInstanceId(Duration timeout) {
																	        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																		    }

															        @Override
																    public Map<MetricName, ? extends Metric> metrics() {
																	            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																		        }

																    @Override
																        public List<PartitionInfo> partitionsFor(String topic) {
																		        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																			    }

																        @Override
																	    public List<PartitionInfo> partitionsFor(String topic, Duration timeout) {
																		            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																			        }

																	    @Override
																	        public Map<String, List<PartitionInfo>> listTopics() {
																			        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																				    }

																	        @Override
																		    public Map<String, List<PartitionInfo>> listTopics(Duration timeout) {
																			            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																				        }

																		    @Override
																		        public Set<TopicPartition> paused() {
																				        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																					    }

																		        @Override
																			    public void pause(Collection<TopicPartition> partitions) {
																				            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																					        }

																			    @Override
																			        public void resume(Collection<TopicPartition> partitions) {
																					        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																						    }

																			        @Override
																				    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch) {
																					            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																						        }

																				    @Override
																				        public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch, Duration timeout) {
																						        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																							    }

																				        @Override
																					    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions) {
																						            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																							        }

																					    @Override
																					        public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions, Duration timeout) {
																							        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																								    }

																					        @Override
																						    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions) {
																							            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																								        }

																						    @Override
																						        public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions, Duration timeout) {
																								        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																									    }

																						        @Override
																							    public OptionalLong currentLag(TopicPartition topicPartition) {
																								            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																									        }

																							    @Override
																							        public ConsumerGroupMetadata groupMetadata() {
																									        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																										    }

																							        @Override
																								    public void enforceRebalance() {
																									            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																										        }

																								    @Override
																								        public void enforceRebalance(String reason) {
																										        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																											    }

																								        @Override
																									    public void close() {
																										         close(Duration.ofMillis(Long.MAX_VALUE));																										        }
public native void close_native(long DDSClient_ptr, long DDSConsumer_ptr, Duration timeout);
																									    @Override
																									        public void close(Duration timeout) {
																											        this.close_native(this.DDSClient_ptr, this.DDSConsumer_ptr, timeout);																												    }

																									        @Override
																										    public void wakeup() {
																											            throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
																												        }
}

